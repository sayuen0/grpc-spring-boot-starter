# 3. gRPCメソッドの追加

- `grpccommon`モジュール内で、新規にLoggingサービスを作成
  - `SendLog` ログ送信
  - `GetLog` ログ取得(単体)
  - `ListLog` ログ取得(全件)
- `grpcserver`モジュール内で、Loggingサービス用のサービスハンドラを作成
- サーバにLoggingリクエストを送る

## `grpccommon`モジュール内で、新規にFizzBuzzメソッドを作成

作業ディレクトリの移動
以下、１つ目のターミナルウィンドウで作業
```bash
$ cd /app/grpccommon
```

Loggingサービス用のprotoファイルを新規作成

```bash
$ touch src/main/proto/loggingService.proto
```

`loggingService.proto`を以下のように書きます

```proto
syntax = "proto3";

package jp.co.everrise.seminar;

import "google/protobuf/timestamp.proto";
import "google/protobuf/empty.proto";

option java_multiple_files = true;
option java_package = "jp.co.everrise.seminar.proto";
option java_outer_classname="LoggingProto";

service LoggingService{
    rpc SendLog(SendLogRequest) returns (SendLogReply){}
    rpc GetLog(GetLogRequest) returns (GetLogReply){}
    rpc ListLog(google.protobuf.Empty) returns (ListLogReply){}
}

message SendLogRequest{
    string message = 1;
}

message SendLogReply{
    int32 id = 1;
    string message = 2;
    google.protobuf.Timestamp timestamp = 3;
}

message GetLogRequest{
    int32 id = 1;
}

message GetLogReply{
    int32 id = 1;
    string message = 2;
    google.protobuf.Timestamp timestamp = 3;
}

message ListLogReply{
    repeated GetLogReply logs = 1;
}
```

ビルド
```
$ ./mvnw clean install
```

## `grpcserver`モジュール内で、Loggingサービス用のサービスハンドラを作成

作業ディレクトリの移動
```bash
$ cd /app/grpcserver
```

ハンドラクラスを作成

```bash
$ touch src/main/java/jp/co/everrise/seminar/grpcserver/service/LoggingServiceImpl.java
```

`LoggingServiceImpl.java` の内容を書きます

```java

package jp.co.everrise.seminar.grpcserver.service;

import com.google.protobuf.Empty;
import com.google.protobuf.Timestamp;
import io.grpc.stub.StreamObserver;
import jp.co.everrise.seminar.proto.*;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.google.protobuf.util.Timestamps.fromMillis;
import static java.lang.System.currentTimeMillis;

@GrpcService
public class LoggingServiceImpl extends LoggingServiceGrpc.LoggingServiceImplBase {
    // ログDBの代わりのメモリマップ
    private Map<Integer, Log> logs = new HashMap<>();
    // ログDB用のカウンタ MySQLのAUTO INCREMENTのようなもの
    private int counter = 0;
    // ロック(同時書き込みと、読み込み中の書き込みを防ぐオブジェクト)
    private ReadWriteLock lock = new ReentrantReadWriteLock();

    // ログレコードクラス
    public class Log{
        public int id; // ID
        public String message; // 本文
        public Timestamp timestamp; // 作成時刻

        public Log(int id, String message, Timestamp timestamp){
            this.id = id;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    @Override
    public void sendLog(SendLogRequest request, StreamObserver<SendLogReply> streamObserver){
        Log log = this.saveLog(request.getMessage()); // ログの保存をし、保存したログを取得
        SendLogReply reply = SendLogReply.newBuilder()
                .setId(log.id).setMessage(log.message).setTimestamp(log.timestamp).build(); // レスポンスの生成
        streamObserver.onNext(reply); // レスポンス送信
        streamObserver.onCompleted();　// 通信完了
    }

    @Override
    public void getLog(GetLogRequest request, StreamObserver<GetLogReply> streamObserver){
        Log log = this.getLog(request.getId()); // IDでログを検索
        if (log == null) {
            streamObserver.onNext(null); // ログがなければ、空オブジェクトを送り終了
            streamObserver.onCompleted();
            return;
        }
        String message = log.message;
        Timestamp timestamp = log.timestamp;
        GetLogReply reply = GetLogReply.newBuilder() // 取得したログからレスポンスを生成
                .setId(request.getId()).setMessage(message).setTimestamp(timestamp).build();
        streamObserver.onNext(reply);
        streamObserver.onCompleted();
    }
  
    @Override
    public void listLog(Empty empty, StreamObserver<ListLogReply> streamObserver) {
        ListLogReply.Builder listLogReplyBuilder = ListLogReply.newBuilder(); // レスポンスの雛形を作成
        for (Log log : this.logs.values()){ // 保存済みのログ全部について, 
            GetLogReply logReply = GetLogReply.newBuilder() // ログレスポンスを生成
                    .setId(log.id).setMessage(log.message).setTimestamp(log.timestamp).build();
            listLogReplyBuilder.addLogs(logReply); // 雛形にログを詰めていく
        }
        ListLogReply reply = listLogReplyBuilder.build(); // レスポンスをエンコード
        streamObserver.onNext(reply);
        streamObserver.onCompleted();
    }

    // IDの一致するログを返す
    private Log getLog(int id) {
        this.lock.readLock().lock(); // 共有ロックをする。ロックされている間は、別リクエストは読みができるが書き込みができない
        Log log = this.logs.get(id); // mapからIDの一致するログを取得
        this.lock.readLock().unlock();　// ロック解除
        return log;
    }

    // ログを保存して返す
    private Log saveLog(String message) {
        this.lock.writeLock().lock(); // 排他ロックをする。ロックされている間は、別リクエストは読み書きができない
        this.counter++;
        Log log = new Log(this.counter, message, fromMillis(currentTimeMillis()));
        this.logs.put(this.counter, log);

        this.lock.writeLock().unlock(); // ロック解除
        return log;
    }
}
```

## サーバを起動する

```
$ ./mvnw spring-boot:run
```

## grpcurlでリクエストを送信する

以下、2つ目のターミナルウィンドウにて作業

コンテナにログイン
```bash
$ cd <このリポジトリをダウンロードしたパス>/grpc-spring-boot-starter-main
$ docker-compose exec java bash
root@ac57d717425a:/app#
```

サーバで提供しているサービス一覧を取得
```bash
$ grpcurl --plaintext localhost:9090 list
grpc.health.v1.Health
grpc.reflection.v1alpha.ServerReflection
jp.co.everrise.seminar.LoggingService
jp.co.everrise.seminar.MyService
```

LoggingServiceのRPC一覧を取得

```bash
$ grpcurl --plaintext localhost:9090 list jp.co.everrise.seminar.LoggingService
jp.co.everrise.seminar.LoggingService.GetLog
jp.co.everrise.seminar.LoggingService.ListLog
jp.co.everrise.seminar.LoggingService.SendLog
```

3種のRPCが定義されていることが確認できる

SendLogで、ログを作成
```bash
$ grpcurl --plaintext -d '{"message": "I am hungry."}'  localhost:9090  jp.co.everrise.seminar.LoggingService.SendLog
{
  "id": 1,
  "message": "I am hungry.",
  "timestamp": "2022-08-27T16:32:08.829Z"
}
```

IDが増えていくこと、タイムスタンプが連動していることを確認
何度か繰り返してログを複数貯めてみましょう
なお、ログは永続化してないので、サーバを再起動したら全て消えます

ListLogで、全ログを取得

```bash
$ grpcurl --plaintext localhost:9090 jp.co.everrise.seminar.LoggingService.ListLog 
{
  "logs": [
    {
      "id": 1,
      "message": "I am hungry.",
      "timestamp": "2022-08-27T16:32:08.829Z"
    },
    {
      "id": 2,
      "message": "I am thirsty.",
      "timestamp": "2022-08-27T16:32:09.943Z"
    },
    {
      "id": 3,
      "message": "I am sleepy.",
      "timestamp": "2022-08-27T16:32:10.717Z"
    }
  ]
}
```

作成したログが全て返ってくることを確認

GetLogで、特定IDのログを取得
```bash
$ grpcurl --plaintext -d '{"id": 2}' localhost:9090  jp.co.everrise.seminar.LoggingService.GetLog
    {
      "id": 2,
      "message": "I am thirsty.",
      "timestamp": "2022-08-27T16:32:09.943Z"
    },
```
