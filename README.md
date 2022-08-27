# gRPCによるマイクロサービス間通信

## 準備
### Install Docker

Dockerをインストールします。使用しているOSに合わせて<https://docs.docker.com/get-docker/>からDockerをインストールしてください。
`Docker Engine`,`Docker CLI client`,`Docker Compose`あたりがインストールされていれば問題はないかと思います。

初回DLの方は疎通確認として、Docker HubからのコンテナイメージのDLが必要なので、次のコマンドを実行します。
`Hello From Docker!` と表示されればOK

```bash
$ docker run hello-world
...
Hello from Docker!
This message shows that your installation appears to be working correctly.
...
```

## ハンズオン

ハンズオンはスライド + このリポジトリを使って行います。


### 0. プロジェクト構成の確認

```bash
├── Dockerfile # JDKイメージを定義
├── docker-compose.yml
├── grpccommon # 共通インタフェースモジュール
│   ├── mvnw # Maven Wrapper(後述)
│   ├── mvnw.cmd # Maven Wrapper
│   ├── pom.xml
│   ├── src
│   └── target
├── grpcserver # サーバモジュール
│   ├── mvnw # Maven Wrapper
│   ├── mvnw.cmd # Maven Wrapper
│   ├── pom.xml
│   ├── src
│   └── target
└── pom.xml # マルチモジュール化に必要
```

#### 0.1 コンテナ内で作業する

以後の作業はほぼDockerコンテナ内で行います

Dockerコンテナを以下のコマンドで作成し、立ち上げます

```bash
$ docker-compose build
$ docker-compose up -d
```

コンテナがバックグラウンドで立ち上がっていることを確認
StateがUpならOK

```bash
$ docker-compose ps
                Name                   Command   State           Ports
-------------------------------------------------------------------------------
grpc-spring-boot-starter-main_java_1   jshell    Up      0.0.0.0:9090->9090/tcp
```


起動しているコンテナ内にログイン
プロンプトの表示が変わればOK

```bash
$ docker-compose exec java bash
root@ac57d717425a:/app# pwd
/app
```


### 1. Protobufインタフェースの用意

ハンズオン

[1.Protobufインタフェースの用意](./docs/chap01.md)

### 2. gRPCサーバの用意

ハンズオン

[2.gRPCサーバの用意](./docs/chap02.md)


### 3. gRPCメソッドの追加

[3.gRPCメソッドの追加](./docs/chap03.md)

### 4. gRPCクライアントの追加
