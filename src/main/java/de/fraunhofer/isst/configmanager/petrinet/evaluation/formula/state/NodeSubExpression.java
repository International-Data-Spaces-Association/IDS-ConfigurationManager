package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;

/**
 * Interface describing NodeSubExpressions, can be used as lambda: {@link Place} -> boolean
 */
@FunctionalInterface
public interface NodeSubExpression {
    boolean evaluate(Place place);
}
