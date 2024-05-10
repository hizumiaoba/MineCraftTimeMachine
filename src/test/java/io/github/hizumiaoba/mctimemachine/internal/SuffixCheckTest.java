package io.github.hizumiaoba.mctimemachine.internal;

import static com.google.common.truth.Truth.assertThat;

import org.junit.jupiter.api.Test;

public class SuffixCheckTest {

  @Test
  public void testAlphaClient() {
    // setup
    VersionHelper.VersionObj client = VersionHelper.VersionObj.fromString("0.1.0-Alpha");

    // tested
    VersionHelper.VersionObj alpha = VersionHelper.VersionObj.fromString("0.1.0-Alpha");
    VersionHelper.VersionObj beta = VersionHelper.VersionObj.fromString("0.1.0-Beta");
    VersionHelper.VersionObj release = VersionHelper.VersionObj.fromString("0.1.0");

    // verify
    assertThat(client.checkSuffixStatus(alpha)).isTrue();
    assertThat(client.checkSuffixStatus(beta)).isTrue();
    assertThat(client.checkSuffixStatus(release)).isTrue();
  }

  @Test
  public void testBetaClient() {
    // setup
    VersionHelper.VersionObj client = VersionHelper.VersionObj.fromString("0.1.0-Beta");

    // tested
    VersionHelper.VersionObj alpha = VersionHelper.VersionObj.fromString("0.1.0-Alpha");
    VersionHelper.VersionObj beta = VersionHelper.VersionObj.fromString("0.1.0-Beta");
    VersionHelper.VersionObj release = VersionHelper.VersionObj.fromString("0.1.0");

    // verify
    assertThat(client.checkSuffixStatus(alpha)).isFalse();
    assertThat(client.checkSuffixStatus(beta)).isTrue();
    assertThat(client.checkSuffixStatus(release)).isTrue();
  }

  @Test
  public void testReleaseClient() {
    // setup
    VersionHelper.VersionObj client = VersionHelper.VersionObj.fromString("0.1.0");

    // tested
    VersionHelper.VersionObj alpha = VersionHelper.VersionObj.fromString("0.1.0-Alpha");
    VersionHelper.VersionObj beta = VersionHelper.VersionObj.fromString("0.1.0-Beta");
    VersionHelper.VersionObj release = VersionHelper.VersionObj.fromString("0.1.0");

    // verify
    assertThat(client.checkSuffixStatus(alpha)).isFalse();
    assertThat(client.checkSuffixStatus(beta)).isFalse();
    assertThat(client.checkSuffixStatus(release)).isTrue();
  }
}
