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
}
