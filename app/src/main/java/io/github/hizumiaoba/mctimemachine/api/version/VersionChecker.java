package io.github.hizumiaoba.mctimemachine.api.version;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.response.JsonResponse;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
public class VersionChecker {

  private final Github github;
  private static final String RELEASE_PATH = "/repos/hizumiaoba/MineCraftTimeMachine/releases";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public VersionChecker() {
    this(new RtGithub()); // since end-users may not have a GitHub account we need to be an anonymous user
  }

  JsonNode fetchResponse() throws IOException {
    JsonResponse response = github.entry()
      .uri().path(RELEASE_PATH)
      .back()
      .fetch()
      .as(JsonResponse.class);
    return convertToJsonNode(response);
  }

  JsonNode convertToJsonNode(JsonResponse response) throws JsonProcessingException {
    return MAPPER.readTree(response.body());
  }

  <T> T parseJsonResponse(JsonNode response, Class<T> clazz)
    throws JsonProcessingException {
    return MAPPER.readValue(response.toString(), clazz);
  }

  <T> T parseJsonResponse(JsonNode response, JavaType type)
    throws JsonProcessingException {
    return MAPPER.readValue(response.toString(), type);
  }

  private boolean isUpdateAvailable(VersionObj clientVersion, VersionObj remoteVersion) {
    return remoteVersion.compareTo(clientVersion) > 0;
  }

  public Optional<VersionObj> getLatestVersionIfAvailable(VersionObj clientVersion, boolean preferPreRelease) {
    try {
      JsonNode response = this.fetchResponse();
      List<MinimalRemoteVersionCrate> remoteVersionCrate = this.parseJsonResponse(response, MAPPER.getTypeFactory().constructCollectionLikeType(
        List.class, MinimalRemoteVersionCrate.class));
      return remoteVersionCrate
        .parallelStream()
        .filter(e -> e.isPrerelease() == preferPreRelease)
        .map(MinimalRemoteVersionCrate::getTagName)
        .map(VersionObj::parse)
        .reduce(VersionObj::max)
        .filter(v -> isUpdateAvailable(clientVersion, v));
    } catch (IOException e) {
      log.error("Failed to fetch the latest version from GitHub.", e);
      return Optional.empty();
    }
  }
}
