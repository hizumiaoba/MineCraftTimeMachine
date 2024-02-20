package io.github.hizumiaoba.mctimemachine;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MineCraftTimeMachineApplication extends Application {

  @Override
  public void start(Stage stage) throws IOException {
    FXMLLoader fxmlLoader = new FXMLLoader(MineCraftTimeMachineApplication.class.getResource("main.fxml"));
    Scene scene = new Scene(fxmlLoader.load());
    stage.setTitle("MCTM - BackUp & Restore your precious world!");
    stage.setScene(scene);
    stage.show();
  }

  public static void main(String[] args) {
    launch();
  }
}
