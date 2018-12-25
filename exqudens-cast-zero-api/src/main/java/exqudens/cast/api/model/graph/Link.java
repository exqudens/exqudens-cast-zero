package exqudens.cast.api.model.graph;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Link {

    private Integer source;
    private Integer target;
    private String  destination;
    private String  type;

}
