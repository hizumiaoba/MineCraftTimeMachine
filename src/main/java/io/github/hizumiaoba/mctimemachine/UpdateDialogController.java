package io.github.hizumiaoba.mctimemachine;

import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import io.github.hizumiaoba.mctimemachine.api.Version;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionHelper;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class UpdateDialogController {

  @FXML
  private CheckBox chkIncludePrerelease;

  @FXML
  private ProgressBar updateProgressBar;

  @FXML
  private Label labelUpdateMessage;

  private VersionObj remoteLatest;

  @FXML
  void onCheckUpdateBtnClick() {
    log.info("Check update button clicked.");
    chkIncludePrerelease.setDisable(true);
    updateProgressBar.setProgress(0.0);
    log.trace("Fetching remote latest version...");
    VersionObj clientVersion = VersionObj.parse(MineCraftTimeMachineApplication.class.getAnnotation(
      Version.class));
    updateProgressBar.setProgress(0.15);
    VersionHelper helper = new VersionHelper(clientVersion);
    try {
      this.remoteLatest = VersionHelper.getLatestRemoteVersion(chkIncludePrerelease.isSelected());
      updateProgressBar.setProgress(0.33);
      log.info("Remote latest version: {}", remoteLatest.asStringNotation());
      log.trace("Client version: {}", clientVersion.asStringNotation());
      updateProgressBar.setProgress(0.67);
      if (helper.checkLatest(remoteLatest, false)) {
        log.info("Client is up-to-date.");
      } else {
        log.info("Client is outdated.");
      }
    } catch (IOException e) {
      ExceptionPopup p = new ExceptionPopup(e, "最新バージョンを取得できませんでした。",
        "UpdateDialogController#onCheckUpdateBtnClick");
      p.pop();
    } finally {
      chkIncludePrerelease.setDisable(false);
      updateProgressBar.setProgress(1.0);
      labelUpdateMessage.setText(helper.constructUpdateMessage(remoteLatest, false));
    }
  }

  @FXML
  void onOpenReleasePageBtnClick() {
    log.info("Open release page button clicked.");
    try {
      // construct GitHub release page URL with remoteLatest
      URI uri = URI.create(
        String.format("https://github.com/hizumiaoba/MineCraftTimeMachine/releases/tag/v%s",
          remoteLatest.asStringNotation()));
      log.info("Opening release page: {}", uri);
      Desktop.getDesktop().browse(uri);
    } catch (IOException e) {
      ExceptionPopup p = new ExceptionPopup(e, "リリースページを開けませんでした。",
        "UpdateDialogController#onOpenReleasePageBtnClick");
      p.pop();
    }
  }

  @FXML
  void onCloseBtnClick(ActionEvent e) {
    log.info("Close button clicked.");
    Stage stage = (Stage) updateProgressBar.getScene().getWindow();
    stage.close();
  }
}
