package com.example.ProductComparison;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class ProductComparisonApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductComparisonApplication.class, args);
	}

}
