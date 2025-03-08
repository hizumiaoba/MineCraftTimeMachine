package io.github.hizumiaoba.mctimemachine;

import io.github.hizumiaoba.mctimemachine.api.Version;
import io.github.hizumiaoba.mctimemachine.api.fs.ArtifactDownloader;
import io.github.hizumiaoba.mctimemachine.api.fs.ArtifactManager;
import io.github.hizumiaoba.mctimemachine.api.fs.DownloadProgressListener;
import io.github.hizumiaoba.mctimemachine.api.version.MinimalRemoteVersionCrate;
import io.github.hizumiaoba.mctimemachine.api.version.VersionChecker;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.TlsVersion;

@Slf4j
public class UpdateModalController {

  public CheckBox prereleaseChkbox;
  @FXML
  private Label versionInfoLabel;
  @FXML
  private Button checkUpdateBtn;
  @FXML
  ProgressBar downloadProgressBar;
  @FXML
  private Button downloadInstallerBtn;
  @FXML
  private CheckBox zipDownloadChkbox;
  @FXML
  private CheckBox openFolderWhenCompleteChkbox;

  private VersionChecker checker;
  private VersionObj clientVersion;
  private static volatile MinimalRemoteVersionCrate remoteVersionCache;
  private final String versionInfo = "使用バージョン：%s, 公開されている最新バージョン：%s";
  private final String releasePageUrl = "https://github.com/hizumiaoba/MineCraftTimeMachine/releases/tag/v%s";

  @FXML
  void initialize() {
    log.trace("UpdateModalController initialized");
    downloadInstallerBtn.setDisable(true);
    if(remoteVersionCache != null) {
      this.versionInfoLabel.setText(this.versionInfo.formatted(clientVersion, downConvert(remoteVersionCache).asStringNotation()));
    }

    try {
      this.checker = new VersionChecker();
    } catch (IOException e) {
      log.error("Failed to initialize VersionChecker due to an I/O error", e);
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("エラー");
      alert.setHeaderText("バージョン確認に必要な初期化処理に失敗しました");
      alert.setContentText("通常のバックアップ処理には影響ありませんが、バージョン確認機能は利用できません。");
      alert.initModality(Modality.APPLICATION_MODAL);
      alert.showAndWait();
    }
    this.clientVersion = VersionObj.parse(MineCraftTimeMachineApplication.class.getAnnotation(
      Version.class));
  }

  private VersionObj downConvert(MinimalRemoteVersionCrate remoteVersionCache) {
    return VersionObj.parse(remoteVersionCache.getTagName());
  }

  @FXML
  void onCloseClicked(ActionEvent e) {
    log.trace("Close button clicked");
    Stage modalStage = (Stage) checkUpdateBtn.getScene().getWindow();
    modalStage.close();
  }

  @FXML
  void onCheckUpdateClicked(ActionEvent e) {
    log.trace("Check update button clicked");
    checkUpdateBtn.setDisable(true);
    prereleaseChkbox.setDisable(true);
    final boolean preferPrerelease = prereleaseChkbox.isSelected();

    synchronized (UpdateModalController.class) {
      if(remoteVersionCache == null) {
        log.trace("Remote version cache is null");
        remoteVersionCache = checker.getLatestVersion(preferPrerelease)
          .map(MinimalRemoteVersionCrate::of)
          .orElseThrow(() -> new RuntimeException("Failed to fetch the latest version"));
      }
    }
    this.checkUpdateBtn.setDisable(false);
    this.prereleaseChkbox.setDisable(false);
    VersionObj remoteVersion = downConvert(remoteVersionCache);
    if(clientVersion.compareTo(remoteVersion) != 0) {
      this.downloadInstallerBtn.setDisable(false);
    }

    this.versionInfoLabel.setText(
      this.versionInfo.formatted(clientVersion.asStringNotation(), remoteVersion.asStringNotation()));
  }

  @FXML
  void onDownloadInstallerClicked(ActionEvent e) {
    log.trace("Download installer button clicked");
    if(remoteVersionCache == null) {
      log.error("Remote version cache is null");
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("エラー");
      alert.setHeaderText("最新バージョン情報がありません");
      alert.setContentText("チェック中にエラーが発生していたか、チェック前にこのボタンが押せる場合は、再度最新かどうかの確認を行ってください。");
      return;
    }
    ConnectionSpec spec = new ConnectionSpec.Builder(ConnectionSpec.COMPATIBLE_TLS)
      .tlsVersions(
        TlsVersion.TLS_1_2,
        TlsVersion.TLS_1_3)
      .cipherSuites(
        CipherSuite.TLS_AES_128_GCM_SHA256,
        CipherSuite.TLS_AES_256_GCM_SHA384)
      .build();
    OkHttpClient okhttpClient = new OkHttpClient.Builder()
      .connectionSpecs(List.of(spec))
      .followRedirects(true)
      .followSslRedirects(true)
      .build();
    final ArtifactDownloader downloader = new ArtifactDownloader(okhttpClient);
    final ArtifactManager manager = new ArtifactManager(
      downloader,
      Paths.get("tmp"),
      remoteVersionCache,
      new DownloadProgressImpl(this.downloadProgressBar, openFolderWhenCompleteChkbox.isSelected())
    );
    final boolean preferZip = zipDownloadChkbox.isSelected();
    Platform.runLater(() -> {
      downloadProgressBar.setProgress(0);
      downloadProgressBar.setVisible(true);
    });
    try {
      if(preferZip) {
        manager.startZipDownload();
      } else {
        manager.startInstallerDownload();
      }
    } catch (IOException ex) {
      log.error("Failed to download artifact", ex);
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("エラー");
      alert.setHeaderText("インストーラのダウンロードに失敗しました");
      alert.initModality(Modality.APPLICATION_MODAL);
      alert.showAndWait();
    }
  }

  @FXML
  private void onOpenReleasePageButton(ActionEvent e) {
    log.trace("Open release page button clicked");
    if(remoteVersionCache == null) {
      log.error("Remote version cache is null");
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("エラー");
      alert.setHeaderText("最新バージョン情報がありません");
      alert.setContentText("チェック中にエラーが発生していたか、チェック前にこのボタンが押せる場合は、再度最新かどうかの確認を行ってください。");
      return;
    }
    VersionObj remoteVersion = downConvert(remoteVersionCache);
    String url = releasePageUrl.formatted(remoteVersion.asStringNotation());
    try {
      java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
    } catch (IOException ex) {
      log.error("Failed to open the release page: {}", url, ex);
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("エラー");
      alert.setHeaderText("リリースページが開けませんでした。");
      alert.setContentText("規定のブラウザを開けませんでした。");
      alert.initModality(Modality.APPLICATION_MODAL);
      alert.showAndWait();
    }
  }

  @RequiredArgsConstructor
  static class DownloadProgressImpl implements DownloadProgressListener {

    private final ProgressBar progressBar;
    private final boolean openFolderWhenComplete;

    @Override
    public void onProgress(long bytesRead, long contentLength) {
      final double progress = (double) bytesRead / contentLength;
      Platform.runLater(() -> progressBar.setProgress(progress));
    }

    @Override
    public void onComplete(String fileName, Path savePath) {
      if (openFolderWhenComplete) {
        try {
          java.awt.Desktop.getDesktop().open(savePath.getParent().toFile());
        } catch (IOException e) {
          log.error("Failed to open the download folder", e);
        }
      }
      Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("完了");
        alert.setHeaderText("インストーラのダウンロードが完了しました");
        alert.setContentText("ダウンロード先: " + savePath);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
      });
    }

    @Override
    public void onError(Exception e, String fileName, Path savePath) {
      if (fileName.equals("null")) {
        // means that the error occurred in client-side
        Platform.runLater(() -> {
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("エラー");
          alert.setHeaderText("インストーラのダウンロードに失敗しました");
          alert.setContentText(
            "クライアント側のエラーが発生しました。この症状が継続する場合は、開発者に報告してください。");
          alert.initModality(Modality.APPLICATION_MODAL);
          alert.showAndWait();
        });
        return;
      }
      // below is the server-side error
      final String message = e.getMessage();
      log.error(message);
      log.error("Error in about to download {} into {}", fileName, savePath.toAbsolutePath(), e);
      Platform.runLater(() -> {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("エラー");
        alert.setHeaderText("インストーラのダウンロードに失敗しました");
        alert.setContentText(
          "サーバー側のエラーが発生しました。インターネット環境、またはサーバーの状態を確認してください。");
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait();
      });
    }
  }
}
