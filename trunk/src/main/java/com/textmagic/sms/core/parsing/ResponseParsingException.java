package com.textmagic.sms.core.parsing;

/**
 *  Class <code>ResponseParsingException</code> is a form of {@link Exception} that indicates that a
 * parsing of TextMagic server response failed
 *
 * @author Rafael bagmanov
 */
public class ResponseParsingException extends Exception {
    public ResponseParsingException() {
        super();
    }

    public ResponseParsingException(String message) {
        super(message);
    }

    public ResponseParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ResponseParsingException(Throwable cause) {
        super(cause);
    }
}
