# Horus: ATCレーダーシミュレーションシステム

## システム概要

**Horus**は、日本のレーダー管制システムを対象としたオープンソースのシミュレーターです。バックエンドは**ウラノス** (Uranus) という愛称で、Go言語で実装されています。フロントエンドは**オモテノス** (Omotenus) という愛称で、TypeScriptで書かれています。Horusという名前はエジプト神話の天空の神ホルスに由来し、ウラノスはギリシャ神話の天空の神ウラノスに由来しています。

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
./node_modules/.bin/esbuild ./frontend/scripts/index.ts --bundle --outfile=./frontend/bundle.js && deno run --allow-net --allow-read https://deno.land/std@0.59.0/http/file_server.ts
```

以上で、DENOとesbuildのインストール、およびindex.tsをbundle.jsにコンパイルしてサーバーを立ち上げる準備が整いました。最後のコマンドは、esbuildを使ってTypeScriptファイルをバンドルし、DENOの標準ライブラリを使用してファイルサーバーを立ち上げます。必要に応じてコマンドを実行してください。



# Go環境のセットアップとmain.goの実行手順

1. **Goのインストール**

    - [Goの公式サイト](https://golang.org/)からGoをダウンロードしてインストールします。

2. **Goの環境設定**

    - インストールが完了したら、コマンドラインで `go version` を実行し、Goが正しくインストールされていることを確認します。
  
3. **依存関係のダウンロード**
    - `go mod tidy` コマンドは、`go.mod`ファイルを整理し、使用されていない依存関係を削除し、必要な依存関係を追加します。
    - `go mod tidy`

4. **コマンドラインでmain.goを実行**

    - コマンドプロンプトやターミナルで以下のコマンドを実行して、`main.go` を実行します。
    - `go run main.go`

ブラウザでhttp://localhost:8080/wsにアクセスすることで、サーバー側のコンソールなどを確認できます。
