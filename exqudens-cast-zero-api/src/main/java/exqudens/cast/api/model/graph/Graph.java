package exqudens.cast.api.model.graph;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Graph {

    private List<Node> nodes;
    private List<Link> links;

}
