package jp.ac.tohoku.qse.takahashi.AtcSimulator.interfaces;

import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello")
@Validated
public class HelloService {

        @RequestMapping(path = "/", method = RequestMethod.GET)
        public String hello() {
            return "Hello, World!";
        }

        @RequestMapping(path = "/goodbye", method = RequestMethod.GET)
        public String goodbye(String name) {
            return "Goodbye, World! " + name;
        }
}
