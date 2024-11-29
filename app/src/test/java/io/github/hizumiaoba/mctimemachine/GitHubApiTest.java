package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jcabi.github.Github;
import com.jcabi.github.RtGithub;
import com.jcabi.http.response.JsonResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Slf4j
@Disabled("This test requires a GitHub API token.")
public class GitHubApiTest {
  ObjectMapper mapper = new ObjectMapper();

  @Test
  void repos() throws IOException {
    Github github = new RtGithub();
    final JsonResponse response = github.entry()
      .uri().path("/repos/hizumiaoba/MineCraftTimeMachine/releases")
      .back()
      .fetch()
      .as(JsonResponse.class);
    JsonNode json = mapper.readTree(response.body());
    log.trace("raw json response:");
    log.trace(mapper.writeValueAsString(json));
    while (json.elements().hasNext()) {
      JsonNode node = json.elements().next();
      assertThat(node.get("tag_name").asText()).contains("v");
      assertThat(node.get("html_url").asText()).startsWith("https://github.com/");
    }
  }
}
