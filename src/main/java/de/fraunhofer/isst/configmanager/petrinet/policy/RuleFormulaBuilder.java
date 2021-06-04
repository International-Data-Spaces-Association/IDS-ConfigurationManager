package de.fraunhofer.isst.configmanager.petrinet.policy;

import de.fraunhofer.iais.eis.Rule;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.Formula;
import de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionFormula;

import java.net.URI;

import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.TT.TT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeMODAL.nodeMODAL;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.state.NodeNOT.nodeNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.ArcExpression.arcExpression;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAF.transitionAF;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionAND.transitionAND;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionEV.transitionEV;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionNOT.transitionNOT;
import static de.fraunhofer.isst.configmanager.petrinet.evaluation.formula.transition.TransitionPOS.transitionPOS;

/**
 * For a given PolicyPattern, Rule and Resource ID (URI), create a Formula
 */
public class RuleFormulaBuilder {

    /**
     * Builds a formula for a given PolicyPattern, Rule and Resource ID (URI)
     *
     * @param pattern         The recognized policy pattern.
     * @param rule            The ids rule.
     * @param target          The requested/accessed element.
     */
    public static Formula buildFormula(final PolicyPattern pattern, final Rule rule, final URI target) {
        switch (pattern) {
            case PROVIDE_ACCESS:
                //when access is provided, policy is Fulfilled everytime
                return TT();
            case USAGE_UNTIL_DELETION:
                return buildUsageUntilDeletionFormula(pattern, rule, target);
            case USAGE_LOGGING:
                return buildLoggingFormula(pattern, rule, target);
            case N_TIMES_USAGE:
                return buildNTimesUsageFormula(pattern, rule, target);
            case USAGE_NOTIFICATION:
                return buildNotificationFormula(pattern, rule, target);
            case CONNECTOR_RESTRICTED_USAGE:
                return buildConnectorRestrictionFormula(pattern, rule, target);
            case PROHIBIT_ACCESS:
                return buildProhibitAccessFormula(pattern, rule, target);
            default:
                //other rules are ignored
                return null;
        }
    }

    static Formula buildNTimesUsageFormula(final PolicyPattern pattern, final Rule rule, final URI target){
        //in every possible path, resource is only allowed to be read maxUsage times
        var maxUsage = RuleUtils.getMaxAccess(rule);
        TransitionFormula formula = transitionPOS(transitionAF(arcExpression(trans -> trans.getContext().getRead().contains(target.toString()), "")));
        for(int i = 0; i< maxUsage; i++){
            formula = transitionPOS(transitionAND(transitionAF(arcExpression(trans -> trans.getContext().getRead().contains(target.toString()), "")), formula));
        }
        return nodeNOT(nodeMODAL(formula));
    }

    static Formula buildUsageUntilDeletionFormula(final PolicyPattern pattern, final Rule rule, final URI target){
        //data has to be deleted after a reading transition but before the final node
        return nodeNOT(
                nodeMODAL(
                        transitionPOS(
                                transitionAND(
                                        transitionAF(arcExpression(x -> x.getContext().getRead() != null
                                                && x.getContext().getRead().contains(target.toString()), "")),
                                        transitionNOT(
                                                transitionEV(
                                                        transitionAF(
                                                                arcExpression(x -> x.getContext().getWrite() != null
                                                                && x.getContext().getWrite().contains(target.toString())
                                                                || x.getContext().getErase() != null
                                                                && x.getContext().getErase().contains(target.toString()),
                                                                "")
                                                        )
                                                )
                                        )
                                )
                        )
                )
        );
    }

    static Formula buildConnectorRestrictionFormula(final PolicyPattern pattern, final Rule rule, final URI target){
        //if a transition is reading the resource, it has to be from the allowedConnector
        var allowedConnector = RuleUtils.getEndpoint(rule);
        return nodeNOT(
                nodeMODAL(
                        transitionPOS(
                                transitionAF(
                                        arcExpression(trans -> trans.getContext().getRead().contains(target.toString())
                                                        && !trans.getContext().getContext().contains(allowedConnector),
                                                "transition tries to read resource which is prohibited per connector!"
                                        )
                                )
                        )
                )
        );
    }

    static Formula buildProhibitAccessFormula(final PolicyPattern pattern, final Rule rule, final URI target){
        //no reachable transition reads forbidden resource
        return nodeNOT(
                nodeMODAL(
                        transitionPOS(
                                transitionAF(
                                        arcExpression(trans -> trans.getContext().getRead().contains(target.toString()),
                                                "transition tries to read prohibited resource!"
                                        )
                                )
                        )
                )
        );
    }

    static Formula buildLoggingFormula(final PolicyPattern pattern, final Rule rule, final URI target){
        //every transition reading the resource has to contain a logging flag in context
        //TODO create Context field for "this service logs access"
        return null;
    }

    static Formula buildNotificationFormula(final PolicyPattern pattern, final Rule rule, final URI target){
        //every transition reading the resource has to contain a notification flag in context
        //TODO create Context field for "this service notifies about access"
        return null;
    }


}
