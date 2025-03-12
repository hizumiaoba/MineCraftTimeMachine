package io.github.hizumiaoba.mctimemachine.api.fs;

import io.github.hizumiaoba.mctimemachine.api.version.MinimalRemoteVersionCrate;
import io.github.hizumiaoba.mctimemachine.api.version.MinimalRemoteVersionCrate.AssetsCrate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.Getter;
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
    OS os = OS.getInstance();
    this.startDownload(os.getFileExtension());
  }

  @Getter
  private enum OS {
    WINDOWS("windows", ".msi"),
    LINUX("linux", ".deb"),
    MAC("mac", ".dmg");
    private final String envIdentifier;
    private final String fileExtension;
    OS(String envIdentifier, String fileExtension) {
      this.envIdentifier = envIdentifier;
      this.fileExtension = fileExtension;
    }
    static OS getInstance() {
      String osName = System.getProperty("os.name", "unknown").toLowerCase();
      if (osName.contains("windows")) {
        return WINDOWS;
      } else if (osName.contains("mac")) {
        return MAC;
      } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
        return LINUX;
      } else {
        throw new IllegalStateException("Unsupported OS: " + osName);
      }
    }
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
    
    Optional<AssetsCrate> optAsset = remoteVersionCache.getAssets()
      .parallelStream()
      .filter(e -> e.getName().endsWith(fileExtension))
      .findFirst();
    if (!optAsset.isPresent()) {
      this.downloadProgressListener.onError(new Exception("No Artifact found for: " + fileExtension), "NoArtifactFound", savePath);
      return;
    }
    AssetsCrate asset = optAsset.get();

    String downloadUrl = asset.getDownloadUrl();
    String installerName = asset.getName();

    this.artifactDownloader.downloadArtifact(downloadUrl, savePath.resolve(installerName), this.downloadProgressListener);
  }
}
