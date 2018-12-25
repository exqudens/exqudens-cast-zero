package exqudens.cast.server.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice(assignableTypes = AllController.class)
public class AllControllerAdvice {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(AllControllerAdvice.class);
        LOG.trace("");
    }

    private AllControllerAdvice() {
        super();
        LOG.trace("");
    }

    @ExceptionHandler
    @ResponseStatus
    @ResponseBody
    String handleException(Exception exception) {
        LOG.error(exception.getMessage(), exception);
        String stackTrace = "";
        try (StringWriter sw = new StringWriter()) {
            try (PrintWriter pw = new PrintWriter(sw)) {
                exception.printStackTrace(pw);
                stackTrace = sw.toString();
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
        return stackTrace.replace(System.lineSeparator(), "<br/>").trim();
    }

}
