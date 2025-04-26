package io.github.hizumiaoba.mctimemachine.fs;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

import io.github.hizumiaoba.mctimemachine.api.BackupDirAttributes;
import io.github.hizumiaoba.mctimemachine.api.fs.DirectoryScanner;
import io.github.hizumiaoba.mctimemachine.api.fs.DirectoryTraversalProgressEvent;
import io.github.hizumiaoba.mctimemachine.api.fs.DirectoryTraversalProgressUpdateListener;
import io.github.hizumiaoba.mctimemachine.api.fs.FileCountCompleteListener;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

@Slf4j
public class DirectoryScanningTest {

  @TempDir
  Path tempDir;
  DirectoryTraversalProgressUpdateListener progressUpdateListener;
  FileCountCompleteListener fileCountCompleteListener;
  DirectoryScanner scanner;

  @BeforeEach
  void setUp() {
    progressUpdateListener = mock(DirectoryTraversalProgressUpdateListener.class);
    fileCountCompleteListener = mock(FileCountCompleteListener.class);
    scanner = new DirectoryScanner();
    scanner.addProgressUpdateListener(progressUpdateListener);
    scanner.addTraversalCompleteListener(fileCountCompleteListener);
  }

  @Test
  void scanningTest() throws InterruptedException {
    // arrange: create test world directories
    List<Path> testWorlds = IntStream.rangeClosed(1,5)
      .mapToObj("New_World%d"::formatted)
      .map(tempDir::resolve)
      .toList();
    testWorlds.parallelStream().forEach(path -> {
      try {
        Files.createDirectories(path);
      } catch (IOException e) {
        fail(e);
      }
    });

    CountDownLatch latch = new CountDownLatch(1);
    doAnswer(invocation -> {
      DirectoryTraversalProgressEvent event = invocation.getArgument(0);
      if(event.current() == testWorlds.size() && event.total() == testWorlds.size()) {
        latch.countDown();
      }
      return null;
    }).when(progressUpdateListener).onProgressUpdate(any(DirectoryTraversalProgressEvent.class));

    // act: scan the directories
    scanner.scanDirectory(tempDir.toString());

    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertThat(completed).isTrue();

    List<BackupDirAttributes> backups = scanner.getBackups();
    assertThat(backups).hasSize(testWorlds.size());
    assertThat(backups.parallelStream().map(BackupDirAttributes::isSpecial).toList()).containsExactly(false, false, false, false, false);
  }
}
