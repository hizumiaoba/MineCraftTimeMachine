package io.github.hizumiaoba.mctimemachine;

import com.melloware.jintellitype.JIntellitype;
import io.github.hizumiaoba.mctimemachine.api.Suffix;
import io.github.hizumiaoba.mctimemachine.api.Version;
import java.io.IOException;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Version(major = 1, minor = 0, patch = 1, suffix = Suffix.NONE)
public class MineCraftTimeMachineApplication extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(MineCraftTimeMachineApplication.class.getResource("main.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    stage.setTitle("MCTM - BackUp & Restore your precious world!");
    stage.setScene(scene);
    stage.setOnCloseRequest(this::onClose);
    stage.show();
  }

  private void onClose(WindowEvent e) {
    log.info("Releasing Global shortcut key hooks...");
    log.info("Bye!: {}", e);
    JIntellitype.getInstance().cleanUp();
  }

  public static void main(String[] args) {
    launch();
  }
}
