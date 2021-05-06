package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class TransitionFORALL_UNTIL implements TransitionFormula {
    private TransitionFormula parameter1;
    private TransitionFormula parameter2;

    public static TransitionFORALL_UNTIL transitionFORALL_UNTIL(final TransitionFormula parameter1,
                                                                final TransitionFormula parameter2){
        return new TransitionFORALL_UNTIL(parameter1, parameter2);
    }

    //like EXIST_UNTIL but requires conditions for all paths
    //TODO fix evaluation: use filtered paths
    @Override
    public boolean evaluate(final Node node, final List<List<Node>> paths) {
        if (!(node instanceof Transition)) {
            return false;
        }

        for (final var path: paths) {
            if (path.get(0).equals(node) && path.size() % 2 == 1) {
                for (var i = 0; i < path.size() - 1; i += 2) {
                    if (!parameter1.evaluate(path.get(i), paths)) {
                        return false;
                    }
                }

                if (!parameter2.evaluate(path.get(path.size() - 1), paths)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public String symbol() {
        return "FORALL_UNTIL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s, %s)", symbol(), parameter1.writeFormula(), parameter2.writeFormula());
    }
}
