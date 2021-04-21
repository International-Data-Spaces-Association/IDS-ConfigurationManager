package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Place;

@FunctionalInterface
public interface NodeSubExpression {
    boolean evaluate(Place place);
}
