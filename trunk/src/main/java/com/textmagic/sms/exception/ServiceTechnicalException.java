package com.textmagic.sms.exception;

/**
 * The class <code>ServiceTechnicalException</code> and its subclasses are a form of
 * <code>ServiceException</code> that indicates that exceptional condition occur
 * on the client side.
 * <br/><br/>
 * This could be:
 * <ul>
 *  <li>Technical failures (example: client can not connect to TextMagic server),
 *  <li>Unsatisfied expectations (example: client can't parse server response)
 *  <li>etc
 * </ul>
 *
 * @author Rafael Bagmanov
 */
public class ServiceTechnicalException extends ServiceException {
    public ServiceTechnicalException() {
        super();
    }

    public ServiceTechnicalException(String message) {
        super(message);
    }

    public ServiceTechnicalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceTechnicalException(Throwable cause) {
        super(cause);
    }
}
