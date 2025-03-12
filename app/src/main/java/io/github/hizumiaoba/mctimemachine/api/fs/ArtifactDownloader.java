package io.github.hizumiaoba.mctimemachine.api.fs;

import java.io.IOException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import org.jetbrains.annotations.NotNull;

@Slf4j
@RequiredArgsConstructor
public class ArtifactDownloader {

  private final OkHttpClient client;

  public void downloadArtifact(String downloadUrl, Path savePath, DownloadProgressListener listener) throws IOException {
    Request request = new Request.Builder()
      .url(downloadUrl)
      .build();
    final String fileName = savePath.getFileName().toString();

    this.client.newCall(request).enqueue(new Callback() {
      @Override
      public void onFailure(@NotNull Call call, @NotNull IOException e) {
        listener.onError(e, "null", savePath);
      }

      @Override
      public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
        if (!response.isSuccessful()) {
          listener.onError(
            new IOException("Server returned unhappy response: " + response.code()),
            fileName, savePath);
          return;
        }

        ResponseBody body = response.body();
        if (body == null) {
          listener.onError(
            new IOException("Server returned empty response. Status: " + response.code())
          , fileName, savePath);
          return;
        }

        long contentLength = body.contentLength();
        BufferedSource source = body.source();
        BufferedSink sink = Okio.buffer(Okio.sink(savePath.toFile()));
        Buffer sinkBuffer = sink.getBuffer();

        long totalBytesRead = 0L;
        final int bufferSize = 8 * 1024;
        long bytesRead;
        while ((bytesRead = source.read(sinkBuffer, bufferSize)) != -1) {
          sink.emit();
          totalBytesRead += bytesRead;
          listener.onProgress(totalBytesRead, contentLength);
        }

        sink.flush();
        sink.close();
        source.close();
        listener.onComplete(fileName, savePath);
      }
    });
  }
}
