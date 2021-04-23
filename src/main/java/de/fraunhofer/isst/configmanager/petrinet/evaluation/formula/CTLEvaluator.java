package de.fraunhofer.isst.configmanager.petrinet.evaluation.formula;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.StateFormula;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Place;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;

public class CTLEvaluator {

    public static boolean evaluateNode(StateFormula ctlExpression, Place place){
        //TODO base evaluation on place
        return ctlExpression.evaluate(place);
    }

    public static boolean evaluateTransition(TransitionFormula ctlExpression, Transition transition){
        //TODO base evaluation on transition
        return ctlExpression.evaluate(transition);
    }

    public static boolean evaluate(Formula ctlExpression, Node node){
            if(ctlExpression instanceof StateFormula && node instanceof Place){
                return evaluateNode((StateFormula) ctlExpression, (Place) node);
            }else if(ctlExpression instanceof TransitionFormula && node instanceof Transition){
                return evaluateTransition((TransitionFormula) ctlExpression, (Transition) node);
            }else{
                //cannot be evaluated
                return false;
            }
    }

}
