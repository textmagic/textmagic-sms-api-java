package com.textmagic.sms.dto;

/**
 * The data transfer object <code>Message</code> is the base representation of
 * all sms message objects being passed between TextMagic server and its clients.
 * <br/><br/>
 * The main attributes are:
 * <ul>
 *  <li> id - the message primary key. The attribute unambiguously defines the message on server.
 *  <li> text - the sms message content
 * </ul>
 *
 * @author Rafael Bagmanov
 */
public class Message {
    protected Long id;
    protected String text;

    public Message() {
    }

    public Message(Long id, String text) {
        this.id = id;
        this.text = text;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message that = (Message) o;

        if (!id.equals(that.id)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", text='" + text + '\'' +
                '}';
    }
}
