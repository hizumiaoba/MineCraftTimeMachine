package io.github.hizumiaoba.mctimemachine.internal.fs;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.hizumiaoba.mctimemachine.api.HttpRequest;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class AbstractHttpHandler<T> implements HttpRequest<T> {

  protected static final OkHttpClient CLIENT = new OkHttpClient();
  protected static final ObjectMapper MAPPER = new ObjectMapper();

  protected final String baseUrl;
  protected final Map<String, String> headers;
  protected Class<T> type;

  @Override
  public CompletableFuture<T> handleAsync() {

  }

  protected Request createRequest(String method, String url, RequestBody body) {
    return new Request.Builder()
      .url(url)
      .method(method, body)
      .headers(okhttp3.Headers.of(Optional.ofNullable(this.headers).orElse(Collections.emptyMap())))
      .build();
  }
}
