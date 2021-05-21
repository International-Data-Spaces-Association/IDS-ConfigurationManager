package de.fraunhofer.isst.configmanager.petrinet.simulator;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcSubExpression;
import de.fraunhofer.isst.configmanager.petrinet.model.ContextObject;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class ParallelEvaluator {

    public static boolean nParallelTransitionsWithCondition(ArcSubExpression condition, int n, List<List<Transition>> parallelSets){
        var setsWithSizeAtLeastN = parallelSets.stream().filter(transitions -> transitions.size() >= n).collect(Collectors.toSet());
        for(var set: setsWithSizeAtLeastN){
            var names = set.stream().map(Transition::getContext).map(ContextObject::getRead).collect(Collectors.toList());
            log.info(names.toString());
            if(set.stream().filter(condition::evaluate).count() >= n) return true;
        }
        return false;
    }
}
