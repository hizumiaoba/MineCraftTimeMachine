package io.github.hizumiaoba.mctimemachine.internal;

import io.github.hizumiaoba.mctimemachine.api.Config;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationConfig implements Config {

  private final Path configPath;
  private Properties properties;
  private static final Map<String, ApplicationConfig> instances;
  private static final Map<String, String> defaultConfigSkeleton;

  static {
    instances = new ConcurrentHashMap<>();

    defaultConfigSkeleton = Map.of(
      "saves_folder_path", "C:\\Users\\%USERNAME%\\AppData\\Roaming\\.minecraft\\saves",
      "backup_saving_folder_path", "path\\to\\your\\backup\\folder",
      "launcher_exe_path", "C:\\XboxGames\\Minecraft Launcher\\Content\\Minecraft.exe",
      "backup_schedule_duration", "20",
      "backup_count", "5",
      "normal_backup_on_shortcut", "true",
      "special_backup_on_shortcut", "true",
      "exit_on_quitting_minecraft", "false",
      "backup_on_quitting_minecraft", "false"
    );
  }

  private ApplicationConfig(String configFile) {
    this.configPath = Paths.get(configFile);
  }

  public static ApplicationConfig getInstance(String configFile) {
    if(instances.containsKey(configFile)) {
      return instances.get(configFile);
    } else {
      ApplicationConfig instance = new ApplicationConfig(configFile);
      updateConfigSkeleton(instance);
      instances.put(configFile, instance);
      return instance;
    }
  }

  /**
   * Update current config when there is update in default config skeleton
   * <p>
   * This is only for migration from old version to new version
   *
   * @param instance current config instance
   */
  private static void updateConfigSkeleton(Config instance) {
    instance.load();
    for(Map.Entry<String, String> entry : defaultConfigSkeleton.entrySet()) {
      if(instance.load(entry.getKey()) == null) {
        instance.set(entry.getKey(), entry.getValue());
      }
    }
  }

  @Override
  public void load() {
    if(properties != null) {
      return;
    }
    properties = new Properties();
    if(Files.notExists(this.configPath)) {
      properties.putAll(defaultConfigSkeleton);
      save();
      return;
    }
    try {
      properties.load(this.configPath.toFile().toURI().toURL().openStream());
    } catch (IOException e) {
      ExceptionPopup popup = new ExceptionPopup(e, "設定ファイルを読み込めなかったか、パスの変換に失敗しました。", "ApplicationConfig#load()");
      popup.pop();
    }
  }

  @Override
  public String load(String key) {
    load();
    return properties.getProperty(key);
  }

  @Override
  public void set(String key, String value) {
    load();
    properties.setProperty(key, value);
  }

  @Override
  public void save() {
    if(properties == null) {
      return;
    }
    try {
      properties.store(Files.newOutputStream(this.configPath), "Minecraft Time Machine Config");
    } catch (IOException e) {
      ExceptionPopup popup = new ExceptionPopup(e, "設定ファイルを保存できませんでした。書き込みが拒否されたか、パスの変換に失敗しました。", "ApplicationConfig#save()");
      popup.pop();
    }
  }
}
