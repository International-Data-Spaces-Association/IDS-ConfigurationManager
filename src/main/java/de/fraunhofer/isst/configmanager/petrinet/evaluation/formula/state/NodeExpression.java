package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class NodeExpression {

    public static NodeExpression nodeExpression(NodeSubExpression nodeSubExpression, String message){
        return new NodeExpression(nodeSubExpression, message);
    }

    private NodeSubExpression subExpression;
    private String message;
}
