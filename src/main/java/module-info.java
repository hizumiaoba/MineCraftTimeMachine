module io.github.hizumiaoba.mctimemachine {
  requires javafx.controls;
  requires javafx.fxml;
  requires kotlin.stdlib;
  requires java.desktop;
  requires org.xerial.sqlitejdbc;
  requires org.slf4j;
  requires ch.qos.logback.classic;
  requires com.google.common;
  requires lombok;
  requires annotations;
  requires jintellitype;
  requires org.kohsuke.github.api;
  requires com.fasterxml.jackson.core;
  requires com.fasterxml.jackson.databind;

  opens io.github.hizumiaoba.mctimemachine to javafx.fxml;
  exports io.github.hizumiaoba.mctimemachine;
}
