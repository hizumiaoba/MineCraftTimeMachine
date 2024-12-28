package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class JsonNodeTest {

  static final ObjectMapper MAPPER = new ObjectMapper();

  @Test
  void test() {
    final String jsonString = "{\"name\":\"John\",\"age\":30,\"car\":null}";
    try {
      JsonNode node = MAPPER.readTree(jsonString);
      assertThat(node.get("name").asText()).isEqualTo("John");
      assertThat(node.get("age").asInt()).isEqualTo(30);
      assertThat(node.get("car").isNull()).isTrue();

      log.trace("converted json node:");
      log.trace(node.toString());
    } catch (JsonProcessingException e) {
      fail(e);
    }
  }

}
