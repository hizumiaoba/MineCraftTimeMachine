package io.github.hizumiaoba.mctimemachine.internal.version;

import io.github.hizumiaoba.mctimemachine.MineCraftTimeMachineApplication;
import io.github.hizumiaoba.mctimemachine.api.Version;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.List;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import org.kohsuke.github.GHRelease;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.kohsuke.github.extras.okhttp3.OkHttpGitHubConnector;

@RequiredArgsConstructor
@Slf4j
public class VersionHelper {

  private final VersionObj clientVersion;

  public boolean checkLatest(VersionObj remoteVersion, boolean ignoreSuffix) {
    return getUpdateType(remoteVersion, ignoreSuffix).equals("latest");
  }

  private String getUpdateType(VersionObj remoteVersion, boolean ignoreSuffix) {
    final boolean isMajorSame = clientVersion.getMajor() == remoteVersion.getMajor();
    final boolean isMinorSame = clientVersion.getMinor() == remoteVersion.getMinor();
    final boolean isPatchSame = clientVersion.getPatch() == remoteVersion.getPatch();
    final boolean isSuffixSame =
      ignoreSuffix || clientVersion.getSuffix().equals(remoteVersion.getSuffix());
    if (isMajorSame && isMinorSame && isPatchSame && isSuffixSame) {
      return "latest";
    } else if (isMajorSame && isMinorSame && isPatchSame) {
      return "suffix";
    } else if (isMajorSame && isMinorSame) {
      return "patch";
    } else if (isMajorSame) {
      return "minor";
    } else {
      return "major";
    }
  }

  public static VersionObj getLatestRemoteVersion(boolean includePrerelease) throws IOException {
    final OkHttpClient client = createUnsafeOkHttpClient();
    GitHub gh = GitHubBuilder.fromEnvironment()
      .withConnector(new OkHttpGitHubConnector(client))
      .build();
    List<GHRelease> releases = gh.getRepository("hizumiaoba/MineCraftTimeMachine")
      .listReleases()
      .toList()
      .parallelStream().filter(r -> includePrerelease || !r.isPrerelease())
      .toList();
    if (releases.isEmpty()) {
      // fall back client version so that no update prompt will be shown
      log.warn(
        "No available releases found with required precondition. Fallback to client version.");
      return VersionObj.parse(MineCraftTimeMachineApplication.class.getAnnotation(Version.class));
    }
    log.debug("Found {} releases", releases.size());
    log.trace("Latest release: {}", releases.get(0).getTagName());
    return VersionObj.parse(releases.get(0).getTagName());
  }

  public String constructUpdateMessage(VersionObj remoteVersion, boolean ignoreSuffix) {
    final String updateType = getUpdateType(remoteVersion, ignoreSuffix);
    final String remoteVersionString = remoteVersion.asStringNotation();
    final String clientVersionString = clientVersion.asStringNotation();
    final String clientVersionNotice = "現在のバージョンは" + clientVersionString + "です。";
    return switch (updateType) {
      case "latest" -> "現在最新バージョンのソフトウェアを使用中です！最新こそ正義！";
      case "suffix" -> "新しい試験運用版のソフトウェアが利用可能です！未知への冒険へ、出発です！ \n最新バージョン： "
        + remoteVersionString + "。 " + clientVersionNotice;
      case "patch" -> "新しいパッチリリースのソフトウェアが利用可能です！バグ修正などが含まれています！ \n最新バージョン： "
        + remoteVersionString + "。 " + clientVersionNotice;
      case "minor" -> "新しいマイナーリリースのソフトウェアが利用可能です！新機能や小規模な機能の挙動改善が含まれています！ \n最新バージョン： "
        + remoteVersionString + "。 " + clientVersionNotice;
      case "major" ->
        "新しいメジャーリリースのソフトウェアが利用可能です！大規模な機能追加や大幅な仕様変更が含まれています！更新の際には互換性にご注意ください！ \n最新バージョン： "
        + remoteVersionString + "。 " + clientVersionNotice;
      default ->
        "更新の種類を特定できません。直接アップデートを確認してください。" + clientVersionNotice;
    };
  }

  /**
   * Create OkHttpClient that ignores All SSL Certificates.
   * This should be used only for interacting with GitHub API and should not be used for general.
   * <p>
   * This method is workaround for issue
   * <a href="https://github.com/hizumiaoba/MineCraftTimeMachine/issues/89">#89</a>
   * @return {@link OkHttpClient} instance that ignores all SSL Certificates.
   */
  private static OkHttpClient createUnsafeOkHttpClient() {
    try {
      final X509TrustManager trustAllCerts = new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {}
        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {}
        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[]{};
        }
      };
      final SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
      sslContext.init(null, new TrustManager[] { trustAllCerts }, new SecureRandom());
      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
      OkHttpClient.Builder builder = new Builder();
      builder.sslSocketFactory(sslSocketFactory, trustAllCerts);
      builder.hostnameVerifier((hostname, session) -> true);
      return builder.build();
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      log.warn("Failed to create unsafe OkHttpClient. Fallback to default OkHttpClient.");
      log.warn("This will cause SSLHandShakeException during interacting with GitHub API.", e);
      return new OkHttpClient();
    }
  }
}
