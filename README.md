# saya: Japanese DTV backend service with powerful features

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.30-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/saya)](https://github.com/SlashNephy/saya/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/saya/Docker)](https://hub.docker.com/r/slashnephy/saya)
[![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/slashnephy/saya)](https://hub.docker.com/r/slashnephy/saya)
[![Docker Pulls](https://img.shields.io/docker/pulls/slashnephy/saya)](https://hub.docker.com/r/slashnephy/saya)
[![license](https://img.shields.io/github/license/SlashNephy/saya)](https://github.com/SlashNephy/saya/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/saya)](https://github.com/SlashNephy/saya/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/saya)](https://github.com/SlashNephy/saya/pulls)

saya is still in heavy development.  

- [REST API docs](https://slashnephy.github.io/saya)
- [Roadmap](https://github.com/SlashNephy/saya/projects/1)

---

# これはなに

[ci7lus/elaina](https://github.com/ci7lus/elaina) 上で DTV 視聴環境を充実させるためにバックエンドとなる API サーバです。


[![elaina.png](https://raw.githubusercontent.com/SlashNephy/saya/master/docs/elaina.png)](https://github.com/ci7lus/elaina)


次の機能を現在実装しています。

- ライブ再生 / 録画番組再生での実況コメントの配信
  - ライブ再生時には [ニコニコ実況](https://jk.nicovideo.jp/) の公式放送およびコミュニティ放送 / Twitter ハッシュタグから取得します。
  - 録画番組再生時には [ニコニコ実況 過去ログ API](https://jikkyo.tsukumijima.net/) / 5ch 過去ログから取得します。
- TS ファイルから EPG 情報を抽出
- and more, coming soon...

その他実装予定の機能などは [Roadmap](https://github.com/SlashNephy/saya/projects/1) をご覧ください。

次のプロジェクトとの併用を想定しています。

- [Chinachu/Mirakurun](https://github.com/Chinachu/Mirakurun) or [mirakc/mirakc](https://github.com/mirakc/mirakc)
  - Mirakurun と mirakc のどちらでも動作します。なくても動作しますが一部制約が生じます。
- [l3tnun/EPGStation](https://github.com/l3tnun/EPGStation)
  - saya を動作させる上では不要です。elaina 上で番組を再生する場合に必要です。
- [ci7lus/elaina](https://github.com/ci7lus/elaina)
  - EPGStation を介した番組プレイヤーです。saya の API をフロントエンドで利用しています。

# Docker

環境構築が容易なので Docker で導入することをおすすめします。

現在のベースイメージは `openjdk:17-jdk-alpine` です。いくつかフレーバーを用意しています。

- `slashnephy/saya:latest`  
  master ブランチへのプッシュの際にビルドされます。安定しています。
- `slashnephy/saya:dev`  
  dev ブランチへのプッシュの際にビルドされます。開発版のため, 不安定である可能性があります。
- `slashnephy/saya:<version>`  
  GitHub 上のリリースに対応します。

`docker-compose.yml`

```yaml
version: '3.8'

services:
  saya:
    container_name: saya
    image: slashnephy/saya:latest
    restart: always
    ports:
      - 1017:1017/tcp # いれいな
    # 環境変数で各種設定を行います
    # () 内の値はデフォルト値を示します
    environment:
      # HTTP サーバのホスト, ポート番号 ("0.0.0.0", 1017)
      # Docker 環境では変更する必要はありません。
      SAYA_HOST: 0.0.0.0
      SAYA_PORT: 1017
      # HTTP サーバのベース URI ("/")
      # リバースプロキシ時に直下以外に置きたい場合に変更します
      SAYA_BASE_URI: /
      # ログレベル ("INFO")
      # 利用可能な値: ALL, TRACE, DEBUG, INFO, WARN, ERROR, OFF
      SAYA_LOG: DEBUG
      # 内部データ 更新間隔 [分] (15)
      SAYA_UPDATE_INTERVAL_MINS: 15
      # Mirakurun のホスト, ポート番号 ("mirakurun", 40772)
      MIRAKURUN_HOST: mirakurun
      MIRAKURUN_PORT: 40772
      # Annict のアクセストークン (null)
      # 以下, 未設定でも動作します
      ANNICT_TOKEN: xxx
      # Twitter の資格情報 (null, ...)
      TWITTER_CK: xxx
      TWITTER_CS: xxx
      TWITTER_AT: xxx
      TWITTER_ATS: xxx
      # /files エンドポイントで TS ファイルを検索するパス (null)
      # 別途 volume マウントが必要です
      MOUNT_POINT: /mnt
      # mirakc-arib へのパス ("/usr/local/bin/mirakc-arib")
      # Docker イメージを使用している場合は指定不要です
      MIRAKC_ARIB_PATH: /usr/local/bin/mirakc-arib
    volumes:
      # 録画 TS ファイルの置き場所
      - /mnt:/mnt:ro

  elaina:
    container_name: elaina
    image: ci7lus/elaina:latest
    restart: always
    ports:
      - 1234:1234/tcp

  mirakurun:
  epgstation:
    # https://github.com/l3tnun/docker-mirakurun-epgstation 等を参考にしてください。
    # サービス名, ポート番号等の変更がある場合には `MIRAKURUN_HOST`, `MIRAKURUN_PORT` の修正が必要になります。
```

```console
# イメージ更新
docker pull slashnephy/saya:latest

# 起動
docker-compose up -d

# ログ表示
docker-compose logs -f

# 停止
docker-compose down
```

up すると `http://localhost:1017/` に saya が, `http://localhost:1234/` に elaina が起動しているはずです。

# Endpoints

saya が提供する API は [endpoints.md](https://github.com/SlashNephy/saya/blob/master/docs/endpoints.md) に一覧があります。

# Acknowledgments

saya および [ci7lus/elaina](https://github.com/ci7lus/elaina) は次のプロジェクトを利用 / 参考にして実装しています。

- [tsukumijima/TVRemotePlus](https://github.com/tsukumijima/TVRemotePlus)
- [tsukumijima/jikkyo-api](https://github.com/tsukumijima/jikkyo-api)
- [asannou/namami](https://github.com/asannou/namami)

DTV 実況コミュニティの皆さまに感謝します。

# License

saya is provided under the MIT license.
