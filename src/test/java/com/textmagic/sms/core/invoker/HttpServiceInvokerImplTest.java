package com.textmagic.sms.core.invoker;

import org.junit.Test;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.jmock.integration.junit4.JMock;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.Mockery;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.hamcrest.Matcher;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collections;
import java.io.UnsupportedEncodingException;
import java.io.IOException;

import static junit.framework.Assert.assertNull;

/**
 * Date: 24.05.2009
 *
 * @author: bagmanov
 */
@RunWith(JMock.class)
public class HttpServiceInvokerImplTest {
    Mockery context = new JUnit4Mockery();

    HttpServiceInvokerImpl serviceInvoker;

    @Before
    public void setUp() throws Exception {
        context.setImposteriser(ClassImposteriser.INSTANCE);
        serviceInvoker = new HttpServiceInvokerImpl();
        serviceInvoker.httpclient = context.mock(HttpClient.class);
    }

    @Test
    public void test() throws Exception{
        final String login = "login";
        final String password = "pas";
        final String command = "cmd1";
        final String paramInMap = "param";
        Map<String, String> params = new HashMap<String, String>();
        params.put("paramInMapKey", paramInMap);
        context.checking(new Expectations(){{
            one(serviceInvoker.httpclient).executeMethod(
                    with(new TypeSafeMatcher<PostMethod>(){
                        public boolean matchesSafely(PostMethod o) {
                            if(o.getParameter("username") == null ||
                                    !o.getParameter("username").getValue().equals(login)){
                                return false;
                            }
                            if(o.getParameter("password") == null ||
                                    !o.getParameter("password").getValue().equals(password)){
                                return false;
                            }
                            if(o.getParameter("cmd") == null ||
                                    !o.getParameter("cmd").getValue().equals(command)){
                                return false;
                            }
                            if(o.getParameter("paramInMapKey") == null ||
                                    !o.getParameter("paramInMapKey").getValue().equals(paramInMap)){
                                return false;
                            }
                            return true;
                        }

                        public void describeTo(Description description) {
                            description.appendText("PostMethod object"); 
                        }
                    })
            ); will(returnValue(200));
        }});
        String result = serviceInvoker.invoke(login, password, command, params);
        assertNull(result);
    }

    @Test(expected = ServiceInvokerException.class)
    public void test_ServerResponse() throws Exception{
        Map<String, String> params = Collections.emptyMap();
        context.checking(new Expectations(){{
            one(serviceInvoker.httpclient).executeMethod(
                    (HttpMethod) with(anything())
            ); will(returnValue(404));
        }});
        String result = serviceInvoker.invoke("login", "pass", "cmd", params);
    }

    @Test(expected = ServiceInvokerException.class)
    public void test_IOException() throws Exception{
        Map<String, String> params = Collections.emptyMap();
        context.checking(new Expectations(){{
            one(serviceInvoker.httpclient).executeMethod(
                    (HttpMethod) with(anything())
            ); will(throwException(new IOException()));
        }});
        String result = serviceInvoker.invoke("login", "pass", "cmd", params);
    }

}
