package io.github.hizumiaoba.mctimemachine.internal;

import io.github.hizumiaoba.mctimemachine.MineCraftTimeMachineApplication;
import io.github.hizumiaoba.mctimemachine.api.Suffix;
import io.github.hizumiaoba.mctimemachine.api.Version;
import java.util.Map;

public class VersionHelper {

  private static final String RELEASE_API_URI = "https://api.github.com/repos/hizumiaoba/MineCraftTimeMachine/releases";
  private static final String Accepting_Header = "application/vnd.github+json";
  private Version currentVersion;

  private static String constructVersionString(int major, int minor, int patch, Suffix suffix) {
    return String.format("%d.%d.%d-%s", major, minor, patch, suffix.toString());
  }

  private Version getCurrentVersion() {
    if (currentVersion == null) {
      currentVersion = MineCraftTimeMachineApplication.class.getAnnotation(Version.class);
    }
    return currentVersion;
  }

  enum UpdateStatus {
    UP_TO_DATE,
    OUTDATED,
    UNSTABLE,
    UNKNOWN;

    public static UpdateStatus fromString(String status) {
      for (UpdateStatus s : UpdateStatus.values()) {
        if (s.toString().equals(status)) {
          return s;
        }
      }
      return UNKNOWN;
    }
  }

  record VersionObj(int major, int minor, int patch, Suffix suffix) {

    public static VersionObj fromString(String version) {
      String[] parts = version.split("-");
      String[] versionParts = parts[0].split("\\.");
      int major = Integer.parseInt(versionParts[0]);
      int minor = Integer.parseInt(versionParts[1]);
      int patch = Integer.parseInt(versionParts[2]);
      Suffix suffix = parts.length != 1 ? Suffix.fromString(parts[1]) : Suffix.NONE;
      return new VersionObj(major, minor, patch, suffix);
    }

    public static VersionObj fromAnnotation(Version version) {
      return new VersionObj(version.major(), version.minor(), version.patch(), version.suffix());
    }

    public String asStringNotation() {
      return constructVersionString(major, minor, patch, suffix);
    }

    public boolean checkSuffixStatus(VersionObj remote) {
      return switch (this.suffix()) {
        case ALPHA -> true;
        case BETA -> remote.suffix() != Suffix.ALPHA;
        case NONE -> remote.suffix() == Suffix.NONE;
      };
    }

    public Map<String, UpdateStatus> checkStatus(VersionObj remote, boolean includeSuffix) {
      UpdateStatus minorStatus = minor < remote.minor() ? UpdateStatus.OUTDATED
        : minor == remote.minor() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNSTABLE;
      UpdateStatus majorStatus = major < remote.major() ? UpdateStatus.OUTDATED
        : major == remote.major() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNSTABLE;
      UpdateStatus patchStatus = patch < remote.patch() ? UpdateStatus.OUTDATED
        : patch == remote.patch() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNSTABLE;
      UpdateStatus suffixStatus =
        includeSuffix ? suffix.compareTo(remote.suffix()) > 0 ? UpdateStatus.OUTDATED
          : suffix.compareTo(remote.suffix()) == 0 ? UpdateStatus.UP_TO_DATE
            : UpdateStatus.UNSTABLE : UpdateStatus.UP_TO_DATE;
      return Map.of("major", majorStatus, "minor", minorStatus, "patch", patchStatus, "suffix",
        suffixStatus);
    }
  }
}
