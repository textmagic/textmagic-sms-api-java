package com.textmagic.sms.core.invoker;

/**
 *  Class <code>ServiceInvokerException</code> is a form of {@link Exception} that indicates that a call
 *  to sms api gateway failed
 *
 * @author Rafael Bagmanov
 */
public class ServiceInvokerException extends Exception{
    public ServiceInvokerException(String message) {
        super(message);
    }

    public ServiceInvokerException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceInvokerException(Throwable cause) {
        super(cause);
    }
}
