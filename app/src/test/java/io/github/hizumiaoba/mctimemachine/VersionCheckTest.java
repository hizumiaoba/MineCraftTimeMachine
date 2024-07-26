package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;

import io.github.hizumiaoba.mctimemachine.internal.version.VersionHelper;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import org.junit.jupiter.api.Test;

public class VersionCheckTest {

  @Test
  public void testClientIsLatest() {
    // Arrange
    VersionObj clientVersion = VersionObj.parse("1.0.0");
    VersionObj serverVersion = VersionObj.parse("1.0.0");

    // Act
    VersionHelper helper = new VersionHelper(clientVersion);

    // Assert
    assertThat(helper.checkLatest(serverVersion, true)).isTrue();
    assertThat(helper.checkLatest(serverVersion, false)).isTrue();
  }

  @Test
  public void testClientisOutdated() {
    // Arrange
    VersionObj clientVersion = VersionObj.parse("1.0.0");
    VersionObj serverPatchVersion = VersionObj.parse("1.0.1");
    VersionObj serverMinorVersion = VersionObj.parse("1.3.0");
    VersionObj serverMajorVersion = VersionObj.parse("2.0.0");
    // Act
    VersionHelper helper = new VersionHelper(clientVersion);

    // Assert
    assertThat(helper.checkLatest(serverPatchVersion, true)).isFalse();
    assertThat(helper.checkLatest(serverPatchVersion, false)).isFalse();

    assertThat(helper.checkLatest(serverMinorVersion, true)).isFalse();
    assertThat(helper.checkLatest(serverMinorVersion, false)).isFalse();

    assertThat(helper.checkLatest(serverMajorVersion, true)).isFalse();
    assertThat(helper.checkLatest(serverMajorVersion, false)).isFalse();
  }

  @Test
  public void testClientIsBeta() {
    // Arrange
    VersionObj clientVersion = VersionObj.parse("1.0.0-beta");
    VersionObj serverVersion = VersionObj.parse("1.0.0");

    // Act
    VersionHelper helper = new VersionHelper(clientVersion);

    // Assert
    assertThat(helper.checkLatest(serverVersion, true)).isTrue();
    assertThat(helper.checkLatest(serverVersion, false)).isFalse();
  }

  @Test
  public void testClientIsBetaAndServerIsAlpha() {
    // Arrange
    VersionObj clientVersion = VersionObj.parse("1.0.0-beta");
    VersionObj serverVersion = VersionObj.parse("1.0.0-alpha");
    VersionObj betaServerVersion = VersionObj.parse("1.0.0-beta");

    // Act
    VersionHelper helper = new VersionHelper(clientVersion);

    // Assert
    assertThat(helper.checkLatest(serverVersion, true)).isTrue();
    assertThat(helper.checkLatest(serverVersion, false)).isFalse();

    assertThat(helper.checkLatest(betaServerVersion, true)).isTrue();
    assertThat(helper.checkLatest(betaServerVersion, false)).isTrue();
  }

  @Test
  public void testClientIsAlpha() {
    // Arrange
    VersionObj clientVersion = VersionObj.parse("1.0.0-alpha");
    VersionObj serverVersion = VersionObj.parse("1.0.0");
    VersionObj betaServerVersion = VersionObj.parse("1.0.0-beta");
    VersionObj alphaServerVersion = VersionObj.parse("1.0.0-alpha");

    // Act
    VersionHelper helper = new VersionHelper(clientVersion);

    // Assert
    assertThat(helper.checkLatest(serverVersion, true)).isTrue();
    assertThat(helper.checkLatest(serverVersion, false)).isFalse();

    assertThat(helper.checkLatest(betaServerVersion, true)).isTrue();
    assertThat(helper.checkLatest(betaServerVersion, false)).isFalse();

    assertThat(helper.checkLatest(alphaServerVersion, true)).isTrue();
    assertThat(helper.checkLatest(alphaServerVersion, false)).isTrue();
  }
}
