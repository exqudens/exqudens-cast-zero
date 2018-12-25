package exqudens.cast.server.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class AllController {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(AllController.class);
        LOG.trace("");
    }

    private AllController() {
        super();
        LOG.trace("");
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    String get() throws Exception {
        LOG.trace("");
        String responseBody = this.getClass().getName();
        return responseBody;
    }

}
