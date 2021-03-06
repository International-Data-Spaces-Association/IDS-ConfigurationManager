/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.fraunhofer.isst.configmanager.petrinet.simulator;

import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcSubExpression;
import de.fraunhofer.isst.configmanager.petrinet.model.Transition;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Methods to check parallel evaluation of a {@link de.fraunhofer.isst.configmanager.petrinet.model.PetriNet}
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
