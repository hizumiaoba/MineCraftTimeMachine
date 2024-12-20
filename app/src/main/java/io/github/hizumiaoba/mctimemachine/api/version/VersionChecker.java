package io.github.hizumiaoba.mctimemachine.api.version;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GitHub;

@RequiredArgsConstructor
@Slf4j
public class VersionChecker {

  private final GitHub github;
  private static final String RELEASE_PATH = "/repos/hizumiaoba/MineCraftTimeMachine/releases";
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public VersionChecker() throws IOException {
    this(GitHub.connectAnonymously()); // since end-users may not have a GitHub account we need to be an anonymous user
  }

  public Optional<GHRelease> getLatestVersion(boolean preferPreRelease) {
    try {
      List<GHRelease> remoteVersionCrate = github.getRepository(RELEASE_PATH).listReleases().toList();
      return remoteVersionCrate
        .parallelStream()
        .filter(e -> preferPreRelease || !e.isPrerelease())
        .findFirst();
    } catch (IOException e) {
      log.error("Failed to fetch the latest version from GitHub.", e);
      return Optional.empty();
    }
  }
}
