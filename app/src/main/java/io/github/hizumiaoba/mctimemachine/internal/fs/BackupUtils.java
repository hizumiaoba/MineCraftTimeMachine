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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
public class BackupUtils {

  private static final SimpleDateFormat dirNameFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
  private Path backupPath;
  private Path savesDirPath;

  public BackupUtils(String backupPath) {
    this(Paths.get(backupPath));
  }

  public BackupUtils(Path backupPath) {
    this.backupPath = backupPath;
  }

  public BackupUtils(String backupPath, String savesDirPath) {
    this(Paths.get(backupPath), Paths.get(savesDirPath));
  }

  public BackupUtils(Path backupPath, Path savesDirPath) {
    this.backupPath = backupPath;
    this.savesDirPath = savesDirPath;
  }

  public void setBackupPath(String backupPath) {
    this.backupPath = Paths.get(backupPath);
  }

  public List<Path> getBackupDirPaths() throws IOException {
    try (var stream = Files.list(this.backupPath)) {
      return stream.toList();
    }
  }

  public void backup(boolean isSpecial, long maxBackupCount) throws IOException {
    AtomicBoolean abort = new AtomicBoolean(false);
    while (!isSpecial && didReachMaxBackupCount(maxBackupCount)) {
      if(abort.get()) {
        log.error("Failed to create backup due to an error. Aborting...");
        break;
      }
      log.info("Reached maximum backup count. Deleting the oldest backup...");
      getOldestBackupDir().ifPresent(p -> {
        try {
          deleteBackupRecursively(p);
          log.info("Deleted the oldest backup: {}", p);
        } catch (IOException e) {
          log.error("Failed to delete the oldest backup: {}", p, e);
          abort.set(true);
        }
      });
    }
    createBackup(isSpecial);
  }

  private void createBackup(boolean isSpecial) throws IOException {
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
        .map(Path::getFileName)
        .map(Path::toString)
        .filter(p -> !p.startsWith("Sp"))
        .count();
    }
  }

  private boolean didReachMaxBackupCount(long max) throws IOException {
    return getBackupCount() >= max;
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
        .filter(p -> !p.getFileName().toString().startsWith("Sp"))
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

  public void duplicate(Path d) throws IOException {
    log.info("Commencing duplication...");
    Path targetDir = this.backupPath.resolve(String.format("%s_copy", d.getFileName()));
    try (Stream<Path> s = Files.walk(d).parallel()) {
      s.forEach(source -> {
        Path target = targetDir.resolve(d.relativize(source));
        try {
          Files.createDirectories(target.getParent());
          Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          log.error("Failed to copy file: {}", source, e);
        }
      });
    }
  }

  public void archive(Path targetDir) throws IOException {
    // create zip archive of the directory `p` and save it to `targetDir`
    Path zip = targetDir.resolve(this.backupPath.getFileName().toString() + ".zip");
    try (var zos = new ZipOutputStream(Files.newOutputStream(zip))) {
      try (var s = Files.walk(this.backupPath)) {
        s.forEach(path -> {
          try {
            zos.putNextEntry(new ZipEntry(this.backupPath.relativize(path).toString()));
            Files.copy(path, zos);
            zos.closeEntry();
          } catch (IOException e) {
            log.error("Failed to archive file: {}", path, e);
          }
        });
      }
    }
  }

  public void restoreBackup(Path p) throws IOException {
    // restore the backup directory `p` to the saves directory
    // once delete all files in the saves directory, then copy all files in the backup directory to the saves directory
    log.info("Commencing restoration...");
    try (var stream = Files.walk(this.savesDirPath)) {
      stream.forEach(path -> {
        try {
          this.deleteBackupRecursively(path);
        } catch (IOException e) {
          log.error("Failed to delete file: {}", path, e);
        }
      });
    }
    try (var stream = Files.walk(p)) {
      stream.forEach(source -> {
        Path target = this.savesDirPath.resolve(p.relativize(source));
        try {
          Files.createDirectories(target.getParent());
          Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
          log.error("Failed to copy file: {}", source, e);
        }
      });
    }
  }
}
