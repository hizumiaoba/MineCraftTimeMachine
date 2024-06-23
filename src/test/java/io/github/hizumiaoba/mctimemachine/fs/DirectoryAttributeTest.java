package io.github.hizumiaoba.mctimemachine.fs;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.hizumiaoba.mctimemachine.api.BackupDirAttributes;
import io.github.hizumiaoba.mctimemachine.internal.fs.DirectoryAttributeUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DirectoryAttributeTest {

  private static final String DATETIME_REGEX = "\\d{4}-\\d{2}-\\d{2} \\d{2}-\\d{2}-\\d{2}";
  private static Path testDir;
  private static Path testBackupDir;

  @BeforeAll
  public static void setup() {
    testDir = Path.of("test");
    try {
      Files.createDirectory(testDir);
      Files.setAttribute(testDir, "creationTime", FileTime.fromMillis(1609426800000L));
    } catch (IOException e) {
      fail(e);
    }

    testBackupDir = Path.of("testBackup");
  }

  @AfterAll
  public static void cleanup() {
    try {
      Files.deleteIfExists(testDir);
      Files.deleteIfExists(testBackupDir);
    } catch (IOException e) {
      fail(e);
    }
  }

  @Test
  public void testDateCreatedAt() {
    String createdAt = DirectoryAttributeUtils.getCreatedAt(testDir);

    assertThat(createdAt).matches(DATETIME_REGEX);
    assertThat(createdAt).isEqualTo("2021-01-01 00-00-00");
  }

  @Test
  public void testListBackupDirs() {
    SecureRandom sr = new SecureRandom();
    int dirCount = sr.nextInt(5) + 1;
    try {
      for (int i = 0; i < dirCount; i++) {
        Path dir = testBackupDir.resolve("test_backup_" + i);
        Files.createDirectories(dir);
      }
    } catch (IOException e) {
      fail(e);
    }

    Map<String, BackupDirAttributes> attributes = DirectoryAttributeUtils.listBackupDirs(
      testBackupDir);
    assertThat(attributes).hasSize(dirCount);
    attributes
      .entrySet()
      .parallelStream()
      .map(Entry::getValue)
      .forEach(
        attr -> {
          assertThat(attr.size()).isAtLeast(0);
          assertThat(attr.createdAt()).isNotNull();
          assertThat(attr.savedWorldCount()).isAtLeast(0);
        }
      );

    try (var stream = Files.list(testBackupDir)) {
      stream.forEach(p -> {
        try {
          Files.deleteIfExists(p);
        } catch (IOException e) {
          fail(e);
        }
      });
    } catch (IOException e) {
      fail(e);
    }
  }
}
