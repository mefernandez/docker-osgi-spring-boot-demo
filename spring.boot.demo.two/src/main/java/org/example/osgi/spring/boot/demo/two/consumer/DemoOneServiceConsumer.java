package org.example.osgi.spring.boot.demo.two.consumer;

import org.example.osgi.spring.boot.demo.one.service.DemoOneService;
import org.example.osgi.spring.boot.demo.two.DemoTwoApplication;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(immediate=true)
public class DemoOneServiceConsumer {

    DemoOneService service;

    @Activate
    public void activate() {
        System.out.println("************************************");
        System.out.println("* @Activate DemoOneServiceConsumer *");
        System.out.println("************************************");
    }

    @Reference
    public void setDemoOneService(DemoOneService demoOneService) {
        System.out.println("*************************************");
        System.out.println("* @Reference DemoOneServiceConsumer *");
        System.out.println("*************************************");
        this.service = demoOneService;
        DemoTwoApplication.appContext.getBeanFactory().registerSingleton("demoOneService", demoOneService);
    }
    
}
