# Introduction #

This tutorial is designed to provide a basic overview of how to use TextMagic java api. When you have completed the tutorial you will have written a simple web-application that sends and receives sms messages through TextMagic Sms Gateway.
You can also download working web application package at the end of the tutorial.

# Getting started #

First of all you need to create account on [TextMagic](https://www.textmagic.com/app/wt/account/api/cmd/password)

Then you need to download
  * [textmagic-sms-api.jar](http://code.google.com/p/textmagic-sms-api-java/downloads/list)
and its dependencies:
  * [Apache Commons http-client](http://repo1.maven.org/maven2/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar)
  * [org.json JSON parsing library](http://repo1.maven.org/maven2/org/json/json/20090211/json-20090211.jar)
  * [Apache Commons Logging](http://repo1.maven.org/maven2/commons-logging/commons-logging/1.1/commons-logging-1.1.jar)
  * [Apache Commons Codec](http://repo1.maven.org/maven2/commons-codec/commons-codec/1.2/commons-codec-1.2.jar)
The TextMagic java api requires java version 1.5 or above.

# Instantiating TextMagicMessageService #

By default the constructor of TextMagicMessageService requires 2 parameters: your TextMagic account username and password.

```
TextMagicMessageService service = new TextMagicMessageService ("your_username", "your_password");
```

This creates thread-safe instance of the service facade.

# Sending Messages #

The api provides set of convenient methods for sending sms messages (please refer to javadoc for complete specification).
The most common used method for sending message to one destination number is

```
SentMessage message = service.send("text", "9991234567");
```

Lets write jsp page for sending sms messages containing 2 form fields: destination number and message text to be sent. Lets name it "send.jsp"

NOTE: All examples in the tutorial do not contain necessary headers and import statements. Please download full working example archive at the end of this tutorial

```
<h2>Sending Message Example</h2>
<%
    TextMagicMessageService service = new TextMagicMessageService ("YOUR_LOGIN", "YOUR_PASSWORD");
    String phone = request.getParameter( "destination" );
    String messageText = request.getParameter( "message" );
    if ( phone != null && messageText != null) {
	System.out.println(messageText);
        try { 
            SentMessage message = service.send (messageText, phone);
	    System.out.println (message.toString());
%>
            Message with <%= message.toString() %> was successfully send.
<%            
        } catch (ServiceException ex) {
%>
            <font color="red">Cant send message. Reason: <%= ex.getMessage() %> </font>
<%
        }
    }
%>

        <form action="send.jsp" method="POST">
        <table><tr><td>Destination:</td><td><input type="text" name="destination"/></td></tr>
        <tr><td>Message:</td><td><textarea style="width:400px; height:150px" name="message"></textarea></td></tr>
	<tr><td></td><td><input type="Submit" value="Send"/><br/></td></tr>
        </table>
        </form>
```

The code is quite obvious: the form submits the two parameters to the same jsp page, and java code gets these http parameters and calls TextMagicMessageService send method.

# Checking your TextMagic account balance #

Sometimes it is required to know beforehand whether you can send messages or can not.
For example, if you don't have enough credits on your balance, you can assume that your sending requests would fail.

TextMagic api provides convenient way to check your balance current state

```
 java.math.BigDecimal balance = service.account();
```

Lets add to our send.jsp page underline string with current balance:

```
        ...
        </form>
	<br/>
	<p>
	Current balance : <%= service.account().toString() %>
	</p>

```

Yes, that simple it is.

# Checking sent message delivery status #

After you sent message to TextMagic server, the message would not get to the recipient at the same instant.
Some operators server can be overloaded and delivering can take some time, the recipients handset can be turned off or even recipients phone number can be wrong and not-existing one.

To check whether you message(or messages) is successfully delivered, or delivering is in progress, or delivering  is failed, the following code can be used

```
MessageStatus status = service.messageStatus (id);
```
where id - is the id of SentMessage object you got from send call.

Lets write simple check\_state.jsp page, that will receive http get "id" parameter and call TextMagic to check the message delivery status:

```
<h2>Message Delivery State</h2>
<%

    TextMagicMessageService service = new TextMagicMessageService ("YOUR_USERNAME", "YOUR_PASSWORD");
    Long id = new Long(request.getParameter("id"));
    if ( id != null) {
        try { 
            MessageStatus status = service.messageStatus (id);
%>
            Message Text : <%= status.getMessage().getText() %> <br/>
            Status: <%= status.getDeliveryState().getDescription() %>
<%            
        } catch (ServiceException ex) {
%>
            <font color="red">Cant receive status Reason: <%= ex.getMessage() %> </font>
<%
        }
    }
%>
```

Now, we need a link to this page, which will provide appropriate message id parameter to requests.

Lets revisit send.jsp page to add this link
```
        try { 
            SentMessage message = service.send (messageText, phone);
	    System.out.println (message.toString());
%>
            Message with <%= message.toString() %> was successfully send. 
            <a href="#" onClick="NewWin=window.open('check_state.jsp?id=<%= message.getId()%>', 'NewWin');">check delivery state</a>
<%            
        } catch (ServiceException ex) {
```

JavaScript function call "window.open" is used just to show delivering status in separate window. This can be easily replaces this classic link

```

<a href="#" onClick="check_state.jsp?id=<%= message.getId()%">check delivery state</a>

```

This is the end of the first part of the tutorial. We didn't cover bulk operations, providing you ability to sent the same message to a set of recipients and check delivery statuses.
Please, read api javadocs for more detailed information.

# Retrieving inbound sms messages #

Once you start using TextMagic for sending messages, you may realize that in some cases you need feedback from your recipients. TextMagic allows your recipients to reply to the messages they get. And even to send messages to your [sender id](http://api.textmagic.com/https-api/sender-id). This inbound messages are stored on TextMagic server and can be accessed in your account administration page or with help of java api.

Two main api calls provides ability to manipulate your inbound sms messages stored on server:

```
List<ReceivedMessage> messages = service.receive();
```
for retrieving messages and

```
service.deleteReply(messageId);
```
for deleting message message from server.

Note: we don't cover bulk api methods that operates with sets of messages

Lets write receive.jsp page that will retrieve all inbound message from server and represent them in a simple table. In addition, lets add facility to remove exact messages from server (note: removed messages can not be restored)

```

<%
    TextMagicMessageService service = new TextMagicMessageService ("YOUR_USERNAME", "YOUR_PASSWORD");

// this part is responsible for removing messages from server

    String messageId = request.getParameter( "remove" );
    if(messageId != null) {
        try{
            service.deleteReply(new Long(messageId));
%>
            <font color="green">Message id = <%= messageId %> successfully deleted</font>
<%
        } catch (ServiceException ex) {
%>
            <font color="red">Message id = <%= messageId %> was not deleted. Reason : <%= ex.getMessage() %></font>

<%            
        }
    }
%>
<br/>
<h2>List of inbound sms messages</h2>

<%
//this part is responsible for retrieving messages from server and represent them in a table
        try { 
            List<ReceivedMessage> messages = service.receive();
            if(!messages.isEmpty()) {
%>
            <table width="100%" style="border: solid 1px">
            <tr>
                <th width="20%" > Sender phone </th> <th width="50%"> Message Text </th> <th width="20%"> Received </th> <th width="10%"></th>
            </tr>
<%                
                for(ReceivedMessage message : messages) {
			System.out.println(message.getReceivedDate());
			System.out.println(System.currentTimeMillis());
%>  
                    <tr><td><%= message.getSenderPhone() %></td> <td><%= message.getText() %></td><td><%= message.getReceivedDate() %></td><td><a href="receive.jsp?remove=<%= message.getId() %>">Delete</a></td></tr>
<%
                }
%>
	    </table>
<%
            }
	} catch (ServiceException ex) {
%>
            <font color="red">Couldn't receive inbound messages. Reason : <%= ex.getMessage() %></font>
<%		
	}
%>

```

# Complete working example #

[Download](http://textmagic-sms-api-java.googlecode.com/files/demo.war)

Here we provide sample application, packaged in standard java web application archive.

As far as it contains only jsp pages, no separate sources are provided.

After deploying application into web container (tomcat, jetty, glassfish, etc..), one should change dummy TextMagic account username and password values with yours real information in receive.jsp, check\_state.jsp and send.jsp pages.