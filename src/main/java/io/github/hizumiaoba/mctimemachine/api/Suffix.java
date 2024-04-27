package io.github.hizumiaoba.mctimemachine.api;

public enum Suffix {

  ALPHA("Alpha"),
  BETA("Beta"),
  NONE(""),
  EXPERIMENTAL("Experimental");

  private final String suffix;

  Suffix(String suffix) {
    this.suffix = suffix;
  }

  public String getSuffix() {
    return suffix;
  }

  public static Suffix fromString(String suffix) {
    for (Suffix s : Suffix.values()) {
      if (s.getSuffix().equals(suffix)) {
        return s;
      }
    }
    return null;
  }
}
