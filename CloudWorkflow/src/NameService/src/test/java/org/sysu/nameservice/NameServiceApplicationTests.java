package org.sysu.nameservice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.sysu.nameservice.service.ActivitiService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class NameServiceApplicationTests {
    @Autowired
    ActivitiService activitiService;

    @Test
    public void contextLoads() {
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
        System.out.println(activitiService.getHelloworld());
    }

}
