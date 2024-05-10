package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.hizumiaoba.mctimemachine.internal.version.VersionHelper;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import java.io.IOException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Disabled for CI/CD")
public class FetchRemoteVersionTest {

  @Test
  public void test() {
    try {
      VersionObj remote = VersionHelper.getLatestRemoteVersion();
      assertThat(remote).isNotNull();
    } catch (IOException e) {
      fail(e);
    }
  }
}
