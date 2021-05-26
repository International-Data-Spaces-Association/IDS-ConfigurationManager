package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class CTLEvaluator {

    public static boolean evaluateNode(final StateFormula ctlExpression,
                                       final Place place,
                                       final List<List<Node>> paths) {
        //base evaluation on place
        return ctlExpression.evaluate(place, paths);
    }

    public static boolean evaluateTransition(final TransitionFormula ctlExpression,
                                             final Transition transition,
                                             final List<List<Node>> paths) {
        //base evaluation on transition
        return ctlExpression.evaluate(transition, paths);
    }

    public static boolean evaluate(final Formula ctlExpression,
                                   final Node node,
                                   final List<List<Node>> paths) {

            if (ctlExpression instanceof StateFormula && node instanceof Place) {
                return evaluateNode((StateFormula) ctlExpression, (Place) node, paths);
            } else if (ctlExpression instanceof TransitionFormula && node instanceof Transition){
                return evaluateTransition((TransitionFormula) ctlExpression, (Transition) node, paths);
            } else {
                //cannot be evaluated
                return false;
            }
    }

}
