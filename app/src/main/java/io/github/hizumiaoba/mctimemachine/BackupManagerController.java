package io.github.hizumiaoba.mctimemachine;

import io.github.hizumiaoba.mctimemachine.api.BackupDirAttributes;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import io.github.hizumiaoba.mctimemachine.api.fs.DirectoryScanner;
import io.github.hizumiaoba.mctimemachine.service.BackupService;
import java.io.IOException;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class BackupManagerController {

  @FXML
  private ListView<String> backupFolderListView;

  @FXML
  private TextField textFieldBackupFolderName;

  @FXML
  private Label labelBackupDateCreated;

  @FXML
  private Label labelBackupKind;

  @FXML
  private Label labelBackupDataSize;

  @FXML
  private Label labelCountBackedupWorlds;

  private Map<String, BackupDirAttributes> attributesCache;

  private BackupService backupService;
  private DirectoryScanner directoryScanner;

  @FXML
  void initialize() throws IOException {
    log.info("BackupManagerController initialized.");
    this.attributesCache = new ConcurrentHashMap<>();
  }
  
  public void updateUI() throws IOException {
    if (backupService == null) {
      log.error("BackupService has not been set");
      ExceptionPopup p = new ExceptionPopup(new IllegalStateException("BackupService is null"), 
        "バックアップサービスが設定されていません", "BackupManagerController#updateUI");
      p.pop();
      return;
    }
    if (this.directoryScanner == null) {
      this.directoryScanner = new DirectoryScanner();
      this.directoryScanner.addProgressUpdateListener(
        event -> log.trace("Progress update: {}/{}", event.current(), event.total()));
      this.directoryScanner.addTraversalCompleteListener(
        totalFiles -> {
          log.info("Directory scan completed. Total files: {}", totalFiles);
          // modal dialog configuration will be handled here due to concurrency of scanDirectory
          ObservableList<String> items = FXCollections.observableArrayList();
          try {
            items = FXCollections.observableList(
              backupService.getBackupDirPaths().parallelStream()
                .map(p -> p.getFileName().toString())
                .toList());
            this.backupFolderListView.getSelectionModel().selectFirst();
            this.backupFolderListView.getSelectionModel().selectedItemProperty().addListener(this::listViewSelectionChanged);
          } catch (IOException e) {
            log.error("Failed to get backup directory names", e);
            ExceptionPopup p = new ExceptionPopup(e, "バックアップフォルダ名の取得に失敗しました",
              "BackupManagerController#updateUI");
            p.pop();
          } finally {
            this.backupFolderListView.setItems(items);
          }
        });
      this.directoryScanner.scanDirectory(this.backupService.getBackupPath().toString());
    }
  }

  private void listViewSelectionChanged(ObservableValue<? extends String> observable, String before, String after) {
    BackupDirAttributes attr = this.attributesCache.computeIfAbsent(after, k ->
      this.directoryScanner.getBackups()
        .parallelStream()
        .filter(attribute -> attribute.dirName().equals(k)).findFirst()
        .orElseThrow(() -> {
          log.warn("No attributes found for backup directory: {}", k);
          return new IllegalStateException("No attributes found for backup directory: " + k);
        }));
    updateAttributeDetail(after, attr);
  }

  private void updateAttributeDetail(String after, BackupDirAttributes attr) {
    this.textFieldBackupFolderName.setText(after);
    this.labelBackupDateCreated.setText(attr.createdAt().toString());
    this.labelBackupKind.setText(attr.isSpecial() ? "特別" : "通常");

    String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
    int unitIndex = (int) (Math.log10(attr.size()) / 3);
    double unitValue = 1 << (unitIndex * 10);

    String readableSize = new DecimalFormat("#,##0.###")
      .format(attr.size() / unitValue) + " "
      + units[unitIndex];
    this.labelBackupDataSize.setText(readableSize);
    this.labelCountBackedupWorlds.setText(String.valueOf(attr.savedWorldCount()));
  }

  private void updateListView() {
    ObservableList<String> newItems = FXCollections.observableArrayList();
    try {
      newItems.addAll(backupService.getBackupDirPaths().parallelStream().map(p -> p.getFileName().toString()).toList());
    } catch (IOException e) {
      log.error("Failed to get backup directory names", e);
      ExceptionPopup p = new ExceptionPopup(e, "バックアップフォルダ名の取得に失敗しました",
        "BackupManagerController#updateListView");
      p.pop();
    } finally {
      this.backupFolderListView.setItems(newItems);
      this.directoryScanner.scanDirectory(this.backupService.getBackupPath().toString());
    }
  }

  @FXML
  private void onDeleteButtonClicked() {
    if (backupService == null) {
      log.error("BackupService has not been set");
      return;
    }
    
    List<Path> dirList;
    try {
      dirList = backupService.getBackupDirPaths();
    } catch (IOException ex) {
      log.error("Failed to traverse backup directories", ex);
      ExceptionPopup p = new ExceptionPopup(ex, "バックアップフォルダの走査に失敗しました",
        "BackupManagerController#onDeleteButtonClicked");
      p.pop();
      return;
    }
    
    dirList
      .parallelStream()
      .filter(d -> d.getFileName()
        .toString()
        .equals(
          this.backupFolderListView.getSelectionModel()
            .getSelectedItem()))
      .findFirst()
      .ifPresentOrElse(d -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("バックアップの削除");
        alert.setHeaderText("バックアップ：" + d.getFileName() + "を削除しますか？");
        alert.setContentText("この操作は元に戻せません。");
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait().ifPresentOrElse(response -> {
          if(response == ButtonType.OK) {
            try {
              backupService.deleteBackupRecursively(d);
            } catch (IOException ex) {
              log.error("Failed to delete backup", ex);
              ExceptionPopup p = new ExceptionPopup(ex, "バックアップの削除に失敗しました",
                "BackupManagerController#onDeleteButtonClicked$lambda-1");
              p.pop();
            } finally {
              updateListView();
            }
          } else {
            log.info("Cancelled deleting backup");
          }
        }, () -> log.warn("Neither OK nor CANCEL button clicked"));
      }, this::onErrorFindingDir);
  }

  @FXML
  private void onCloseButtonClicked() {
    log.info("Close button clicked.");
    Stage s = (Stage) this.backupFolderListView.getScene().getWindow();
    s.close();
  }

  private void onErrorFindingDir() {
    log.warn(
      "No backup folder found. possibly no folder selected or encountered an error internally");
    log.trace("Got selected item: {}",
      this.backupFolderListView.getSelectionModel().getSelectedItem());
    log.trace("Got backup folder list: {}", this.backupFolderListView.getItems());

    Alert notFoundAlert = new Alert(Alert.AlertType.ERROR);
    notFoundAlert.setTitle("エラー");
    notFoundAlert.setHeaderText("バックアップフォルダが見つかりませんでした");
    notFoundAlert.setContentText("バックアップフォルダが選択されていないか、内部でエラーが発生している可能性があります。"
      + "\n設定タブを確認し、正しく選択出来ている場合は開発者に報告してください。");
    notFoundAlert.initModality(Modality.APPLICATION_MODAL);
    notFoundAlert.showAndWait();
  }

  @FXML
  private void onCopyDirectoryButtonClicked() {
    if (backupService == null) {
      log.error("BackupService has not been set");
      return;
    }
    
    List<Path> dirList;
    try {
      dirList = backupService.getBackupDirPaths();
    } catch (IOException ex) {
      log.error("Failed to traverse backup directories", ex);
      ExceptionPopup p = new ExceptionPopup(ex, "バックアップフォルダの走査に失敗しました",
        "BackupManagerController#onCopyDirectoryButtonClicked");
      p.pop();
      return;
    }
    dirList
      .parallelStream()
      .filter(d -> d.getFileName()
        .toString()
        .equals(
          this.backupFolderListView.getSelectionModel()
            .getSelectedItem()))
      .findFirst()
      .ifPresentOrElse(d -> {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("バックアップの複製");
        alert.setHeaderText("バックアップ：" + d.getFileName() + "を複製しますか？");
        alert.setContentText("新しいフォルダ名は%s_copyとなります。".formatted(d.getFileName()));
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.showAndWait().ifPresentOrElse(response -> {
          if(response == ButtonType.OK) {
            try {
              backupService.duplicate(d);
            } catch (IOException ex) {
              log.error("Failed to copy backup", ex);
              ExceptionPopup p = new ExceptionPopup(ex, "バックアップのコピーに失敗しました",
                "BackupManagerController#onCopyDirectoryButtonClicked$lambda-1");
              p.pop();
            } finally {
              updateListView();
            }
          } else {
            log.info("Cancelled copying backup");
          }
        }, () -> log.warn("Neither OK nor CANCEL button clicked"));
      }, this::onErrorFindingDir);
  }

  public void onRestoreWorldButtonClicked(ActionEvent actionEvent) throws IOException {
    if (backupService == null) {
      log.error("BackupService has not been set");
      return;
    }
    
    String selectedWorld = backupFolderListView.getSelectionModel().getSelectedItem();
    Path selectedWorldPath = backupService.getBackupDirPaths().stream()
      .filter(p -> p.getFileName().toString().equals(selectedWorld))
      .findFirst()
      .orElseThrow();
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setTitle("ワールドの復元");
    alert.setHeaderText("ワールド：" + selectedWorld + "を復元しますか？");
    alert.setContentText("この操作は元に戻せません。バックアップフォルダは削除されません。");
    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    alert.initModality(Modality.APPLICATION_MODAL);
    alert.showAndWait().ifPresent(response -> {
      if (response == ButtonType.OK) {
        try {
          backupService.restoreBackup(selectedWorldPath);
          Alert info = new Alert(Alert.AlertType.INFORMATION);
          info.setTitle("ワールドの復元");
          info.setHeaderText("ワールドの復元が完了しました");
          info.setContentText("ワールド：" + selectedWorld + "を復元しました。");
          info.initModality(Modality.APPLICATION_MODAL);
          info.showAndWait();
        } catch (IOException e) {
          log.error("Failed to restore world", e);
          ExceptionPopup p = new ExceptionPopup(e, "ワールドの復元に失敗しました",
            "BackupManagerController#onRestoreWorldButtonClicked");
          p.pop();
        }
      } else if (response == ButtonType.CANCEL) {
        log.info("Cancelled restoring world");
      } else {
        log.warn("Neither OK nor CANCEL button clicked");
      }
    });
  }
  
  public void setBackupService(BackupService backupService) {
    this.backupService = backupService;
    try {
      updateUI();
    } catch (IOException e) {
      log.error("Failed to update UI after setting backup service", e);
      ExceptionPopup p = new ExceptionPopup(e, "UIの更新に失敗しました", 
        "BackupManagerController#setBackupService");
      p.pop();
    }
  }
}
