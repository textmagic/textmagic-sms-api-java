package com.textmagic.sms;

import com.textmagic.sms.dto.SentMessage;
import com.textmagic.sms.dto.MessageStatus;
import com.textmagic.sms.dto.ReceivedMessage;
import com.textmagic.sms.exception.ServiceBackendException;
import com.textmagic.sms.exception.ServiceTechnicalException;
import com.textmagic.sms.core.invoker.HttpServiceInvoker;
import com.textmagic.sms.core.invoker.ServiceInvokerException;
import com.textmagic.sms.core.invoker.HttpServiceInvokerImpl;
import com.textmagic.sms.core.parsing.TextMagicResponseParser;
import com.textmagic.sms.core.parsing.ResponseParsingException;
import com.textmagic.sms.core.parsing.TextMagicJSONResponseParser;
import com.textmagic.sms.util.StringUtils;
import com.textmagic.sms.util.GsmCharsetUtil;

import java.util.*;
import java.util.regex.Pattern;
import java.math.BigDecimal;

/**
 * Java facade for <a href="www.textmagic.com">TextMagic SMS Gateway</a> service.
 * The class provide convient set of methods to access TextMagic http api.
 * <br/><br/>
 * The class use {@link HttpServiceInvoker} implementation for calling http service
 * and {@link com.textmagic.sms.core.parsing.TextMagicResponseParser} implementation for parsing http api responses
 *
 * @author Rafael Bagmanov
 */
public class TextMagicMessageService implements MessageService {

    // universal constants
    private static final int MAX_SMS_PARTS_COUNT = 3;
    private static final int MAX_PLAIN_SMS_TEXT_LENGTH = 160;
    private static final int MAX_UNICODE_SMS_TEXT_LENGTH = 70;

    // http api commands
    private static final String SEND_COMMAND = "send";
    private static final String ACCOUNT_COMMAND = "account";
    private static final String MESSAGE_STATUS_COMMAND = "message_status";
    private static final String RECEIVE_COMMAND = "receive";
    private static final String DELETE_REPLY_COMMAND = "delete_reply";

    private String login;
    private String password;
    HttpServiceInvoker invoker;
    TextMagicResponseParser parser;

    /**
     * Constructs facade object.
     *
     * Initialize HttpServiceInvoker with {@link com.textmagic.sms.core.invoker.HttpServiceInvokerImpl} implementation
     *  and TextMagicResponseParser with {@link com.textmagic.sms.core.parsing.TextMagicJSONResponseParser} implementation
     *
     * @param login your TextMagic account username
     * @param password your TextMagic account password
     */
    public TextMagicMessageService(String login, String password) {
        this.login = login;
        this.password = password;
        this.invoker = new HttpServiceInvokerImpl();
        this.parser = new TextMagicJSONResponseParser();

    }

    /**
     * Sets alternative HttpServiceInvoker implementation
     *
     * @param invoker appropriate implementation of {@link com.textmagic.sms.core.invoker.HttpServiceInvoker} to be used in http api calling
     */
    public void setInvoker(HttpServiceInvoker invoker) {
        this.invoker = invoker;
    }

    /**
     * Sets alternative TextMagicResponseParser implementation
     *
     * @param parser appropriate implementation of {@link TextMagicResponseParser} to be used in parsing
     */
    public void setParser(TextMagicResponseParser parser) {
        this.parser = parser;
    }


    /**
     * Covinient shortcut for send(String text, Integer maxLength, boolean useUnicode, List<String> phones) method
     * maxLength is set to 3. <br/>
     * Value of useUnicode flag will be set based on whether <tt>text</tt> contains unicode
     * (non GSM 03.38) characters
     *
     * @param text message body to be sent. max length in case of plain message - 480, in case of unicode message - 210
     * @param phone the msisdn of the message recipient
     * @return populated SentMessage dto
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if server is inaccessible or response is unexpected
     * @throws IllegalArgumentException if phone format is invalid, or text length is too long
     */
    public SentMessage send(String text, String phone) throws ServiceBackendException, ServiceTechnicalException{
        List<SentMessage> list = send(text, Arrays.asList(phone));
        if (list.size() != 1) {
            throw new ServiceTechnicalException("The server response is unexpected. " +
                    "The response object is not populated with single result: [" + Arrays.toString(list.toArray()) + "]");
        }
        return list.get(0);
    }

    /**
     * Covinient shortcut for send(String text, Integer maxLength, boolean useUnicode, List<String> phones) method
     * maxLength is set to 3.<br/>
     * Value of useUnicode flag will be set based on whether <tt>text</tt> contains unicode
     * (non GSM 03.38) characters
     *
     * @param text message body to be sent. max length in case of plain message - 480, in case of unicode message - 210
     * @param phones list of msisdn of the message recipients
     * @return list of populated {@link SentMessage} dtos
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if server is inaccessible or response is unexpected
     * @throws IllegalArgumentException if one of phones format is invalid, or text length is too long
     */
    public List<SentMessage> send(String text, List<String> phones) throws ServiceBackendException, ServiceTechnicalException{
        boolean useUnicode = !GsmCharsetUtil.isLegalString(text);
        return sendInternal(text, MAX_SMS_PARTS_COUNT, useUnicode, phones);
    }

    /**
     * Sends sms messages with <tt>text</tt> body to phones specified.
     *<br/></br/>
     * The text can be in 2 formats :
     * <ul>
     * <li> plain text - if all text characters belongs to <a href="http://api.textmagic.com/https-api/supported-character-sets">GSM 03.38 character set</a>
     * <li> unicode text - can include Arabic, Japanese, Russian, Chinese and other world languages characters
     * </ul>
     * <br/></br/>
     * One sms message can contain 160 symbols in case of plain text, and 70 in case of unicode.<br/>
     * If the text length is not fit, the text can be divided into parts.<br/>
     * The <tt>maxLength</tt> parameter provides the ability to set the maximum amount of parts the message can be divided.
     * Current standart do not supports more than 3 parts.
     *
     * @param text the message to be sent
     * @param useUnicode specifies whether message contains non-GSM characters (true) or not (false_
     * @param maxLength maximum number of parts the text can be divided. accepts 1-3 integer values included
     * @param phones the list of msisdn the message should be sent to
     * @return list of populated {@link SentMessage} dtos
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if server is inaccessible or response is unexpected
     * @throws IllegalArgumentException if one of phones format is invalid, text length is too long, text contains non-GSM characters but useUnicode = false, maxLength in out of bounds
     *
     */
    public List<SentMessage> send(String text, boolean useUnicode, Integer maxLength, List<String> phones) throws ServiceBackendException,
            ServiceTechnicalException{
        if(maxLength > 3 || maxLength < 1) {
            throw new IllegalArgumentException("maxLength valude is invalid");
        }
        if(!useUnicode && ! GsmCharsetUtil.isLegalString(text)){
            throw new IllegalArgumentException("Text '" +text + "' contains illegal characters. " +
                    "Consider calling 'send' with useUnicode=true");
        }
        return sendInternal(text, maxLength, useUnicode, phones);
    }

    /**
     * The method for internal use. Do not checks maxLength and unicode params. But do check text length.
     *
     * @param text
     * @param maxLength
     * @param useUnicode
     * @param phones
     * @return
     * @throws ServiceBackendException
     * @throws ServiceTechnicalException
     */
    protected List<SentMessage> sendInternal(String text, Integer maxLength, boolean useUnicode, List<String> phones) throws ServiceBackendException, ServiceTechnicalException {
        int smsPartLength = useUnicode ? MAX_UNICODE_SMS_TEXT_LENGTH : MAX_PLAIN_SMS_TEXT_LENGTH;
        if (smsPartLength * maxLength < text.length()) {
            throw new IllegalArgumentException("Message text length is too long.");
        }
        validatePhonesFormat(phones);
        Map<String, String> params = new HashMap<String, String>();
        params.put("text", text);
        params.put("phone", StringUtils.join(phones, ","));
        params.put("unicode", useUnicode ? "1" : "0");
        params.put("max_length", maxLength.toString());
        try {
            String response = invoke(SEND_COMMAND, params);
            return parser.parseSendResponse(response);
        } catch (ResponseParsingException ex) {
            throw new ServiceTechnicalException(ex.getMessage(), ex);
        }

    }

    private final static Pattern msisdnFormat = Pattern.compile("^\\d{8,20}$");

    /**
     * Checks whether all phones in provided list have correct msisdn format
     * <br/>
     * The current check is based only on: msisdn is 8-20 digits value<br/>
     * The method can be overriden if more precise check is needed
     *
     * @param phones phone numbers to be validated
     * @throws IllegalArgumentException if founds invalid phone number  
     */
    protected void validatePhonesFormat(List<String> phones){
        for (String phone : phones) {
            if(!msisdnFormat.matcher(phone).matches()){
                throw new IllegalArgumentException("Phone '" + phone + "' has invalid format");
            }
        }
    }

    /**
     * Does main http api calling cycle: calls http service and check whether the response contains error code
     *
     * @param command http api command to be called
     * @param params the params for http api command
     * @return backend response
     * @throws ResponseParsingException if parser could not determine whether response contains error code
     * @throws ServiceBackendException if http service return error code
     * @throws ServiceTechnicalException if http service call failed
     */
    protected String invoke(String command, Map<String, String> params) throws ResponseParsingException, ServiceBackendException, ServiceTechnicalException {
        try {
            String response = invoker.invoke(login, password, command, params);
            if (parser.isFailureResponse(response)) {
                throw parser.parseFailureResponse(response);
            }
            return response;
        } catch (ServiceInvokerException ex) {
            throw new ServiceTechnicalException("Couldn't invoke service with '" + command + "' command", ex);
        }
    }

    /**
     * Returns current TextMagic account balance
     *
     * @return current balance value
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if http service call failed or response is unexpected
     */
    public BigDecimal account() throws ServiceBackendException, ServiceTechnicalException {
        Map<String, String> parameters = Collections.emptyMap();
        try {
            String response = invoke(ACCOUNT_COMMAND, parameters);
            return parser.parseAccountResponse(response);
        } catch (ResponseParsingException ex) {
             throw new ServiceTechnicalException(ex.getMessage(), ex);
        }
    }

    /**
     * This is convinient shortcut for <code>messageStatus(List&gtLong&lt messageIds)</code> method 
     *
     * @param messageId id of the message, which status is to be queried
     * @return status of requested message
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if http service call failed or response is unexpected
     */
    public MessageStatus messageStatus(Long messageId) throws ServiceBackendException, ServiceTechnicalException {
        List<MessageStatus> list = messageStatus(Arrays.asList(messageId));
        if(list.isEmpty()) {
            return null;
        }
        if(list.size() > 1){
            throw new ServiceTechnicalException("The server response is unexpected. " +
                    "The response object is not populated with single result: [" + Arrays.toString(list.toArray()) + "]");
        }
        return list.get(0);
    }

    /**
     * Retrieve list of {@link com.textmagic.sms.dto.MessageStatus} dtos from server
     *
     * @param messageIds ids of the messages, whose statuses are to be queried
     * @return list of {@link com.textmagic.sms.dto.MessageStatus} dtos
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if http service call failed or response is unexpected
     */
    public List<MessageStatus> messageStatus(List<Long> messageIds) throws ServiceBackendException, ServiceTechnicalException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ids",StringUtils.join(messageIds, ","));
        try {
            String response = invoke(MESSAGE_STATUS_COMMAND, params);
            return parser.parseMessageStatusResponse(response);
        } catch (ResponseParsingException ex) {
            throw new ServiceTechnicalException(ex.getMessage(), ex);
        }
    }

    /**
     * Retrieves all inbound sms messages from server.
     * <br/>
     * Please check {@link ReceivedMessage} for definition of inbound sms message
     *
     * @return list of {@link ReceivedMessage} dtos
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if http service call failed or response is unexpected
     */
    public List<ReceivedMessage> receive() throws ServiceBackendException, ServiceTechnicalException {
        return receive(0L);
    }

    /**
     * Retrieves all inbound sms messages from server whose id number is bigger than <tt>lastRecievedId</tt>.
     * <br/>
     * Please check {@link ReceivedMessage} for definition of inbound sms message
     *
     * @param lastRecievedId define min value for id of messages to be retrieved
     * @return list of {@link ReceivedMessage} dtos
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if http service call failed or response is unexpected
     */

    public List<ReceivedMessage> receive(Long lastRecievedId) throws ServiceBackendException, ServiceTechnicalException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("last_retrieved_id", lastRecievedId.toString());
        try {
            String response = invoke(RECEIVE_COMMAND, params);
            return parser.parseReceiveResponse(response);
        } catch (ResponseParsingException ex) {
            throw new ServiceTechnicalException(ex.getMessage(), ex);
        }
    }

    /**
     * Deletes inbound sms messages from server
     * <br/>
     * Please check {@link ReceivedMessage} for definition of inbound sms message
     *
     * @param messageIds ids of message to be deleted
     * @return list of ids of messages being actually deleted as result of service call
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if http service call failed or response is unexpected
     */
    public List<Long> deleteReply(List<Long> messageIds) throws ServiceBackendException, ServiceTechnicalException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("ids",StringUtils.join(messageIds, ","));
        try {
            String response = invoke(DELETE_REPLY_COMMAND, params);
            return parser.parseDeleteReplyResponse(response);
        } catch (ResponseParsingException ex) {
            throw new ServiceTechnicalException(ex.getMessage(), ex);
        }
    }

    /**
     * Deletes the exact inbound sms message from server
     * <br/>
     * @param messageId the id of the message to be deleted
     * @return the id of the message being actually deleted
     * @throws ServiceBackendException if server responds with error code
     * @throws ServiceTechnicalException if http service call failed or response is unexpected
     */
    public Long deleteReply(Long messageId) throws ServiceBackendException, ServiceTechnicalException {
        List<Long> list = deleteReply(Arrays.asList(messageId));
        if(list.size() != 1){
            throw new ServiceTechnicalException("The server response is unexpected. " +
                    "The response object is not populated with single result: [" + Arrays.toString(list.toArray()) + "]");
        }
        return list.get(0);
    }
}
