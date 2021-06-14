package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Place;

/**
 * Interface describing NodeSubExpressions, can be used as lambda: {@link Place} -> boolean
 */
@FunctionalInterface
public interface NodeSubExpression {
    boolean evaluate(Place place);
}
