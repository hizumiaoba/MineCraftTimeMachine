package io.github.hizumiaoba.mctimemachine.internal.fs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class BackupUtils {

  private static final SimpleDateFormat dirNameFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
  private Path backupPath;

  public BackupUtils(String backupPath) {
    this(Paths.get(backupPath));
  }

  public BackupUtils(Path backupPath) {
    this.backupPath = backupPath;
  }

  public void setBackupPath(String backupPath) {
    this.backupPath = Paths.get(backupPath);
  }

  public List<Path> getBackupDirPaths() throws IOException {
    try (var stream = Files.list(this.backupPath)) {
      return stream.toList();
    }
  }

  public void backup(Path savesDirPath, boolean isSpecial, long maxBackupCount) throws IOException {
    if (didReachMaxBackupCount(maxBackupCount)) {
      log.info("Reached maximum backup count. Deleting the oldest backup...");
      getOldestBackupDir().ifPresent(p -> {
        try {
          deleteBackupRecursively(p);
          log.info("Deleted the oldest backup: {}", p);
        } catch (IOException e) {
          log.error("Failed to delete the oldest backup: {}", p, e);
        }
      });
      createBackup(savesDirPath, isSpecial);
    }
  }

  private void createBackup(Path savesDirPath, boolean isSpecial) throws IOException {
    log.info("Commencing backup...");
    Path targetDir = this.backupPath.resolve(
      String.format("%s%s", isSpecial ? "Sp_" : "", dirNameFormat.format(new Date())));
    Files.createDirectories(targetDir);
    log.info("Created backup directory: {}", targetDir);
    try (var stream = Files.walk(savesDirPath)) {
      stream.forEach(source -> {
        Path target = targetDir.resolve(savesDirPath.relativize(source));
        try {
          Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          log.error("Failed to copy file: {}", source, e);
        }
      });
    }
    log.info("Backup completed: {}", targetDir);
  }

  private long getBackupCount() throws IOException {
    try (var stream = Files.list(this.backupPath)) {
      return stream
        .filter(Files::isDirectory)
        .filter(p -> !p.getFileName().startsWith("Sp"))
        .count();
    }
  }

  private boolean didReachMaxBackupCount(long max) throws IOException {
    return getBackupCount() == max;
  }

  public void deleteBackupRecursively(Path p) throws IOException {
    try (var dirStream = Files.newDirectoryStream(p)) {
      for (var target : dirStream) {
        if (Files.isDirectory(target)) {
          deleteBackupRecursively(target);
        }
        Files.deleteIfExists(target);
      }
    }
    Files.deleteIfExists(p);
  }

  private Optional<Path> getOldestBackupDir() throws IOException {
    try (var stream = Files.list(this.backupPath)) {
      return stream
        .filter(Files::isDirectory)
        .filter(p -> !p.getFileName().startsWith("Sp"))
        .min((p1, p2) -> {
          try {
            return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
          } catch (IOException e) {
            log.error("Failed to compare last modified time of {} and {} due to I/O Error", p1, p2,
              e);
            return 0;
          }
        });
    }
  }

  public void createBackupDir() {
    log.debug("Checking backup directory...");
    try {
      Files.createDirectories(this.backupPath);
    } catch (IOException e) {
      log.error("Failed to create backup directory: {}", this.backupPath, e);
    }
    log.debug("Backup directory is ready: {}", this.backupPath);
  }
}
