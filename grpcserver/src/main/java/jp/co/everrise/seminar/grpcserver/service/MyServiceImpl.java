package jp.co.everrise.seminar.grpcserver.service;

import io.grpc.stub.StreamObserver;
import jp.co.everrise.seminar.proto.*;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
public class MyServiceImpl extends MyServiceGrpc.MyServiceImplBase {
   @Override
    public void sayHello(HelloRequest request, StreamObserver<HelloReply> responseObserver) {
        HelloReply reply = HelloReply.newBuilder()
                .setMessage("Hello," + request.getName())
                .build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    @Override
    public void fizzBuzz(FizzBuzzRequest request, StreamObserver<FizzBuzzReply> responseObserver) {
        // リクエストから数値が渡ってくるので、それに対してFizzBuzzの回答を返す
        // 3 → "fizz" 15 → "fizzBuzz" 4 → "4"
        FizzBuzzReply reply = FizzBuzzReply.newBuilder()
                .setAnswer("Hello: " + request.getName() + ", FizzBuzz: " + this.getFizzBuzzAnswer(request.getNumber())).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }

    private String getFizzBuzzAnswer(int number) {
        if (number % 15 == 0) {
                return "FizzBuzz";
        }else if(number % 5 == 0) {
                return "Buzz";
        }else if(number % 3 == 0) {
                return "Fizz";
        } 
        return String.valueOf(number);
    }
}