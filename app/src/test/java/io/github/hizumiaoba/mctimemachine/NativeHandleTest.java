package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import io.github.hizumiaoba.mctimemachine.internal.natives.NativeHandleUtil;
import java.util.Optional;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class NativeHandleTest {

  @Test
  @Disabled("This test is already completed under development environment")
  public void testGetMinecraftProcessId() {
    Optional<ProcessHandle> processHandle = NativeHandleUtil.getMinecraftProcessId();
    processHandle.ifPresentOrElse(
      handle -> {
        assertThat(handle.pid()).isGreaterThan(0);
        assertThat(handle.isAlive()).isTrue();
      },
      () -> fail("Failed to get Minecraft process id")
    );
  }

  @Test
  @Disabled("This test is already completed under development environment")
  public void onExitEventSettingTest() {
    Optional<ProcessHandle> mcProcess = NativeHandleUtil.getMinecraftProcessId();
    mcProcess.ifPresentOrElse(
      handle -> handle.onExit().thenRun(() -> assertThat(true).isTrue()).join(),
      () -> fail("Failed to get Minecraft process id")
    );
  }
}
