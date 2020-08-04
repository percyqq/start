package org.learn.web;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @create: 2020-07-24 16:34
 */
@RestController
public class AController {

    @RequestMapping("/st")
    public String a() {
        return "wtf.";
    }
}
