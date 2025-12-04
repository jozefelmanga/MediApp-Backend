package com.mediapp.doctor_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(excludeName = {
		"org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration",
		"org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
public class DoctorServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DoctorServiceApplication.class, args);
	}

}
