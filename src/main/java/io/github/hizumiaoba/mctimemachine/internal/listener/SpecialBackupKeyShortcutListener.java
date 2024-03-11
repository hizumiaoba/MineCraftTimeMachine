package io.github.hizumiaoba.mctimemachine.internal.listener;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import io.github.hizumiaoba.mctimemachine.internal.keyhook.AbstractNativeKeyListener;
import io.github.hizumiaoba.mctimemachine.internal.keyhook.ModifierKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class SpecialBackupKeyShortcutListener extends AbstractNativeKeyListener {

  private final Runnable task;

  @Override
  protected boolean isKeyPressed(int keycode, ModifierKey modifierKey) {
    return keycode == NativeKeyEvent.VC_Z && modifierKey == ModifierKey.CTRL_SHIFT;
  }

  @Override
  protected void execute() {
    log.info("Special backup key shortcut detected");
    task.run();
  }
}
