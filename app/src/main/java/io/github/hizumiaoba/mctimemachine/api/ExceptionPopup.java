package io.github.hizumiaoba.mctimemachine.api;

import javax.swing.JOptionPane;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionPopup {

  private final Throwable thrown;
  private final String message;
  private final String occurence;
  private static final String messageSkeleton = "例外：%s が %s を処理中に発生しました。\n\n%s";


  public ExceptionPopup(Throwable thrown, String message, String occurence) {
    this.thrown = thrown;
    this.message = message;
    this.occurence = occurence;
  }

  public void pop() {
    if (isContinuable()) {
      log();
    } else {
      forceExit();
    }
  }

  private int show() {
    return JOptionPane.showOptionDialog(
      null,
      String.format(messageSkeleton, thrown.getClass().getName(), occurence, message),
      String.format("%s を処理中に例外が発生しました", occurence),
      JOptionPane.YES_NO_OPTION,
      JOptionPane.ERROR_MESSAGE,
      null,
      new String[] { "続行", "強制終了" },
      "強制終了"
    );
  }

  private boolean isContinuable() {
    return show() == JOptionPane.YES_OPTION;
  }

  private void forceExit() {
    System.exit(1);
  }

  private void log() {
    log.error("User chose to continue after exception.");
    log.error("Occurred during {}", occurence);
    log.error(message, thrown);
  }
}
