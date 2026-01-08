package com.grpcApi.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.chirpstack.api.GatewayServiceGrpc;
import io.grpc.Channel;

@Configuration
public class GatewayGrpcStubConfig {

    @Bean
    public GatewayServiceGrpc.GatewayServiceBlockingStub gatewayServiceStub(
            Channel chirpstackChannel) {

        return GatewayServiceGrpc.newBlockingStub(chirpstackChannel);
    }
}
