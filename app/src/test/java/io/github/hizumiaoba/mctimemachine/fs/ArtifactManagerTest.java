package io.github.hizumiaoba.mctimemachine.fs;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.hizumiaoba.mctimemachine.api.fs.ArtifactDownloader;
import io.github.hizumiaoba.mctimemachine.api.fs.ArtifactManager;
import io.github.hizumiaoba.mctimemachine.api.fs.DownloadProgressListener;
import io.github.hizumiaoba.mctimemachine.api.version.MinimalRemoteVersionCrate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ArtifactManagerTest {

  @Mock
  private ArtifactDownloader fileDownloader;

  @Mock
  private MinimalRemoteVersionCrate remoteVersionCache;

  private ArtifactManager downloadManager;
  private Path savePath;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    savePath = Path.of("test-download");
    downloadManager = new ArtifactManager(fileDownloader, savePath, remoteVersionCache, new TestDownloadProgressListener());
  }

  @AfterAll
  public static void tearDown() {
    try {
      Files.deleteIfExists(Path.of("test-download"));
    } catch (IOException e) {
      fail(e);
    }
  }

  private String getInstallerExtension() {
    String osName = System.getProperty("os.name", "unknown").toLowerCase();
    if (osName.contains("windows")) return ".msi";
    else if (osName.contains("mac")) return ".dmg";
    else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) return ".deb";
    else throw new IllegalStateException("Unsupported OS: " + osName);
  }

  @Test
  public void testStartInstallerDownload() throws IOException {
    String ext = getInstallerExtension();
    MinimalRemoteVersionCrate.AssetsCrate asset = mock(MinimalRemoteVersionCrate.AssetsCrate.class);
    when(asset.getName()).thenReturn("test-installer" + ext);
    when(asset.getDownloadUrl()).thenReturn("http://example.com/test-installer" + ext);
    when(remoteVersionCache.getAssets()).thenReturn(List.of(asset));

    downloadManager.startInstallerDownload();

    ArgumentCaptor<DownloadProgressListener> listenerCaptor = ArgumentCaptor.forClass(DownloadProgressListener.class);
    verify(fileDownloader).downloadArtifact(eq("http://example.com/test-installer" + ext),
            eq(savePath.resolve("test-installer" + ext)), listenerCaptor.capture());

    DownloadProgressListener listener = listenerCaptor.getValue();
    listener.onComplete("test-installer" + ext, savePath.resolve("test-installer" + ext));
  }

  @Test
  public void testStartZipDownload() throws IOException {
    MinimalRemoteVersionCrate.AssetsCrate asset = mock(MinimalRemoteVersionCrate.AssetsCrate.class);
    when(asset.getName()).thenReturn("test-file.zip");
    when(asset.getDownloadUrl()).thenReturn("http://example.com/test-file.zip");
    when(remoteVersionCache.getAssets()).thenReturn(List.of(asset));

    downloadManager.startZipDownload();

    ArgumentCaptor<DownloadProgressListener> listenerCaptor = ArgumentCaptor.forClass(DownloadProgressListener.class);
    verify(fileDownloader).downloadArtifact(eq("http://example.com/test-file.zip"),
            eq(savePath.resolve("test-file.zip")), listenerCaptor.capture());

    DownloadProgressListener listener = listenerCaptor.getValue();
    listener.onComplete("test-file.zip", savePath.resolve("test-file.zip"));
  }

  private static class TestDownloadProgressListener implements DownloadProgressListener {
    @Override
    public void onProgress(long bytesRead, long contentLength) {
      System.out.println("Progress: " + bytesRead + "/" + contentLength);
    }

    @Override
    public void onComplete(String fileName, Path savePath) {
      System.out.println("Download complete: " + fileName + " saved to " + savePath);
    }

    @Override
    public void onError(Exception e, String fileName, Path savePath) {
      System.err.println("Error downloading " + fileName + " to " + savePath + ": " + e.getMessage());
    }
  }
}