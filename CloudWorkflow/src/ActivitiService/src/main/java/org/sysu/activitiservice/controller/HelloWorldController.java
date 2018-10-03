package org.sysu.activitiservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloWorldController {

    private final static Logger logger = LoggerFactory.getLogger(HelloWorldController.class);

    @RequestMapping("/helloworld")
    @ResponseBody
    public String helloworld() {
        logger.info("logback test");
        return "hello world";
    }


}
