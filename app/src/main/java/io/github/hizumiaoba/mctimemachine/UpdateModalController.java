package io.github.hizumiaoba.mctimemachine;

import io.github.hizumiaoba.mctimemachine.api.Version;
import io.github.hizumiaoba.mctimemachine.api.version.MinimalRemoteVersionCrate;
import io.github.hizumiaoba.mctimemachine.api.version.VersionChecker;
import io.github.hizumiaoba.mctimemachine.internal.version.VersionObj;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CipherSuite;
import okhttp3.ConnectionSpec;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.TlsVersion;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class UpdateModalController {

  @FXML
  private Label versionInfoLabel;
  @FXML
  private Button checkUpdateBtn;
  @FXML
  ProgressBar downloadProgressBar;

  @FXML
  private Button downloadInstallerBtn;

  private VersionChecker checker;
  private VersionObj clientVersion;
  private static volatile MinimalRemoteVersionCrate remoteVersionCache;
  private final String versionInfo = "使用バージョン：%s, 公開されている最新バージョン：%s";
  private final String releasePageUrl = "https://github.com/hizumiaoba/MineCraftTimeMachine/releases/tag/v%s";

  @FXML
  void initialize() {
    log.trace("UpdateModalController initialized");
    Tooltip checkUpdateBtnTooltip = new Tooltip("リポジトリに接続し、最新かどうかの確認を行います");
    checkUpdateBtn.setTooltip(checkUpdateBtnTooltip);
    Tooltip downloadInstallerBtnTooltip = new Tooltip("最新のインストーラをダウンロードします。");
    downloadInstallerBtn.setTooltip(downloadInstallerBtnTooltip);
    downloadInstallerBtn.setDisable(true);
    if(remoteVersionCache != null) {
      this.versionInfoLabel.setText(this.versionInfo.formatted(clientVersion, downConvert(remoteVersionCache).asStringNotation()));
    }

    this.checker = new VersionChecker();
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

    synchronized (UpdateModalController.class) {
      if(remoteVersionCache == null) {
        log.trace("Remote version cache is null");
        remoteVersionCache = checker.getLatestVersion(false)
          .orElseThrow(() -> new RuntimeException("Failed to get latest version"));
      }
    }
    this.checkUpdateBtn.setDisable(false);
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
    Platform.runLater(() -> download(Paths.get("tmp"), okhttpClient));
  }

  private void download(Path savePath, OkHttpClient okhttpClient) {
    try {
      Files.createDirectories(savePath);
    } catch (IOException e) {
      log.error("Failed to create directories: {}", savePath, e);
      throw new RuntimeException(e);
    }
    String downloadUrl = remoteVersionCache.getAssets().
      parallelStream()
      .filter(e -> e.getName().endsWith(".msi"))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("Failed to find installer"))
      .getDownloadUrl();
    Request request = new Request.Builder()
      .url(downloadUrl)
      .build();
    okhttpClient
      .newCall(request)
      .enqueue(new Callback() {
        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
          log.error("Client-side error has occurred to enqueue the request to: {}", call.request().url(), e);
          Alert alert = new Alert(Alert.AlertType.ERROR);
          alert.setTitle("エラー");
          alert.setHeaderText("インストーラのダウンロードに失敗しました");
          alert.setContentText("クライアント側のエラーが発生しました。この症状が継続する場合は、開発者に報告してください。");
          alert.initModality(Modality.APPLICATION_MODAL);
          alert.showAndWait();
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
          if(!response.isSuccessful()) {
            log.error("Server has responded with an error {}: {}", response.code(), response.message());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("エラー");
            alert.setHeaderText("インストーラのダウンロードに失敗しました");
            alert.setContentText("サーバー側のエラーが発生しました。インターネット環境、またはサーバーの状態を確認してください。");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
          }
          ResponseBody body = Objects.requireNonNull(response.body(), "Response body is null");
          long contentLength = body.contentLength();
          BufferedSource source = body.source();

          String installerName = remoteVersionCache.getAssets().parallelStream()
            .filter(e -> e.getName().endsWith(".msi"))
            .findFirst()
            .orElseThrow(() -> new RuntimeException("Failed to find installer"))
            .getName();
          BufferedSink sink = Okio.buffer(Okio.sink(savePath.resolve(installerName).toFile()));
          Buffer sinkBuffer = sink.getBuffer();

          long totalBytesRead = 0;
          int bufferSize = 8 * 1024;
          for (long bytesRead; (bytesRead = source.read(sinkBuffer, bufferSize)) != -1; ) {
            sink.emit();
            totalBytesRead += bytesRead;
            final double progress = (double) totalBytesRead / contentLength;
            Platform.runLater(() -> downloadProgressBar.setProgress(progress));
          }
          sink.flush();
          sink.close();
          source.close();
          Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("完了");
            alert.setHeaderText("インストーラのダウンロードが完了しました");
            alert.setContentText("ダウンロード先: " + savePath);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
          });
        }
      });

  }

}