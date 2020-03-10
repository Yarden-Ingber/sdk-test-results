package com.yarden.restServiceDemo;

import com.yarden.restServiceDemo.reportService.VGStatusPageRequestTimer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RestServiceDemoApplication {

	public static void main(String[] args) {
		VGStatusPageRequestTimer.start();
		SpringApplication.run(RestServiceDemoApplication.class, args);
	}

}
