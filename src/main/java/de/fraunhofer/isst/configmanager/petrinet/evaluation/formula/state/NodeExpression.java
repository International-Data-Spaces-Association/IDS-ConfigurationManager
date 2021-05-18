package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NodeExpression {
    private NodeSubExpression subExpression;
    private String message;

    public static NodeExpression nodeExpression(final NodeSubExpression nodeSubExpression,
                                                final String message){
        return new NodeExpression(nodeSubExpression, message);
    }
}
