package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import de.fraunhofer.isst.configmanager.petrinet.simulator.PetriNetSimulator;
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

        check: for (final var path: paths) {
            int offset;
            if(PetriNetSimulator.circleFree(path)){
                if(!path.get(0).equals(node)) continue;
                if (path.size() % 2 == 1) {
                    offset = 1;
                }else {
                    offset = 2;
                }
                for (var i = 0; i < path.size() - offset; i += 2) {
                    var res1 = parameter1.evaluate(path.get(i), paths);
                    var res2 = parameter2.evaluate(path.get(i), paths);
                    if(res2) continue check;
                    if(!res1) return false;
                }
                if (!parameter2.evaluate(path.get(path.size() - offset), paths)) {
                    return false;
                }
            }else{
                //TODO path contains circle
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
