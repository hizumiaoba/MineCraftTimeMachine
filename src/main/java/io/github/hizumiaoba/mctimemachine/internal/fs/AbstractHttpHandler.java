package io.github.hizumiaoba.mctimemachine.internal.fs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hizumiaoba.mctimemachine.api.HttpRequest;
import io.github.hizumiaoba.mctimemachine.internal.concurrent.ConcurrentThreadFactory;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class AbstractHttpHandler<T> implements HttpRequest<T> {

  protected static final OkHttpClient CLIENT = new OkHttpClient();
  protected static final ObjectMapper MAPPER = new ObjectMapper();
  protected static final Map<String, Future<Response>> RESPONSE_MAP = Collections.synchronizedMap(
    new HashMap<>());
  protected static final ExecutorService requestPoller = Executors.newSingleThreadExecutor(
    new ConcurrentThreadFactory("Request Schedule Thread", "RequestPoller", true));

  protected final String baseUrl;
  protected final String method;
  protected final Map<String, String> data;
  protected final Map<String, String> headers;
  protected Class<T> type;

  @Override
  public CompletableFuture<T> handleAsync(String uuid) {
    return CompletableFuture.supplyAsync(() -> pollResponse(uuid))
      .thenApply(this::handleResponse);
  }

  @Override
  public <U> CompletableFuture<U> handleAsync(String uuid, Function<? super T, ? extends U> fn) {
    return this.handleAsync(uuid).thenApply(fn);
  }

  @Override
  public void handleAsync(String uuid, Consumer<? super T> action) {
    this.handleAsync(uuid).thenAccept(action);
  }

  protected Request createRequest(String method, String url, RequestBody body) {
    return new Request.Builder()
      .url(url)
      .method(method, body)
      .headers(okhttp3.Headers.of(
        Optional.ofNullable(this.headers).orElseThrow(IllegalArgumentException::new)))
      .build();
  }

  protected RequestBody createRequestBody(Map<String, String> data, String mime)
    throws JsonProcessingException {
    return RequestBody.create(MAPPER.writeValueAsString(data), okhttp3.MediaType.parse(mime));
  }

  protected String queueRequest() {
    final String url = constructUrl();
    String uuid = UUID.randomUUID().toString();
    Future<Response> future = requestPoller.submit(() -> CLIENT.newCall(
        createRequest(this.method, url,
          createRequestBody(data, this.headers.getOrDefault("Content-Type", "application/json"))))
      .execute());
    RESPONSE_MAP.put(uuid, future);
    return uuid;
  }

  protected String constructUrl() {
    throw new UnsupportedOperationException("Use this method in more concrete class");
  }

  protected Response pollResponse(String uuid) {
    final Future<Response> future = RESPONSE_MAP.get(uuid);
    if (future == null) {
      log.error("No response found for UUID: {}", uuid);
      return null;
    }
    try {
      return future.get();
    } catch (Exception e) {
      log.error("Failed to poll response", e);
    }
    return null;
  }

  protected T handleResponse(Response response) {
    if (response.body() == null) {
      throw new NullPointerException("Response body is null");
    }
    try {
      return MAPPER.readValue(response.body().string(), type);
    } catch (IOException e) {
      log.error("Failed to parse response", e);
      return null;
    }
  }
}
