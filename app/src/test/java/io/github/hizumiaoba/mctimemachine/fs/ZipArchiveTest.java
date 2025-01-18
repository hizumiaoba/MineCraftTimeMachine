package io.github.hizumiaoba.mctimemachine.fs;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import io.github.hizumiaoba.mctimemachine.internal.fs.BackupUtils;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("This was gone too long in mac environment")
public class ZipArchiveTest {

  static int dummyFileCount = 10;

  @Test
  public void testZipArchive() {
    Path p = Paths.get("test");
    Path target = Paths.get("test/archive");

    BackupUtils utils = new BackupUtils(p);
    try {
      utils.archive(target);
    } catch (IOException e) {
      fail(e);
    }

    Path zip = target.resolve("test.zip");
    assertThat(Files.exists(zip)).isTrue();
  }

  @BeforeAll
  public static void setup() {
    String fileName = "test/dummy_%d.txt";
    String dummyContent = "dummy content %d";

    try {
      Files.createDirectories(Paths.get("test/archive"));
    } catch (IOException e) {
      fail(e);
    }

    for (int i = 0; i < dummyFileCount; i++) {
      String content = String.format(dummyContent, i);
      String path = String.format(fileName, i);
      try (BufferedWriter w = new BufferedWriter(new FileWriter(path))) {
        w.write(content);
      } catch (IOException e) {
        fail(e);
      }
    }
  }

  @AfterAll
  public static void cleanup() {
    String fileName = "test/dummy_%d.txt";

    for (int i = 0; i < dummyFileCount; i++) {
      String path = String.format(fileName, i);
      try {
        Files.deleteIfExists(Paths.get(path));
      } catch (IOException e) {
        fail(e);
      }
    }
    deleteBackupRecursively(Paths.get("test"));
  }

  private static void deleteBackupRecursively(Path p) {
    try (Stream<Path> s = Files.list(p)) {
      s.forEach(path -> {
        try {
          if (Files.isDirectory(path)) {
            deleteBackupRecursively(path);
          } else {
            Files.deleteIfExists(path);
          }
        } catch (IOException e) {
          fail(e);
        }
      });
      Files.deleteIfExists(p);
    } catch (IOException e) {
      fail(e);
    }
  }
}
