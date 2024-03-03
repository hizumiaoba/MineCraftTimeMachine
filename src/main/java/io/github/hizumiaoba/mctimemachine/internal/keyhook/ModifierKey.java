package io.github.hizumiaoba.mctimemachine.internal.keyhook;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;

public enum ModifierKey {
  NONE,
  CTRL,
  ALT,
  SHIFT,
  CTRL_SHIFT,
  CTRL_ALT,
  SHIFT_ALT,
  CTRL_ALT_SHIFT;

  /**
   * キーイベントから修飾キーの列挙型を生成します。
   *
   * @param e キーイベント
   * @return キーイベントの修飾キーに対応する列挙型
   */
  public static ModifierKey of(NativeKeyEvent e) {
    String modifire = NativeKeyEvent.getModifiersText(e.getModifiers()).toUpperCase();
    if (modifire.contains(CTRL.name())
      && modifire.contains(ALT.name())
      && modifire.contains(SHIFT.name())) {
      return CTRL_ALT_SHIFT;
    } else if (modifire.contains(CTRL.name())
      && modifire.contains(ALT.name())) {
      return CTRL_ALT;
    } else if (modifire.contains(CTRL.name())
      && modifire.contains(SHIFT.name())) {
      return CTRL_SHIFT;
    } else if (modifire.contains(ALT.name())
      && modifire.contains(SHIFT.name())) {
      return SHIFT_ALT;
    } else if (modifire.contains(CTRL.name())) {
      return CTRL;
    } else if (modifire.contains(ALT.name())) {
      return ALT;
    } else if (modifire.contains(SHIFT.name())) {
      return SHIFT;
    } else {
      return NONE;
    }
  }
}
