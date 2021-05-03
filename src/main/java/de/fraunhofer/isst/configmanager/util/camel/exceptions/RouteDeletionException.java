package de.fraunhofer.isst.configmanager.util.camel.exceptions;

/**
 * Thrown to indicate that an error occurred while trying to delete a Camel route.
 */
public class RouteDeletionException extends Exception {

    /**
     * Constructs a RouteDeletionException with the specified message.
     *
     * @param msg the message.
     */
    public RouteDeletionException(final String msg) {
        super(msg);
    }

    /**
     * Constructs a RouteDeletionException with the specified message and cause.
     *
     * @param msg the message.
     * @param cause the cause.
     */
    public RouteDeletionException(final String msg, final Throwable cause) {
        super(msg, cause);
    }

}
