package com.textmagic.sms;

import org.junit.Test;
import org.junit.Before;
import com.textmagic.sms.core.parsing.TextMagicJSONResponseParser;
import com.textmagic.sms.core.parsing.ResponseParsingException;
import com.textmagic.sms.exception.ServiceBackendException;
import com.textmagic.sms.dto.SentMessage;
import com.textmagic.sms.dto.MessageStatus;
import com.textmagic.sms.dto.ReceivedMessage;

import java.util.List;
import java.util.Date;
import java.math.BigDecimal;

import static junit.framework.Assert.*;

/**
 * Date: 23.05.2009
 *
 * @author: bagmanov
 */
public class TextResponseParserImplTest {

    TextMagicJSONResponseParser parser;

    @Before
    public void setUp() throws Exception {
        parser = new TextMagicJSONResponseParser();
    }


    @Test
    public void testIsFailureResponse_Success() throws Exception {
        assertTrue(parser.isFailureResponse("{\"error_code\":10,\"error_message\":\"Wrong parameter value 5 for parameter max_length\"}"));
        assertFalse(parser.isFailureResponse("{\"balance\":\"50\"}"));
    }

    @Test(expected = ResponseParsingException.class)
    public void testIsFailureResponse_Exception() throws Exception {
        parser.isFailureResponse("{incorrect json []}");
    }

    @Test
    public void testParseFailureResponse() throws Exception {
        ServiceBackendException exception = parser.parseFailureResponse("{\"error_code\":8,\"error_message\":\"IP address is not allowed\"}");
        assertEquals(8, exception.getErrorCode().intValue());
        assertEquals("IP address is not allowed", exception.getErrorMessage());
    }

    @Test(expected = ResponseParsingException.class)
    public void testParseFailureResponse_IncorrectJson() throws Exception {
        parser.parseFailureResponse("{\"error_code\":8, {incorrect json []}}");
    }

    @Test(expected = ResponseParsingException.class)
    public void testParseFailureResponse_NotFailureResonse() throws Exception {
        parser.parseFailureResponse("{\"balance\":\"50\"}");
    }

    @Test
    public void testParseSendResponse() throws Exception {
        String jsonResponse = "{\"message_id\":{\"1\":\"phone1\", \"2\":\"phone2\" },\"sent_text\":\"plain english\",\"parts_count\":2}";
        List<SentMessage> result = parser.parseSendResponse(jsonResponse);
        SentMessage firstMessage = result.get(0);
        SentMessage secondMessage = result.get(1);
        if (firstMessage.getId() == 2) {
            SentMessage temp = firstMessage;
            firstMessage = secondMessage;
            secondMessage = temp;
        }
        assertEquals(1, firstMessage.getId().longValue());
        assertEquals("plain english", firstMessage.getText());
        assertEquals(2, firstMessage.getPartsCount().shortValue());
        assertEquals("phone1", firstMessage.getRecipientPhone());

        assertEquals(2, secondMessage.getId().longValue());
        assertEquals("plain english", secondMessage.getText());
        assertEquals(2, secondMessage.getPartsCount().shortValue());
        assertEquals("phone2", secondMessage.getRecipientPhone());
    }

    @Test(expected = ResponseParsingException.class)
    public void testParseSendResponse_NotResponseMessage() throws Exception {
        parser.parseSendResponse("{\"balance\":\"50\"}");
    }

    @Test(expected = ResponseParsingException.class)
    public void testParseSendResponse_EmptyMessageId() throws Exception {
        String jsonResponse = "{\"message_id\":{},\"sent_text\":\"plain english\",\"parts_count\":2}";
        parser.parseSendResponse(jsonResponse);
    }

    @Test
    public void testParseAccountResponse() throws Exception {
        BigDecimal balance = parser.parseAccountResponse("{\"balance\":\"45.5\"}");
        assertTrue(45.5 == balance.doubleValue());
    }

    @Test(expected = ResponseParsingException.class)
    public void testParseAccountResponse_InvalidValue() throws Exception {
        BigDecimal balance = parser.parseAccountResponse("{\"balance\":\"aaaa\"}");
    }

    @Test
    public void testParseMessageStatusResponse() throws Exception {
        String json = "{\"1\":" +
                "{\"text\":\"message1\"," +
                "\"status\":\"d\"," +
                "\"created_time\":\"1242937000\"," +
                "\"reply_number\":\"987654\"," +
                "\"completed_time\":null," +
                "\"credits_cost\":\"0.5\"}," +
                "\"2\":" +
                "{\"text\":\"message2\"," +
                "\"status\":\"j\"," +
                "\"created_time\":\"1234567\"," +
                "\"reply_number\":\"12345\"," +
                "\"completed_time\": \"111111\"," +
                "\"credits_cost\":\"6.5\"}," +
                "}";

        List<MessageStatus> result = parser.parseMessageStatusResponse(json);

        MessageStatus firstResult = result.get(0);
        MessageStatus secondResult = result.get(1);
        if (firstResult.getMessage().getId() == 2) {
            MessageStatus temp = secondResult;
            secondResult = firstResult;
            firstResult = temp;
        }

        assertEquals(1, firstResult.getMessage().getId().longValue());
        assertEquals("message1", firstResult.getMessage().getText());
        assertEquals(new Date(1242937000L * 1000L), firstResult.getCreatedTime());
        assertNull(firstResult.getCompletedTime());
        assertEquals("987654", firstResult.getReplyNumber());
        assertEquals(0.5D, firstResult.getCreditsCost().doubleValue());
        assertEquals(MessageStatus.DeliveryState.Delivered, firstResult.getDeliveryState());

        assertEquals(2, secondResult.getMessage().getId().longValue());
        assertEquals("message2", secondResult.getMessage().getText());
        assertEquals(new Date(1234567 * 1000L), secondResult.getCreatedTime());
        assertEquals(new Date(111111 * 1000L), secondResult.getCompletedTime());
        assertEquals("12345", secondResult.getReplyNumber());
        assertEquals(6.5D, secondResult.getCreditsCost().doubleValue());
        assertEquals(MessageStatus.DeliveryState.Rejected, secondResult.getDeliveryState());

    }

    @Test
    public void test_UnknowState() throws Exception{
        String json = "{\"1\":" +
                  "{\"text\":\"message1\"," +
                  "\"status\":\"z\"," +
                  "\"created_time\":\"1242937000\"," +
                  "\"reply_number\":\"987654\"," +
                  "\"completed_time\":null," +
                  "\"credits_cost\":\"0.5\"}" +
                  "}";

       List<MessageStatus> result = parser.parseMessageStatusResponse(json);

        MessageStatus firstResult = result.get(0);

        assertEquals(1, firstResult.getMessage().getId().longValue());
        assertEquals("message1", firstResult.getMessage().getText());
        assertEquals(new Date(1242937000L * 1000L), firstResult.getCreatedTime());
        assertNull(firstResult.getCompletedTime());
        assertEquals("987654", firstResult.getReplyNumber());
        assertEquals(0.5D, firstResult.getCreditsCost().doubleValue());
        assertEquals(MessageStatus.DeliveryState.Unknown, firstResult.getDeliveryState());

    }

    @Test(expected = ResponseParsingException.class)
    public void testParseMessageStatus_IncorrectResponse() throws Exception{
        parser.parseMessageStatusResponse("{\"balance\":\"aaaa\"}");
    }

    @Test(expected = ResponseParsingException.class)
    public void testParseMessageStatus_NumerFormatException() throws Exception{
        String json = "{\"1\":" +
                  "{\"text\":\"message1\"," +
                  "\"status\":\"z\"," +
                  "\"created_time\":\"12429a37000\"," +
                  "\"reply_number\":\"9876a54\"," +
                  "\"completed_time\":null," +
                  "\"credits_cost\":\"0.5\"}" +
                  "}";
        parser.parseMessageStatusResponse(json);
    }

    @Test
    public void testParseRecieveResponse() throws Exception {
        String json = "{\"messages\":" +
                            "[{\"message_id\":\"1\"," +
                                 "\"from\":\"from1\"," +
                                 "\"timestamp\":1242939052," +
                                 "\"text\":\"message1\"}," +
                              "{\"message_id\":\"2\"," +
                                 "\"from\":\"from2\"," +
                                 "\"timestamp\":1242939235," +
                                 "\"text\":\"message2\"}]," +
                        "\"unread\":0}";
        List<ReceivedMessage> result = parser.parseReceiveResponse(json);
        ReceivedMessage firstResult = result.get(0);
        ReceivedMessage secondResult = result.get(1);
        if (firstResult.getId() == 2) {
            ReceivedMessage temp = secondResult;
            secondResult = firstResult;
            firstResult = temp;
        }
        assertEquals(1, firstResult.getId().longValue());
        assertEquals("from1", firstResult.getSenderPhone());
        assertEquals("message1", firstResult.getText());
        assertEquals(new Date(1242939052 * 1000L), firstResult.getReceivedDate());

        assertEquals(2, secondResult.getId().longValue());
        assertEquals("from2", secondResult.getSenderPhone());
        assertEquals("message2", secondResult.getText());
        assertEquals(new Date(1242939235 * 1000L), secondResult.getReceivedDate());
    }

    @Test
    public void testParseRecieveResponse_NoMessages() throws Exception {
        String json = "{\"messages\": []," +
                        "\"unread\":0}";
        List<ReceivedMessage> result = parser.parseReceiveResponse(json);
        assertEquals(0, result.size());
    }

    @Test
    public void testParseDeleteReplyResoinse() throws Exception{
        String json = "{\"deleted\":[\"1\", \"2\", \"3\"]}";
        List<Long> ids = parser.parseDeleteReplyResponse(json);
        assertEquals(3, ids.size());
        assertTrue(ids.contains(1L));
        assertTrue(ids.contains(2L));
        assertTrue(ids.contains(3L));
    }

    @Test(expected = ResponseParsingException.class)
    public void testParseDeleteReplyResponse_WrongResponse() throws Exception{
        parser.parseDeleteReplyResponse("{\"balance\":\"50\"}");

    }


}
