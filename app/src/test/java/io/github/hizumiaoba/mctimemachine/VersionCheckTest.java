package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.hizumiaoba.mctimemachine.api.version.VersionChecker;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionCheckTest {

  @Test
  void checkIfUpdateAvailable() {
    VersionChecker checker = new VersionChecker();
    VersionObj mockClientVersion = VersionObj.parse("v1.0.0");
    checker.getLatestVersionIfAvailable(mockClientVersion, false)
      .ifPresentOrElse(v -> {
        boolean newer = v.compareTo(mockClientVersion) > 0;
        assertThat(newer).isTrue();
      }, Assertions::fail);
  }

  @Test
  void checkEmptyIfUpdateNotAvailable() {
    VersionChecker checker = new VersionChecker();
    VersionObj mockClientVersion = VersionObj.parse("v100.100.100");
    checker.getLatestVersionIfAvailable(mockClientVersion, false)
      .ifPresentOrElse(
        v -> fail("Update should not be available but found: %s".formatted(v.asStringNotation())),
        assertThat(true)::isTrue);
  }
}
