package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;

import java.util.List;

public class CTLEvaluator {

    public static boolean evaluateNode(StateFormula ctlExpression, Place place, List<List<Node>> paths){
        //TODO base evaluation on place
        return ctlExpression.evaluate(place, paths);
    }

    public static boolean evaluateTransition(TransitionFormula ctlExpression, Transition transition, List<List<Node>> paths){
        //TODO base evaluation on transition
        return ctlExpression.evaluate(transition, paths);
    }

    public static boolean evaluate(Formula ctlExpression, Node node, List<List<Node>> paths){

            if(ctlExpression instanceof StateFormula && node instanceof Place){
                return evaluateNode((StateFormula) ctlExpression, (Place) node, paths);
            }else if(ctlExpression instanceof TransitionFormula && node instanceof Transition){
                return evaluateTransition((TransitionFormula) ctlExpression, (Transition) node, paths);
            }else{
                //cannot be evaluated
                return false;
            }
    }

}
