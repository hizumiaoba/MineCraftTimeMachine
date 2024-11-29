package io.github.hizumiaoba.mctimemachine.api.version;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
  }
}
