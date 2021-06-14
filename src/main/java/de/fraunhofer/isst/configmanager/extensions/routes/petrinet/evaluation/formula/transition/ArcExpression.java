package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Custom Expression to be evaluated on a {@link de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Transition}
 */
@Getter
@AllArgsConstructor
public class ArcExpression {

    /**
     * Subexpression (function from {@link de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Transition} to boolean
     */
    private ArcSubExpression subExpression;

    /**
     * Information message to return when subExpression is not fulfilled by a transition
     */
    private String message;

    public static ArcExpression arcExpression(final ArcSubExpression subExpression,
                                              final String message) {
        return new ArcExpression(subExpression, message);
    }
}
