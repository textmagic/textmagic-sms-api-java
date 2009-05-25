package com.textmagic.sms.exception;

/**
 * The class <code>ServiceBackendException</code> and its subclasses are a form of
 * <code>ServiceException</code> that indicates coditions where TextMagic server received clients
 * request, but could not fulfil it and responded with error code
 * <br/><br/>
 * See <a href="http://api.textmagic.com/https-api/api-error-codes">Api Error Coded</a>
 *
 * @author Rafael Bagmanov
 */
public class ServiceBackendException extends ServiceException {

    private Integer errorCode;
    private String errorMessage;


    public ServiceBackendException(Integer errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     *  TextMagic server error code
     *
     * @return code recieved from server
     */
    public Integer getErrorCode() {
        return errorCode;
    }

    /**
     * TextMagic server error message
     *
     * @return text received from server
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String getMessage() {
        return "Server responsed with error code [ " + errorCode +" - " + errorMessage + " ]";
    }


}
