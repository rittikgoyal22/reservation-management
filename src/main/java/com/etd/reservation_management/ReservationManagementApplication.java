package com.etd.reservation_management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class ReservationManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservationManagementApplication.class, args);
	}

}
