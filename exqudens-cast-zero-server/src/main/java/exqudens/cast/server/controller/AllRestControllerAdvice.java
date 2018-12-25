package exqudens.cast.server.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(assignableTypes = AllRestController.class)
public class AllRestControllerAdvice {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(AllRestControllerAdvice.class);
        LOG.trace("");
    }

    private AllRestControllerAdvice() {
        super();
        LOG.trace("");
    }

    @ExceptionHandler
    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
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
