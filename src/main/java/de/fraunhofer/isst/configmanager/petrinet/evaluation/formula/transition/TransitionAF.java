package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class TransitionAF implements TransitionFormula {

    public static TransitionAF transitionAF(ArcExpression parameter){
        return new TransitionAF(parameter);
    }

    private ArcExpression parameter;

    @Override
    public boolean evaluate(Node node) {
        return node instanceof Transition && parameter.getSubExpression().evaluate((Transition) node);
    }

    @Override
    public String symbol() {
        return "AF";
    }

    @Override
    public String writeFormula() {
        return String.format("%s(%s)", symbol(), "expression");
    }
}
