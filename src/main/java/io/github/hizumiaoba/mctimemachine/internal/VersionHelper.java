package io.github.hizumiaoba.mctimemachine.internal;

import io.github.hizumiaoba.mctimemachine.MineCraftTimeMachineApplication;
import io.github.hizumiaoba.mctimemachine.api.Suffix;
import io.github.hizumiaoba.mctimemachine.api.Version;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

@Slf4j
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

  private List<VersionObj> getAllRemoteVersions() {
    GitHub gh = null;
    try {
      gh = GitHub.connectAnonymously();
      GHRepository repo = gh.getRepository("hizumiaoba/MineCraftTimeMachine");
      return repo.listReleases().toList().parallelStream()
        .map(r -> VersionObj.fromString(r.getTagName())).toList();
    } catch (IOException e) {
      log.error("Failed to fetch remote versions", e);
      // fall back client version so that any update check won't be triggered
      return List.of(VersionObj.fromAnnotation(getCurrentVersion()));
    }
  }

  public boolean check() {
    List<VersionObj> remoteVersions = getAllRemoteVersions();
    final VersionObj currentVersion = VersionObj.fromAnnotation(getCurrentVersion());
    final boolean isClientUnstable = currentVersion.suffix() != Suffix.NONE;
    for (VersionObj remoteVersion : remoteVersions) {
      if (currentVersion.doesCompletelyMatch(remoteVersion, true)) {
        return false;
      }
      if (isClientUnstable && remoteVersion.suffix() == Suffix.NONE) {
        return true;
      } else if (currentVersion.suffix() != remoteVersion.suffix()) {
        continue;
      }
      Map<String, UpdateStatus> status = currentVersion.checkStatus(remoteVersion);
      return status
        .keySet()
        .parallelStream()
        .map(status::get)
        .map(UpdateStatus.UP_TO_DATE::equals)
        .reduce(false, (a, b) -> a || b);
    }
    return true;
  }

  record VersionObj(int major, int minor, int patch, Suffix suffix) {

    public static VersionObj fromString(String version) {
      if (version.startsWith("v")) {
        version = version.substring(1);
      }
      String[] parts = version.split("-");
      String[] versionParts = parts[0].split("\\.");
      int major = Integer.parseInt(versionParts[0]);
      int minor = Integer.parseInt(versionParts[1]);
      int patch = Integer.parseInt(versionParts[2]);
      Suffix suffix = parts.length != 1 ? Suffix.fromString(parts[1]) : Suffix.NONE;
      return new VersionObj(major, minor, patch, suffix);
    }

    static VersionObj fromAnnotation(Version version) {
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

    public Map<String, UpdateStatus> checkStatus(VersionObj remote) {
      UpdateStatus majorStatus = major < remote.major() ? UpdateStatus.OUTDATED
        : major == remote.major() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNKNOWN;
      UpdateStatus minorStatus = minor < remote.minor() ? UpdateStatus.OUTDATED
        : minor == remote.minor() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNKNOWN;
      UpdateStatus patchStatus = patch < remote.patch() ? UpdateStatus.OUTDATED
        : patch == remote.patch() ? UpdateStatus.UP_TO_DATE : UpdateStatus.UNKNOWN;
      return Map.of("major", majorStatus, "minor", minorStatus, "patch", patchStatus);
    }

    public boolean doesCompletelyMatch(VersionObj remote, boolean ignoreSuffix) {
      return major == remote.major() && minor == remote.minor() && patch == remote.patch()
        && (ignoreSuffix || suffix == remote.suffix());
    }
  }
}
