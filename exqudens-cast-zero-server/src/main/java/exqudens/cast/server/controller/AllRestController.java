package exqudens.cast.server.controller;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import exqudens.cast.api.model.Item;
import exqudens.cast.api.model.Order;
import exqudens.cast.api.model.User;
import exqudens.cast.api.model.graph.Graph;
import exqudens.cast.api.util.Graphs;

@RestController
public class AllRestController {

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(AllRestController.class);
        LOG.trace("");
    }

    private AllRestController() {
        super();
        LOG.trace("");
    }

    @RequestMapping(
        value = "/api/graph/order",
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
        method = RequestMethod.POST
    )
    @ResponseStatus(value = HttpStatus.OK)
    Graph apiGraphOrder(@RequestBody Graph request) {
        LOG.trace("");
        List<Order> orders = Graphs.graphToList(request, Order.class);
        orders.get(1).getItems().add(new Item("description_4", orders.get(1)));
        Graph response = Graphs.listToGraph(
            Object.class,
            orders.stream().map(Object.class::cast).collect(Collectors.toList()),
            System::identityHashCode,
            Arrays.asList(User.class, Order.class, Item.class),
            null,
            null
        );
        return response;
    }

}
