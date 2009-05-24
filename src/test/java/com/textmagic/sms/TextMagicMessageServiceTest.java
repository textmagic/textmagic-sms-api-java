package com.textmagic.sms;

import org.junit.runner.RunWith;
import org.junit.Before;
import org.junit.Test;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.integration.junit4.JMock;
import org.jmock.Expectations;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.allOf;
import org.hamcrest.Matchers;
import com.textmagic.sms.core.invoker.HttpServiceInvoker;
import com.textmagic.sms.core.invoker.ServiceInvokerException;
import com.textmagic.sms.core.parsing.TextMagicResponseParser;
import com.textmagic.sms.core.parsing.ResponseParsingException;
import com.textmagic.sms.dto.SentMessage;
import com.textmagic.sms.dto.MessageStatus;
import com.textmagic.sms.dto.Message;
import com.textmagic.sms.dto.ReceivedMessage;
import com.textmagic.sms.exception.ServiceTechnicalException;
import com.textmagic.sms.exception.ServiceBackendException;
import com.textmagic.sms.util.StringUtils;

import java.util.*;
import java.math.BigDecimal;

import static junit.framework.Assert.*;

/**
 * Date: 23.05.2009
 *
 * @author: bagmanov
 */
@RunWith(JMock.class)
public class TextMagicMessageServiceTest {
    JUnit4Mockery context = new JUnit4Mockery();
    TextMagicMessageService service;
    private static final String LOGIN = "myLogin";
    private static final String PASSWORD = "myPassword";
    HttpServiceInvoker serviceInvoker;
    TextMagicResponseParser responseParser;
    private static final String MY_TEXT = "my text";
    private static final String CORRECT_MSISDN = "79261234567";

    @Before
    public void setUp() throws Exception {
        serviceInvoker = context.mock(HttpServiceInvoker.class);
        responseParser = context.mock(TextMagicResponseParser.class);

        service = new TextMagicMessageService(LOGIN, PASSWORD);
        service.setInvoker(serviceInvoker);
        service.setParser(responseParser);
    }

    @Test
    public void testSend() throws Exception{
        final List<SentMessage> messages = new ArrayList<SentMessage>();
        SentMessage message =  new SentMessage();
        message.setId(2L);
        messages.add(message);
        final String invokerResponse = "dummy response";
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("send")),
                    with(allOf(
                            Matchers.hasEntry("text", MY_TEXT),
                            Matchers.hasEntry("unicode", "0"),
                            Matchers.hasEntry("max_length", "3"),
                            Matchers.hasEntry("phone", CORRECT_MSISDN)
                    ))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseSendResponse(invokerResponse);will(returnValue(messages));
        }});
        SentMessage result = service.send(MY_TEXT, CORRECT_MSISDN);
        assertSame(message, result);
    }

    @Test (expected = ServiceTechnicalException.class)
    public void testSend_NoSentMessageResponse() throws Exception{
        final List<SentMessage> messages = new ArrayList<SentMessage>();
        final String invokerResponse = "dummy response";
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("send")),
                    with(allOf(
                            Matchers.hasEntry("text", MY_TEXT),
                            Matchers.hasEntry("unicode", "0"),
                            Matchers.hasEntry("max_length", "3"),
                            Matchers.hasEntry("phone", CORRECT_MSISDN)
                    ))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseSendResponse(invokerResponse);will(returnValue(messages));
        }});
        SentMessage result = service.send(MY_TEXT, CORRECT_MSISDN);
    }

    @Test
    public void testSend_BackendException() throws Exception{
        final String invokerResponse = "dummy response";
        final ServiceBackendException backendExc = new ServiceBackendException(1,"2");
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("send")),
                    with(allOf(
                            Matchers.hasEntry("text", MY_TEXT),
                            Matchers.hasEntry("unicode", "0"),
                            Matchers.hasEntry("max_length", "3"),
                            Matchers.hasEntry("phone", CORRECT_MSISDN)
                    ))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(true));
            one(responseParser).parseFailureResponse(invokerResponse);will(returnValue(backendExc));
        }});
        try {
            service.send(MY_TEXT, CORRECT_MSISDN);
            fail("exception should be thrown");
        } catch (ServiceBackendException e) {
            assertSame(backendExc, e);
        }
    }

    @Test
    public void testSend_InvalidMSISDN_WrongLength() throws Exception{
        String phone = "12345678901234567890123";
        try {
            service.send(MY_TEXT, phone);
            fail("exception should be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(phone));
        }
    }
    @Test
    public void testSend_InvalidMSISDN_InvalidCharactes() throws Exception{
        String phone = "1234567890aaaa";
        try {
            service.send(MY_TEXT, phone);
            fail("exception should be thrown");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains(phone));
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSend_MessageIsTooLong() throws Exception {
        String message = StringUtils.repeat("1", 600);
        service.send(message, CORRECT_MSISDN);

    }

    @Test
    public void testSendUnicode() throws Exception {
        final String text = "это настоящий русский текст";
        final String invokerResponse = "dummy response";
        final ServiceBackendException backendExc = new ServiceBackendException(1,"2");
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("send")),
                    with(allOf(
                            Matchers.hasEntry("text", text),
                            Matchers.hasEntry("unicode", "1"),
                            Matchers.hasEntry("max_length", "3"),
                            Matchers.hasEntry("phone", CORRECT_MSISDN)
                    ))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(true));
            one(responseParser).parseFailureResponse(invokerResponse);will(returnValue(backendExc));
        }});
        try {
            service.send(text, CORRECT_MSISDN);
            fail("exception should be thrown");
        } catch (ServiceBackendException e) {
            assertSame(backendExc, e);
        }
    }

    @Test
    public void testSendUnicode_TooLong() throws Exception {
        final String text = StringUtils.repeat("р", 221);
        try {
            service.send(text, CORRECT_MSISDN);
            fail("exception should be thrown");
        } catch (IllegalArgumentException e) {
            /* passed */
        }
    }

    @Test
    public void testAccount() throws Exception {
        final String invokerResponse = "dummy response";
        final ServiceBackendException backendExc = new ServiceBackendException(1,"2");
        final Map<String,String> emptyMap = Collections.emptyMap();
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("account")),
                    with(equalTo(emptyMap))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseAccountResponse(invokerResponse); will(returnValue(new BigDecimal(6.5)));
        }});
        BigDecimal result = service.account();
        assertEquals(new BigDecimal(6.5), result);
    }

    @Test (expected = ServiceTechnicalException.class)
    public void testAccount_ParserException() throws Exception {
        final String invokerResponse = "dummy response";
        final Map<String,String> emptyMap = Collections.emptyMap();
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("account")),
                    with(equalTo(emptyMap))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseAccountResponse(invokerResponse); will(throwException(new ResponseParsingException()));
        }});
        BigDecimal result = service.account();
        assertEquals(new BigDecimal(6.5), result);
    }

    @Test
    public void testMessageStatus_List() throws Exception {
        final String invokerResponse = "dummy response";
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        final List<MessageStatus> parsingResult = Collections.emptyList();
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("message_status")),
                    with(Matchers.hasEntry("ids", "1,2,3"))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseMessageStatusResponse(invokerResponse); will(returnValue(parsingResult));
        }});
        List<MessageStatus> result = service.messageStatus(ids);
        assertSame(parsingResult, result);
    }

    @Test (expected = ServiceTechnicalException.class)
    public void testMessageStatus_List_ParsingException() throws Exception {
        final String invokerResponse = "dummy response";
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        final List<MessageStatus> parsingResult = Collections.emptyList();
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("message_status")),
                    with(Matchers.hasEntry("ids", "1,2,3"))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseMessageStatusResponse(invokerResponse); will(throwException(new ResponseParsingException()));
        }});
        List<MessageStatus> result = service.messageStatus(ids);
    }

    @Test
     public void testMessageStatus() throws Exception {
         final String invokerResponse = "dummy response";
         Long id = 1L;
         final MessageStatus messageStatus = new MessageStatus();
         final List<MessageStatus> parsingResult = Arrays.asList(messageStatus);
         context.checking(new Expectations() {{
             one(serviceInvoker).invoke(
                     with(equalTo(LOGIN)),
                     with(equalTo(PASSWORD)),
                     with(equalTo("message_status")),
                     with(Matchers.hasEntry("ids", "1"))
             ); will(returnValue(invokerResponse));
             one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
             one(responseParser).parseMessageStatusResponse(invokerResponse); will(returnValue(parsingResult));
         }});
         MessageStatus result = service.messageStatus(id);
         assertSame(messageStatus, result);
     }

    @Test
     public void testMessageStatus_EmptyList() throws Exception {
         final String invokerResponse = "dummy response";
         Long id = 1L;
         final List<MessageStatus> parsingResult = Collections.emptyList();
         context.checking(new Expectations() {{
             one(serviceInvoker).invoke(
                     with(equalTo(LOGIN)),
                     with(equalTo(PASSWORD)),
                     with(equalTo("message_status")),
                     with(Matchers.hasEntry("ids", "1"))
             ); will(returnValue(invokerResponse));
             one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
             one(responseParser).parseMessageStatusResponse(invokerResponse); will(returnValue(parsingResult));
         }});
         assertNull(service.messageStatus(id));
     }

     @Test (expected = ServiceTechnicalException.class)
     public void testMessageStatus_TwoElementsInList() throws Exception {
         final String invokerResponse = "dummy response";
         Long id = 1L;
         MessageStatus status = new MessageStatus();
         status.setMessage(new Message(1L, "dummyText"));
         status.setDeliveryState(MessageStatus.DeliveryState.AcknowledgedByOperator);
         MessageStatus status2 = new MessageStatus();
         status2.setMessage(new Message(2L, "text"));
         status2.setDeliveryState(MessageStatus.DeliveryState.QueuedAtOperator);
         final List<MessageStatus> parsingResult = Arrays.asList(status, status2);
         context.checking(new Expectations() {{
             one(serviceInvoker).invoke(
                     with(equalTo(LOGIN)),
                     with(equalTo(PASSWORD)),
                     with(equalTo("message_status")),
                     with(Matchers.hasEntry("ids", "1"))
             ); will(returnValue(invokerResponse));
             one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
             one(responseParser).parseMessageStatusResponse(invokerResponse); will(returnValue(parsingResult));
         }});
         MessageStatus result = service.messageStatus(id);
     }

     @Test
     public void testReceiveAll() throws Exception {
        final String invokerResponse = "dummy response";
        final List<ReceivedMessage> parsingResult = Collections.emptyList();
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("receive")),
                    with(Matchers.hasEntry("last_retrieved_id", "0"))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseReceiveResponse(invokerResponse); will(returnValue(parsingResult));
        }});
        List<ReceivedMessage> result = service.receive();
        assertSame(parsingResult, result);
    }
    
    @Test(expected = ServiceTechnicalException.class)
    public void testReceive_With_LastRetreivedId_ParseException() throws Exception {
        final String invokerResponse = "dummy response";
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("receive")),
                    with(Matchers.hasEntry("last_retrieved_id", "5"))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseReceiveResponse(invokerResponse); will(throwException(new ResponseParsingException()));
        }});
        List<ReceivedMessage> result = service.receive(5L);
    }

   @Test
    public void testDeleteReply_List() throws Exception {
        final String invokerResponse = "dummy response";
        List<Long> ids = Arrays.asList(1L, 2L, 3L);
        final List<Long> parsingResult = Collections.emptyList();
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("delete_reply")),
                    with(Matchers.hasEntry("ids", "1,2,3"))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseDeleteReplyResponse(invokerResponse); will(returnValue(parsingResult));
        }});
        List<Long> result = service.deleteReply(ids);
        assertSame(parsingResult, result);
    }

    @Test
    public void testDeleteReply() throws Exception {
        final String invokerResponse = "dummy response";

        final List<Long> parsingResult = Arrays.asList(1L);
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("delete_reply")),
                    with(Matchers.hasEntry("ids", "1"))
            ); will(returnValue(invokerResponse));
            one(responseParser).isFailureResponse(invokerResponse); will(returnValue(false));
            one(responseParser).parseDeleteReplyResponse(invokerResponse); will(returnValue(parsingResult));
        }});
        Long result = service.deleteReply(1L);
        assertEquals(1L, result.longValue());
    }
    @Test (expected = ServiceTechnicalException.class)
    public void testDeleteReply_ServiceInvokerException() throws Exception {
        final String invokerResponse = "dummy response";

        final List<Long> parsingResult = Arrays.asList(1L);
        context.checking(new Expectations() {{
            one(serviceInvoker).invoke(
                    with(equalTo(LOGIN)),
                    with(equalTo(PASSWORD)),
                    with(equalTo("delete_reply")),
                    with(Matchers.hasEntry("ids", "1"))
            ); will(throwException(new ServiceInvokerException("")));
        }});
        Long result = service.deleteReply(1L);
    }


}
