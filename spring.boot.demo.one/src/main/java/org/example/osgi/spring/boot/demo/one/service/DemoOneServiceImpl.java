package org.example.osgi.spring.boot.demo.one.service;

import org.example.osgi.spring.boot.demo.one.DemoOneApplication;
import org.example.osgi.spring.boot.demo.one.repository.DemoOneRepository;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Component(
	property = {
		"service.exported.interfaces=*",
		"aries.rsa.port=8202"
	})
@Service
public class DemoOneServiceImpl implements DemoOneService {
	
	@Override
	public String count() {
		return "DemoOneService repository.count() = " + repository.count();
	}
	
	@Lazy
	@Autowired
	private DemoOneRepository repository;
	
	@Activate
	protected void activate(BundleContext bundleContext) {
		System.out.println("********************************");
		System.out.println("* @Activate DemoOneServiceImpl *");
		System.out.println("********************************");
		repository = DemoOneApplication.appContext.getBean(DemoOneRepository.class);
	}


}
