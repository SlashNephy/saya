# saya: Japanese DTV backend service with powerful features

[![Kotlin](https://img.shields.io/badge/Kotlin-1.4.21-blue)](https://kotlinlang.org)
[![Docker Build Status](https://img.shields.io/docker/build/slashnephy/saya)](https://hub.docker.com/r/slashnephy/saya)
[![Docker Image Size (latest by date)](https://img.shields.io/docker/image-size/slashnephy/saya)](https://hub.docker.com/r/slashnephy/saya)
[![license](https://img.shields.io/github/license/SlashNephy/saya)](https://github.com/SlashNephy/saya/blob/master/LICENSE)
[![issues](https://img.shields.io/github/issues/SlashNephy/saya)](https://github.com/SlashNephy/saya/issues)
[![pull requests](https://img.shields.io/github/issues-pr/SlashNephy/saya)](https://github.com/SlashNephy/saya/pulls)

saya is still in heavy development.  

- [REST API docs](https://atmos.starry.blue/saya)
- [Roadmap](https://github.com/SlashNephy/saya/projects/1)

---

# これはなに

[ci7lus/elaina](https://github.com/ci7lus/elaina) 上で DTV 視聴環境を充実させるためにバックエンドとなる API サーバです。


[![elaina.png](https://raw.githubusercontent.com/SlashNephy/saya/master/docs/elaina.png)](https://github.com/ci7lus/elaina)


次の機能を現在実装しています。

- ライブ再生 / 録画番組再生での実況コメントの配信
  - ライブ再生時には [ニコニコ実況](https://jk.nicovideo.jp/) の公式放送およびコミュニティ放送 / Twitter ハッシュタグから取得します。
  - 録画番組再生時には [ニコニコ実況 過去ログ API](https://jikkyo.tsukumijima.net/) / 5ch 過去ログから取得します。
- and more, coming soon...

その他実装予定の機能などは [Roadmap](https://github.com/SlashNephy/saya/projects/1) をご覧ください。

次のプロジェクトとの併用を想定しています。

- [Chinachu/Mirakurun](https://github.com/Chinachu/Mirakurun) or [mirakc/mirakc](https://github.com/mirakc/mirakc)
  - Mirakurun と mirakc のどちらでも動作します。
- [l3tnun/EPGStation](https://github.com/l3tnun/EPGStation)
- [ci7lus/elaina](https://github.com/ci7lus/elaina)

# Setup

環境構築が容易なので Docker で導入することをおすすめします。

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

起動すると `http://localhost:1017/` にサーバが起動しているはずです。

# Endpoints

TODO...

# Acknowledgments

saya および [ci7lus/elaina](https://github.com/ci7lus/elaina) は次のプロジェクトを利用 / 参考にして実装しています。

- [tsukumijima/TVRemotePlus](https://github.com/tsukumijima/TVRemotePlus)
- [tsukumijima/jikkyo-api](https://github.com/tsukumijima/jikkyo-api)

DTV 実況コミュニティの皆さまに感謝します。

# License

saya is provided under the MIT license.
