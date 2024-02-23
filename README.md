# MinecraftTimeMachine

シンプルなMinecraftのバックアップツールです。

## ⚠️注意⚠️

このツールは現状**アルファ版**として開発中です。利用できない機能、ならびに潜在的なバグが存在する可能性があります。
このツールを用いない適切な方法で安全にバックアップを取っておくとともに、
バックアップを取らなかった場合に生じたデータ破損などは自己責任でお願いします。

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

~~
特に、自分はへっぽこ趣味プログラマーですので浅い知見のまま使用している機能がいくつかあると思われます。~~
~~そのあたりのご指摘なども大歓迎です。~~

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
