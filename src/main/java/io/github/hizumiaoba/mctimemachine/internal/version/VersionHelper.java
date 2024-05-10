package io.github.hizumiaoba.mctimemachine.internal.version;

import io.github.hizumiaoba.mctimemachine.MineCraftTimeMachineApplication;
import io.github.hizumiaoba.mctimemachine.api.Version;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
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

  public String constructUpdateMessage(VersionObj remoteVersion, boolean ignoreSuffix) {
    final String updateType = getUpdateType(remoteVersion, ignoreSuffix);
    final String remoteVersionString = remoteVersion.asStringNotation();
    final String clientVersionString = clientVersion.asStringNotation();
    final String clientVersionNotice = "現在のバージョンは" + clientVersionString + "です。";
    return switch (updateType) {
      case "latest" -> "現在最新バージョンのソフトウェアを使用中です！最新こそ正義！";
      case "suffix" -> "新しい試験運用版のソフトウェアが利用可能です！未知への冒険へ、出発です！: "
        + remoteVersionString + "。 " + clientVersionNotice;
      case "patch" -> "新しいパッチリリースのソフトウェアが利用可能です！バグ修正などが含まれています！: "
        + remoteVersionString + "。 " + clientVersionNotice;
      case "minor" -> "新しいマイナーリリースのソフトウェアが利用可能です！新機能や小規模な機能の挙動改善が含まれています！: "
        + remoteVersionString + "。 " + clientVersionNotice;
      case "major" -> "新しいメジャーリリースのソフトウェアが利用可能です！大規模な機能追加や大幅な仕様変更が含まれています！更新の際には互換性にご注意ください！: "
        + remoteVersionString + "。 " + clientVersionNotice;
      default ->
        "更新の種類を特定できません。直接アップデートを確認してください。" + clientVersionNotice;
    };
  }

  public VersionObj getClientVersion() {
    return VersionObj.parse(MineCraftTimeMachineApplication.class.getAnnotation(Version.class));
  }

  public VersionObj getRemoteVersion() {
    return VersionObj.parse(MineCraftTimeMachineApplication.class.getAnnotation(Version.class));
  }
}
