package org.example.osgi.spring.boot.demo.one.controller;

import org.example.osgi.spring.boot.demo.one.service.DemoOneService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DemoOneController {
	
	@Lazy
	@Autowired
	private DemoOneService service;
	
	@GetMapping(path = "/demo-one")
	public String demo() {
		return "Demo One";
	}

	@GetMapping(path = "/service")
	public String service() {
		return service.count();
	}
}
