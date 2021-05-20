package de.fraunhofer.isst.configmanager.petrinet.simulator;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcExpression;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcSubExpression;
import de.fraunhofer.isst.configmanager.petrinet.model.Node;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;

import java.util.List;

public class ParallelEvaluator {

    public static boolean nParallelTransitionsWithCondition(ArcSubExpression condition, int n, List<List<Transition>> parallelSets){
        for(var set: parallelSets){
            if(set.size() >= n && set.stream().filter(condition::evaluate).count() >= n) return true;
        }
        return false;
    }
}
