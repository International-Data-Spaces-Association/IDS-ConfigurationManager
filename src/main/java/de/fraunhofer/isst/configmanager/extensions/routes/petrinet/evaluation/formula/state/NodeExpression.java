package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Custom Expression to be evaluated on a {@link de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Place}
 */
@Getter
@AllArgsConstructor
public class NodeExpression {

    /**
     * Subexpression (function from {@link de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Place} to boolean
     */
    private NodeSubExpression subExpression;

    /**
     * Information message to return when subExpression is not fulfilled by a transition
     */
    private String message;

    public static NodeExpression nodeExpression(final NodeSubExpression nodeSubExpression,
                                                final String message){
        return new NodeExpression(nodeSubExpression, message);
    }
}
