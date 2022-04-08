package org.example.community.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/alpha")
public class AplaController {
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "hello World";
    }

}
