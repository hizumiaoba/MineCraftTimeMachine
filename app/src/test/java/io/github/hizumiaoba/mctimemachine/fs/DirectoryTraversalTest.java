package io.github.hizumiaoba.mctimemachine.fs;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import io.github.hizumiaoba.mctimemachine.api.BackupDirAttributes;
import io.github.hizumiaoba.mctimemachine.api.fs.DirectoryScanner;
import io.github.hizumiaoba.mctimemachine.api.fs.DirectoryTraversalProgressEvent;
import io.github.hizumiaoba.mctimemachine.api.fs.DirectoryTraversalProgressUpdateListener;
import io.github.hizumiaoba.mctimemachine.api.fs.FileCountCompleteListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

@Slf4j
public class DirectoryTraversalTest {

  @TempDir Path tempDir;
  private ExecutorService traversalTaskPool;
  private ExecutorService internalEventTaskPool;
  private DirectoryScanner directoryScanner;
  private DirectoryTraversalProgressUpdateListener progressUpdateListener;
  private FileCountCompleteListener fileCountCompleteListener;

  @BeforeEach
  void setUp() {
    traversalTaskPool = Executors.newWorkStealingPool(8);
    internalEventTaskPool = Executors.newWorkStealingPool(8);
    progressUpdateListener = mock(DirectoryTraversalProgressUpdateListener.class);
    fileCountCompleteListener = mock(FileCountCompleteListener.class);
    directoryScanner = new DirectoryScanner(traversalTaskPool, internalEventTaskPool);
    directoryScanner.addProgressUpdateListener(progressUpdateListener);
    directoryScanner.addTraversalCompleteListener(fileCountCompleteListener);
  }

  @AfterEach
  void tearDown() throws InterruptedException {
    traversalTaskPool.shutdown();
    internalEventTaskPool.shutdown();
    final boolean traversalTaskPoolTermination = traversalTaskPool.awaitTermination(1,
      TimeUnit.SECONDS);
    final boolean internalEventTaskPoolTermination = internalEventTaskPool.awaitTermination(1,
      TimeUnit.SECONDS);
    if (!traversalTaskPoolTermination) {
      log.warn("Traversal task pool did not terminate as expected.");
      log.warn("Commencing to shutdown the pool forcibly.");
      traversalTaskPool.shutdownNow();
    }
    if (!internalEventTaskPoolTermination) {
      log.warn("Internal event task pool did not terminate as expected.");
      log.warn("Commencing to shutdown the pool forcibly.");
      internalEventTaskPool.shutdownNow();
    }
  }

  @Test
  void scanDirectoryValidDirectoryShouldFireFileCountCompleteEvent() throws InterruptedException {
    String directoryPath = tempDir.toString();
    CountDownLatch latch = new CountDownLatch(1);
    doAnswer(invocation -> {
      latch.countDown();
      return null;
    }).when(fileCountCompleteListener).onFileCountComplete(anyLong());

    directoryScanner.scanDirectory(directoryPath);

    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertThat(completed).isTrue();

    verify(fileCountCompleteListener, atLeastOnce()).onFileCountComplete(0);
  }

  @Test
  void scanDirectoryEmptyPathShouldThrowIllegalArgumentException() {
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> directoryScanner.scanDirectory(""));
    assertThat(expected).hasMessageThat().contains("Path cannot be null or empty");
  }

  @Test
  void scanDirectoryNullPathShouldThrowIllegalArgumentException() {
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> directoryScanner.scanDirectory(null));
    assertThat(expected).hasMessageThat().contains("Path cannot be null or empty");
  }

  @Test
  void scanDirectoryNonExistentPathShouldThrowIllegalArgumentException() {
    Path nonExistentPath = tempDir.resolve("nonExistent");
    IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> directoryScanner.scanDirectory(nonExistentPath.toString()));
    assertThat(expected).hasMessageThat().contains("The path %s does not exist".formatted(nonExistentPath.toString()));
  }

  @Test
  void getBackupsNoTraversalCompletedShouldThrowIllegalStateException() {
    IllegalStateException expected = assertThrows(IllegalStateException.class, () -> directoryScanner.getBackups());
    assertThat(expected).hasMessageThat().contains("No traversal have been completed before retrieving backup data.");
  }

  @Test
  void scanDirectoryWithSubdirectoriesShouldAddBackupDirAttributesToList() throws IOException, InterruptedException {
    Path subDir1 = tempDir.resolve("subDir1");
    Path subDir2 = tempDir.resolve("subDir2");
    Files.createDirectory(subDir1);
    Files.createDirectory(subDir2);

    CountDownLatch latch = new CountDownLatch(1);
    doAnswer(invocation -> {
      DirectoryTraversalProgressEvent event = invocation.getArgument(0);
      if (event.getCurrent() == 2 && event.getTotal() == 2) {
        latch.countDown();
      }
      return null;
    }).when(progressUpdateListener).onProgressUpdate(any(DirectoryTraversalProgressEvent.class));

    directoryScanner.scanDirectory(tempDir.toString());

    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertThat(completed).isTrue();

    List<BackupDirAttributes> backups = directoryScanner.getBackups();
    assertThat(backups).hasSize(2);
  }

  @Test
  void scanDirectoryWithFilesAndSubdirectoriesShouldOnlyProcessDirectories() throws IOException, InterruptedException {
    Path subDir1 = tempDir.resolve("subDir1");
    Path file1 = tempDir.resolve("file1.txt");
    Files.createDirectory(subDir1);
    Files.createFile(file1);

    CountDownLatch latch = new CountDownLatch(1);
    doAnswer(invocation -> {
      DirectoryTraversalProgressEvent event = invocation.getArgument(0);
      if (event.getCurrent() == 1 && event.getTotal() == 1) {
        latch.countDown();
      }
      return null;
    }).when(progressUpdateListener).onProgressUpdate(any(DirectoryTraversalProgressEvent.class));

    directoryScanner.scanDirectory(tempDir.toString());

    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertThat(completed).isTrue();

    List<BackupDirAttributes> backups = directoryScanner.getBackups();
    assertThat(backups).hasSize(1);
  }

  @Test
  void scanDirectoryWithSpecialDirectoriesShouldSetIsSpecialCorrectly()
    throws IOException, InterruptedException {
    Path specialDir = tempDir.resolve("Sp_special");
    Path normalDir = tempDir.resolve("normal");
    Files.createDirectory(specialDir);
    Files.createDirectory(normalDir);

    CountDownLatch latch = new CountDownLatch(1);
    doAnswer(invocation -> {
      DirectoryTraversalProgressEvent event = invocation.getArgument(0);
      if (event.getCurrent() == 2 && event.getTotal() == 2) {
        latch.countDown();
      }
      return null;
    }).when(progressUpdateListener).onProgressUpdate(any(DirectoryTraversalProgressEvent.class));

    directoryScanner.scanDirectory(tempDir.toString());

    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertThat(completed).isTrue();

    List<BackupDirAttributes> backups = directoryScanner.getBackups();
    assertThat(backups).hasSize(2);

    assertThat(backups.stream().anyMatch(BackupDirAttributes::isSpecial)).isTrue();
    assertThat(backups.stream().anyMatch(backup -> !backup.isSpecial())).isTrue();
  }

@Test
void scanDirectoryProgressUpdateListenerShouldReceiveProgressUpdates()
  throws IOException, InterruptedException {
  Path subDir1 = tempDir.resolve("subDir1");
  Path subDir2 = tempDir.resolve("subDir2");
  Files.createDirectory(subDir1);
  Files.createDirectory(subDir2);

  CountDownLatch latch = new CountDownLatch(1);
  doAnswer(invocation -> {
    DirectoryTraversalProgressEvent event = invocation.getArgument(0);
    if (event.getCurrent() == 2 && event.getTotal() == 2) {
      latch.countDown();
    }
    return null;
  }).when(progressUpdateListener).onProgressUpdate(any(DirectoryTraversalProgressEvent.class));

  directoryScanner.scanDirectory(tempDir.toString());

  boolean completed = latch.await(2, TimeUnit.SECONDS);
  assertThat(completed).isTrue();

  ArgumentCaptor<DirectoryTraversalProgressEvent> eventArgumentCaptor = ArgumentCaptor.forClass(
    DirectoryTraversalProgressEvent.class);
  verify(progressUpdateListener, atLeastOnce()).onProgressUpdate(eventArgumentCaptor.capture());

  List<DirectoryTraversalProgressEvent> capturedEvents = eventArgumentCaptor.getAllValues();
  assertThat(capturedEvents).isNotEmpty();

  assertThat(capturedEvents.get(capturedEvents.size() - 1).getCurrent()).isEqualTo(2);
  assertThat(capturedEvents.get(capturedEvents.size() - 1).getTotal()).isEqualTo(2);
}

  @Test
  void scanDirectoryWithSubdirectoriesBackupDirAttributesShouldHaveCorrectValues() throws IOException, InterruptedException {
    Path subDir1 = tempDir.resolve("subDir1");
    Files.createDirectory(subDir1);
    File subDirFile = subDir1.toFile();
    long expectedSize = subDirFile.length();
    boolean expectedIsSpecial = subDirFile.getName().startsWith("Sp_");
    FileTime expectedCreatedAt = Files.readAttributes(subDir1, BasicFileAttributes.class).creationTime();
    int expectedSavedWorldCount = Objects.requireNonNullElse(subDirFile.listFiles(), new File[0]).length;

    CountDownLatch latch = new CountDownLatch(1);
    doAnswer(invocation -> {
      DirectoryTraversalProgressEvent event = invocation.getArgument(0);
      if (event.getCurrent() == 1 && event.getTotal() == 1) {
        latch.countDown();
      }
      return null;
    }).when(progressUpdateListener).onProgressUpdate(any(DirectoryTraversalProgressEvent.class));

    directoryScanner.scanDirectory(tempDir.toString());

    boolean completed = latch.await(2, TimeUnit.SECONDS);
    assertThat(completed).isTrue();

    List<BackupDirAttributes> backups = directoryScanner.getBackups();
    assertThat(backups).hasSize(1);

    BackupDirAttributes backup = backups.get(0);
    assertThat(backup.size()).isEqualTo(expectedSize);
    assertThat(backup.isSpecial()).isEqualTo(expectedIsSpecial);
    assertThat(backup.createdAt()).isEqualTo(expectedCreatedAt);
    assertThat(backup.savedWorldCount()).isEqualTo(expectedSavedWorldCount);
  }
}