package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.simulator;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.ArcSubExpression;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Transition;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Methods to check parallel evaluation of a {@link de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNet}
 */
@Slf4j
public class ParallelEvaluator {

    /**
     * @param condition a condition to be fulfilled by a transition
     * @param n number of transitions to fulfill the condition in parallel
     * @param parallelSets sets of parallel transitions (previously calculated through stepgraph of unfolded petrinet)
     * @return true if at least n transitions fulfilling condition are parallely executed at some point
     */
    public static boolean nParallelTransitionsWithCondition(ArcSubExpression condition, int n, List<List<Transition>> parallelSets){
        var setsWithSizeAtLeastN = parallelSets.stream().filter(transitions -> transitions.size() >= n).collect(Collectors.toSet());
        for(var set: setsWithSizeAtLeastN){
            if(set.stream().filter(condition::evaluate).count() >= n) return true;
        }
        return false;
    }
}
