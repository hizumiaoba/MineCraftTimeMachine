package io.github.hizumiaoba.mctimemachine;

import io.github.hizumiaoba.mctimemachine.api.BackupDirAttributes;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
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
import lombok.Setter;
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

  @Setter
  private Map<String, BackupDirAttributes> attributes;

  private static Map<String, BackupDirAttributes> retrieveAttributes() throws IOException {
    Map<String, BackupDirAttributes> attributes = new HashMap<>();
    try (Stream<Path> s = MainController.backupUtils.getBackupDirPaths().stream()) {
      s.forEach(p -> {
        BackupDirAttributes attr;
        try (Stream<Path> list = Files.list(p)) {
          attr = new BackupDirAttributes(
            calculateSizeRecursively(p),
            p.getFileName().toString().startsWith("Sp_"),
            Files.readAttributes(p, BasicFileAttributes.class).creationTime(),
            (int) list.filter(Files::isDirectory).count()
          );
          attributes.put(p.getFileName().toString(), attr);
        } catch (IOException e) {
          log.warn("Failed to get attributes for {}", p.getFileName(), e);
          log.warn("Skipping this directory");
        }
      });
    }
    return attributes;
  }

  private static long calculateSizeRecursively(Path p) {
    long size = 0;
    try (Stream<Path> s = Files.list(p)) {
      size = s.map(path -> {
        try {
          return Files.isDirectory(path) ? calculateSizeRecursively(path) : Files.size(path);
        } catch (IOException e) {
          log.warn("Failed to get size of {}", path.getFileName(), e);
        }
        return 0L;
      }).reduce(0L, Long::sum);
    } catch (IOException e) {
      log.warn("Failed to calculate size of {}", p.getFileName(), e);
    }
    return size;
  }

  @FXML
  void initialize() throws IOException {
    log.info("BackupManagerController initialized.");
    this.attributes = retrieveAttributes();
    ObservableList<String> items = FXCollections.observableArrayList();
    try {
      items = FXCollections.observableList(
        MainController.backupUtils.getBackupDirPaths().parallelStream()
          .map(p -> p.getFileName().toString())
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

  private void updateListView() {
    ObservableList<String> newItems = FXCollections.observableArrayList();
    try {
      newItems.addAll(MainController.backupUtils.getBackupDirPaths().parallelStream().map(p -> p.getFileName().toString()).toList());
    } catch (IOException e) {
      log.error("Failed to get backup directory names", e);
      ExceptionPopup p = new ExceptionPopup(e, "バックアップフォルダ名の取得に失敗しました",
        "BackupManagerController#updateListView");
      p.pop();
    } finally {
      this.backupFolderListView.setItems(newItems);
    }
  }

  @FXML
  private void onDeleteButtonClicked() {
    List<Path> dirList;
    try {
      dirList = MainController.backupUtils.getBackupDirPaths();
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
              MainController.backupUtils.deleteBackupRecursively(d);
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
    List<Path> dirList;
    try {
      dirList = MainController.backupUtils.getBackupDirPaths();
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
              MainController.backupUtils.duplicate(d);
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
    String selectedWorld = backupFolderListView.getSelectionModel().getSelectedItem();
    Path selectedWorldPath = MainController.backupUtils.getBackupDirPaths().stream()
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
          MainController.backupUtils.restoreBackup(selectedWorldPath);
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
}
