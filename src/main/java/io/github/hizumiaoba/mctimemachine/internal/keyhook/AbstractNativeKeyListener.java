/**
 * Referenced {@link https://qiita.com/mamamap/items/b3b8d5914b8fc4879d12} page
 */

package io.github.hizumiaoba.mctimemachine.internal.keyhook;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractNativeKeyListener implements Predicate<NativeKeyEvent>,
  NativeKeyListener {

  private static final Set<Integer> KEEP_KEY = new HashSet<>();

  @Override
  public boolean test(NativeKeyEvent e) {
    return isKeyPressed(e.getKeyCode(), ModifierKey.of(e));
  }

  public boolean isKeeping(int keycode) {
    return KEEP_KEY.contains(keycode);
  }

  public void addKeepKey(int keycode) {
    log.trace("add keep key: {}", keycode);
    KEEP_KEY.add(keycode);
  }

  public void releaseKey(int keycode) {
    log.trace("release key: {}", keycode);
    KEEP_KEY.remove(keycode);
  }

  @Override
  public final void nativeKeyPressed(NativeKeyEvent e) {
    // avoid to call multiple times
    if (isKeeping(e.getKeyCode())) {
      return;
    }
    addKeepKey(e.getKeyCode());
    if (test(e)) {
      execute();
    }
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent e) {
    // release pressed key
    releaseKey(e.getKeyCode());
  }

  @Override
  public final void nativeKeyTyped(NativeKeyEvent e) {
    // Prohibit the override of the method
    // do nothing
  }

  protected abstract boolean isKeyPressed(int keycode, ModifierKey modifierKey);

  protected abstract void execute();
}
