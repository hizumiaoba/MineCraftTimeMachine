package io.github.hizumiaoba.mctimemachine.api.fs;

import com.google.common.base.Strings;
import io.github.hizumiaoba.mctimemachine.api.BackupDirAttributes;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DirectoryScanner {
  private final List<DirectoryTraversalProgressUpdateListener> progressUpdateListeners;
  private final List<FileCountCompleteListener> FileCountCompleteListeners;
  private final ExecutorService traversalTaskPool;
  private final ExecutorService internalEventTaskPool;
  private long totalFiles;
  private long processedFiles;
  private File targetDirectory;
  private final List<BackupDirAttributes> backups;

  public DirectoryScanner() {
    this(
      Executors.newWorkStealingPool(),
      Executors.newWorkStealingPool()
    );
  }

  public DirectoryScanner(ExecutorService traversalTaskPool, ExecutorService internalEventTaskPool) {
    this(traversalTaskPool, internalEventTaskPool, new ArrayList<>(), new ArrayList<>());
  }

  private DirectoryScanner(
    ExecutorService traversalTaskPool,
    ExecutorService internalEventTaskPool,
    List<DirectoryTraversalProgressUpdateListener> progressUpdateListeners,
    List<FileCountCompleteListener> FileCountCompleteListeners
  ) {
    this.traversalTaskPool = traversalTaskPool;
    this.internalEventTaskPool = internalEventTaskPool;
    this.progressUpdateListeners = progressUpdateListeners;
    this.FileCountCompleteListeners = FileCountCompleteListeners;
    this.backups = Collections.synchronizedList(new ArrayList<>());
  }

  public void addProgressUpdateListener(DirectoryTraversalProgressUpdateListener listener) {
    this.progressUpdateListeners.add(listener);
  }

  public void addTraversalCompleteListener(FileCountCompleteListener listener) {
    this.FileCountCompleteListeners.add(listener);
  }

  public List<BackupDirAttributes> getBackups() {
    if(backups.isEmpty()) {
      throw new IllegalStateException("No traversal have been completed before retrieving backup data.");
    }
    return backups;
  }

  private void fireProgressUpdate(long current, long total) {
    internalEventTaskPool.submit(() -> {
      log.info("firing progress update event: {} / {}", current, total);
      final long startTime = System.currentTimeMillis();
      CompletableFuture<DirectoryTraversalProgressEvent> eventCompletableFuture = CompletableFuture.supplyAsync(() -> new DirectoryTraversalProgressEvent(total, current), traversalTaskPool);
      final List<CompletableFuture<Void>> futures = Collections.synchronizedList(new ArrayList<>());
      for (DirectoryTraversalProgressUpdateListener listener : progressUpdateListeners) {
        CompletableFuture<Void> future = eventCompletableFuture.thenAcceptAsync(
          listener::onProgressUpdate, internalEventTaskPool);
        futures.add(future);
      }
      joinTasks(futures, startTime);
    });
  }

  private void fireFileCountComplete(final long totalFiles) {
    internalEventTaskPool.submit(() -> {
      log.info("firing counting complete event");
      final long startTime = System.currentTimeMillis();
      final List<CompletableFuture<Void>> futures = Collections.synchronizedList(new ArrayList<>());
      for (FileCountCompleteListener listener : FileCountCompleteListeners) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> listener.onFileCountComplete(totalFiles), internalEventTaskPool);
        futures.add(future);
      }
      joinTasks(futures, startTime);
    });
  }

  private <T extends CompletableFuture<?>> void joinTasks(List<T> tasks, final long startTime) {
    CompletableFuture.allOf(tasks.toArray(new CompletableFuture<?>[0])).whenCompleteAsync(
      (ignore, throwable) -> {
        log.info("{} tasks registered.", tasks.size());
        if(throwable != null) {
          log.error("Uncaught exception while waiting for tasks to complete", throwable);
          log.error("{} tasks have reported failure.", tasks.parallelStream().filter(CompletableFuture::isCompletedExceptionally).count());
          log.error("The result may be inconsistent!");
          return;
        }
        log.info("all tasks complete. took {} ms", System.currentTimeMillis() - startTime);
      }
    , internalEventTaskPool);
  }

  public void scanDirectory(String directoryPath) {
    validatePath(directoryPath);
    this.targetDirectory = Paths.get(directoryPath).toFile();
    traversalTaskPool.submit(this::startTraversal);
  }

  private void validatePath(String path) {
    if (Strings.isNullOrEmpty(path)) {
      throw new IllegalArgumentException("Path cannot be null or empty");
    }
    if(Files.notExists(Path.of(path))) {
      throw new IllegalArgumentException("The path %s does not exist".formatted(path));
    }
  }

  private void startTraversal() {
    this.totalFiles = countFiles(targetDirectory);
    fireFileCountComplete(this.totalFiles);
    traversalTaskPool.submit(this::processDirectory);
  }

  private long countFiles(File directory) {
    long count = 0;
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          count++;
        }
      }
    }
    return count;
  }

  private void processDirectory() {
    this.processedFiles = 0;
    try {
      processDirectoryRecursive(targetDirectory);
    } catch (IOException e) {
      log.error("Failed to process directory", e);
    }
    log.info("Scanning completed. Processed {} files.", processedFiles);
  }

  private void processDirectoryRecursive(File directory) throws IOException {
    File[] files = directory.listFiles();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          final String dirName = file.getName();
          final long size = file.length();
          final boolean isSpecial = file.getName().startsWith("Sp_");
          final int savedWorldCount = Objects.requireNonNullElse(file.listFiles(), new File[0]).length;
          final FileTime createdAt = Files.readAttributes(file.toPath(), BasicFileAttributes.class).creationTime();
          BackupDirAttributes attributes = new BackupDirAttributes(
            dirName,
            size,
            isSpecial,
            createdAt,
            savedWorldCount
          );
          log.trace("Found directory: {}", attributes);
          backups.add(attributes);
          processedFiles++;
          fireProgressUpdate(processedFiles, totalFiles);
        }
      }
    }
  }
}
