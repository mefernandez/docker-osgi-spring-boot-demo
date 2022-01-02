package org.example.osgi.spring.boot.demo.one;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.osgi.io.OsgiBundleResourcePatternResolver;

@SpringBootApplication
public class DemoOneApplication implements BundleActivator {

	public static void main(String[] args) {
		SpringApplication.run(DemoOneApplication.class, args);
	}

	public static ConfigurableApplicationContext appContext;

	@Override
	public void start(BundleContext context) throws Exception {
		TomcatURLStreamHandlerFactory.disable();
		
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        
        // trick to enable scan: get osgi resource pattern resolver
        // @see https://www.it-swarm-es.com/es/osgi/se-puede-usar-spring-boot-con-osgi-si-no-hay-planes-para-tener-un-arranque-de-primavera-osgi/1053027058/
        OsgiBundleResourcePatternResolver resourceResolver = new OsgiBundleResourcePatternResolver(context.getBundle());
        appContext = new SpringApplication(resourceResolver, DemoOneApplication.class).run();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		SpringApplication.exit(appContext, () -> 0);
	}

}
