package de.fraunhofer.isst.configmanager.configmanagement.entities.usagecontrol;

public enum Pattern {

    /**
     * Standard pattern to allow unrestricted access.
     */
    PROVIDE_ACCESS("PROVIDE_ACCESS"),
    /**
     * Default pattern if no other is detected. v2.0: NO_POLICY("no-policy")
     */
    PROHIBIT_ACCESS("PROHIBIT_ACCESS"),
    /**
     * Type: NotMoreThanN v2.0: COUNT_ACCESS("count-access") https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/NTimesUsageTemplates/N_TIMES_USAGE_OFFER_TEMPLATE.jsonld
     */
    N_TIMES_USAGE("N_TIMES_USAGE"),
    /**
     * Type: DurationOffer https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/TimeRestrictedUsageTemplates/DURATION_USAGE_OFFER_TEMPLATE.jsonld
     */
    DURATION_USAGE("DURATION_USAGE"),
    /**
     * Type: IntervalUsage v2.0: TIME_INTERVAL("time-interval") https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/TimeRestrictedUsageTemplates/USAGE_DURING_INTERVAL_OFFER_TEMPLATE.jsonld
     */
    USAGE_DURING_INTERVAL("USAGE_DURING_INTERVAL"),
    /**
     * Type: DeleteAfterInterval v2.0: DELETE_AFTER("delete-after")
     * https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/TimeRestrictedUsageTemplates/USAGE_UNTIL_DELETION_OFFER_TEMPLATE.jsonld
     */
    USAGE_UNTIL_DELETION("USAGE_UNTIL_DELETION"),
    /**
     * Type: Logging v2.0: LOG_ACCESS("log-access") https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/UsageLoggingTemplates/USAGE_LOGGING_OFFER_TEMPLATE.jsonld
     */
    USAGE_LOGGING("USAGE_LOGGING"),
    /**
     * Type: Notification https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/UsageNotificationTemplates/USAGE_NOTIFICATION_OFFER_TEMPLATE.jsonld
     */
    USAGE_NOTIFICATION("USAGE_NOTIFICATION"),
    /**
     * Type: Connector-restricted access https://github.com/International-Data-Spaces-Association/InformationModel/blob/master/examples/contracts-and-usage-policy/templates/ConnectorbasedAgreementTemplates/CONNECTORBASED_OFFER_TEMPLATE.jsonld
     */
    CONNECTOR_RESTRICTED_USAGE("CONNECTOR_RESTRICTED_USAGE");

    private final String pattern;

    Pattern(String string) {
        pattern = string;
    }

    @Override
    public String toString() {
        return pattern;
    }
}
