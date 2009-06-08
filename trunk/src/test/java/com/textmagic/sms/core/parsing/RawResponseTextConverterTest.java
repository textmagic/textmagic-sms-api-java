package com.textmagic.sms.core.parsing;

import org.junit.Test;
import static junit.framework.Assert.assertEquals;
import com.textmagic.sms.dto.MessageStatus;

/**
 * @author Bagmanov
 */
public class RawResponseTextConverterTest {

    RawResponseTextConverter converter = new RawResponseTextConverter();

    @Test
    public void testConvert () throws Exception{
        final String freeEncyclopediaInRussian = "\\u0421\\u0432\\u043e\\u0431\\u043e\\u0434\\u043d\\u0430\\u044f \\u044d\\u043d\\u0446\\u0438\\u043a\\u043b\\u043e\\u043f\\u0435\\u0434\\u0438\\u044f";
        String result = converter.convert(freeEncyclopediaInRussian);
        assertEquals("Свободная энциклопедия", result);
        
    }

    @Test
    public void testConvertWithSlashAtTheEnd () throws Exception{
        String result = converter.convert("text\\");
        assertEquals("text\\", result);
    }


}
