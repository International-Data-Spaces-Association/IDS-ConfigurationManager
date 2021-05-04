package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.petrinet.model.Transition;

@FunctionalInterface
public interface ArcSubExpression {
    boolean evaluate(Transition transition);
}
