package com.grpcApi.dto;

public class GatewayResponse {

    private String gatewayId;
    private String name;
    private String status;
    private String lastSeen;

    public GatewayResponse(String gatewayId, String name, String status, String lastSeen) {
        this.gatewayId = gatewayId;
        this.name = name;
        this.status = status;
        this.lastSeen = lastSeen;
    }

    public String getGatewayId() {
        return gatewayId;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getLastSeen() {
        return lastSeen;
    }
}