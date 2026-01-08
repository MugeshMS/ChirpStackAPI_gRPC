package com.grpcApi.controller;

import com.grpcApi.service.GatewayGrpcService;
import com.grpcApi.session.GrpcSessionStore;
import io.chirpstack.api.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ui")
public class GatewayUiController {

    private final GatewayGrpcService gatewayService;
    private final GrpcSessionStore sessionStore;

    public GatewayUiController(GatewayGrpcService gatewayService,
                               GrpcSessionStore sessionStore) {
        this.gatewayService = gatewayService;
        this.sessionStore = sessionStore;
    }

    /* ---------------- CONNECT PAGE ---------------- */

    @GetMapping("/connect")
    public String connectPage() {
        return "connect";
    }

    @PostMapping("/gateways")
    public String connect(
            @RequestParam String tenantId,
            @RequestParam String apiKey) {

        sessionStore.setTenantId(tenantId);
        sessionStore.setApiKey(apiKey);

        return "redirect:/ui/gateways/list";
    }

    /* ---------------- GATEWAY LIST ---------------- */

    @GetMapping("/gateways/list")
    public String listGateways(Model model) {

        List<GatewayListItem> gateways =
                gatewayService.listGateways();

        model.addAttribute("gateways", gateways);
        return "gateway-list";
    }

    /* ---------------- GATEWAY DETAILS ---------------- */

    @GetMapping("/gateways/{id}")
    public String gatewayDetails(
            @PathVariable String id,
            Model model) {

        // --------------------------------------------------
        // 1️⃣ Gateway basic details
        // --------------------------------------------------
        GetGatewayResponse response = gatewayService.getGatewayById(id);
        Gateway gateway = response.getGateway();

        String lastSeenReadable = "Never";
        if (response.hasLastSeenAt()) {
            Instant instant = Instant.ofEpochSecond(
                    response.getLastSeenAt().getSeconds(),
                    response.getLastSeenAt().getNanos()
            );
            lastSeenReadable = instant
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .toString();
        }

        // --------------------------------------------------
        // 2️⃣ Gateway metrics
        // --------------------------------------------------
        GetGatewayMetricsResponse metrics =
                gatewayService.getGatewayMetricsInternal(id);

        // --------------------------------------------------
        // 3️⃣ RX / TX packets over time
        // --------------------------------------------------
        List<String> labels = new ArrayList<>();
        List<Double> rxValues = new ArrayList<>();
        List<Double> txValues = new ArrayList<>();

        if (metrics.getRxPackets().getDatasetsCount() > 0) {
            for (var ts : metrics.getRxPackets().getTimestampsList()) {
                labels.add(
                        Instant.ofEpochSecond(ts.getSeconds())
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .toString()
                );
            }

            var rxDataset = metrics.getRxPackets().getDatasets(0);
            for (double v : rxDataset.getDataList()) {
                rxValues.add(v);
            }
        }

        if (metrics.getTxPackets().getDatasetsCount() > 0) {
            var txDataset = metrics.getTxPackets().getDatasets(0);
            for (double v : txDataset.getDataList()) {
                txValues.add(v);
            }
        }

        // --------------------------------------------------
        // 4️⃣ RX packets per Frequency (AGGREGATED)
        // --------------------------------------------------
        Metric rxPerFreq = metrics.getRxPacketsPerFreq();
        List<String> rxFreqLabels = new ArrayList<>();
        List<Double> rxFreqValues = new ArrayList<>();

        for (var ds : rxPerFreq.getDatasetsList()) {
            rxFreqLabels.add(ds.getLabel());

            double sum = 0;
            for (double v : ds.getDataList()) {
                sum += v;
            }
            rxFreqValues.add(sum);
        }

        // --------------------------------------------------
        // 5️⃣ TX packets per Frequency (AGGREGATED)
        // --------------------------------------------------
        Metric txPerFreq = metrics.getTxPacketsPerFreq();
        List<String> txFreqLabels = new ArrayList<>();
        List<Double> txFreqValues = new ArrayList<>();

        for (var ds : txPerFreq.getDatasetsList()) {
            txFreqLabels.add(ds.getLabel());

            double sum = 0;
            for (double v : ds.getDataList()) {
                sum += v;
            }
            txFreqValues.add(sum);
        }

        // --------------------------------------------------
        // 6️⃣ RX packets per Data Rate (AGGREGATED)
        // --------------------------------------------------
        Metric rxPerDr = metrics.getRxPacketsPerDr();
        List<String> rxDrLabels = new ArrayList<>();
        List<Double> rxDrValues = new ArrayList<>();

        for (var ds : rxPerDr.getDatasetsList()) {
            rxDrLabels.add(ds.getLabel());

            double sum = 0;
            for (double v : ds.getDataList()) {
                sum += v;
            }
            rxDrValues.add(sum);
        }

        // --------------------------------------------------
        // 7️⃣ TX packets per Data Rate (AGGREGATED)
        // --------------------------------------------------
        Metric txPerDr = metrics.getTxPacketsPerDr();
        List<String> txDrLabels = new ArrayList<>();
        List<Double> txDrValues = new ArrayList<>();

        for (var ds : txPerDr.getDatasetsList()) {
            txDrLabels.add(ds.getLabel());

            double sum = 0;
            for (double v : ds.getDataList()) {
                sum += v;
            }
            txDrValues.add(sum);
        }

        // --------------------------------------------------
        // 8️⃣ Add to model
        // --------------------------------------------------
        model.addAttribute("gateway", gateway);
        model.addAttribute("lastSeenAt", lastSeenReadable);

        model.addAttribute("labels", labels);
        model.addAttribute("rxValues", rxValues);
        model.addAttribute("txValues", txValues);

        model.addAttribute("rxFreqLabels", rxFreqLabels);
        model.addAttribute("rxFreqValues", rxFreqValues);

        model.addAttribute("txFreqLabels", txFreqLabels);
        model.addAttribute("txFreqValues", txFreqValues);

        model.addAttribute("rxDrLabels", rxDrLabels);
        model.addAttribute("rxDrValues", rxDrValues);

        model.addAttribute("txDrLabels", txDrLabels);
        model.addAttribute("txDrValues", txDrValues);

        return "gateway-details";
    }



    /* ---------------- DELETE GATEWAY ---------------- */

    @PostMapping("/gateways/{id}/delete")
    public String deleteGateway(@PathVariable String id) {

        gatewayService.deleteGateway(id);
        return "redirect:/ui/gateways/list";
    }

    /* ---------------- CREATE GATEWAY ---------------- */

    @GetMapping("/create")
    public String showCreateGatewayPage() {
        return "gateway-create";
    }

    // 🔹 Handle Create Gateway Form
    @PostMapping("/create")
    public String createGateway(
            @RequestParam String gatewayId,
            @RequestParam String name,
            @RequestParam String description
    ) {
        gatewayService.createGateway(
                gatewayId,
                name,
                description,
                null // tenant already stored in session
        );

        return "redirect:/ui/gateways/list";
    }

}

