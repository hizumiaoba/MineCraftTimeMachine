package io.github.hizumiaoba.mctimemachine.api.version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.IOException;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.kohsuke.github.GHAsset;
import org.kohsuke.github.GHRelease;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MinimalRemoteVersionCrate {

  @JsonProperty("tag_name")
  private String tagName;
  @JsonProperty("html_url")
  private String htmlUrl;
  private boolean prerelease;
  private List<AssetsCrate> assets;

  @NoArgsConstructor
  @AllArgsConstructor
  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class AssetsCrate {
    @JsonProperty("browser_download_url")
    private String downloadUrl;
    private long size;
    private String contentType;
    private String name;

    static AssetsCrate of(GHAsset raw) {
      return new AssetsCrate(
        raw.getBrowserDownloadUrl(),
        raw.getSize(),
        raw.getContentType(),
        raw.getName()
      );
    }
  }

  public static MinimalRemoteVersionCrate of(GHRelease raw) {
    try {
      return new MinimalRemoteVersionCrate(
        raw.getTagName(),
        raw.getHtmlUrl().toExternalForm(),
        raw.isPrerelease(),
        List.of(raw.listAssets().toList().parallelStream().map(AssetsCrate::of).toArray(AssetsCrate[]::new))
      );
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
