module io.github.hizumiaoba.mctimemachine {
  requires javafx.controls;
  requires javafx.fxml;
  requires kotlin.stdlib;
  requires java.desktop;
  requires java.security.sasl;
  requires org.xerial.sqlitejdbc;
  requires org.slf4j;
  requires ch.qos.logback.classic;
  requires com.google.common;
  requires lombok;
  requires annotations;
  requires jintellitype;
  requires org.apache.commons.lang3;
  requires okhttp3;
  requires jcabi.github;
  requires jcabi.http;
  requires okio;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;
  requires com.fasterxml.jackson.annotation;

  opens io.github.hizumiaoba.mctimemachine to javafx.fxml;
  opens io.github.hizumiaoba.mctimemachine.api.version to com.fasterxml.jackson.databind;
  exports io.github.hizumiaoba.mctimemachine;
}
