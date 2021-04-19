package com.springboot.springbott;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.Ordered;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@EntityScan(basePackages = "com.springboot.model")
@ComponentScan(basePackages = {"com.*"})
@EnableJpaRepositories(basePackages = {"com.springboot.repository"})
@EnableTransactionManagement
@EnableWebMvc
public class SpringbotApplication implements WebMvcConfigurer{

	public static void main(String[] args) {
		SpringApplication.run(SpringbotApplication.class, args);
		
		/*
		//Criptografa senha
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		String result = encoder.encode("36360");
		System.out.println(result);
	*/
	}
	
	@Override
	public void addViewControllers(ViewControllerRegistry registry) {
		registry.addViewController("/login").setViewName("/login");
		registry.setOrder(Ordered.LOWEST_PRECEDENCE);
	}

}