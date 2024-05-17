package io.github.hizumiaoba.mctimemachine.internal.version;

import io.github.hizumiaoba.mctimemachine.api.Suffix;
import io.github.hizumiaoba.mctimemachine.api.Version;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class VersionObj {

  private final int major;
  private final int minor;
  private final int patch;
  private Suffix suffix;

  public static VersionObj parse(String versionNotation) {
    // versionNotation should be "major.minor.patch" or "major.minor.patch-suffix"
    // and may contain "v" prefix
    String[] versionParts = versionNotation.replace("v", "").split("-");
    String[] versionNumbers = versionParts[0].split("\\.");
    int major = Integer.parseInt(versionNumbers[0]);
    int minor = Integer.parseInt(versionNumbers[1]);
    int patch = Integer.parseInt(versionNumbers[2]);
    Suffix suffix = versionParts.length > 1 ? Suffix.parse(versionParts[1]) : Suffix.NONE;
    return new VersionObj(major, minor, patch, suffix);
  }

  public static VersionObj parse(Version versionAnnotation) {
    int major = versionAnnotation.major();
    int minor = versionAnnotation.minor();
    int patch = versionAnnotation.patch();
    Suffix suffix = versionAnnotation.suffix();
    return new VersionObj(major, minor, patch, suffix);
  }

  public String asStringNotation() {
    return String.format("%d.%d.%d%s", major, minor, patch,
      Suffix.NONE.equals(suffix) ? "" : "-" + suffix.getSuffix());
  }
}
