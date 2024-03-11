package io.github.hizumiaoba.mctimemachine.internal.keyhook;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;

public class GlobalNativeKeyListenerExecutor implements NativeKeyListener {

  private final List<AbstractNativeKeyListener> listeners;

  public GlobalNativeKeyListenerExecutor(AbstractNativeKeyListener... listeners) {
    this(Lists.newArrayList(listeners));
  }

  public GlobalNativeKeyListenerExecutor(List<AbstractNativeKeyListener> listeners) {
    this.listeners = listeners;
  }

  @Override
  public void nativeKeyPressed(NativeKeyEvent e) {
    Optional<AbstractNativeKeyListener> listener = listeners.stream().filter(l -> l.test(e))
      .findAny();
    listener.ifPresent(l -> l.nativeKeyPressed(e));
  }

  @Override
  public void nativeKeyReleased(NativeKeyEvent e) {
    Optional<AbstractNativeKeyListener> listener = listeners.stream().filter(l -> l.test(e))
      .findAny();
    listener.ifPresent(l -> l.nativeKeyReleased(e));
  }

  @Override
  public void nativeKeyTyped(NativeKeyEvent e) {
    // do nothing
  }
}
