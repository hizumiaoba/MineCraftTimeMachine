package io.github.hizumiaoba.mctimemachine;

import com.melloware.jintellitype.HotkeyListener;
import com.melloware.jintellitype.JIntellitype;
import io.github.hizumiaoba.mctimemachine.MainController.GlobalShortcutKeyListener.Shortcut;
import io.github.hizumiaoba.mctimemachine.api.Config;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import io.github.hizumiaoba.mctimemachine.internal.ApplicationConfig;
import io.github.hizumiaoba.mctimemachine.internal.concurrent.ConcurrentThreadFactory;
import io.github.hizumiaoba.mctimemachine.internal.natives.NativeHandleUtil;
import io.github.hizumiaoba.mctimemachine.service.BackupService;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory.IntegerSpinnerValueFactory;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

  @FXML
  private CheckBox enableAutoExitOnQuittingGamesChkbox;

  @FXML
  private CheckBox enableAutoBackupOnQuittingGamesChkbox;

  private Config mainConfig;
  private static final ExecutorService es;
  private static final ThreadFactory internalControllerThreadFactory = new ConcurrentThreadFactory(
    "Internal", "Controller", true);
  private BackupService backupService;

  static {
    es = Executors.newCachedThreadPool(new ConcurrentThreadFactory("Main GUI", "Controller", true));
  }

  @FXML
  void initialize() {
    String confPath = System.getProperty("user.home")
      + File.separator + ".mctimemachine" + File.separator
      + "application.properties";
    mainConfig = ApplicationConfig.getInstance(confPath);
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
      mainConfig.set("exit_on_quitting_minecraft",
        enableAutoExitOnQuittingGamesChkbox.isSelected() ? "true" : "false");
      mainConfig.set("backup_on_quitting_minecraft",
        enableAutoBackupOnQuittingGamesChkbox.isSelected() ? "true" : "false");
      mainConfig.save();
      es.shutdownNow();
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
    enableAutoExitOnQuittingGamesChkbox.setSelected(
      Boolean.parseBoolean(mainConfig.load("exit_on_quitting_minecraft")));
    enableAutoBackupOnQuittingGamesChkbox.setSelected(
      Boolean.parseBoolean(mainConfig.load("backup_on_quitting_minecraft")));
    
    backupService = new BackupService(backupSavingFolderPathField.getText(), savesFolderPathField.getText());
    
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      if (backupService != null) {
        backupService.shutdown();
        BackupService.getBackupSchedulerExecutors().shutdownNow();
      }
    }));
  }

  @FXML
  void onBackupNowBtnClick() {
    runConcurrentTask(es, () -> {
      backupService.updateBackupPath(backupSavingFolderPathField.getText());
      backupService.createNormalBackup(
        backupCountSpinner.getValue(),
        (started) -> Platform.runLater(() -> {
          changeBackupButtonState(true);
          backupNowBtn.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;");
          backupNowBtn.setText("バックアップ中…");
          backupNowWithShortcutChkbox.setDisable(true);
          specialBackupNowWithShortcutChkbox.setDisable(true);
        }),
        (completed) -> Platform.runLater(() -> {
          changeBackupButtonState(false);
          backupNowBtn.setStyle("");
          backupNowBtn.setText("いますぐバックアップ");
          backupNowWithShortcutChkbox.setDisable(false);
          specialBackupNowWithShortcutChkbox.setDisable(false);
        })
      );
    });
  }

  @FXML
  void onBackupScheduledBtnClick() {
    runConcurrentTask(es, () -> {
      backupService.updateBackupPath(backupSavingFolderPathField.getText());
      boolean isStarted = backupService.scheduleBackup(
        backupScheduleDurationSpinner.getValue(),
        () -> {
          log.trace("Backup scheduler is running.");
          onBackupNowBtnClick();
        },
        (isActive) -> Platform.runLater(() -> {
          if (isActive) {
            backupScheduledBtn.setText("定期バックアップ中！");
            backupScheduledBtn.setStyle("-fx-background-color: #ff0000; -fx-text-fill: #fff; -fx-font-weight: bold;");
          } else {
            backupScheduledBtn.setStyle("");
            backupScheduledBtn.setText("定期バックアップ開始");
          }
        })
      );
    });
  }

  @FXML
  void onOpenBackupListBtnClick() throws IOException {
    log.trace("Opening the backup list.");
    FXMLLoader loader = new FXMLLoader(
      MineCraftTimeMachineApplication.class.getResource("manager.fxml"));
    Stage managerDialogStage = new Stage();
    managerDialogStage.setScene(new Scene(loader.load()));
    
    BackupManagerController managerController = loader.getController();
    managerController.setBackupService(backupService);
    
    managerDialogStage.initModality(Modality.APPLICATION_MODAL);
    managerDialogStage.showAndWait();
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
      AtomicBoolean atomicEnableAutoBackupOnQuittingGames = new AtomicBoolean(
        enableAutoBackupOnQuittingGamesChkbox.isSelected());
      AtomicBoolean atomicEnableAutoExitOnQuittingGames = new AtomicBoolean(
        enableAutoExitOnQuittingGamesChkbox.isSelected());
      Platform.runLater(() -> {
        enableAutoBackupOnQuittingGamesChkbox.setDisable(true);
        enableAutoExitOnQuittingGamesChkbox.setDisable(true);
      });
      boolean triggerAutomation =
        atomicEnableAutoBackupOnQuittingGames.get() || atomicEnableAutoExitOnQuittingGames.get();
      try {
        Runtime.getRuntime().exec(launcherExePathField.getText());
        if (triggerAutomation) {
          ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor(new ConcurrentThreadFactory("MainController", "process-killer-scheduled", true));
          final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
          log.debug("Scheduling to kill the program after 3 minutes.");
          scheduledExecutor.schedule(() -> {
            Optional<ProcessHandle> handle = isWindows ? NativeHandleUtil.getMinecraftProcessId()
              : NativeHandleUtil.getMinecraftProcess();
            handle.ifPresentOrElse(it -> {
              log.debug("scheduling to kill the program");
              it.onExit().thenRun(() -> {
                log.debug("Minecraft process has been exited.");
                onMinecraftProcessExit(atomicEnableAutoBackupOnQuittingGames.get(),
                  atomicEnableAutoExitOnQuittingGames.get());
              });
            }, () -> log.warn("No Minecraft process was found."));
            Platform.runLater(() -> {
              enableAutoBackupOnQuittingGamesChkbox.setDisable(false);
              enableAutoExitOnQuittingGamesChkbox.setDisable(false);
            });
          }, 3, TimeUnit.MINUTES);
        }
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "外部プロセスを開始できませんでした。",
          "MainController#onOpenLauncherBtnClick()$lambda");
        popup.pop();
      }
    });
  }

  private void onMinecraftProcessExit(boolean enableAutoBackup, boolean enableAutoExit) {
    Platform.runLater(() -> {
      if(enableAutoBackup) {
        onBackupNowBtnClick();
      }
      if(enableAutoExit) {
        System.exit(0);
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
    File f = openFileChooser("バックアップを保存するフォルダを選択してください。",
        new File(System.getProperty("user.home")),
        "フォルダ", "*/");
      if (f == null) {
        return;
      }
    assignPath(backupSavingFolderPathField, f);
  }

  @FXML
  void onSelectSavesFolderBtnClick() {
    File f = openFileChooser("\".minecraft/saves\"フォルダを選択してください。",
        new File(System.getProperty("user.home")),
        "フォルダ", "*/");
      if (f == null) {
        return;
      }
    assignPath(savesFolderPathField, f);
  }

  @FXML
  void onSelectLauncherExeBtnClick() {
    File f = openFileChooser("ランチャーの実行ファイルを選択してください。",
      new File(System.getProperty("user.home")),
      "実行ファイル", "*.exe");
    if (f == null) {
      return;
    }
    assignPath(launcherExePathField, f);
  }

  private File openFileChooser(String title, File initialDir, String extDesc, String... exts) {
    FileChooser fc = new FileChooser();
    fc.setTitle(title);
    fc.setInitialDirectory(initialDir);
    fc.getExtensionFilters().add(new FileChooser.ExtensionFilter(extDesc, exts));
    log.debug("awaiting for the user to select the file.");
    File f = fc.showOpenDialog(null);
    if (f == null) {
      log.debug("Got nothing.");
      return null;
    }
    return f;
  }

  private void changeBackupButtonState(boolean isDisabled) {
    this.backupNowBtn.setDisable(isDisabled);
    this.specialBackupNowBtn.setDisable(isDisabled);
    this.backupScheduledBtn.setDisable(isDisabled);
    this.openBackupListBtn.setDisable(isDisabled);
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
        Desktop.getDesktop().browse(URI.create("https://mctimemachine.youtrack.cloud/form/4e4b584c-2709-425a-8eb4-e3125fa27b95"));
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "ブラウザを開けませんでした。", "MainController#onSendFeedbackBtnClick()$lambda");
        popup.pop();
      }
    });
  }

  @FXML
  void onSpecialBackupNowBtnClick() {
    runConcurrentTask(es, () -> {
      backupService.updateBackupPath(backupSavingFolderPathField.getText());
      backupService.createSpecialBackup(
        backupCountSpinner.getValue(),
        (started) -> Platform.runLater(() -> {
          changeBackupButtonState(true);
          specialBackupNowBtn.setStyle("-fx-text-fill: #ff0000; -fx-font-weight: bold;");
          specialBackupNowBtn.setText("特殊バックアップ中…");
          backupNowWithShortcutChkbox.setDisable(true);
          specialBackupNowWithShortcutChkbox.setDisable(true);
        }),
        (completed) -> Platform.runLater(() -> {
          changeBackupButtonState(false);
          specialBackupNowBtn.setStyle("");
          specialBackupNowBtn.setText("いますぐ特殊バックアップ");
          backupNowWithShortcutChkbox.setDisable(false);
          specialBackupNowWithShortcutChkbox.setDisable(false);
        })
      );
    });
  }

  @FXML
  void onOpenReleasePageOnWebBtnClick() throws IOException {
    log.trace("Opening dialog to show the update modal.");
    // open new dialog with `manager.fxml`
    FXMLLoader loader = new FXMLLoader(
      MineCraftTimeMachineApplication.class.getResource("updateModal.fxml"));
    Stage updateDialogStage = new Stage();
    updateDialogStage.setScene(new Scene(loader.load()));
    updateDialogStage.initModality(Modality.APPLICATION_MODAL);
    updateDialogStage.showAndWait();
  }

  private void runConcurrentTask(ExecutorService service, Runnable task) {
    service.execute(task);
  }

  public void onOpenAppLogFolderBtnClick() {
    runConcurrentTask(es, () -> {
      try {
        log.debug("Opening the App Log folder.");
        Desktop.getDesktop().open(Paths.get("").toAbsolutePath().toFile());
      } catch (IOException e) {
        ExceptionPopup popup = new ExceptionPopup(e, "フォルダを開けませんでした。",
          "MainController#onOpenBackupSavingFolderBtnClick()$lambda");
        popup.pop();
      }
    });
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
