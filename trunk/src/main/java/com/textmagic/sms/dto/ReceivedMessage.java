package com.textmagic.sms.dto;

import java.util.Date;

/**
 * The data transfer object <code>ReceivedMessage</code> is representation of
 * inbound sms messages received by server.
 *
 * The inbound messages appear on TextMagic server in case of somebody replied to the message
 * being sent through the gateway or sends message to your sender ID (see <a href = "http://api.textmagic.com/https-api/sender-id">Sms Reply Options</a>)
 *
 * In addition to {@link com.textmagic.sms.dto.Message}'s attributes, <code>RecievedMessage</code> provides:
 * <ul>
 * <li> senderPhone  - the phone number (MSISDN) of the message sender
 * <li> receivedDate - {@link java.util.Date} when sms message was received by server
 * <ul>
 *
 *
 * @author Rafael Bagmanov
 */
public class ReceivedMessage extends Message{
    private String senderPhone;
    private Date receivedDate;

    /**
     * phone number (MSISDN) of the message sender
     * @return phone number
     */
    public String getSenderPhone() {
        return senderPhone;
    }

    public void setSenderPhone(String senderPhone) {
        this.senderPhone = senderPhone;
    }

    /**
     * time when sms message was received by server
     * @return receiving time
     */
    public Date getReceivedDate() {
        return receivedDate;
    }

    public void setReceivedDate(Date recievedDate) {
        this.receivedDate = recievedDate;
    }

    @Override
    public String toString() {
        return "ReceivedMessage{" +
                "id=" + id +
                ", text='" + text + '\'' +
                ", senderPhone='" + senderPhone + '\'' +
                ", receivedDate=" + receivedDate +
                '}';
    }
}
