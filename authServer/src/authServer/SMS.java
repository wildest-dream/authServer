package authServer;
import java.util.Random;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
//this file is for testing
public class SMS {
	public static final String ACCOUNT_SID =
            "AC0bd8a502cc087419203ccd742052a3b0";
    public static final String AUTH_TOKEN =
            "37b1e60ac11c2c21afd4be4245516fb2";
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		/*Twilio.init(ACCOUNT_SID, AUTH_TOKEN);

        Message message = Message
                .creator(new PhoneNumber("+353894158559"), // to
                        new PhoneNumber("+12018904689"), // from
                        "Where's Wallace?")
                .create();*/
		Random rand=new Random();
		for(int i=0;i<20;i++) {
			System.out.println(rand.nextInt(4));
		}
        //System.out.println(message.getSid());
	}

}
