package com.textmagic.sms.core.invoker;

import com.textmagic.sms.core.invoker.ServiceInvokerException;

import java.util.Map;

/**
 * A <tt>HttpServiceInvoker</tt> is invoker of TextMagic Gateway Http API.
 *
 * @author Rafael Bagmanov
 */
public interface HttpServiceInvoker {
    /**
     * Calls TextMagic sms gateway command
     * 
     * @param login the TextMagic account username
     * @param password the TextMagic account password
     * @param commandName the http api command name
     * @param parameters the command parameters to pass
     * @return response of http api gateway
     * @throws ServiceInvokerException if TextMagic server is inaccessible or server responds with http error status code
     */
    public String invoke(String login, String password, String commandName, Map<String, String> parameters) throws ServiceInvokerException;
    
}
