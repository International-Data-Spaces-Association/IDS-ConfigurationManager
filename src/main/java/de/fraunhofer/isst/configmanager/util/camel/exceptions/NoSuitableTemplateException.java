package de.fraunhofer.isst.configmanager.util.camel.exceptions;

/**
 * Thrown to indicate that no suitable Camel route template was found for a given AppRoute.
 */
public class NoSuitableTemplateException extends Exception {

    /**
     * Constructs a NoSuitableTemplateException with the specified message.
     *
     * @param msg the message.
     */
    public NoSuitableTemplateException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a NoSuitableTemplateException with the specified message and cause.
     *
     * @param msg the message.
     * @param cause the cause.
     */
    public NoSuitableTemplateException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
