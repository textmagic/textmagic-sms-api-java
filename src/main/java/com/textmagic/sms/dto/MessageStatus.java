package com.textmagic.sms.dto;

import java.util.Date;
import java.math.BigDecimal;

/**
 * The data transfer object <code>MessageStatus</code> is representation of message delivering status.
 *<br/><br/>
 * The main attributes are:
 * <ul>
 * <li> message - the message object, whose status this <tt><MessageStatus/tt> object represents
 * <li> deliveryState - current state of delivering progress. see {@link com.textmagic.sms.dto.MessageStatus.DeliveryState}
 * <li> createdTime - the {@link Date} when sms was created
 * <li> replyNumber - the phone number (MSISDN) the message recipient can reply to. See <a href = "http://api.textmagic.com/https-api/sender-id">Reply options</a>
 * <li> creditsCost - the cost of the sent message. the value will be provided only if delivering completed (the <tt>deliveryState</tt> is final.)
 * <li> completedTime - the {@link Date} when delivering was completed. the value will be provided only if delivering is over (the <tt>deliveryState</tt> is final)
 * </ul>
 *
 * @author Rafael Bagmanov
 */
public class MessageStatus {
    /**
     * DeliveryState is used to indicate current state of messages being sent through the TextMagic gateway
     * In addition, enum provide 2 attributes:
     * <ul>
     * <li> isFinal - if isFinal is false, this means message delivering is in progress, otherwise delivering ended
     * <li> description - user friendly message text explaining current state  
     * </ul>
     *
     */
    public static enum DeliveryState {
        QueuedAtTextMagic (false, "The message is queued on the TextMagic server."),
        SentToOperator (false, "The message has been sent to the mobile operator"),
        AcknowledgedByOperator (false, "The message is acknowledged by mobile operator"),
        QueuedAtOperator (false, "The message has been queued by the mobile operator"),
        Delivered (true, "The message has been successfully delivered to the handset"),
        SendingError (true, "There is an error while delivering message."),
        DeliveringError (true, "There is an error while sending message."),
        Rejected (true, "The message is rejected by mobile operator."),
        Unknown (true, "The status is unknown.");

        private boolean isFinal;
        private String description;

        /**
         * getter for isFinal
         * @return if isFinal is false, this means message delivering is in progress, otherwise delivering ended
         */
        public boolean isFinal() {
            return isFinal;
        }

        /**
         * Getter for description
         * @return user friendly message text explaining current state
         */
        public String getDescription() {
            return description;
        }

        DeliveryState(boolean aFinal, String description) {
            isFinal = aFinal;
            this.description = description;
        }
    }

    private Message message;
    private DeliveryState deliveryState;
    private Date createdTime;
    private String replyNumber;
    private BigDecimal creditsCost;
    private Date completedTime;

    /**
     * message object, whose status the <tt><MessageStatus/tt> object represents
     * @return message object
     */
    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * current state of delivering progress
     * @return delivering progress state
     */
    public DeliveryState getDeliveryState() {
        return deliveryState;
    }

    public void setDeliveryState(DeliveryState deliveryState) {
        this.deliveryState = deliveryState;
    }

    /**
     * time when sms was created
     * @return creation date
     */
    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    /**
     * phone number (MSISDN) the message recipient can reply to.
     * @return the phone number
     */
    public String getReplyNumber() {
        return replyNumber;
    }

    public void setReplyNumber(String replyNumber) {
        this.replyNumber = replyNumber;
    }

    /**
     * the cost of the sent message
     * @return credit cost
     */
    public BigDecimal getCreditsCost() {
        return creditsCost;
    }

    public void setCreditsCost(BigDecimal creditsCost) {
        this.creditsCost = creditsCost;
    }

    /**
     * time when delivering was completed
     * @return completion time
     */
    public Date getCompletedTime() {
        return completedTime;
    }

    public void setCompletedTime(Date completedTime) {
        this.completedTime = completedTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MessageStatus that = (MessageStatus) o;

        if (!message.equals(that.message)) return false;
        if (deliveryState != that.deliveryState) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = message.hashCode();
        result = 31 * result + deliveryState.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "MessageStatus{" +
                "message=" + message.toString() +
                ", deliveryState=" + deliveryState +
                ", createdTime=" + createdTime +
                ", replyNumber='" + replyNumber + '\'' +
                ", creditsCost=" + creditsCost +
                ", completedTime=" + completedTime +
                '}';
    }
}
