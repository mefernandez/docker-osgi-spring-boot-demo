package org.example.osgi.spring.boot.demo.one;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

public class ServletInitializer extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		System.out.println("Demo Three ServletInitializer");
		return application.sources(DemoOneApplication.class);
	}

}
