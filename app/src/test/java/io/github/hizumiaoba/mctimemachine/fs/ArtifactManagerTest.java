package io.github.hizumiaoba.mctimemachine.fs;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.hizumiaoba.mctimemachine.api.fs.ArtifactDownloader;
import io.github.hizumiaoba.mctimemachine.api.fs.ArtifactManager;
import io.github.hizumiaoba.mctimemachine.api.fs.DownloadProgressListener;
import io.github.hizumiaoba.mctimemachine.api.version.MinimalRemoteVersionCrate;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import javafx.scene.control.ProgressBar;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ArtifactManagerTest {

  @Mock
  private ArtifactDownloader fileDownloader;

  @Mock
  private ProgressBar downloadProgressBar;

  @Mock
  private MinimalRemoteVersionCrate remoteVersionCache;

  private ArgumentCaptor<DownloadProgressListener> listenerCaptor;
  private ArtifactManager downloadManager;
  private Path savePath;

  @BeforeEach
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    savePath = Path.of("test-download");
    this.listenerCaptor = ArgumentCaptor.forClass(DownloadProgressListener.class);
    downloadManager = new ArtifactManager(fileDownloader, downloadProgressBar, savePath, remoteVersionCache, listenerCaptor.getValue());
  }

  @Test
  public void testStartInstallerDownload() throws IOException {
    MinimalRemoteVersionCrate.AssetsCrate asset = mock(MinimalRemoteVersionCrate.AssetsCrate.class);
    when(asset.getName()).thenReturn("test-installer.msi");
    when(asset.getDownloadUrl()).thenReturn("http://example.com/test-installer.msi");
    when(remoteVersionCache.getAssets()).thenReturn(List.of(asset));

    downloadManager.startInstallerDownload();

    verify(fileDownloader).downloadArtifact(eq("http://example.com/test-installer.msi"), eq(savePath.resolve("test-installer.msi")), listenerCaptor.capture());

    DownloadProgressListener listener = listenerCaptor.getValue();
    listener.onComplete("test-installer.msi", savePath.resolve("test-installer.msi"));
  }

  @Test
  public void testStartZipDownload() throws IOException {
    MinimalRemoteVersionCrate.AssetsCrate asset = mock(MinimalRemoteVersionCrate.AssetsCrate.class);
    when(asset.getName()).thenReturn("test-file.zip");
    when(asset.getDownloadUrl()).thenReturn("http://example.com/test-file.zip");
    when(remoteVersionCache.getAssets()).thenReturn(List.of(asset));

    downloadManager.startZipDownload();

    ArgumentCaptor<DownloadProgressListener> listenerCaptor = ArgumentCaptor.forClass(DownloadProgressListener.class);
    verify(fileDownloader).downloadArtifact(eq("http://example.com/test-file.zip"), eq(savePath.resolve("test-file.zip")), listenerCaptor.capture());

    DownloadProgressListener listener = listenerCaptor.getValue();
    listener.onComplete("test-file.zip", savePath.resolve("test-file.zip"));
  }
}