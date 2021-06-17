package de.fraunhofer.isst.configmanager.extensions.routes.petrinet.simulator;

import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.evaluation.formula.transition.ArcSubExpression;
import de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.Transition;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Methods to check parallel evaluation of a {@link de.fraunhofer.isst.configmanager.extensions.routes.petrinet.model.PetriNet}.
 */
@Slf4j
@UtilityClass
public class ParallelEvaluator {

    /**
     * @param condition a condition to be fulfilled by a transition
     * @param n number of transitions to fulfill the condition in parallel
     * @param parallelSets sets of parallel transitions (previously calculated through stepgraph of unfolded petrinet)
     * @return true if at least n transitions fulfilling condition are parallely executed at some point
     */
    public static boolean nParallelTransitionsWithCondition(final ArcSubExpression condition,
                                                            final int n,
                                                            final List<List<Transition>> parallelSets) {
        final var setsWithSizeAtLeastN = parallelSets.stream().filter(transitions -> transitions.size() >= n).collect(Collectors.toSet());
        for (final var set: setsWithSizeAtLeastN) {
            if (set.stream().filter(condition::evaluate).count() >= n) {
                return true;
            }
        }
        return false;
    }
}
