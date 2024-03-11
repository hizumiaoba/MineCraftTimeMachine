package io.github.hizumiaoba.mctimemachine.internal.listener;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import io.github.hizumiaoba.mctimemachine.internal.concurrent.ConcurrentThreadFactory;
import io.github.hizumiaoba.mctimemachine.internal.keyhook.AbstractNativeKeyListener;
import io.github.hizumiaoba.mctimemachine.internal.keyhook.ModifierKey;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class NormalBackupKeyShortcutListener extends AbstractNativeKeyListener {

  private static final ScheduledExecutorService delayExecutor = Executors.newSingleThreadScheduledExecutor(
    new ConcurrentThreadFactory("Normal Backup Key Shortcut Listener", "delayed-executor", true)
  );
  private final Runnable task;

  @Override
  public boolean isKeyPressed(int keycode, ModifierKey mod) {
    return keycode == NativeKeyEvent.VC_B && mod == ModifierKey.CTRL_SHIFT;
  }

  @Override
  public void execute() {
    log.info("Normal backup key shortcut detected");
    delayExecutor.schedule(task, 10, TimeUnit.SECONDS);
  }
}
