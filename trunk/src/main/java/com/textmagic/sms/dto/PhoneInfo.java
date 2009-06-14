package com.textmagic.sms.dto;

import java.math.BigDecimal;

/**
 * Data transfer object <code>PhoneInfo</code> provides information about phone number, i.e. the country phone registered in
 * and price of single message to this destination
 *
 * @author Rafael Bagmanov
 */
public class PhoneInfo {
    private String phone;
    private BigDecimal price;
    private String coutryCode;

    /**
     * Phone number  
     *
     * @return MSISDN
     */
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Price of the single message to this destination phone number
     *
     * @return price price value
     */
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /**
     * code of a country, the phone number registered in
     *
     * @return country code
     */
    public String getCoutryCode() {
        return coutryCode;
    }

    public void setCoutryCode(String coutryCode) {
        this.coutryCode = coutryCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PhoneInfo phoneInfo = (PhoneInfo) o;

        if (phone != null ? !phone.equals(phoneInfo.phone) : phoneInfo.phone != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = phone != null ? phone.hashCode() : 0;
        return result;
    }

    @Override
    public String toString() {
        return "PhoneInfo{" +
                "phone='" + phone + '\'' +
                ", price=" + price +
                ", coutryCode='" + coutryCode + '\'' +
                '}';
    }
}
