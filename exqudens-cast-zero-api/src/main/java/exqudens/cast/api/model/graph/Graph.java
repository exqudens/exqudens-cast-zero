package exqudens.cast.api.model.graph;

import java.util.List;

public class Graph {

    private List<Node> nodes;
    private List<Link> links;

    public Graph() {
    }

    public Graph(List<Node> nodes, List<Link> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public List<Link> getLinks() {
        return links;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

}
