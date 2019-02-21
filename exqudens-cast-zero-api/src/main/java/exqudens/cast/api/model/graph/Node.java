package exqudens.cast.api.model.graph;

import java.util.Map;

public class Node {

    private String              type;
    private Map<String, Object> data;

    public Node() {
    }

    public Node(String type, Map<String, Object> data) {
        this.type = type;
        this.data = data;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

}
