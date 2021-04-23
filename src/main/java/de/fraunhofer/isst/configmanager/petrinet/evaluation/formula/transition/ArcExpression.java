package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ArcExpression {

    public static ArcExpression arcExpression(ArcSubExpression subExpression, String message){
        return new ArcExpression(subExpression, message);
    }

    private ArcSubExpression subExpression;
    private String message;
}
