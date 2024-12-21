package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.hizumiaoba.mctimemachine.api.version.VersionChecker;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionCheckTest {

  @Test
  void checkClientConnection() {
    VersionChecker checker = null;
    try {
      checker = new VersionChecker();
    } catch (IOException e) {
      fail(e);
    }
    VersionObj mockClientVersion = VersionObj.parse("v1.0.0");
    checker.getLatestVersion(false)
      .ifPresentOrElse(r -> {
        assertThat(r).isNotNull();
        VersionObj remoteVersion = VersionObj.parse(r.getTagName());
        boolean newer = remoteVersion.compareTo(mockClientVersion) > 0;
        assertThat(newer).isTrue();
      }, Assertions::fail);
  }

}
