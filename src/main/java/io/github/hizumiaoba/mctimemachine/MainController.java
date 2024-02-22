package io.github.hizumiaoba.mctimemachine;

import io.github.hizumiaoba.mctimemachine.api.Config;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import io.github.hizumiaoba.mctimemachine.internal.ApplicationConfig;
import io.github.hizumiaoba.mctimemachine.internal.concurrent.ConcurrentThreadFactory;
import io.github.hizumiaoba.mctimemachine.internal.fs.BackupUtils;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MainController {


  @FXML
  private ResourceBundle resources;

  @FXML
  private URL location;

  @FXML
  private Spinner<Integer> backupCountSpinner;

  @FXML
  private Button backupNowBtn;

  @FXML
  private CheckBox backupNowWithShortcutChkbox;

  @FXML
  private TextField backupSavingFolderPathField;

  @FXML
  private Spinner<Integer> backupScheduleDurationSpinner;

  @FXML
  private Button backupScheduledBtn;

  @FXML
  private TextField launcherExePathField;

  @FXML
  private Button openBackupListBtn;

  @FXML
  private Button openBackupSavingFolderBtn;

  @FXML
  private Button openConfigEditorBtn;

  @FXML
  private Button openLauncherBtn;

  @FXML
  private Button openRelatedFolderBtn;

  @FXML
  private Button openSavesFolderBtn;

  @FXML
  private TextField savesFolderPathField;

  @FXML
  private Button selectBackupSavingFolderBtn;

  @FXML
  private Button selectLauncherExeBtn;

  @FXML
  private Button selectSavesFolderBtn;

  @FXML
  private Button sendFeedbackBtn;

  @FXML
  private Button specialBackupNowBtn;

  @FXML
  private CheckBox specialBackupNowWithShortcutChkbox;

  @FXML
  private TabPane mainTabPane;

  private Config mainConfig;
  private static final ExecutorService es;
  private static final ThreadFactory internalControllerThreadFactory = new ConcurrentThreadFactory(
    "Internal", "Controller", true);
  private static final ScheduledExecutorService backupSchedulerExecutors;
  private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
  private static ScheduledFuture<?> backupScheduledFuture;
  @Getter
  private BackupUtils backupUtils;

  static {
    es = Executors.newCachedThreadPool(new ConcurrentThreadFactory("Main GUI", "Controller", true));
    backupSchedulerExecutors = Executors.newSingleThreadScheduledExecutor(
      new ConcurrentThreadFactory("Backup", "Scheduler", true));
  }

  @FXML
  void initialize() {
    mainConfig = ApplicationConfig.getInstance("application.properties");
    Runtime.getRuntime().addShutdownHook(internalControllerThreadFactory.newThread(() -> {
      log.info("saving configurations.");
      mainConfig.set("saves_folder_path", savesFolderPathField.getText());
      mainConfig.set("backup_saving_folder_path", backupSavingFolderPathField.getText());
      mainConfig.set("launcher_exe_path", launcherExePathField.getText());
      mainConfig.set("backup_count", backupCountSpinner.getValue().toString());
      mainConfig.set("backup_schedule_duration", backupScheduleDurationSpinner.getValue().toString());
      mainConfig.save();
      es.shutdownNow();
      backupSchedulerExecutors.shutdownNow();
    }));

    savesFolderPathField.setText(mainConfig.load("saves_folder_path"));
    backupSavingFolderPathField.setText(mainConfig.load("backup_saving_folder_path"));
    launcherExePathField.setText(mainConfig.load("launcher_exe_path"));
    backupCountSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 100, 1));
    backupScheduleDurationSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 2000, 1));
    backupCountSpinner.getValueFactory().setValue(Integer.parseInt(mainConfig.load("backup_count")));
    backupScheduleDurationSpinner.getValueFactory().setValue(Integer.parseInt(mainConfig.load("backup_schedule_duration")));
    backupUtils = new BackupUtils(backupSavingFolderPathField.getText());
  }

  private void checkPath() {
    if (!this.backupUtils.getBackupPath()
      .equals(Paths.get(this.backupSavingFolderPathField.getText()))) {
      log.debug("Backup path has been changed.");
      this.backupUtils.setBackupPath(this.backupSavingFolderPathField.getText());
    }
  }

  @FXML
  void onBackupNowBtnClick() {

    es.submit(() -> {
      Platform.runLater(() -> {
        backupNowBtn.setStyle(
          "-fx-text-fill: #ff0000; -fx-font-weight: bold;"
        );
        backupNowBtn.setText("バックアップ中…");
        backupNowWithShortcutChkbox.setDisable(true);
        specialBackupNowWithShortcutChkbox.setDisable(true);
      });
      checkPath();
      this.backupUtils.createBackupDir();
      try {
        this.backupUtils.backup(Paths.get(
            savesFolderPathField.getText()),
          false,
          backupCountSpinner.getValue());
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "バックアップを作成できませんでした。",
          "MainController#onBackupNowBtnClick()$lambda");
        popup.pop();
      }
      Platform.runLater(() -> {
        backupNowBtn.setStyle("");
        backupNowBtn.setText("いますぐバックアップ");
        backupNowWithShortcutChkbox.setDisable(false);
        specialBackupNowWithShortcutChkbox.setDisable(false);
      });
    });
  }

  @FXML
  void onBackupScheduledBtnClick() {
    checkPath();
    this.backupUtils.createBackupDir();
    if (backupScheduledFuture == null) {
      log.trace("Guessed that the backup scheduler is not running.");
      backupScheduledFuture = backupSchedulerExecutors.scheduleAtFixedRate(
        () -> {
          log.trace("Backup scheduler is running.");
          onBackupNowBtnClick();
        }, 0,
        backupScheduleDurationSpinner.getValue(), TimeUnit.MINUTES);
      backupScheduledBtn.setText("定期バックアップ中！");
      backupScheduledBtn.setStyle(
        "-fx-background-color: #ff0000; -fx-text-fill: #fff; -fx-font-weight: bold;");
    } else {
      log.trace("Guessed that the backup scheduler is running.");
      if (backupScheduledFuture.cancel(false)) {
        log.debug("Backup scheduler could be canceled.");
      }
      backupScheduledBtn.setStyle("");
      backupScheduledBtn.setText("定期バックアップ開始");
      backupScheduledFuture = null;
    }
  }

  @FXML
  void onOpenBackupListBtnClick() {
    System.out.println("Open Backup List button clicked");
  }

  @FXML
  void onOpenBackupSavingFolderBtnClick() {
    System.out.println("Open Backup Saving Folder button clicked");
  }

  @FXML
  void onOpenConfigEditorBtnClick() {
    System.out.println("Open Config Editor button clicked");
  }

  @FXML
  void onOpenLauncherBtnClick() {
    es.execute(() -> {
      log.trace("create external process to open the launcher.");
      log.trace("launcher path: {}", launcherExePathField.getText());
      try {
        Runtime.getRuntime().exec(launcherExePathField.getText());
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "外部プロセスを開始できませんでした。", "MainController#onOpenLauncherBtnClick()$lambda");
        popup.pop();
      }
    });
  }

  @FXML
  void onOpenRelatedFolderBtnClick() {
    System.out.println("Open Related Folder button clicked");
  }

  @FXML
  void onOpenSavesFolderBtnClick() {
    System.out.println("Open Saves Folder button clicked");
  }

  @FXML
  void onSelectBackupSavingFolderBtnClick() {
    System.out.println("Select Backup Saving Folder button clicked");
  }

  @FXML
  void onSelectLauncherExeBtnClick() {
    System.out.println("Select Launcher Exe button clicked");
  }

  @FXML
  void onSelectSavesFolderBtnClick() {
    System.out.println("Select Saves Folder button clicked");
  }

  @FXML
  void onSendFeedbackBtnClick() {
    es.execute(() -> {
      log.trace("Opening browser to access the git repository.");
      try {
        Desktop.getDesktop().browse(URI.create("https://github.com/hizumiaoba/MineCraftTimeMachine/issues"));
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "ブラウザを開けませんでした。", "MainController#onSendFeedbackBtnClick()$lambda");
        popup.pop();
      }
    });
  }

  @FXML
  void onSpecialBackupNowBtnClick() {
    es.execute(() -> {
      Platform.runLater(() -> {
        specialBackupNowBtn.setStyle(
          "-fx-text-fill: #ff0000; -fx-font-weight: bold;"
        );
        specialBackupNowBtn.setText("特殊バックアップ中…");
        backupNowWithShortcutChkbox.setDisable(true);
        specialBackupNowWithShortcutChkbox.setDisable(true);
      });
      checkPath();
      this.backupUtils.createBackupDir();
      log.info("Starting special backup...");
      try {
        this.backupUtils.backup(
          Paths.get(savesFolderPathField.getText()),
          true,
          backupCountSpinner.getValue());
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "特殊バックアップを作成できませんでした。",
          "MainController#onSpecialBackupNowBtnClick()$lambda");
        popup.pop();
      }
      Platform.runLater(() -> {
        specialBackupNowBtn.setStyle("");
        specialBackupNowBtn.setText("いますぐ特殊バックアップ");
        backupNowWithShortcutChkbox.setDisable(false);
        specialBackupNowWithShortcutChkbox.setDisable(false);
      });
      log.info("Special backup completed.");
    });
  }
}
