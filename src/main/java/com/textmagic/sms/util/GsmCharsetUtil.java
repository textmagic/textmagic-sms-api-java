package com.textmagic.sms.util;

/**
 * The class provides set of static methods that helps to check String content against
 * GSM 03.38 character set
 *
 * @author Rafael Bagmanov
 */
public class GsmCharsetUtil {
    public static final String chars = "@£$¥èéùìòÇ\nØø\rÅåΔ_ΦΓΛΩΠΨΣΘΞ\t\f^{}\\\\[~]|€ÆæßÉ !\"#¤%&'()*+,-./0123456789:;<=>?¡ABCDEFGHIJKLMNOPQRSTUVWXYZÄÖÑÜ§¿abcdefghijklmnopqrstuvwxyzäöñüà";

    /**
     * Indicates whether provided character belongs to GSM 03.38 character set
     *
     * @param character
     * @return true - belongs, false - not
     */
    public static boolean isLegalCharacter(char character) {
        return chars.indexOf(character)!= -1;
    }

    /**
     * Indicates whether provided Strings consists only of GSM 03.38 characters
     *
     * @param str String to check
     * @return true - all Strings characters belongs to GSM 03.38, false - not
     */
    public static boolean isLegalString(String str){
        for(int i = 0; i < str.length(); i++){
            if(!GsmCharsetUtil.isLegalCharacter(str.charAt(i))){
                return false;
            }
        }
        return true;
    }
}
