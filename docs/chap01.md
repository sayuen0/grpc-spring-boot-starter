# 1.Protobufインタフェースの用意

- `src/main/proto`に、`.proto`ファイルを作成
- `./proto`ファイルに、サービス定義を書く
- `./mvnw install` コマンドで、`.proto`ファイルを`.java`ファイルにコンパイル

※原則、ファイル操作コマンドはコンテナ内で行うよう記載してます
(ファイルパスを適宜読み替えて、ファイル操作に関してはコンテナ外で行っても挙動的には問題ありません。
ただしmvnwコマンドは必ずコンテナ内で行ってください)


## コンテナに入る




作業ディレクトリに移動
```bash
$ cd /app/grpccommon
```


## pom.xmlの確認

`protobuf-maven-plugin`が、mavenの実行コマンドとしてprotobufのコンパイルを提供

## .protoファイルの作成

ファイル本体を作成
```bash
$ touch src/main/proto/helloService.proto
```

## .protoファイルにサービス定義を書く

`helloService.proto` ファイルを以下のように書きます

```proto
syntax = "proto3";

package jp.co.everrise.seminar; // protobufパッケージ名(Javaのパッケージ名とは無関係)

option java_multiple_files = true; // 生成されるJavaファイルが分割される
option java_package = "jp.co.everrise.seminar.proto"; // javaのパッケージ名
option java_outer_classname = "HelloWorldProto"; // javaでのクラス名

// サービス定義。複数のProcedure(関数)を書くことができる
service MyService {
    // rpc 関数名 (リクエスト型) returns (レスポンス型) {}
    rpc SayHello (HelloRequest) returns (HelloReply) {}
}

// リクエスト型定義
message HelloRequest {
    // プロパティにはメッセージ型内で一意の一意の番号を振る必要がある
    string name = 1; // 文字列プロパティを定義
}

// レスポンス型定義
message HelloReply {
    string message = 1;
}
```

## .protoファイルを.javaファイルにコンパイル

mvnw(Maven Wrapper)は、ローカルでMaven依存関係を解決するためのスクリプト

```bash
$ ./mvnw clean install # targetディレクトリを一度削除し、その後コンパイル
```

初回だけ依存ライブラリのDLを大量に行うので、数分かかるため、待ちます

完了したら、`target/generated-sources/protobuf/java`配下に `.java`ファイルが生成されていることを確認


```bash
$ ls -l target/generated-sources/protobuf/java/jp/co/everrise/seminar/proto/
total 52
-rw-r--r-- 1 root root 18517 Aug 27 05:49 HelloReply.java
-rw-r--r-- 1 root root   559 Aug 27 05:49 HelloReplyOrBuilder.java
-rw-r--r-- 1 root root 19461 Aug 27 05:49 HelloRequest.java
-rw-r--r-- 1 root root   811 Aug 27 05:49 HelloRequestOrBuilder.java
-rw-r--r-- 1 root root  2762 Aug 27 05:49 HelloWorldProto.java
```

```bash
ls -l target/generated-sources/protobuf/grpc-java/jp/co/everrise/seminar/proto/
total 12
-rw-r--r-- 1 root root 11417 Aug 27 05:49 MyServiceGrpc.java
```

次のことを確認


- メッセージ型には、作成したプロパティに関しgetter, setterが実装されていること
- `MyServiceGrpc.java`に、`MyServiceImplBase`innerクラスがあり、そこに`sayHello()`が定義されていること

## Next:
[2.gRPCサーバの用意へ](./chap02.md)
