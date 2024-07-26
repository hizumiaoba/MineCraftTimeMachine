package io.github.hizumiaoba.mctimemachine.api;

public interface Config {

  void load();

  String load(String key);

  void set(String key, String value);

  void save();
}
