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
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javax.swing.JOptionPane;
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
            Files.list(p).map(path -> {
              try {
                return Files.size(path);
              } catch (IOException e) {
                log.warn("Failed to get size of {}", path.getFileName(), e);
              }
              return 0L;
            }).reduce(0L, Long::sum),
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
        try {
          MainController.backupUtils.deleteBackupRecursively(d);
        } catch (IOException ex) {
          log.error("Failed to delete backup", ex);
          ExceptionPopup p = new ExceptionPopup(ex, "バックアップの削除に失敗しました",
            "BackupManagerController#onDeleteButtonClicked$lambda-1");
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
        try {
          MainController.backupUtils.duplicate(d);
        } catch (IOException ex) {
          log.error("Failed to copy backup", ex);
          ExceptionPopup p = new ExceptionPopup(ex, "バックアップのコピーに失敗しました",
            "BackupManagerController#onCopyDirectoryButtonClicked$lambda-1");
          p.pop();
        }
      }, this::onErrorFindingDir);
  }
}
