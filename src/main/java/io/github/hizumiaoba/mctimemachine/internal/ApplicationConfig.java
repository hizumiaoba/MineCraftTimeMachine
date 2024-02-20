package io.github.hizumiaoba.mctimemachine.internal;

import io.github.hizumiaoba.mctimemachine.api.Config;
import io.github.hizumiaoba.mctimemachine.api.ExceptionPopup;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationConfig implements Config {

  private final String configFile;
  private Properties properties;
  private static final Map<String, ApplicationConfig> instances = new ConcurrentHashMap<>();

  private ApplicationConfig(String configFile) {
    this.configFile = configFile;
  }

  public static ApplicationConfig getInstance(String configFile) {
    if(instances.containsKey(configFile)) {
      return instances.get(configFile);
    } else {
      ApplicationConfig instance = new ApplicationConfig(configFile);
      instances.put(configFile, instance);
      return instance;
    }
  }

  @Override
  public void load() {
    if(properties != null) {
      return;
    }
    properties = new Properties();
    try {
      properties.load(Paths.get(configFile).toFile().toURI().toURL().openStream());
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
      properties.store(Paths.get(configFile).toFile().toURI().toURL().openConnection()
        .getOutputStream(), "Minecraft Time Machine Config");
    } catch (IOException e) {
      ExceptionPopup popup = new ExceptionPopup(e, "設定ファイルを保存できませんでした。書き込みが拒否されたか、パスの変換に失敗しました。", "ApplicationConfig#save()");
      popup.pop();
    }
  }
}
