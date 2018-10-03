package org.sysu.nameservice.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HelloWorldController {

    private static Logger logger = LoggerFactory.getLogger(HelloWorldController.class);

    @RequestMapping("/helloworld")
    @ResponseBody
    public String helloworld() {
        logger.info("hello world");
        return "hello world";
    }
}
