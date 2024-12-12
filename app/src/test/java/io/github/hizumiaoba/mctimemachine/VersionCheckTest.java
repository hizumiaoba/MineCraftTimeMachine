package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;

import io.github.hizumiaoba.mctimemachine.api.version.VersionChecker;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionCheckTest {

  @Test
  void checkClientConnection() {
    VersionChecker checker = new VersionChecker();
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
