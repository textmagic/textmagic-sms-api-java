package com.textmagic.sms.exception;

/**
 * The class <code>ServiceException</code> is the super class of all errors and
 * exceptions that can happen as the result of communication with Sms gateway through {@link com.textmagic.sms.MessageService}
 *
 * @author Rafael Bagmanov
 */
public class ServiceException extends Exception{
    public ServiceException() {
        super();
    }

    public ServiceException(String message) {
        super(message);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }
}
