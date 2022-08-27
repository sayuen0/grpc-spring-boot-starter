# 3. gRPCメソッドの追加

- `grpccommon`モジュール内で、新規にLoggingサービスを作成
- `grpcserver`モジュール内で、Loggingサービス用のサービスハンドラを作成
- サーバにLoggingリクエストを送る

## `grpccommon`モジュール内で、新規にFizzBuzzメソッドを作成

作業ディレクトリの移動
```
cd /app/grpccommon
```

Loggingサービス用のprotoファイルを新規作成

```
$ touch src/main/proto/loggingService.proto
```

`loggingService.proto`を以下のように記述

```
```
