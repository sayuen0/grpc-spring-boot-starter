
package jp.co.everrise.seminar.grpcserver.service;

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
    // ログDB用のカウンタ
    private int counter = 0;
    // ロック(同時書き込みと、読み込み中の書き込みを防ぐオブジェクト)
    private ReadWriteLock lock = new ReentrantReadWriteLock();
    // ログレコードクラス
    public class Log{
        public int id;
        public String message;
        public Timestamp timestamp;

        public Log(int id, String message, Timestamp timestamp){
            this.id = id;
            this.message = message;
            this.timestamp = timestamp;
        }
    }

    @Override
    public void getLog(GetLogRequest request, StreamObserver<GetLogReply> streamObserver){
        Log log = this.getLog(request.getId());
        if (log == null) {
            streamObserver.onNext(null);
            streamObserver.onCompleted();
            return;
        }
        String message = log.message;
        Timestamp timestamp = log.timestamp;
        GetLogReply reply = GetLogReply.newBuilder()
                .setId(request.getId()).setMessage(message).setTimestamp(timestamp).build();
        streamObserver.onNext(reply);
        streamObserver.onCompleted();
    }

    @Override
    public void sendLog(SendLogRequest request, StreamObserver<SendLogReply> streamObserver){
        Log log = this.saveLog(request.getMessage());
        SendLogReply reply = SendLogReply.newBuilder()
                .setId(log.id).setMessage(log.message).setTimestamp(log.timestamp).build();
        streamObserver.onNext(reply);
        streamObserver.onCompleted();
    }

    // log引数からIDの一致するログを見つけて返す
    private Log getLog(int id) {
        this.lock.readLock().lock();
        Log log =  this.logs.get(id);
        this.lock.readLock().unlock();
        return log;
    }

    // ログを作成する
    private Log saveLog(String message) {
        this.lock.writeLock().lock(); // ロックする
        this.counter++;
        Log log = new Log(this.counter, message, fromMillis(currentTimeMillis()));
        this.logs.put(this.counter, log);

        this.lock.writeLock().unlock(); // ロック解除
        return log;
    }
}