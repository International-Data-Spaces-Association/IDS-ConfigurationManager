package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeExpression {

    public static NodeExpression nodeExpression(NodeSubExpression nodeSubExpression, String message){
        return new NodeExpression(nodeSubExpression, message);
    }

    private NodeSubExpression subExpression;
    private String message;
}
