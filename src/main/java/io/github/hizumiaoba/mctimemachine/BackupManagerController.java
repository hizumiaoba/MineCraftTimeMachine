package io.github.hizumiaoba.mctimemachine;

import io.github.hizumiaoba.mctimemachine.api.BackupDirAttributes;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import io.github.hizumiaoba.mctimemachine.internal.fs.BackupUtils;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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

  @Setter
  private BackupUtils backupUtils;

  @Setter
  private Map<String, BackupDirAttributes> attributes;

  @FXML
  private void initialize() {
    log.info("BackupManagerController initialized.");
    ObservableList<String> items = FXCollections.observableArrayList();
    try {
      items = FXCollections.observableList(
        this.backupUtils.getBackupDirPaths().parallelStream().map(p -> p.getFileName().toString())
          .toList());
      this.backupFolderListView.getSelectionModel().selectFirst();
      this.backupFolderListView.getSelectionModel().selectedItemProperty().addListener(
        (observable, before, after) -> {
          BackupDirAttributes attr = this.attributes.get(after);
          if (attr == null) {
            log.warn("No attributes found for {}", after);
            return;
          }
          this.textFieldBackupFolderName.setText(after);
          this.labelBackupDateCreated.setText(attr.createdAt().toString());
          this.labelBackupKind.setText(attr.isSpecial() ? "特別" : "通常");
          this.labelBackupDataSize.setText(String.valueOf(attr.size()));
          this.labelCountBackedupWorlds.setText(String.valueOf(attr.savedWorldCount()));
        });
    } catch (IOException e) {
      log.error("Failed to get backup directory names", e);
      ExceptionPopup p = new ExceptionPopup(e, "バックアップフォルダ名の取得に失敗しました",
        "BackupManagerController#initialize");
      p.pop();
    } finally {
      this.backupFolderListView.setItems(items);
    }
  }

  @FXML
  private void onDeleteButtonClicked() {
    List<Path> dirList;
    try {
      dirList = this.backupUtils.getBackupDirPaths();
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
        try {
          this.backupUtils.deleteBackupRecursively(d);
        } catch (IOException ex) {
          log.error("Failed to delete backup", ex);
          ExceptionPopup p = new ExceptionPopup(ex, "バックアップの削除に失敗しました",
            "BackupManagerController#onDeleteButtonClicked$lambda-1");
          p.pop();
        }
      }, this::onErrorFindingDir);
  }

  @FXML
  private void onCopyDirectoryButtonClicked() {
    List<Path> dirList;
    try {
      dirList = this.backupUtils.getBackupDirPaths();
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
        try {
          this.backupUtils.duplicate(d);
        } catch (IOException ex) {
          log.error("Failed to copy backup", ex);
          ExceptionPopup p = new ExceptionPopup(ex, "バックアップのコピーに失敗しました",
            "BackupManagerController#onCopyDirectoryButtonClicked$lambda-1");
          p.pop();
        }
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
    JOptionPane.showMessageDialog(null,
      "バックアップフォルダが見つかりませんでした。選択されていないか、内部でエラーが発生している可能性があります。",
      "エラー", JOptionPane.ERROR_MESSAGE);
  }
}
