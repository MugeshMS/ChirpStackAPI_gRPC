package com.grpcApi.service;

import com.google.protobuf.Timestamp;
import io.chirpstack.api.*;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.grpcApi.session.GrpcSessionStore;

import java.time.Instant;
import java.util.List;

@Service
public class GatewayGrpcService {

//    @Value("${chirpstack.tenant.id}")
    private  String tenantId;

    private final GatewayServiceGrpc.GatewayServiceBlockingStub gatewayStub;
    private final GrpcSessionStore sessionStore;

    public GatewayGrpcService(
            GatewayServiceGrpc.GatewayServiceBlockingStub gatewayStub,GrpcSessionStore sessionStore) {
        this.gatewayStub = gatewayStub;
        this.sessionStore=sessionStore;
        System.out.println(sessionStore.getTenantId());

    }

    public List<GatewayListItem> listGateways() {

        ListGatewaysRequest request =
                ListGatewaysRequest.newBuilder()
                        .setLimit(10)
                        .setTenantId(sessionStore.getTenantId())
                        .build();

        ListGatewaysResponse response = gatewayStub.list(request);

        System.out.println("Total gateways: " + response.getTotalCount());

        for (GatewayListItem item : response.getResultList()) {
            System.out.println("-------------------------");
            System.out.println("Gateway ID : " + item.getGatewayId());
            System.out.println("Name       : " + item.getName());
            System.out.println("State      : " + item.getState());
            System.out.println("Properties : " + item.getProperties());
            System.out.println("Last Seen  : " + item.getLastSeenAt());
        }

        return response.getResultList();
    }



    public GetGatewayResponse getGatewayById(String gatewayId) {

        // 1. Build request
        GetGatewayRequest request = GetGatewayRequest.newBuilder()
                .setGatewayId(gatewayId)
                .build();

        // 2. Call ChirpStack via gRPC
        GetGatewayResponse response = gatewayStub.get(request);

        // 3. Read response
        Gateway gateway = response.getGateway();

        System.out.println("Gateway ID   : " + gateway.getGatewayId());
        System.out.println("Name         : " + gateway.getName());
        System.out.println("Tenant ID   : " + gateway.getTenantId());
        System.out.println("Description : " + gateway.getDescription());
        Instant createdAt =
                Instant.ofEpochSecond(
                        response.getCreatedAt().getSeconds(),
                        response.getCreatedAt().getNanos()
                );

        Instant updatedAt =
                Instant.ofEpochSecond(
                        response.getUpdatedAt().getSeconds(),
                        response.getUpdatedAt().getNanos()
                );

        System.out.println("Created At  : " + createdAt);
        System.out.println("Updated At  : " + updatedAt);


//        System.out.println("Created At  : " + response.getCreatedAt());
//        System.out.println("Updated At  : " + response.getUpdatedAt());
        System.out.println("Last Seen   : " + response.getLastSeenAt());
        return response;
    }

    // Create Gateway
    public void createGateway(
            String gatewayId,
            String name,
            String description,
            String tenantId
    ) {

        // 1. Build Gateway object
        Gateway gateway = Gateway.newBuilder()
                .setGatewayId(gatewayId)
                .setName(name)
                .setDescription(description)
                .setTenantId(sessionStore.getTenantId())
                .setStatsInterval(30) // seconds
                .build();

        // 2. Wrap in Create request
        CreateGatewayRequest request =
                CreateGatewayRequest.newBuilder()
                        .setGateway(gateway)
                        .build();

        // 3. Call ChirpStack
        gatewayStub.create(request);

        System.out.println("Gateway created successfully: " + gatewayId);
    }


    // update Gateway Request

    public void updateGatewayName(
            String gatewayId,
            String newName,
            String newDescription
    ) {

        // 1. Fetch existing gateway
        GetGatewayRequest getRequest =
                GetGatewayRequest.newBuilder()
                        .setGatewayId(gatewayId)
                        .build();

        GetGatewayResponse getResponse =
                gatewayStub.get(getRequest);

        Gateway existingGateway = getResponse.getGateway();

        // 2. Create updated gateway (copy + modify)
        Gateway updatedGateway =
                Gateway.newBuilder(existingGateway)
                        .setName(newName)
                        .setDescription(newDescription)
                        .build();

        // 3. Wrap in Update request
        UpdateGatewayRequest updateRequest =
                UpdateGatewayRequest.newBuilder()
                        .setGateway(updatedGateway)
                        .build();

        // 4. Call ChirpStack
        gatewayStub.update(updateRequest);

        System.out.println("Gateway updated successfully: " + gatewayId);
    }
//    Delete Gateway
    public void deleteGateway(String gatewayId){
        DeleteGatewayRequest deleteGatewayRequest =
                DeleteGatewayRequest.newBuilder()
                        .setGatewayId(gatewayId)
                        .build();
        gatewayStub.delete(deleteGatewayRequest);

        System.out.println("Gateway with Id :"+gatewayId+" deleted Successfull");
    }
    // RX/TX Packet Metrics
    public void getGatewayMetrics(String gatewayId) {

        // 1. Define time range (last 1 hour)
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);

        Timestamp start =
                Timestamp.newBuilder()
                        .setSeconds(oneHourAgo.getEpochSecond())
                        .build();

        Timestamp end =
                Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .build();

        // 2. Build request
        GetGatewayMetricsRequest request =
                GetGatewayMetricsRequest.newBuilder()
                        .setGatewayId(gatewayId)
                        .setStart(start)
                        .setEnd(end)
                        .setAggregation(Aggregation.MINUTE)
                        .build();

        // 3. Call ChirpStack
        GetGatewayMetricsResponse response =
                gatewayStub.getMetrics(request);

        // 4. Print metrics
        System.out.println("RX Packets : " + response.getRxPackets());
        System.out.println("TX Packets : " + response.getTxPackets());
    }

//    Duty-Cycle Metrics

    public void getGatewayDutyCycleMetrics(String gatewayId) {

        // 1. Define time range (last 1 hour)
        Instant now = Instant.now();
        Instant oneHourAgo = now.minusSeconds(3600);

        Timestamp start =
                Timestamp.newBuilder()
                        .setSeconds(oneHourAgo.getEpochSecond())
                        .build();

        Timestamp end =
                Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .build();

        // 2. Build request
        GetGatewayDutyCycleMetricsRequest request =
                GetGatewayDutyCycleMetricsRequest.newBuilder()
                        .setGatewayId(gatewayId)
                        .setStart(start)
                        .setEnd(end)
                        .build();

        // 3. Call ChirpStack
        GetGatewayDutyCycleMetricsResponse response =
                gatewayStub.getDutyCycleMetrics(request);

        // 4. Print results
        System.out.println("Max Load %     : " + response.getMaxLoadPercentage());
        System.out.println("Window Load %  : " + response.getWindowPercentage());
    }
    public GetGatewayMetricsResponse getGatewayMetricsInternal(String gatewayId) {

        Instant now = Instant.now();
        Instant startTime = now.minusSeconds(24 * 60 * 60); // last 24 hours

        Timestamp start =
                Timestamp.newBuilder()
                        .setSeconds(startTime.getEpochSecond())
                        .build();

        Timestamp end =
                Timestamp.newBuilder()
                        .setSeconds(now.getEpochSecond())
                        .build();

        GetGatewayMetricsRequest request =
                GetGatewayMetricsRequest.newBuilder()
                        .setGatewayId(gatewayId)
                        .setStart(start)
                        .setEnd(end)
                        .setAggregation(Aggregation.HOUR)
                        .build();

        return gatewayStub.getMetrics(request);
    }




}
