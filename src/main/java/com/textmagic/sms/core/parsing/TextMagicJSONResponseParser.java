package com.textmagic.sms.core.parsing;

import com.textmagic.sms.exception.ServiceBackendException;
import com.textmagic.sms.dto.*;

import java.util.*;
import java.math.BigDecimal;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The default {@link com.textmagic.sms.core.parsing.TextMagicResponseParser} implementation.
 * Parses JSON-based http api responses.<br/>
 * The implementation use "org.json" library for JSON parsing
 *
 * @author Rafael Bagmanov
 */
public class TextMagicJSONResponseParser implements TextMagicResponseParser {

    private final static Log log = LogFactory.getLog(TextMagicJSONResponseParser.class);

    private static final Map<String, MessageStatus.DeliveryState> codeToStateMap = new HashMap<String, MessageStatus.DeliveryState>();
    static {
        codeToStateMap.put ("q", MessageStatus.DeliveryState.QueuedAtTextMagic);
        codeToStateMap.put ("r", MessageStatus.DeliveryState.SentToOperator);
        codeToStateMap.put ("a", MessageStatus.DeliveryState.AcknowledgedByOperator);
        codeToStateMap.put ("b", MessageStatus.DeliveryState.QueuedAtOperator);
        codeToStateMap.put ("d", MessageStatus.DeliveryState.Delivered);
        codeToStateMap.put ("f", MessageStatus.DeliveryState.DeliveringError);
        codeToStateMap.put ("e", MessageStatus.DeliveryState.SendingError);
        codeToStateMap.put ("j", MessageStatus.DeliveryState.Rejected);
        codeToStateMap.put ("u", MessageStatus.DeliveryState.Unknown);
    }

    private RawResponseTextConverter textConverter;

    public TextMagicJSONResponseParser() {
        textConverter = new RawResponseTextConverter();
    }

    public boolean isFailureResponse(String response) throws ResponseParsingException {
        try {
            JSONObject responseObject = new JSONObject(response);
            return responseObject.has("error_code");
        } catch (JSONException e) {
            throw new ResponseParsingException("Couldn't determine whether response '" + response + "' contains error code", e);
        }
    }

    public ServiceBackendException parseFailureResponse(String response) throws ResponseParsingException {
        try {
            JSONObject responseObject = new JSONObject(response);
            Integer errorCode = responseObject.getInt("error_code");
            String errorMessage = responseObject.getString("error_message");
            return new ServiceBackendException(errorCode, errorMessage);
        } catch (JSONException e) {
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway failure response", e);
        }
    }

    public List<SentMessage> parseSendResponse(String response) throws ResponseParsingException {
        try {
            List<SentMessage> result = new ArrayList<SentMessage>();
            JSONObject responseObject = new JSONObject(response);
            String sentText = textConverter.convert(responseObject.getString("sent_text"));
            Short partsCount = (short)responseObject.getInt("parts_count");
            JSONObject response2 = responseObject.getJSONObject("message_id");
            Iterator<String> messageIdIterator = response2.keys();
            if(!messageIdIterator.hasNext()){
                throw new ResponseParsingException("Gateway response '" + response + "' is unexpected - message_id cant be empty");
            }
            while(messageIdIterator.hasNext()) {
                String messageId = messageIdIterator.next();
                SentMessage message = new SentMessage();
                message.setId(new Long(messageId));
                message.setRecipientPhone(response2.getString(messageId));
                message.setText(sentText);
                message.setPartsCount(partsCount);
                result.add(message);
            }
            return result;
        } catch (JSONException e){
           throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'send' response", e);
        } catch (NumberFormatException e){
           throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'send' response", e);
        }
    }

    public BigDecimal parseAccountResponse(String response) throws ResponseParsingException {
        try {
            JSONObject responseObject = new JSONObject(response);
            return new BigDecimal(responseObject.getString("balance"));
        } catch (JSONException e) {
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'account' response", e);
        } catch (NumberFormatException e){
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'account' response", e);
        }
    }

    public List<MessageStatus> parseMessageStatusResponse(String response) throws ResponseParsingException {
        try {
            List<MessageStatus> result = new ArrayList<MessageStatus>();
            JSONObject responseObject = new JSONObject(response);
            Iterator<String> messageIdIterator = responseObject.keys();
            while(messageIdIterator.hasNext()){
                String messageId = messageIdIterator.next();
                JSONObject statusObject = responseObject.getJSONObject(messageId);
                MessageStatus messageStatus = new MessageStatus();
                String messageText = textConverter.convert(statusObject.getString("text"));
                Message message = new Message(new Long(messageId), messageText);
                messageStatus.setMessage(message);
                Date createdTime = new Date(statusObject.getLong("created_time") * 1000L);
                messageStatus.setCreatedTime(createdTime);
                if(statusObject.has("completed_time") && !statusObject.isNull("completed_time")) {
                    Date completedTime = new Date(statusObject.getLong("completed_time") * 1000L);
                    messageStatus.setCompletedTime(completedTime);
                }
                if(statusObject.has("credits_cost") && !statusObject.isNull("credits_cost")){
                    messageStatus.setCreditsCost(new BigDecimal(statusObject.getString("credits_cost")));
                }
                messageStatus.setReplyNumber(statusObject.getString("reply_number"));
                messageStatus.setDeliveryState(parseState(statusObject.getString("status")));
                result.add(messageStatus);
            }
            return  result;
        } catch (JSONException e) {
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'message_status' response", e);
        } catch (NumberFormatException e){
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'message_status' response", e);
        }

    }

    private MessageStatus.DeliveryState parseState(String statusStr) throws ResponseParsingException {
        MessageStatus.DeliveryState state = codeToStateMap.get(statusStr);
        if(state == null){
            log.warn("Recieved message state '"+ statusStr+"' is unknown");
            state = MessageStatus.DeliveryState.Unknown;
        }
        return state;
    }


    public List<ReceivedMessage> parseReceiveResponse(String response) throws ResponseParsingException {
        try {
            List<ReceivedMessage> result = new ArrayList<ReceivedMessage>();
            JSONObject responseObject = new JSONObject(response);
            JSONArray messageArray = responseObject.getJSONArray("messages");
            int length = messageArray.length();
            for(int i = 0; i < length; i++){

                JSONObject messageObjet = messageArray.getJSONObject(i);

                ReceivedMessage message = new ReceivedMessage();
                message.setId(messageObjet.getLong("message_id"));

                String messageText = textConverter.convert(messageObjet.getString("text"));
                message.setText(messageText);
                message.setSenderPhone(messageObjet.getString("from"));
                Date receivedDate = new Date(messageObjet.getLong("timestamp") * 1000L);
                message.setReceivedDate(receivedDate);
                result.add(message);
            }
            return  result;
        } catch (JSONException e) {
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'receive' response", e);
        } catch (NumberFormatException e){
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'receive' response", e);
        }

    }

    public List<Long> parseDeleteReplyResponse(String response) throws ResponseParsingException {
        try {
            List<Long> ids = new ArrayList<Long>();
            JSONArray idArray = new JSONObject(response).getJSONArray("deleted");
            int length = idArray.length();
            for (int i = 0; i < length; i++) {
                ids.add(idArray.getLong(i));
            }
            return ids;
        } catch (JSONException e) {
            throw new ResponseParsingException("Couldn't determine whether response '" + response + "' contains error code", e);
        }

    }

    public List<PhoneInfo> parseCheckNumberResponse(String response) throws ResponseParsingException {
        try {
            List<PhoneInfo> phoneInfos = new ArrayList<PhoneInfo>();
            JSONObject responseObject = new JSONObject(response);
            Iterator<String> phoneInfoIterator = responseObject.keys();
            while(phoneInfoIterator.hasNext()){
                String phone = phoneInfoIterator.next();
                JSONObject infoJSON = responseObject.getJSONObject(phone);
                PhoneInfo info = new PhoneInfo();
                info.setPhone(phone);
                info.setCoutryCode(infoJSON.getString("country"));
                info.setPrice(new BigDecimal(infoJSON.getString("price")));
                phoneInfos.add(info);
            }
            return phoneInfos;
        } catch (JSONException e) {
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'check_number' response", e);
        } catch (NumberFormatException e){
            throw new ResponseParsingException("Couldn't parse '" + response + "' as gateway 'check_number' response", e);
        }
    }
}
