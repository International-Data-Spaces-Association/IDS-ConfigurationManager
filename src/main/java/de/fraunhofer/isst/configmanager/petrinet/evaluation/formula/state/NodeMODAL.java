package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Arc;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeMODAL implements StateFormula {

    public static NodeMODAL nodeMODAL(TransitionFormula parameter){
        return new NodeMODAL(parameter);
    }

    private TransitionFormula parameter;

    //TODO
    // MODAL, is true if parameter evaluates to true for a transition following the current state
    @Override
    public boolean evaluate(Node node) {
        return node instanceof Place &&
                node.getSourceArcs().stream()
                        .map(Arc::getTarget)
                        .map(transition -> parameter.evaluate(transition))
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
