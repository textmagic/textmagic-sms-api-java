package com.textmagic.sms.core.invoker;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.FileRequestEntity;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Map;
import java.io.IOException;

import com.textmagic.sms.util.StringUtils;

/**
 * The default {@link com.textmagic.sms.core.invoker.HttpServiceInvoker} implementation.
 * The implementation is based on <a href="http://hc.apache.org/httpclient-3.x/">apache commons httpclient</a>
 * By default the invoker access the gateway through direct https connection.
 * <br/><br/>
 * To change default httpclient configuration, one should inherit <tt>HttpServiceInvokerImpl</tt>
 * and provide customization in childs constructor:
 *<br/><br/>
 *  <pre>
 *      public class ExtendedServiceInvokerImpl extends HttpServiceInvoker {
 *        public ExtendedServiceInvokerImpl() {
 *            super();
 *            httpclient.getHostConfiguration().setProxy("dummy.com", 80);
 *         }
 *      }
 *  </pre>
 * <br/>
 * To change https to http protocol, one should override <code>textMagicUrl<code> value 
 *
 * @author Rafael Bagmanov
 */
public class HttpServiceInvokerImpl implements HttpServiceInvoker{
    Log log = LogFactory.getLog(HttpServiceInvokerImpl.class);

    protected String textMagicUrl = "https://www.textmagic.com/app/api";
    protected HttpClient httpclient;

    /**
     *  Constructs the invoker and instantiate httpclient as {@link HttpClient}
     */
    public HttpServiceInvokerImpl() {
        httpclient = new HttpClient();
    }

    public String invoke(String login, String password, String commandName, Map<String, String> parameters) throws ServiceInvokerException {
        PostMethod post = new UTF8PostMethod(textMagicUrl);
        post.addParameter("username", login);
        post.addParameter("password", password);
        post.addParameter("cmd", commandName);
        for (String key : parameters.keySet()) {
            post.addParameter(key, parameters.get(key));
        }
        try {
            if(log.isDebugEnabled()){
                String logStr = String.format("<<< [login = %s; command = %s; parameters = %s]", login,
                        commandName, StringUtils.toString(parameters));
                log.debug(logStr);
            }
            int result = httpclient.executeMethod(post);
            if(log.isDebugEnabled()) {
                String logStr = String.format(">>> [http_result = %d; body = %s", result, post.getResponseBodyAsString());
                log.debug(logStr);
            }
            if(result < 200 || result > 299){
                throw new ServiceInvokerException("Server responded with " + result + " http code");
            }
            return post.getResponseBodyAsString();  
        } catch (IOException ex) {
            if(log.isDebugEnabled()) {
                log.debug (">>> exception thrown" + ex.getMessage());
            }
            throw new ServiceInvokerException (ex.getMessage(), ex);
        } finally {
            post.releaseConnection();
        }
    }
    
    private static class UTF8PostMethod extends PostMethod {
		public UTF8PostMethod(String url) {
			super(url);
		}

		@Override
		public String getRequestCharSet() {
			return "UTF-8";
		}
	}
}
