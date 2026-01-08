package com.grpcApi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.grpcApi.service.GatewayGrpcService;

@SpringBootApplication
public class ChirpstackGrpcApplication {

	public static void main(String[] args) {
		SpringApplication.run(ChirpstackGrpcApplication.class, args);
	}
//	Get All Gateways
//	 @Bean
//	    CommandLineRunner run(GatewayGrpcService gatewayGrpcService) {
//	        return args -> {
//	            gatewayGrpcService.listGateways();
//	        };
//	    }
//	Get Gateway By Id
//	@Bean
//	CommandLineRunner run(GatewayGrpcService service) {
//		return args -> {
//			service.getGatewayById("9f1c2a6e4e9b4a6c");
//		};
//	}
// Create Gateway
//	@Bean
//	CommandLineRunner run(GatewayGrpcService service) {
//		return args -> {
//
//			service.createGateway(
//					"0102030405060709",          // NEW gateway ID
//					"Gateway-Created-By-API",    // Name
//					"Created using gRPC API",    // Description
//					"3528cf43-9f9c-4414-8702-0aea4c80bb9d" // Tenant ID
//			);
//
//		};
//	}
//	Update Gateway
//	@Bean
//	CommandLineRunner run(GatewayGrpcService service) {
//		return args -> {
//
//			service.updateGatewayName(
//					"0102030405060709", // existing gateway ID
//					"Updated-Gateway-Name",
//					"Updated via gRPC API"
//			);
//
//		};
//	}

//	Delete Gateway..
//	@Bean
//	CommandLineRunner run(GatewayGrpcService service){
//		return  args ->{
//			service.deleteGateway("0102030405060709");
//		};
//		}


//	Get Gateway Metrics
//	@Bean
//	CommandLineRunner run(GatewayGrpcService service) {
//		return args -> {
//
//			service.getGatewayMetrics("9f1c2a6e4e9b4a6c");
//
//		};
//}

// Duty-cycle Metrics
//@Bean
//CommandLineRunner run(GatewayGrpcService service) {
//	return args -> {
//
//		service.getGatewayDutyCycleMetrics("9f1c2a6e4e9b4a6c");
//
//	};
//}




}
