package de.fraunhofer.isst.configmanager.util.camel.exceptions;

/**
 * Thrown to indicate that an error occurred during creation or deployment of a Camel route.
 */
public class RouteCreationException extends Exception {

    /**
     * Constructs a RouteCreationException with the specified message.
     *
     * @param msg the message.
     */
    public RouteCreationException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a RouteCreationException with the specified message and cause.
     *
     * @param msg the message.
     * @param cause the cause.
     */
    public RouteCreationException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
