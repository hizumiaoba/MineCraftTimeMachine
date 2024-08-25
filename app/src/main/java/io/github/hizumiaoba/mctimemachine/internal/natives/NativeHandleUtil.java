package io.github.hizumiaoba.mctimemachine.internal.natives;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeHandleUtil {

  private NativeHandleUtil() {}

  public static Optional<ProcessHandle> getMinecraftProcessId() {
    ProcessBuilder pwsh = new ProcessBuilder("powershell", "-Command", "Get-Process -Name MinecraftLauncher | Where-Object { $_.MainWindowTitle -like \"Minecraft*\" } | Select-Object -ExpandProperty Id");
    BufferedReader reader = null;
    try {
      reader = new BufferedReader(new InputStreamReader(pwsh.start().getInputStream()));
    } catch (IOException e) {
      log.error("Failed to start shell process to find Minecraft process id", e);
      return Optional.empty();
    }
    List<String> lines = reader.lines().toList();
    if(lines.size() != 1) {
      log.warn("It seems that there are multiple Minecraft processes running? Found {} processes: ", lines.size());
      lines.forEach(log::warn);
      log.warn("No process id will be returned");
      return Optional.empty();
    }
    log.trace("Found Minecraft process id: {}", lines.get(0));
    return ProcessHandle.of(Long.parseLong(lines.get(0)));
  }
}
