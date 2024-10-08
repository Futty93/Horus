# Horus: ATCレーダーシミュレーションシステム

## システム概要

**Horus**は、日本のレーダー管制システムを対象としたオープンソースのシミュレーターです。バックエンドは**ウラノス** (Uranus) という愛称で、Java言語で実装されています。フロントエンドは**オモテノス** (Omotenus) という愛称で、TypeScriptで書かれています。Horusという名前はエジプト神話の天空の神ホルスに由来し、ウラノスはギリシャ神話の天空の神ウラノスに由来しています。

### 開発背景

欧州にはデルフト工科大学が開発した管制レーダーシミュレーターのオープンソースモデルがありますが、日本では利用が難しい現状があります。さらに、日本には誰でも気軽に利用できるオープンソースのレーダー管制シミュレーターが存在しないため、研究者は安全性向上のための実験を行う際に独自のシミュレーターを作成する必要があります。これが研究のスピードを鈍化させる原因となっています。

このような背景から、Horusは研究者の負担を軽減し、研究のスピードを向上させることを目的に開発されました。Horusを利用することで、日本のレーダー管制システムに関する安全性実験や研究をより効率的に進めることができます。

# DENOとesbuildのインストールと使用手順

## DENOのインストール

### Linux / macOS

```
deno install -qAf https://deno.land/x/deno/cli.ts
```

### macOS (Homebrew)

```
brew install deno
```

## コンパイラのインストール

```
npm install --save-exact --save-dev esbuild
```

## index.tsをbundle.jsにコンパイルし、サーバーを立ち上げる

```
deno task dev
```

## ブラウザからフロントエンドにアクセス

[オモテノス](http://localhost:4507/Frontend/index.html)

以上で、DENOとesbuildのインストール、およびindex.tsをbundle.jsにコンパイルしてサーバーを立ち上げる準備が整いました。最後のコマンドは、esbuildを使ってTypeScriptファイルをバンドルし、DENOの標準ライブラリを使用してファイルサーバーを立ち上げます。必要に応じてコマンドを実行してください。

# Java環境のセットアップとmain.goの実行手順

1. **Javaのインストール**

    - [JDKのダウンロードサイト](https://www.oracle.com/jp/java/technologies/downloads/#java22)からJavaをダウンロードしてインストールします。

2. **Javaの環境設定**

    - インストールが完了したら、コマンドラインで `java --version` を実行し、Javaが正しくインストールされていることを確認します。
  
3. **依存関係のダウンロード**
    - Gradleを使用しています。実行時によしなにしてくれるはずです。

4. **コマンドラインで実行**

`cd ./Backend`と`./gradlew bootRun`を実行してバックエンドを起動します。

ブラウザで<http://localhost:8080/docs.htmlにアクセスすることで、サーバー側のコマンドを確認できます。>
