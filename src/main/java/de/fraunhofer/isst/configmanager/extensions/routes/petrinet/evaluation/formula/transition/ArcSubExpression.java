package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Transition;

/**
 * Interface describing ArcSubExpressions, can be used as lambda: {@link Transition} -> boolean.
 */
@FunctionalInterface
public interface ArcSubExpression {
    boolean evaluate(Transition transition);
}
