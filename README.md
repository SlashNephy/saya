# saya: 📺 API server to enhance the web-based DTV watching experiences w/ elaina

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.30-blue)](https://kotlinlang.org)
[![GitHub release (latest by date)](https://img.shields.io/github/v/release/SlashNephy/saya)](https://github.com/SlashNephy/saya/releases)
[![GitHub Workflow Status](https://img.shields.io/github/workflow/status/SlashNephy/saya/Docker)](https://hub.docker.com/r/slashnephy/saya)
[![Docker Image Size (tag)](https://img.shields.io/docker/image-size/slashnephy/saya/latest)](https://hub.docker.com/r/slashnephy/saya)
[![Docker Pulls](https://img.shields.io/docker/pulls/slashnephy/saya)](https://hub.docker.com/r/slashnephy/saya)
[![license](https://img.shields.io/github/license/SlashNephy/saya)](https://github.com/SlashNephy/saya/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/saya)](https://github.com/SlashNephy/saya/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/saya)](https://github.com/SlashNephy/saya/pulls)

saya is still in heavy development. まだ一般の利用向けに最適化されていません。

- [endpoints.md](https://github.com/SlashNephy/saya/blob/master/docs/endpoints.md)
- [Roadmap](https://github.com/SlashNephy/saya/projects/1)

---

# これはなに

EPGStation を使用している環境で Web ベースの視聴環境を拡張することを目的とした API バックエンドサーバです。フロントエンドのコードは [ci7lus/elaina](https://github.com/ci7lus/elaina) に公開されています。

そのため, saya 単体では予約・録画機能を有しません。

[![elaina.png](https://raw.githubusercontent.com/SlashNephy/saya/master/docs/elaina.png)](https://github.com/ci7lus/elaina)

次の機能を現在実装しています。

- ライブ再生 / 録画番組再生での実況コメントの配信
  - ライブ再生時には次のソースから取得します。
    + [ニコニコ実況](https://jk.nicovideo.jp/) の公式放送およびコミュニティ放送 (ログイン不要)
    + Twitter ハッシュタグ (Filter ストリーム or 検索 API)
    + 5ch DAT
  - 録画番組再生時には次のソースから取得します。
    + [ニコニコ実況 過去ログ API](https://jikkyo.tsukumijima.net/)
    + 5ch 過去ログ
- TS ファイルから EPG 情報を抽出
- and more, coming soon...

その他実装予定の機能などは [Roadmap](https://github.com/SlashNephy/saya/projects/1) をご覧ください。

saya は以下のプロジェクトとの併用を想定しています。

```
             +--------------------------+     +----------------+     +----------------+     +-------------+
 Client A -> |   Live Comment Stream    |     |                |     |   EPGStation   |     |  Mirakurun  |
             |         WebSockets       |     |                | <-> |                | <-> |   mirakc    |
 Client B -> |    /comments/***/live    |     |     elaina     |     |  0.0.0.0:8888  |     +-------------+
             +--------------------------+ <-> |                |     +----------------+     +-------------+
             +--------------------------+     |                |     +----------------+     |  niconico   |
             | Timeshift Comment Stream |     |  0.0.0.0:1234  |     |      saya      |     |    5ch      |
 Client C -> |         WebSockets       |     |                | <-> |                | <-> |   Annict    |
             | /comments/***/timeshift  |     |                |     |  0.0.0.0:1017  |     |      etc... |
             +--------------------------+     +----------------+     +----------------+     +-------------+
```

- [Chinachu/Mirakurun](https://github.com/Chinachu/Mirakurun) or [mirakc/mirakc](https://github.com/mirakc/mirakc)
  - チャンネル情報の取得に使用されます。
  - Mirakurun と mirakc のどちらでも動作します。なくても動作しますが一部制約が生じます。
- [l3tnun/EPGStation](https://github.com/l3tnun/EPGStation)
  - saya を動作させる上では不要です。elaina 上で番組を再生する場合に必要です。
- [ci7lus/elaina](https://github.com/ci7lus/elaina)
  - EPGStation を介した番組プレイヤーです。saya の API をフロントエンドで利用しています。

# Get Started

## Docker

以下の README には Docker に関する用語が多く含まれます。必要に応じて [Docker 概要](https://docs.docker.jp/get-started/overview.html) (公式ドキュメント) を参照してください。

### イメージ

いくつかのイメージタグを用意しています。現在 linux/amd64 プラットホームのみサポートしています。

- `slashnephy/saya:latest`
  + master ブランチへのプッシュの際にビルドされます。基本的に最新の安定版バージョンになります。
  + 比較的安定しています。
- `slashnephy/saya:dev`
  + dev ブランチへのプッシュの際にビルドされます。
  + 開発版のため, 不安定である可能性があります。
- `slashnephy/saya:<version>`
  + GitHub 上のリリースに対応します。
- `slashnephy/saya:***-vaapi`
  + Intel CPU 内蔵 GPU or AMD グラフィックカードのユーザ向けです。
  + VAAPI によるハードウェアエンコーディングを有効化した ffmpeg を同梱しています。
- `slashnephy/saya:***-nvenc`
  + NVIDIA グラフィックカードのユーザ向けです。
  + NVEnc によるハードウェアエンコーディングを有効化した ffmpeg を同梱しています。

### docker-compose

環境構築が容易なので docker-compose で導入することをおすすめします。docker-compose についての説明は [Docker Compose 概要](https://docs.docker.jp/compose/overview.html) (公式ドキュメント) などを参照してください。一言でいうと docker-compose は「複数のアプリケーションを一度に起動するためのツール」で, それを指示するための構成ファイルである `docker-compose.yml` を作成する必要があります。

`docker-compose.yml`

```yaml
version: '3.8'

services:
  saya:
    container_name: saya
    image: slashnephy/saya:latest
    restart: always
    # `***-nvenc` イメージを使用する場合に必要
    # runtime: nvidia
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
      # Twitter の資格情報 (null, null, null, null)
      TWITTER_CK: xxx
      TWITTER_CS: xxx
      TWITTER_AT: xxx
      TWITTER_ATS: xxx
      # Twitter からツイートを取得する際にストリーミング API を使用するか (false)
      # 接続に失敗した場合には通常の検索 API にフォールバックします。
      # 試験的な機能のため, 一部の環境で動作しない可能性があります。
      TWITTER_PREFER_STREAMING_API: 'true'
      # 5ch API への接続情報 (null, null, null, null, null)
      GOCHAN_HM_KEY: xxx
      GOCHAN_APP_KEY: xxx
      GOCHAN_AUTH_UA: xxx
      GOCHAN_AUTH_X_2CH_UA: xxx
      GOCHAN_UA: xxx
      # モリタポアカウントの資格情報 (null, null)
      MORITAPO_EMAIL: xxx
      MORITAPO_PASSWORD: xxx
      # /files エンドポイントで TS ファイルを検索するパス (null)
      # 別途 volume マウントが必要です
      MOUNT_POINT: /mnt
      # mirakc-arib へのパス ("/usr/local/bin/mirakc-arib")
      # Docker イメージを使用している場合は指定不要です
      MIRAKC_ARIB_PATH: /path/to/mirakc-arib
      # ffmpeg へのパス ("/usr/local/bin/ffmpeg")
      # Docker イメージを使用している場合は指定不要です
      FFMPEG_PATH: /path/to/ffmpeg
    volumes:
      # 録画 TS ファイルの置き場所
      - /mnt:/mnt:ro
      # definitions.yml を書き換えて使用したい場合
      # - ./definitions.yml:/app/docs/definitions.yml:ro

  elaina:
    container_name: elaina
    image: ci7lus/elaina:latest
    restart: always
    ports:
      - 1234:1234/tcp

  # Mirakurun/mirakc, EPGStation コンテナの構成例は
  # https://github.com/l3tnun/docker-mirakurun-epgstation 等を参考にしてください。
  # サービス名, ポート番号等の変更がある場合には `MIRAKURUN_HOST`, `MIRAKURUN_PORT` の修正が必要になります。
  mirakurun:
    container_name: Mirakurun
    image: chinachu/mirakurun:latest
    restart: always
    ports:
      - 40772:40772/tcp
    cap_add:
      - SYS_ADMIN
      - SYS_NICE
  epgstation:
    container_name: EPGStation
    image: l3tnun/epgstation:alpine
    restart: always
    ports:
      - 8888:8888/tcp
```

このように `docker-compose.yml` を作成し, 同じディレクトリで docker-compose を実行します。Linux 環境では root 権限で実行する必要があります。

```console
# 更新
docker-compose pull

# バックグラウンドで起動
docker-compose up -d

# ログ表示
docker-compose logs -f

# 破棄
docker-compose down
```

`up -d` すると `http://localhost:1017/` に saya が, `http://localhost:1234/` に elaina が起動しているはずです。

## 直接実行

リリースから Jar を取ってきて実行するか, `./gradlew run` で実行できます。

Java の実行環境は JRE 8 以降が必要です。

設定値の変更は現在, 環境変数経由でしか行なえません。ご了承ください。

```console
SAYA_LOG=DEBUG java -jar /path/to/saya.jar
```

# Endpoints

saya が提供する API は [endpoints.md](https://github.com/SlashNephy/saya/blob/master/docs/endpoints.md) に一覧があります。

# Contribution

IDE は IntelliJ IDEA をおすすめします。

saya の開発には以下のブランチモデルを採用しています。

- `master` ブランチ  
  安定版とみなされます。基本的にバージョンアップの際に `dev` から merge されます。
- `dev` ブランチ  
  開発版とみなされます。
- `feature-***` ブランチ  
  特定の機能の開発に使用します。開発が一段落したのちに `dev` に squash merge されます。

不安定なプロジェクトにつき, 互換性のない変更や方針変更が発生する可能性があります。ご了承ください。

```console
# ビルド
./gradlew build

# 実行
./gradlew run
```

# Acknowledgments

saya および [ci7lus/elaina](https://github.com/ci7lus/elaina) は次のプロジェクトを利用 / 参考にして実装しています。

- [tsukumijima/TVRemotePlus](https://github.com/tsukumijima/TVRemotePlus)
- [tsukumijima/jikkyo-api](https://github.com/tsukumijima/jikkyo-api)
- [asannou/namami](https://github.com/asannou/namami)
- [silane/TVTComment](https://github.com/silane/TVTComment)

DTV 実況コミュニティの皆さまに感謝します。

# License

saya is provided under the MIT license.
