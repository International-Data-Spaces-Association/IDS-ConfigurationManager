package de.fraunhofer.isst.configmanager.petrinet.policy;

import de.fraunhofer.iais.eis.*;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.text.ParseException;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

/**
 * RuleUtils Class from DataspaceConnector
 */
@Slf4j
public class RuleUtils {

    /**
     * Read the properties of an ids rule to automatically recognize the policy pattern.
     *
     * @param rule The ids rule.
     * @return The recognized policy pattern.
     */
    public static PolicyPattern getPatternByRule(final Rule rule) {
        PolicyPattern detectedPattern = null;

        if (rule instanceof Prohibition) {
            detectedPattern = PolicyPattern.PROHIBIT_ACCESS;
        } else if (rule instanceof Permission) {
            final var constraints = rule.getConstraint();
            final var postDuties = ((Permission) rule).getPostDuty();

            if (constraints != null && constraints.get(0) != null) {
                if (constraints.size() > 1) {
                    if (postDuties != null && postDuties.get(0) != null) {
                        detectedPattern = PolicyPattern.USAGE_UNTIL_DELETION;
                    } else {
                        detectedPattern = PolicyPattern.USAGE_DURING_INTERVAL;
                    }
                } else {
                    final var firstConstraint = (ConstraintImpl) constraints.get(0);
                    final var leftOperand = firstConstraint.getLeftOperand();
                    final var operator = firstConstraint.getOperator();
                    if (leftOperand == LeftOperand.COUNT) {
                        detectedPattern = PolicyPattern.N_TIMES_USAGE;
                    } else if (leftOperand == LeftOperand.ELAPSED_TIME) {
                        detectedPattern = PolicyPattern.DURATION_USAGE;
                    } else if (leftOperand == LeftOperand.SYSTEM
                            && operator == BinaryOperator.SAME_AS) {
                        detectedPattern = PolicyPattern.CONNECTOR_RESTRICTED_USAGE;
                    } else {
                        detectedPattern = null;
                    }
                }
            } else {
                if (postDuties != null && postDuties.get(0) != null) {
                    final var action = postDuties.get(0).getAction().get(0);
                    if (action == Action.NOTIFY) {
                        detectedPattern = PolicyPattern.USAGE_NOTIFICATION;
                    } else if (action == Action.LOG) {
                        detectedPattern = PolicyPattern.USAGE_LOGGING;
                    } else {
                        detectedPattern = null;
                    }
                } else {
                    detectedPattern = PolicyPattern.PROVIDE_ACCESS;
                }
            }
        }

        return detectedPattern;
    }

    /**
     * Gets the allowed number of accesses defined in a policy.
     *
     * @param rule the policy rule object.
     * @return the number of allowed accesses.
     */
    public static Integer getMaxAccess(final Rule rule) throws NumberFormatException {
        final var constraint = rule.getConstraint().get(0);
        final var value = ((ConstraintImpl) constraint).getRightOperand().getValue();
        final var operator = ((ConstraintImpl) constraint).getOperator();

        int number;
        try {
            number = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            if (log.isDebugEnabled()) {
                log.debug("Failed to parse value to integer. [exception=({})]",
                        e.getMessage(), e);
            }
            throw e;
        }

        if (number < 0) {
            number = 0;
        }

        switch (operator) {
            case EQ:
            case LTEQ:
                return number;
            case LT:
                return number - 1;
            default:
                return 0;
        }
    }

    /**
     * Gets the endpoint value to send notifications to defined in a policy.
     *
     * @param rule The ids rule.
     * @return The endpoint value.
     */
    public static String getEndpoint(final Rule rule) throws NullPointerException {
        final var constraint = rule.getConstraint().get(0);
        return ((ConstraintImpl) constraint).getRightOperand().getValue();
    }

}
