package com.grpcApi.config;

import com.grpcApi.grpc.AuthClientInterceptor;
import com.grpcApi.session.GrpcSessionStore;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ClientInterceptors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GrpcClientConfig {



     @Value("${chirpstack.port}")
    private int port;
     @Value("${chirpstack.IP}")
     private String IP;
    private final GrpcSessionStore store;

    public GrpcClientConfig(GrpcSessionStore store) {
        this.store = store;

    }
    @Bean
    public Channel chirpstackChannel() {

        ManagedChannel baseChannel =
                ManagedChannelBuilder
                        .forAddress(IP, port)
                        .usePlaintext()
                        .build();

        return ClientInterceptors.intercept(
                baseChannel,
                new AuthClientInterceptor(store)
        );
    }
}