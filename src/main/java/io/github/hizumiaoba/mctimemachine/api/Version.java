package io.github.hizumiaoba.mctimemachine.api;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Version {

  int major();

  int minor();

  int patch();

  Suffix suffix() default Suffix.NONE;
}
