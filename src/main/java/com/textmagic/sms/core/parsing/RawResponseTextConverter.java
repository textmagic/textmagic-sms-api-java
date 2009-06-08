package com.textmagic.sms.core.parsing;

import java.io.UnsupportedEncodingException;

/**
 * Currently the TextMagic gateway, in case of sms body being encoded in Unicode, http api represents it as
 * "\\uXXXX\\uXXXX\..." codes. <code>RawReponseTextConverter </code> translates this codes into readable java
 * String

 * @author Rafael Bagmanov
 */
public class RawResponseTextConverter {
    /**
     * Convert raw String with hex codes to decoded string
     *
     * @param text Raw "\\uXXXX\\uXXXX" text received from gateway
     * @return readable decoded text
     */
    public String convert(String text) {
        try {
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < text.length(); i++) {
                if(text.charAt(i) == '\\' && (i != text.length() -1) && text.charAt(i+1) == 'u'){
                    Short intVal = Short.valueOf(text.substring(i+2, i+6), 16);
                    result.append(new String(shortToByteArray(intVal),"UTF-16"));
                    i += 5;
                } else {
                    result.append(text.charAt(i));
                }
            }
            return result.toString();
        } catch (UnsupportedEncodingException ex){
            throw new IllegalStateException("Text '" + text + "' contains UTF-16 codes, but UTF-16 encoding is not supported", ex);
        }
    }

    private byte[] shortToByteArray(short value) {
        return new byte[] {
                (byte)(value >>> 8),
                (byte)value};
    }

}
