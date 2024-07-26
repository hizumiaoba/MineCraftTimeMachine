package io.github.hizumiaoba.mctimemachine.internal.fs;

import io.github.hizumiaoba.mctimemachine.api.BackupDirAttributes;
import io.github.hizumiaoba.mctimemachine.api.WorldAttribute;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class DirectoryAttributeUtils {

  private static final SimpleDateFormat dirDatetimeFormat = new SimpleDateFormat(
    "yyyy-MM-dd HH-mm-ss");

  public static String getCreatedAt(Path path) {
    try {
      BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
      FileTime creationTime = attr.creationTime();
      return dirDatetimeFormat.format(creationTime.toMillis());
    } catch (IOException e) {
      log.error("Failed to get the creation time of the directory: {}", path, e);
      return null;
    }
  }

  public static Map<String, BackupDirAttributes> listBackupDirs(Path backupPath) {
    Map<String, BackupDirAttributes> attributes = new HashMap<>();
    try (var stream = Files.list(backupPath)) {
      stream.parallel().forEach(p -> {
        try {
          long size = Files.size(p);
          boolean isSpecial = isSpecial(p);
          FileTime createdAt = getCreatedAtAsFileTime(p);
          int savedWorldCount;
          try (Stream<Path> innerStream = Files.list(p)) {
            savedWorldCount = (int) innerStream.count();
          }
          attributes.put(p.getFileName().toString(),
            new BackupDirAttributes(size, isSpecial, createdAt, savedWorldCount));
        } catch (IOException e) {
          log.warn("Failed to get the attributes of the directory: {}", p, e);
          attributes.put(p.getFileName().toString(), null);
        }
      });
    } catch (IOException e) {
      log.error("Failed to list the backup directories: {}", backupPath, e);
      return Collections.emptyMap();
    }
    return attributes;
  }

  public static Map<String, WorldAttribute> listWorldDirs(Path savesPath) {
    Map<String, WorldAttribute> attributes = new HashMap<>();
    try (Stream<Path> stream = Files.list(savesPath)) {
      stream.parallel().forEach(p -> {
        try {
          long size = Files.size(p);
          FileTime createdAt = getCreatedAtAsFileTime(p);
          int savedWorldCount;
          try (Stream<Path> innerStream = Files.list(p)) {
            savedWorldCount = (int) innerStream.count();
          }
          attributes.put(p.getFileName().toString(),
            new WorldAttribute(size, createdAt, savedWorldCount));
        } catch (IOException e) {
          log.warn("Failed to get the attributes of the directory: {}", p, e);
          attributes.put(p.getFileName().toString(), null);
        }
      });
    } catch (IOException e) {
      log.error("Failed to list the world directories: {}", savesPath, e);
      return Collections.emptyMap();
    }
    return attributes;
  }

  public static boolean isSpecial(Path path) {
    return path.getFileName().toString().startsWith("Sp_");
  }

  public static FileTime getCreatedAtAsFileTime(Path path) {
    try {
      BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
      return attr.creationTime();
    } catch (IOException e) {
      log.error("Failed to get the creation time of the directory: {}", path, e);
      return null;
    }
  }
}
