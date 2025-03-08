package io.github.hizumiaoba.mctimemachine.api.fs;

import io.github.hizumiaoba.mctimemachine.api.version.MinimalRemoteVersionCrate;
import io.github.hizumiaoba.mctimemachine.api.version.MinimalRemoteVersionCrate.AssetsCrate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class ArtifactManager {

  private final ArtifactDownloader artifactDownloader;
  private final Path savePath;
  private final MinimalRemoteVersionCrate remoteVersionCache;
  private final DownloadProgressListener downloadProgressListener;

  public void startInstallerDownload() throws IOException {
    this.startDownload(".msi");
  }

  public void startZipDownload() throws IOException {
    this.startDownload(".zip");
  }

  private void startDownload(String fileExtension) throws IOException {
    try {
      Files.createDirectories(this.savePath);
    } catch (IOException e) {
      log.error("Failed to create directories for saving artifact", e);
      return;
    }

    AssetsCrate asset = remoteVersionCache.getAssets()
      .parallelStream()
      .filter(e -> e.getName().endsWith(fileExtension))
      .findFirst()
      .orElseThrow(() -> new RuntimeException("No artifact found"));

    String downloadUrl = asset.getDownloadUrl();
    String installerName = asset.getName();

    this.artifactDownloader.downloadArtifact(downloadUrl, savePath.resolve(installerName), this.downloadProgressListener);
  }
}
