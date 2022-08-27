
package jp.co.everrise.seminar.grpcserver.service;

import io.grpc.stub.StreamObserver;
import jp.co.everrise.seminar.proto.*;
import net.devh.boot.grpc.server.service.GrpcService;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

@GrpcService
public class LoggingServiceImpl extends LoggingServiceGrpc.LoggingServiceImplBase {
    @Override
    public void getLog(GetLogRequest request, StreamObserver<GetLogReply> streamObserver){
        GetLogReply reply = this.getLogMessage(request.getId());
        streamObserver.onNext(reply);
        streamObserver.onCompleted();        
    }

    // logファイルからIDの一致するログを見つけて返す
    private GetLogReply getLogMessage(int id) {

        try(FileReader reader = new FileReader("logs.json"))
        {
            JSONParser jsonParser = new JSONParser();
            Object obj = jsonParser.parse(reader);
            JSONArray logs = (JSONArray) obj;
            String message = "";
            for (Object log: logs){
                int logId = (JSONObject) log.get("id");
                if (logId == id) {
                    message = (JSONObject) log.get("message");
                    break;
                }
            }
            // Object logObj = logs.find(log -> return (JSONObject) log.get("id") == id;);
            // String message = (JSONObject) logObj.get("message");
            GetLogReply reply = GetLogReply.newBuilder()
                .setId(id).setMessage(message).build();
            return reply;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}