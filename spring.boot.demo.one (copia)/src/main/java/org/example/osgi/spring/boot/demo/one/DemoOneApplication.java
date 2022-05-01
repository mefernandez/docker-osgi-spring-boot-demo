package org.example.osgi.spring.boot.demo.one;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.apache.catalina.webresources.TomcatURLStreamHandlerFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.http.HttpContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
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
		System.out.println("DemoOneApplication.start(BundleContext context)");
		
		TomcatURLStreamHandlerFactory.disable();
		
        Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());
        
        // trick to enable scan: get osgi resource pattern resolver
        // @see https://www.it-swarm-es.com/es/osgi/se-puede-usar-spring-boot-con-osgi-si-no-hay-planes-para-tener-un-arranque-de-primavera-osgi/1053027058/
        OsgiBundleResourcePatternResolver resourceResolver = new OsgiBundleResourcePatternResolver(context.getBundle());
        appContext = new SpringApplication(resourceResolver, DemoOneApplication.class).run();
        HttpServlet springServlet = appContext.getBean(HttpServlet.class);
//		System.out.println("DemoOneApplication HttpServlet.name:    " + springServlet.getServletName());
//		System.out.println("DemoOneApplication HttpServlet.context: " + springServlet.getServletContext());
		registerServlet(context, springServlet);
	}
	
	private void registerServlet(BundleContext context, HttpServlet springServlet) throws ServletException, NamespaceException {
//		System.out.println("DemoOneApplication.start(BundleContext context)");
//		ServiceReference<WebContainerService> webContainerRef = context.getServiceReference(WebContainerService.class);
//		WebContainerService service = context.getService(webContainerRef);
//		System.out.println("DemoOneApplication WebContainerService: " + service);

		ServiceReference<HttpService> httpServiceRef = context.getServiceReference(HttpService.class);
		System.out.println("DemoOneApplication HttpService reference: " + httpServiceRef);
		HttpService service = context.getService(httpServiceRef);
		System.out.println("DemoOneApplication HttpService: " + service);
		
		System.out.println("DemoOneApplication HttpService.registerServlet /one-servlet");
		HttpContext httpContext = service.createDefaultHttpContext();
		service.registerServlet("/one-servlet", springServlet, null, httpContext);
}

	@Override
	public void stop(BundleContext context) throws Exception {
		System.out.println("DemoOneApplication.start(BundleContext context)");
		ServiceReference<HttpService> httpServiceRef = context.getServiceReference(HttpService.class);
		HttpService service = context.getService(httpServiceRef);
		System.out.println("DemoOneApplication HttpService.unregister /one-servlet");
		service.unregister("/one-servlet");
		
		SpringApplication.exit(appContext, () -> 0);
	}

}
