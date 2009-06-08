package com.textmagic.sms;

import com.textmagic.sms.dto.ReceivedMessage;
import com.textmagic.sms.dto.MessageStatus;
import com.textmagic.sms.dto.SentMessage;
import com.textmagic.sms.exception.ServiceBackendException;
import com.textmagic.sms.exception.ServiceTechnicalException;

import java.math.BigDecimal;
import java.util.List;

/**
 * <code>MessageService</code> is just a developer-friendly interface of {@link com.textmagic.sms.TextMagicMessageService}
 * <br/>
 * Please, see {@link com.textmagic.sms.TextMagicMessageService} for full specification
 *
 * @author Rafael Bagmanov
 */
public interface MessageService {

    public SentMessage send(String text, String phone) throws ServiceBackendException, ServiceTechnicalException;

    public List<SentMessage> send(String text, List<String> phones) throws ServiceBackendException, ServiceTechnicalException;

    public List<SentMessage> send(String text, List<String> phones, boolean useUnicode, Integer maxLength) throws ServiceBackendException, ServiceTechnicalException;

    public BigDecimal account() throws ServiceBackendException, ServiceTechnicalException;

    public MessageStatus messageStatus(Long messageId) throws ServiceBackendException, ServiceTechnicalException;

    public List<MessageStatus> messageStatus(List<Long> messageIds) throws ServiceBackendException, ServiceTechnicalException;

    public List<ReceivedMessage> receive() throws ServiceBackendException, ServiceTechnicalException;

    public List<ReceivedMessage> receive(Long lastRecievedId) throws ServiceBackendException, ServiceTechnicalException;

    public List<Long> deleteReply(List<Long> messageIds) throws ServiceBackendException, ServiceTechnicalException;

    public Long deleteReply(Long messageId) throws ServiceBackendException, ServiceTechnicalException;


}
