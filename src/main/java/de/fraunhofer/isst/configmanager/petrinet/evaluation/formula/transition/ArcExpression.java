package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ArcExpression {
    private ArcSubExpression subExpression;
    private String message;

    public static ArcExpression arcExpression(final ArcSubExpression subExpression,
                                              final String message) {
        return new ArcExpression(subExpression, message);
    }
}
