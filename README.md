# MinecraftTimeMachine

シンプルなMinecraftのバックアップツールです。

## ⚠️注意⚠️

このツールは現状**アルファ版**として開発中です。利用できない機能、ならびに潜在的なバグが存在する可能性があります。
このツールを用いない適切な方法で安全にバックアップを取っておくとともに、
バックアップを取らなかった場合に生じたデータ破損などは自己責任でお願いします。

### Minecraft起動中のバックアップについて

このツールでは以下の開発者の環境で、Minecraftをプレイ中のバックアップが可能であることを確認しています。
ですが過信はせず、最低でもワールドに入っていない状態（タイトル画面）でのバックアップの実行を強くお勧めします。

```log
-- System Details --
Details:
	Minecraft Version: 1.7.10
	Operating System: Windows 10 (amd64) version 10.0
	Java Version: 1.8.0_51, Oracle Corporation
	Java VM Version: Java HotSpot(TM) 64-Bit Server VM (mixed mode), Oracle Corporation
	Memory: 145999072 bytes (139 MB) / 268435456 bytes (256 MB) up to 4294967296 bytes (4096 MB)
	JVM Flags: 8 total; -XX:HeapDumpPath=MojangTricksIntelDriversForPerformance_javaw.exe_minecraft.exe.heapdump -Xmx4G -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=20 -XX:G1ReservePercent=20 -XX:MaxGCPauseMillis=50 -XX:G1HeapRegionSize=256M
	AABB Pool Size: 0 (0 bytes; 0 MB) allocated, 0 (0 bytes; 0 MB) used
	IntCache: cache: 0, tcache: 0, allocated: 0, tallocated: 0
	FML: 
	GL info: ' Vendor: 'NVIDIA Corporation' Version: '4.6.0 NVIDIA 550.09' Renderer: 'NVIDIA GeForce GTX 1660 SUPER/PCIe/SSE2'
```

## 使い方

任意の場所で `MinecraftTimeMachine.exe` を実行するだけ！

詳しい説明や、設定画面については、[wiki](https://github.com/hizumiaoba/MinecraftTimeMachine/wiki)
をご覧ください！

## ダウンロード

[Releases](https://github.com/hizumiaoba/MinecraftTimeMachine/releases) から最新版をダウンロードできます。

現状はバージョンチェックやサイドローディング機能などは搭載していません。逐次バージョンを確認することをおすすめします。

## 現状の機能

- ワールドデータのバックアップ
- 一定時間ごとの自動バックアップ
- 通常のバックアップとは別で個数制限に左右されない特殊バックアップ機能
- Minecraftランチャーの起動機能

## 開発者向け情報

### ビルド方法

このツールは `Java` と `Gradle` を用いて開発しています。

1. このリポジトリをクローンする
2. クローンしたディレクトリに移動する
3. `gradlew jpackageImage` を実行する
4. `build` ディレクトリ以下に実行ファイルや関連モジュールが生成されます。

### コントリビュート

このツールはオープンソースプロジェクトです。バグ報告や機能追加のリクエスト、プルリクエストなどは大歓迎です。

PRを贈る際は、以下の点にご注意ください。

- できるだけ小さな単位でのPRをお願いします。
- 自動生成などでも構いませんので、PR本文に変更点の詳細などをご記入ください。
- PRを向ける先は `develop` ブランチです。

PRを作るまでもない、小さな誤字報告などは [Issues](https://github.com/hizumiaoba/MinecraftTimeMachine/issues)
や [Discussions](https://github.com/hizumiaoba/MinecraftTimeMachine/discussions) もご活用ください。

## ライセンス

このツールのソースはApache 2.0ライセンスとして公開しています。詳しくは[LICENSE](LICENSE)をご覧ください。

また、利用しているライブラリのライセンスについては[NOTICE](NOTICE)をご覧ください。

## ロードマップ

[Issues](https://github.com/hizumiaoba/MinecraftTimeMachine/issues) にて管理しています。

実装してほしい機能などをお持ちの方は是非お気軽にご提案ください！

## 連絡先

バグ報告や機能追加のリクエストなどは、[Issues](https://github.com/hizumiaoba/MinecraftTimeMachine/issues)
にて受け付けています。

その他の連絡や質問などは、[Discussions](https://github.com/hizumiaoba/MinecraftTimeMachine/discussions)
にて受け付けています。

## 謝辞

このツールは `MinecraftBackup plus Alpha (MBpA)` からインスパイアされて開発されています。
偉大なる先駆者さまに感謝と敬意を表します。
