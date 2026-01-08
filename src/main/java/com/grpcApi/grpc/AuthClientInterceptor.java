package com.grpcApi.grpc;

import com.grpcApi.session.GrpcSessionStore;
import io.grpc.*;

public class AuthClientInterceptor implements ClientInterceptor {

    private final GrpcSessionStore store;

    public AuthClientInterceptor(GrpcSessionStore store) {
        this.store = store;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(
                next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                String apiKey = store.getApiKey();

                if (apiKey != null && !apiKey.isEmpty()) {
                    Metadata.Key<String> authKey =
                            Metadata.Key.of("authorization",
                                    Metadata.ASCII_STRING_MARSHALLER);

                    headers.put(authKey, "Bearer " + apiKey);
                }

                super.start(responseListener, headers);
            }
        };
    }
}
