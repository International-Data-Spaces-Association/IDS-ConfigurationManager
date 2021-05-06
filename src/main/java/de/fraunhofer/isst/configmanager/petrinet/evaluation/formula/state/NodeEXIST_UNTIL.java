package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class NodeEXIST_UNTIL implements StateFormula {

    private StateFormula parameter1;
    private StateFormula parameter2;

    public static NodeEXIST_UNTIL nodeEXIST_UNTIL(final StateFormula parameter1,
                                                  final StateFormula parameter2) {
        return new NodeEXIST_UNTIL(parameter1, parameter2);
    }

    @Override
    // True if a path exists, where parameter1 is true on each node of the path,
    // and parameter2 is true on the final node of the path
    //TODO fix evaluation: use filtered paths
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        if (!(node instanceof Place)) {
            return false;
        }

        check: for (final var path: paths) {
            if (path.get(0).equals(node) && path.size() % 2 == 1) {
                for (var i = 0; i < path.size() - 1; i += 2) {
                    if(!parameter1.evaluate(path.get(i), paths)) {
                        continue check;
                    }
                }

                if (parameter2.evaluate(path.get(path.size() - 1), paths)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String symbol() {
        return "EXIST_UNTIL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
