package io.github.hizumiaoba.mctimemachine.api;

public @interface Version {

  int major();

  int minor();

  int patch();

  Suffix suffix() default Suffix.NONE;
}
