package com.mycompany.invoiceapp;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import com.twilio.exception.TwilioException;
import java.util.logging.Logger;
import java.util.logging.Level;

public class SmsSender {
    private static final Logger logger = Logger.getLogger(SmsSender.class.getName());
    private static boolean initialized = false;

    static {
        // Initialize Twilio with environment variables
        String accountSid = System.getenv("TWILIO_ACCOUNT_SID");
        String authToken = System.getenv("TWILIO_AUTH_TOKEN");
        
        if (accountSid != null && authToken != null) {
            Twilio.init(accountSid, authToken);
            initialized = true;
        }
    }

    public static void sendSms(String to, String messageBody) {
        if (!initialized) {
            logger.severe("Twilio not initialized. Set TWILIO_ACCOUNT_SID and TWILIO_AUTH_TOKEN environment variables.");
            return;
        }

        String fromNumber = System.getenv("TWILIO_FROM_NUMBER");
        if (fromNumber == null) {
            logger.severe("TWILIO_FROM_NUMBER environment variable not set.");
            return;
        }

        try {
            Message message = Message.creator(
                    new PhoneNumber(to),
                    new PhoneNumber(fromNumber),
                    messageBody
            ).create();

            logger.info("SMS sent successfully. SID: " + sanitizeForLog(message.getSid()));
        } catch (TwilioException e) {
            logger.log(Level.SEVERE, "Failed to send SMS", e);
        }
    }

    private static String sanitizeForLog(String input) {
        if (input == null) return "null";
        return input.replaceAll("[\u0000-\u001F\u007F]", "_");
    }
}
