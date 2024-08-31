package io.github.hizumiaoba.mctimemachine.internal.natives;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class NativeHandleUtil {

  private NativeHandleUtil() {}

  public static Optional<ProcessHandle> getMinecraftProcessId() {
    ProcessBuilder pwsh = new ProcessBuilder("powershell", "-Command", "\"Get-Process -Name javaw | Where-Object { $_.MainWindowTitle -like 'Minecraft*' } | Select-Object -ExpandProperty Id | Out-File -Encoding ASCII .\\.mctm-mc.id.txt \"");
    final String idFile = ".mctm-mc.id.txt";
    List<String> lines;
    try {
      Process p = pwsh.start();
      p.onExit().join();
      Path path = Paths.get(idFile);
      if (Files.notExists(path)) {
        log.warn("File {} cannot be found", idFile);
        return Optional.empty();
      }
      lines = Files.readAllLines(path, StandardCharsets.UTF_8);
      Files.deleteIfExists(path);
    } catch (IOException e) {
      log.error("Failed to start shell process to find Minecraft process id", e);
      return Optional.empty();
    }
    if(lines.size() != 1) {
      log.warn("It seems that there are multiple Minecraft processes running? Found {} processes: ", lines.size());
      lines.forEach(log::warn);
      log.warn("No process id will be returned");
      return Optional.empty();
    }
    log.trace("Found Minecraft process id: {}", lines.get(0));
    int pid = Integer.parseInt(lines.get(0).trim());
    return ProcessHandle.of(pid);
  }

  @Deprecated
  public static Optional<ProcessHandle> getMinecraftProcess() {
    Optional<ProcessHandle> handle = ProcessHandle.allProcesses().parallel().filter(h -> h.info().command().isPresent()).filter(h -> h.info().command().get().contains("javaw")).findFirst();
    return handle;
  }

}
