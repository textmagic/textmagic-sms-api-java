package com.textmagic.sms.dto;

/**
 * The data transfer object <code>SentMessage</code> is the servers representation of sms message
 * being sent by clients through TextMagic Gateway.
 *
 * In addition to {@link Message}'s attributes, <code>SentMessage</code> contains:
 * <ul>
 * <li>recipientPhone - the phone number (msisdn) the message was sent to
 * <li>partsCount - the number of parts the message text was divided into 
 * </ul>
 *
 * @author Rafael Bagmanov
 */
public class SentMessage extends Message{
    private String recipientPhone;
    private Short partsCount;

    /**
     * phone number (msisdn) the message was sent to
     * @return phone number
     */
    public String getRecipientPhone() {
        return recipientPhone;
    }

    public void setRecipientPhone(String recipientPhone) {
        this.recipientPhone = recipientPhone;
    }

    /**
     * number of parts the message text was divided into
     * @return number of parts
     */
    public Short getPartsCount() {
        return partsCount;
    }

    public void setPartsCount(Short partsCount) {
        this.partsCount = partsCount;
    }

    @Override
    public String toString() {
        return "SentMessage{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", recipientPhone='" + recipientPhone + '\'' +
                ", partsCount=" + partsCount +
                '}';
    }
}
