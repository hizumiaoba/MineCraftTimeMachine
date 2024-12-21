package io.github.hizumiaoba.mctimemachine;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

@Slf4j
@Disabled("This test requires a GitHub API token.")
public class GitHubApiTest {

  public static final String REPO_FULL_NAME = "hizumiaoba/MineCraftTimeMachine";

  @Test
  void repos() {
    GitHub github;
    try {
      github = GitHub.connectAnonymously();
    } catch (IOException e) {
      fail(e);
      return;
    }
    GHRepository repo;
    List<GHRelease> releases;
    try {
      repo = github.getRepository(REPO_FULL_NAME);
      releases = repo.listReleases().toList();
    } catch (IOException e) {
      fail(e);
      return;
    }

    assertThat(repo).isNotNull();
    assertThat(repo.getFullName()).isEqualTo(REPO_FULL_NAME);
    assertThat(releases).isNotEmpty();
  }
}
