package com.yarden.restServiceDemo;

import com.yarden.restServiceDemo.reportService.VisualGridStatusPageRequestTimer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestServiceDemoApplication {

	public static void main(String[] args) {
		VisualGridStatusPageRequestTimer.start();
		SpringApplication.run(RestServiceDemoApplication.class, args);
	}

}
