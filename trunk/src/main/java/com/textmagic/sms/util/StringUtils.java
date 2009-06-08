package com.textmagic.sms.util;

import java.util.List;
import java.util.Map;

/**
 *  This class contains various static methods for manipulating Strings.
 *
 * @author Rafael Bagmanov
 */
public class StringUtils {

    private StringUtils(){}

    /**
     * Joins string representations of all objects from list into one String
     * Elements are converted to strings as by  <tt>String.valueOf(object)</tt>
     *
     * @param list List of objects whose string representation to be joined
     * @param separator The value to be set between neighboring objects representations
     * @return Joined String
     */
    public static String join(List list, String separator) {
        StringBuilder str = new StringBuilder();
        for (Object item : list) {
            str.append(String.valueOf(item)).append(separator);
        }
        return str.substring(0, str.length() - separator.length());
    }

    /**
     * Constructs a string as a result of <tt>count</tt> repetition of <tt>str</tt> string fragment
     *
     * @param str the string fragment to be repeated
     * @param count repetition count
     * @return constructed string
     */
    public static String repeat(String str, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < count; i++) {
              stringBuilder.append(str);
        }
        return stringBuilder.toString();
    }

    /**
     * Returns a string representation of the contents of the specified Map
     * The string representation consists of a list of the Map's entries,
     * enclosed in square brackets (<tt>"[]"</tt>).
     * Each entry is represented as <tt>key = value</tt>
     *
     * @param map the Map whose string representation to return
     * @return string representation of <tt>map</tt>
     */
    public static String toString(Map<String, String> map){
        StringBuilder string = new StringBuilder("[");
        for (Map.Entry<String, String> keyValue : map.entrySet()) {
            string.append(keyValue.getKey()).append(" = ").append(keyValue.getValue()).append(";");
        }
        return string.append("]").toString();
    }
}

