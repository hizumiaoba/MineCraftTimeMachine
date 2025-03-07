package io.github.hizumiaoba.mctimemachine.api.fs;

import java.nio.file.Path;

public interface DownloadProgressListener {
  void onProgress(long bytesRead, long contentLength);

  void onComplete(String fileName, Path savePath);

  void onError(Exception e, String fileName, Path savePath);
}
