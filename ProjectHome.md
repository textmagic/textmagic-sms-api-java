# Convenient java api for [TextMagic](http://www.textmagic.com) sms gateway #

The steps to start using TextMagic Java api:

  1. Create account on http://www.textmagic.com/
  1. Download textmagic-sms-api.jar
  1. Download depedencies
    * [Commons HttpClient 3.1](http://repo1.maven.org/maven2/commons-httpclient/commons-httpclient/3.1/commons-httpclient-3.1.jar)
    * [org.json JSON processing library](http://repo1.maven.org/maven2/org/json/json/20090211/json-20090211.jar)
    * [Commons Logging 1.1](http://repo1.maven.org/maven2/commons-logging/commons-logging/1.1/commons-logging-1.1.jar)
    * [Commons Codec 1.2](http://repo1.maven.org/maven2/commons-codec/commons-codec/1.2/commons-codec-1.2.jar)
  1. Copy textmagic-sms-api.jar and dependencies to your project class path
  1. Write code
```
import com.textmagic.sms.TextMagicMessageService;
import com.textmagic.sms.exception.ServiceException;

public class DummyCode {
    public static void main (String []argz) {
          String dummyPhone = "99912345678";
          TextMagicMessageService service = new TextMagicMessageService ("my_login", "my_password");
          try {
               service.send("Hello, World!", dummyPhone);
          } catch(ServiceException ex) {
               System.out.println(" :-( ");
          }
    }
}
```
  1. have fun :)

# <a href='http://www.textmagic.com/affiliate/fordevelopers.html'>SMS Gateway Affiliate Programme For Developers</a> #

Here’s what you’ll get:

<li>A 10% share of the lifetime value of the customer. If a customer spends £5,000 on SMS credit during his or her membership of TextMagic, you’ll earn £500.</li>

<li>Bonus: we’ll pay you a £15 flat fee for each new paying customer referral. You’ll still get your 10% revenue share from their initial order, too.</li>


