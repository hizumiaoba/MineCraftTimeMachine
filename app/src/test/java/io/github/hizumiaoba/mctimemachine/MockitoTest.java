package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class MockitoTest {

  @Mock
  List<String> mock;

  @Test
  void test() {
    try(AutoCloseable ignored = MockitoAnnotations.openMocks(this)) {
      when(mock.get(anyInt())).thenReturn("Hello Mockito!");

      for (int i = 0; i < 10; i++) {
        assertThat(mock.get(i)).isEqualTo("Hello Mockito!");
      }
    } catch (Exception e) {
      fail(e);
    }
  }
}
