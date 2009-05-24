package com.textmagic.sms.core.parsing;

import com.textmagic.sms.dto.SentMessage;
import com.textmagic.sms.dto.MessageStatus;
import com.textmagic.sms.dto.ReceivedMessage;
import com.textmagic.sms.exception.ServiceBackendException;

import java.util.List;
import java.math.BigDecimal;

/**
 * <code>TextMagicResponseParser</code> is parser of TextMagic Gateway Http Api response   
 * Class provides a set of methods for parsing responses of all gateway commands
 *
 * @author Rafael Bagmanov
 */
public interface TextMagicResponseParser {
    /**
     * Checks whether reponse contains failue error code
     *
     * @param response http api response body
     * @return true - if response contains error code, false - otherwise
     * @throws ResponseParsingException if <tt>response</tt> could not be parsed
     */
    public boolean isFailureResponse(String response) throws ResponseParsingException;

    /**
     * Constructs {@link ServiceBackendException} based on error_code and error_message <tt>response</tt> fields
     *
     * @param response http api response body
     * @return ServiceBackendException with populated errorCode and errorMessage attribute
     * @throws ResponseParsingException if <tt>response</tt> could not be parsed
     */
    public ServiceBackendException parseFailureResponse(String response) throws ResponseParsingException;

    /**
     * Constructs list of {@link SentMessage} dtos from 'send' command http response body
     *
     * @param response 'send' command http response body
     * @return list of SendMessage dtos
     * @throws ResponseParsingException if <tt>response</tt> could not be parsed
     */
    public List<SentMessage> parseSendResponse(String response) throws ResponseParsingException;

    /**
     * Constructs BigDecimal balance value from 'account' command http response body
     *
     * @param response 'account' command http response body
     * @return current balance value
     * @throws ResponseParsingException if <tt>response</tt> could not be parsed
     */
    public BigDecimal parseAccountResponse(String response) throws ResponseParsingException;

    /**
     * Constructs list of {@link MessageStatus} dtos from 'message_status' command http response body
     *
     * @param response 'message_status' command http response body
     * @return parsing result as list of MessageStatus dtos
     * @throws ResponseParsingException if <tt>response</tt> could not be parsed
     */
    public List<MessageStatus> parseMessageStatusResponse(String response) throws ResponseParsingException;

    /**
     * Constructs list of {@link com.textmagic.sms.dto.ReceivedMessage} dtos from 'receive' command http response body
     *
     * @param response 'receive' command http response body
     * @return list of {@link com.textmagic.sms.dto.ReceivedMessage} dtos
     * @throws ResponseParsingException if <tt>response</tt> could not be parsed
     */
    public List<ReceivedMessage> parseReceiveResponse(String response) throws ResponseParsingException;

    /**
     * Constructs list of deleted reply messages IDs from 'delete_reply' command http response body
     *
     * @param response 'delete_reply' command http response body
     * @return list of deleted reply messages IDs
     * @throws ResponseParsingException if <tt>response</tt> could not be parsed
     */
    public List<Long> parseDeleteReplyResponse(String response) throws ResponseParsingException;
}
