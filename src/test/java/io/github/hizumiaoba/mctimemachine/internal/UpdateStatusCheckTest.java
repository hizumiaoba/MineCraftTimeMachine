package io.github.hizumiaoba.mctimemachine.internal;

import static com.google.common.truth.Truth.assertThat;

import io.github.hizumiaoba.mctimemachine.internal.VersionHelper.UpdateStatus;
import java.util.Map;
import org.junit.jupiter.api.Test;

public class UpdateStatusCheckTest {

  @Test
  public void testStatusCheck() {
    VersionHelper.VersionObj currentVersionMock = VersionHelper.VersionObj.fromString("1.0.0");

    VersionHelper.VersionObj remoteVersionMock = VersionHelper.VersionObj.fromString("1.2.5-Beta");

    Map<String, UpdateStatus> status = currentVersionMock.checkStatus(remoteVersionMock);
    final boolean suffixStatus = currentVersionMock.checkSuffixStatus(remoteVersionMock);

    assertThat(status.get("major")).isEqualTo(UpdateStatus.UP_TO_DATE);
    assertThat(status.get("minor")).isEqualTo(UpdateStatus.OUTDATED);
    assertThat(status.get("patch")).isEqualTo(UpdateStatus.OUTDATED);
    assertThat(suffixStatus).isFalse();
  }
}
