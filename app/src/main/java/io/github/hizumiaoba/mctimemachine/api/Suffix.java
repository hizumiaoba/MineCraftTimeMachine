package io.github.hizumiaoba.mctimemachine.api;

import lombok.Getter;

@Getter
public enum Suffix {
  NONE(""),
  ALPHA("alpha"),
  BETA("beta");

  private final String suffix;

  Suffix(String suffix) {
    this.suffix = suffix;
  }

  public static Suffix parse(String suffix) {
    for (Suffix s : values()) {
      if (s.suffix.equals(suffix)) {
        return s;
      }
    }
    return NONE;
  }
}
