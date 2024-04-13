package io.github.hizumiaoba.mctimemachine;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import io.github.hizumiaoba.mctimemachine.MainController.GlobalShortcutKeyListener.Shortcut;
import io.github.hizumiaoba.mctimemachine.api.Config;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import io.github.hizumiaoba.mctimemachine.internal.ApplicationConfig;
import io.github.hizumiaoba.mctimemachine.internal.concurrent.ConcurrentThreadFactory;
import io.github.hizumiaoba.mctimemachine.internal.fs.BackupUtils;
import java.awt.Desktop;
import java.io.File;
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
import javafx.stage.FileChooser;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
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
      mainConfig.set("normal_backup_on_shortcut",
        backupNowWithShortcutChkbox.isSelected() ? "true" : "false");
      mainConfig.set("special_backup_on_shortcut",
        specialBackupNowWithShortcutChkbox.isSelected() ? "true" : "false");
      mainConfig.save();
      es.shutdownNow();
      backupSchedulerExecutors.shutdownNow();
    }));

    int MOD_CTRL_SHIFT = JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT;

    JIntellitype.getInstance().registerHotKey(
      Shortcut.BACKUP_NORMAL.id,
      MOD_CTRL_SHIFT,
      'B');
    JIntellitype.getInstance().registerHotKey(
      Shortcut.BACKUP_SPECIAL.id,
      MOD_CTRL_SHIFT,
      'Z');
    JIntellitype.getInstance().addHotKeyListener(new GlobalShortcutKeyListener(this));

    savesFolderPathField.setText(mainConfig.load("saves_folder_path"));
    backupSavingFolderPathField.setText(mainConfig.load("backup_saving_folder_path"));
    launcherExePathField.setText(mainConfig.load("launcher_exe_path"));
    backupCountSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 100, 1));
    backupScheduleDurationSpinner.setValueFactory(new IntegerSpinnerValueFactory(1, 2000, 1));
    backupCountSpinner.getValueFactory().setValue(Integer.parseInt(mainConfig.load("backup_count")));
    backupScheduleDurationSpinner.getValueFactory().setValue(Integer.parseInt(mainConfig.load("backup_schedule_duration")));
    backupNowWithShortcutChkbox.setSelected(
      Boolean.parseBoolean(mainConfig.load("normal_backup_on_shortcut")));
    specialBackupNowWithShortcutChkbox.setSelected(
      Boolean.parseBoolean(mainConfig.load("special_backup_on_shortcut")));
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
    runConcurrentTask(es, () -> {
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
    runConcurrentTask(es, () -> {
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
        Platform.runLater(() -> {
          backupScheduledBtn.setText("定期バックアップ中！");
          backupScheduledBtn.setStyle(
            "-fx-background-color: #ff0000; -fx-text-fill: #fff; -fx-font-weight: bold;");
        });
      } else {
        log.trace("Guessed that the backup scheduler is running.");
        if (backupScheduledFuture.cancel(false)) {
          log.debug("Backup scheduler could be canceled.");
        }
        Platform.runLater(() -> {
          backupScheduledBtn.setStyle("");
          backupScheduledBtn.setText("定期バックアップ開始");
        });
        backupScheduledFuture = null;
      }
    });
  }

  @FXML
  void onOpenBackupListBtnClick() {
    System.out.println("Open Backup List button clicked");
  }

  @FXML
  void onOpenBackupSavingFolderBtnClick() {
    runConcurrentTask(es, () -> {
      try {
        log.debug("Opening the backup saving folder.");
        Desktop.getDesktop().open(Paths.get(backupSavingFolderPathField.getText()).toFile());
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "フォルダを開けませんでした。",
          "MainController#onOpenBackupSavingFolderBtnClick()$lambda");
        popup.pop();
      }
    });
  }

  @FXML
  void onOpenConfigEditorBtnClick() {
    System.out.println("Open Config Editor button clicked");
  }

  @FXML
  void onOpenLauncherBtnClick() {
    runConcurrentTask(es, () -> {
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
    runConcurrentTask(es, () -> {
      try {
        log.debug("Opening the saves folder.");
        Desktop.getDesktop().open(Paths.get(savesFolderPathField.getText()).toFile());
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "フォルダを開けませんでした。",
          "MainController#onOpenSavesFolderBtnClick()$lambda");
        popup.pop();
      }
    });
  }

  @FXML
  void onSelectBackupSavingFolderBtnClick() {
    runConcurrentTask(es, () -> {
      File f = opebFileChooser("バックアップを保存するフォルダを選択してください。",
        new File(System.getProperty("user.home")));
      if (f == null) {
        return;
      }
      Platform.runLater(() -> assignPath(backupSavingFolderPathField, f));
    });
  }

  @FXML
  void onSelectSavesFolderBtnClick() {
    runConcurrentTask(es, () -> {
      File f = opebFileChooser("\".minecraft/saves\"フォルダを選択してください。",
        new File(System.getProperty("user.home")));
      if (f == null) {
        return;
      }
      Platform.runLater(() -> assignPath(savesFolderPathField, f));
    });
  }

  @FXML
  void onSelectLauncherExeBtnClick() {
      FileChooser fc = new FileChooser();
      fc.setTitle("ランチャーの実行ファイルを選択してください。");
      fc.setInitialDirectory(new File(System.getProperty("user.home")));
      log.debug("awaiting for the user to select the executable file.");
      File f = fc.showOpenDialog(null);
      if (f == null) {
        log.debug("Got nothing.");
        return;
      }
      log.debug("Got the file: {}", f.getAbsolutePath());
      launcherExePathField.setText(f.getAbsolutePath());
  }

  private File opebFileChooser(String title, File initialDir) {
    FileChooser fc = new FileChooser();
    fc.setTitle(title);
    fc.setInitialDirectory(initialDir);
    log.debug("awaiting for the user to select the file.");
    File f = fc.showOpenDialog(null);
    if (f == null) {
      log.debug("Got nothing.");
      ExceptionPopup p = new ExceptionPopup(new NullPointerException(),
        "ファイルを選択できませんでした。", "MainController#opebFileChooser()$lambda");
      p.pop();
      return null;
    }
    return f;
  }

  private void assignPath(TextField injected, File target) {
    log.debug("injecting the path: {}", target.getAbsolutePath());
    injected.setText(target.getAbsolutePath());
  }

  @FXML
  void onSendFeedbackBtnClick() {
    runConcurrentTask(es, () -> {
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
    runConcurrentTask(es, () -> {
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

  private void runConcurrentTask(ExecutorService service, Runnable task) {
    service.execute(task);
  }

  @RequiredArgsConstructor
  static class GlobalShortcutKeyListener implements HotkeyListener {

    private final MainController mc;
    private static final ScheduledExecutorService oneshotExecutor = Executors.newSingleThreadScheduledExecutor(
      new ConcurrentThreadFactory("MainController", "Shortcut Key Listener", true));

    @Override
    public void onHotKey(int identifier) {
      switch (Shortcut.fromId(identifier)) {
        case BACKUP_NORMAL -> {
          if (!mc.backupNowWithShortcutChkbox.isSelected()) {
            log.trace("Abort backup shortcut because the checkbox is not selected.");
            return;
          }
          log.debug("Normal backup shortcut is pressed.");
          oneshotExecutor.schedule(() -> Platform.runLater(mc::onBackupNowBtnClick), 10,
            TimeUnit.SECONDS);
        }
        case BACKUP_SPECIAL -> {
          if (!mc.specialBackupNowWithShortcutChkbox.isSelected()) {
            log.trace("Abort special backup shortcut because the checkbox is not selected.");
            return;
          }
          log.debug("Special backup shortcut is pressed.");
          oneshotExecutor.schedule(() -> Platform.runLater(mc::onSpecialBackupNowBtnClick), 10,
            TimeUnit.SECONDS);
        }
        case OPEN_LAUNCHER -> {
          log.debug("Launcher shortcut is pressed.");
          Platform.runLater(mc::onOpenLauncherBtnClick);
        }
        case UNKNOWN -> log.warn("Unknown shortcut is pressed.");
      }
    }

    public enum Shortcut {
      BACKUP_NORMAL(101),
      BACKUP_SPECIAL(102),
      OPEN_LAUNCHER(901),
      UNKNOWN(-1);

      private final int id;

      Shortcut(int id) {
        this.id = id;
      }

      public static Shortcut fromId(int id) {
        for (Shortcut s : values()) {
          if (s.id == id) {
            return s;
          }
        }
        return UNKNOWN;
      }
    }
  }
}
