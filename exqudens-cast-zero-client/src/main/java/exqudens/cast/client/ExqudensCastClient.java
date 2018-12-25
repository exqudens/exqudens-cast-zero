package exqudens.cast.client;

import exqudens.cast.api.model.graph.Graph;

public interface ExqudensCastClient {

    static ExqudensCastClient newInstance(String host, Integer port, String name, Integer connectTimeout) {
        return new ExqudensCastClientImpl(host, port, name, connectTimeout);
    }

    Graph apiGraphOrder(Graph graph);

}
