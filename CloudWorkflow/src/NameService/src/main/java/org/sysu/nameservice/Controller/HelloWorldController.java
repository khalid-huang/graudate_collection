package org.sysu.nameservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sysu.nameservice.service.ActivitiService;

import java.util.Map;

@RestController
public class HelloWorldController {

    private static Logger logger = LoggerFactory.getLogger(HelloWorldController.class);

//    @RequestMapping("/helloworld")
//    @ResponseBody
//    public String helloworld() {
//        logger.info("hello world");
//        return "hello world";
//    }

    @Autowired
    ActivitiService activitiService;

    @RequestMapping(value = "getHelloworld")
    public ResponseEntity<?> getHelloworld() {
        System.out.println("ns hello world");
        return activitiService.getHelloworld();
    }

    @RequestMapping(value = "helloworld", method = RequestMethod.POST)
    public ResponseEntity<?> helloworld(@RequestBody(required = false)Map<String,Object> variables) {
        System.out.println("ns hello world");
        System.out.println(variables);
        return activitiService.helloworld(variables);
    }

}
