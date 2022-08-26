FROM eclipse-temurin:11-jdk-focal

# grpcurl: curlを模したgRPCクライアントをインストール
RUN wget https://github.com/fullstorydev/grpcurl/releases/download/v1.8.7/grpcurl_1.8.7_linux_x86_64.tar.gz && \
    tar -xf grpcurl_1.8.7_linux_x86_64.tar.gz && \
    rm grpcurl_1.8.7_linux_x86_64.tar.gz && \
    mv ./grpcurl /usr/local/bin

WORKDIR /app
COPY . ./
