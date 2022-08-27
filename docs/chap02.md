# 2.gRPCサーバの用意

- サービスハンドラをJavaで書く
  - 1で作成したProtobufインタフェースから生成されたJavaコードをを用いる
- サーバを起動する
- `grpcurl`でリクエストを送信する


作業ディレクトリに移動

```
$ cd /app/grpcserver
```

## サービスハンドラをJavaで書く


ハンドラ: リクエストを受け取ったらレスポンスとして処理するための関数

ハンドラ用のパッケージを作成

```
$ mkdir src/main/java/jp/co/everrise/seminar/grpcserver/service
```

ハンドラクラスを作成

```
$ touch src/main/java/jp/co/everrise/seminar/grpcserver/service/MyServiceImpl.java
```

`MyServiceImpl`の中身を書きます


```java
package jp.co.everrise.seminar.grpcserver.service;

import io.grpc.stub.StreamObserver;
import jp.co.everrise.seminar.proto.*;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService // 5.
public class MyServiceImpl extends MyServiceGrpc.MyServiceImplBase {
   @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) { // 1.
        HelloReply reply = HelloReply.newBuilder() // 2.
                .setMessage("Hello," + request.getName())
                .build();
        responseObserver.onNext(reply); // 3.
        responseObserver.onCompleted(); // 4.
    }
}
```

1. `MyServiceImplBase`の`sayHello`をオーバーライドする。第一引数はリクエストメッセージ型、第二引数はレスポンスメッセージ型を生成するStreamObserver
2. レスポンスメッセージ型を`newBuilder()`で用意し、リクエストから読み出したnameをレスポンスのmessageにセット。その後`build()`でメッセージをエンコード
3. `onNext(reply)`でクライアントへレスポンスを送信
4. `onCompleted()`で通信を完了する
5. `@GrpcService`アノテーションがついたクラスは、gRPCサービスとして認識される

## サーバを起動する

```
$ ./mvnw spring-boot:run
```

初回は長いので、待ちましょう

`Started GrpcserverApplication` のログが表示されたらOK


```
[INFO] Attaching agents: []

  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v2.7.3)

2022-08-27 06:31:41.236  INFO 1040 --- [           main] j.c.e.s.g.GrpcserverApplication          : Starting GrpcserverApplication using Java 11.0.16.1 on ac57d717425a with PID 1040 (/app/grpcserver/target/classes started by root in /app/grpcserver)
2022-08-27 06:31:41.240  INFO 1040 --- [           main] j.c.e.s.g.GrpcserverApplication          : No active profile set, falling back to 1 default profile: "default"
2022-08-27 06:31:42.055  INFO 1040 --- [           main] g.s.a.GrpcServerFactoryAutoConfiguration : Detected grpc-netty-shaded: Creating ShadedNettyGrpcServerFactory
2022-08-27 06:31:42.313  INFO 1040 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: jp.co.everrise.seminar.MyService, bean: myServiceImpl, class: jp.co.everrise.seminar.grpcserver.service.MyServiceImpl
2022-08-27 06:31:42.313  INFO 1040 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: grpc.health.v1.Health, bean: grpcHealthService, class: io.grpc.services.HealthServiceImpl
2022-08-27 06:31:42.313  INFO 1040 --- [           main] n.d.b.g.s.s.AbstractGrpcServerFactory    : Registered gRPC service: grpc.reflection.v1alpha.ServerReflection, bean: protoReflectionService, class: io.grpc.protobuf.services.ProtoReflectionService
2022-08-27 06:31:42.458  INFO 1040 --- [           main] n.d.b.g.s.s.GrpcServerLifecycle          : gRPC Server started, listening on address: *, port: 9090
2022-08-27 06:31:42.470  INFO 1040 --- [           main] j.c.e.s.g.GrpcserverApplication          : Started GrpcserverApplication in 1.792 seconds (JVM running for 2.138)
```

サーバポートが`9090`番を使用していることを確認

##  grpcurlでリクエストを送信するs

grpcurl: gRPC版のカールのようなコマンド

ホスト(MacOS)側でもう一つターミナルを開き、コンテナにログイン
(サーバは立ち上げたままにし、exitもしない)

```
$ cd <このリポジトリをダウンロードしたパス>/grpc-spring-boot-starter-main
$ docker-compose exec java bash
root@ac57d717425a:/app#
```

grpcurlコマンドが入っているか確認
```
$ grpcurl --version
grpcurl v1.8.7
```

サーバで提供しているサービス一覧を取得
```
$ grpcurl --plaintext localhost:9090 list
grpc.health.v1.Health
grpc.reflection.v1alpha.ServerReflection
jp.co.everrise.seminar.MyService
```

MyServiceのRPC一覧を取得
```
$ grpcurl --plaintext localhost:9090 list jp.co.everrise.seminar.MyService
jp.co.everrise.seminar.MyService.SayHello
```

MyServiceのSayHelloメソッドに、リクエストを送信
`HelloRequest`型の持つ`name`プロパティを渡す

```
$ grpcurl --plaintext -d '{"name": "Bob"}' localhost:9090 jp.co.everrise.seminar.MyService.SayHello
{
  "message": "Hello,Bob"
}
```

メッセージが返ってきたら、成功

## Next:
[3.gRPCメソッドの追加へ](./chap03.md)
