package io.github.hizumiaoba.mctimemachine;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MineCraftTimeMachineApplication extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(MineCraftTimeMachineApplication.class.getResource("main.fxml"));
    stage.setOnCloseRequest(this::onClose);
    Scene scene = new Scene(fxmlLoader.load());
    stage.setTitle("MCTM - BackUp & Restore your precious world!");
    stage.setScene(scene);
    stage.show();
  }

  private void onClose(WindowEvent e) {
    log.trace("Release key hooks via close event: {}", e);
    try {
      GlobalScreen.unregisterNativeHook();
    } catch (NativeHookException ex) {
      ExceptionPopup popup = new ExceptionPopup(ex, "キーボードショートカットの解除に失敗しました。",
        "MineCraftTimeMachineApplication#onClose");
      popup.pop();
    }
  }

  public static void main(String[] args) {
    launch();
  }
}
