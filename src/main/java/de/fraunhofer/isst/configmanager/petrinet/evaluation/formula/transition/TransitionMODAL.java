package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionMODAL implements TransitionFormula {

    public static TransitionMODAL transitionMODAL(StateFormula parameter){
        return new TransitionMODAL(parameter);
    }

    private StateFormula parameter;

    //TODO
    // MODAL, is true if parameter evaluates to true for a state following the current transition
    @Override
    public boolean evaluate(Node node) {
        return node instanceof Transition &&
                node.getSourceArcs().stream()
                        .map(Arc::getTarget)
                        .map(place -> parameter.evaluate(place))
                        .reduce(false, (a,b) -> a || b);
    }

    @Override
    public String symbol() {
        return "MODAL";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), parameter.writeFormula());
    }

}
