package io.github.hizumiaoba.mctimemachine;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;

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
  void initialize() {
  }

  @FXML
  void onBackupNowBtnClick() {
    System.out.println("Backup Now button clicked");
  }

  @FXML
  void onBackupScheduledBtnClick() {
    System.out.println("Backup Scheduled button clicked");
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
    System.out.println("Open Launcher button clicked");
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
    System.out.println("Send Feedback button clicked");
  }

  @FXML
  void onSpecialBackupNowBtnClick() {
    System.out.println("Special Backup Now button clicked");
  }

  @FXML
  void onSpecialBackupNowWithShortcutChkboxClick() {
    System.out.println("Special Backup Now with Shortcut checkbox clicked");
  }

  @FXML
  void onBackupNowWithShortcutChkboxClick() {
    System.out.println("Backup Now with Shortcut checkbox clicked");
  }

  @FXML
  void onBackupCountSpinnerChange() {
    System.out.println("Backup Count Spinner changed");
  }

  @FXML
  void onBackupScheduleDurationSpinnerChange() {
    System.out.println("Backup Schedule Duration Spinner changed");
  }

  @FXML
  void onBackupSavingFolderPathFieldChange() {
    System.out.println("Backup Saving Folder Path Field changed");
  }

  @FXML
  void onLauncherExePathFieldChange() {
    System.out.println("Launcher Exe Path Field changed");
  }

  @FXML
  void onSavesFolderPathFieldChange() {
    System.out.println("Saves Folder Path Field changed");
  }
}
