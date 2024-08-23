package io.github.hizumiaoba.mctimemachine.api;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionPopup {

  private static final ButtonType FORCE_EXIT = new ButtonType("強制終了");
  private static final ButtonType CONTINUE_ACTION = new ButtonType("続行");
  private final Throwable thrown;
  private final String message;
  private final String occurence;
  private static final String messageSkeleton = "例外：%s が %s を処理中に発生しました。";


  public ExceptionPopup(Throwable thrown, String message, String occurence) {
    this.thrown = thrown;
    this.message = message;
    this.occurence = occurence;
  }

  public void pop() {
    Platform.runLater(() -> {
      if (isContinuable()) {
        log();
      } else {
        forceExit();
      }
    });
  }

  private ButtonType show() {
    Alert alert = new Alert((AlertType.ERROR));
    alert.setTitle(String.format("%s を処理中に例外が発生しました", occurence));
    alert.setHeaderText(String.format(messageSkeleton, thrown.getClass().getName(), occurence));
    alert.setContentText(message);
    alert.getButtonTypes().setAll(
      CONTINUE_ACTION,
      FORCE_EXIT
    );
    return alert.showAndWait().orElse(FORCE_EXIT);
  }

  private boolean isContinuable() {
    return show().getText().equals("続行");
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
