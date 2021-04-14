package de.fraunhofer.isst.configmanager.api.service.resources;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.Action;
import de.fraunhofer.iais.eis.BinaryOperator;
import de.fraunhofer.iais.eis.ConstraintBuilder;
import de.fraunhofer.iais.eis.ContractOffer;
import de.fraunhofer.iais.eis.ContractOfferBuilder;
import de.fraunhofer.iais.eis.DutyBuilder;
import de.fraunhofer.iais.eis.LeftOperand;
import de.fraunhofer.iais.eis.NotMoreThanNOfferBuilder;
import de.fraunhofer.iais.eis.PermissionBuilder;
import de.fraunhofer.iais.eis.ProhibitionBuilder;
import de.fraunhofer.iais.eis.util.RdfResource;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Objects;

@Service
public class ResourceContractBuilder {

    public ContractOffer buildUsageUntilDeletion(final String startDate,
                                                 final String endDate,
                                                 final String deletionDate) {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("usage-until-deletion")))
                        ._action_(Util.asList(Action.USE))
                        ._constraint_(Util.asList(new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                ._operator_(BinaryOperator.AFTER)
                                ._rightOperand_(new RdfResource(startDate,
                                        URI.create("xsd:dateTimeStamp")))
                                .build(), new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                ._operator_(BinaryOperator.BEFORE)
                                ._rightOperand_(new RdfResource(endDate,
                                        URI.create("xsd:dateTimeStamp")))
                                .build()))
                        ._postDuty_(Util.asList(new DutyBuilder()
                                ._action_(Util.asList(Action.DELETE))
                                ._constraint_(Util.asList(new ConstraintBuilder()
                                        ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                        ._operator_(BinaryOperator.TEMPORAL_EQUALS)
                                        ._rightOperand_(new RdfResource(deletionDate,
                                                URI.create("xsd:dateTimeStamp")))
                                        .build()))
                                .build()))
                        .build()))
                .build();
    }

    public ContractOffer buildUsageDuringInterval(final String fromDate,
                                                  final String toDate) {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("usage-during-interval")))
                        ._action_(Util.asList(Action.USE))
                        ._constraint_(Util.asList(new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                ._operator_(BinaryOperator.AFTER)
                                ._rightOperand_(new RdfResource(fromDate,
                                        URI.create("xsd:dateTimeStamp")))
                                .build(), new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.POLICY_EVALUATION_TIME)
                                ._operator_(BinaryOperator.BEFORE)
                                ._rightOperand_(new RdfResource(toDate,
                                        URI.create("xsd:dateTimeStamp")))
                                .build()))
                        .build()))
                .build();
    }

    public ContractOffer buildUsageLogging() {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("usage-logging")))
                        ._action_(Util.asList(Action.USE))
                        ._postDuty_(Util.asList(new DutyBuilder()
                                ._action_(Util.asList(Action.LOG))
                                .build()))
                        .build()))
                .build();
    }

    public ContractOffer buidUsageNotification(final String url) {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("usage-notification")))
                        ._action_(Util.asList(Action.USE))
                        ._postDuty_(Util.asList(new DutyBuilder()
                                ._action_(Util.asList(Action.NOTIFY))
                                ._constraint_(Util.asList(new ConstraintBuilder()
                                        ._leftOperand_(LeftOperand.ENDPOINT)
                                        ._operator_(BinaryOperator.DEFINES_AS)
                                        ._rightOperand_(
                                                new RdfResource(url, URI.create("xsd:anyURI")))
                                        .build()))
                                .build()))
                        .build()))
                .build();
    }

    public ContractOffer buildDurationUsage(final String number) {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("duration-usage")))
                        ._action_(Util.asList(Action.USE))
                        ._constraint_(Util.asList(new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.ELAPSED_TIME)
                                ._operator_(BinaryOperator.SHORTER_EQ)
                                ._rightOperand_(new RdfResource(number, URI.create("xsd:duration")))
                                .build()))
                        .build()))
                .build();
    }

    @NotNull
    public BinaryOperator getBinaryOperator(final JsonNode jsonNode) {
        final var operator = jsonNode.get("binaryoperator").asText();

        BinaryOperator binaryOperator;
        if ("<".equals(operator)) {
            binaryOperator = BinaryOperator.LT;
        } else if ("<=".equals(operator)) {
            binaryOperator = BinaryOperator.LTEQ;
        } else {
            binaryOperator = BinaryOperator.EQ;
        }
        return binaryOperator;
    }

    public ContractOffer buildNTimesUsage(final BinaryOperator binaryOperator,
                                          final String number,
                                          final String pipEndpoint) {
        return new NotMoreThanNOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("n-times-usage")))
                        ._action_(Util.asList(Action.USE))
                        ._constraint_(Util.asList(new ConstraintBuilder()
                                ._leftOperand_(LeftOperand.COUNT)
                                ._operator_(binaryOperator)
                                ._rightOperand_(new RdfResource(number, URI.create("xsd:double")))
                                ._pipEndpoint_(
                                        URI.create(pipEndpoint))
                                .build()))
                        .build()))
                .build();
    }

    public ContractOffer buildProhibitAccess() {
        return new ContractOfferBuilder()
                ._prohibition_(Util.asList(new ProhibitionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("prohibit-access")))
                        ._action_(Util.asList(Action.USE))
                        .build()))
                .build();
    }

    public ContractOffer buildProvideAccess() {
        return new ContractOfferBuilder()
                ._permission_(Util.asList(new PermissionBuilder()
                        ._title_(Util.asList(new TypedLiteral("Usage Policy")))
                        ._description_(Util.asList(new TypedLiteral("provide-access")))
                        ._action_(Util.asList(Action.USE))
                        .build()))
                .build();
    }

    /**
     * @param contractJson the contract offer
     * @return json node
     */
    public JsonNode getJsonNodeFromContract(final String contractJson) throws JsonProcessingException {
        final var mapper = new ObjectMapper();
        return mapper.readTree(Objects.requireNonNull(contractJson));
    }
}
