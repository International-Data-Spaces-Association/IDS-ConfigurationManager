package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import de.fraunhofer.isst.configmanager.petrinet.simulator.PetriNetSimulator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * evaluates to true, if on any possible path every transition fulfills parameter1, until a transition fulfills parameter2
 */
@AllArgsConstructor
@Slf4j
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
            if(!path.get(0).equals(node)) continue;
            int offset;
            if(PetriNetSimulator.circleFree(path)){
                if (path.size() % 2 == 1) {
                    offset = 1;
                }else {
                    offset = 2;
                }
                for (var i = 2; i < path.size() - offset; i += 2) {
                    var res1 = parameter1.evaluate(path.get(i), paths);
                    var res2 = parameter2.evaluate(path.get(i), paths);
                    if(res2) continue check;
                    if(!res1){
                        log.info(path.get(i).toString());
                        return false;
                    }
                }
                if (!parameter2.evaluate(path.get(path.size() - offset), paths)) {
                    log.info(path.get(path.size() - offset).toString());
                    return false;
                }
            }else{
                //if something on the circle fulfills param2 accept, if something does not fulfill param1 reject
                for (var i = 2; i<path.size() - 1; i+=2){
                    var res1 = parameter1.evaluate(path.get(i), paths);
                    var res2 = parameter2.evaluate(path.get(i), paths);
                    if(res2) continue check;
                    if(!res1){
                        log.info(path.get(i).toString());
                        return false;
                    }
                }
                //if everything on circle fulfills param1 but not param2
                var lastTransition = path.get(path.size()-1) instanceof Transition ? path.get(path.size()-1) : path.get(path.size()-2);
                var newPaths = new ArrayList<>(paths);
                newPaths.remove(path);
                if(newPaths.stream().noneMatch(x -> x.get(0).equals(node))) return false;
                if(!this.evaluate(lastTransition, newPaths)) return false;
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
