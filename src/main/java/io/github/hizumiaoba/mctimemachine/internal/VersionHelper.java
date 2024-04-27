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

  static class VersionObj {

    private final int major;
    private final int minor;
    private final int patch;
    private final Suffix suffix;

    public VersionObj(int major, int minor, int patch, Suffix suffix) {
      this.major = major;
      this.minor = minor;
      this.patch = patch;
      this.suffix = suffix;
    }

    public static VersionObj fromString(String version) {
      String[] parts = version.split("-");
      String[] versionParts = parts[0].split("\\.");
      int major = Integer.parseInt(versionParts[0]);
      int minor = Integer.parseInt(versionParts[1]);
      int patch = Integer.parseInt(versionParts[2]);
      Suffix suffix = Suffix.fromString(parts[1]);
      return new VersionObj(major, minor, patch, suffix);
    }

    public static VersionObj fromAnnotation(Version version) {
      return new VersionObj(version.major(), version.minor(), version.patch(), version.suffix());
    }

    public int getMajor() {
      return major;
    }

    public int getMinor() {
      return minor;
    }

    public int getPatch() {
      return patch;
    }

    public Suffix getSuffix() {
      return suffix;
    }

    public String asStringNotation() {
      return constructVersionString(major, minor, patch, suffix);
    }

    public Map<String, UpdateStatus> checkStatus(VersionObj expectGreater, boolean includeSuffix) {
      UpdateStatus minorStatus = minor < expectGreater.getMinor() ? UpdateStatus.OUTDATED
        : minor == expectGreater.getMinor() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNSTABLE;
      UpdateStatus majorStatus = major < expectGreater.getMajor() ? UpdateStatus.OUTDATED
        : major == expectGreater.getMajor() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNSTABLE;
      UpdateStatus patchStatus = patch < expectGreater.getPatch() ? UpdateStatus.OUTDATED
        : patch == expectGreater.getPatch() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNSTABLE;
      UpdateStatus suffixStatus =
        includeSuffix ? suffix.compareTo(expectGreater.getSuffix()) > 0 ? UpdateStatus.OUTDATED
          : suffix.compareTo(expectGreater.getSuffix()) == 0 ? UpdateStatus.UP_TO_DATE
            : UpdateStatus.UNSTABLE : UpdateStatus.UP_TO_DATE;
      return Map.of("major", majorStatus, "minor", minorStatus, "patch", patchStatus, "suffix",
        suffixStatus);
    }
  }
}
