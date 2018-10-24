package app.kamix.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SmsMessage;
import android.util.Log;

import app.kamix.kamixui.fragments.VerifyMobileFragment;

public class SmsReceiver extends BroadcastReceiver {

    private static final String TAG = SmsReceiver.class.getSimpleName();
    private static final String SENDER = "KAMIX";
    private static final String OTP_DELIMITER = "-";

    private VerifyMobileFragment verifyMobileFragment = null;

    public SmsReceiver(VerifyMobileFragment verifyMobileFragment){
        this.verifyMobileFragment = verifyMobileFragment;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final Bundle bundle = intent.getExtras();
        try {
            if (bundle != null) {
                Object[] pdusObj = (Object[]) bundle.get("pdus");
                for (Object aPdusObj : pdusObj) {
                    SmsMessage currentMessage = SmsMessage.createFromPdu((byte[]) aPdusObj);
                    String senderAddress = currentMessage.getDisplayOriginatingAddress();
                    String message = currentMessage.getDisplayMessageBody();

                    Log.e(TAG, "Received SMS: " + message + ", Sender: " + senderAddress);
                    // if the SMS is not from our gateway, ignore the message
                    if (!senderAddress.toLowerCase().contains(SENDER.toLowerCase())) {
                        return;
                    }
                    // verification code from sms
                    String verificationCode = getVerificationCode(message);

                    Log.e(TAG, "OTP received: " + verificationCode);
                    //Intent hhtpIntent = new Intent(context, HttpService.class);
                    //hhtpIntent.putExtra("otp", verificationCode);
                    //context.startService(hhtpIntent);
                    verifyMobileFragment.handleCodeReceived(verificationCode);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage());
        }
    }

    /**
     * Getting the OTP from sms message body
     * ':' is the separator of OTP from the message
     *
     * @param message
     * @return
     */
    private String getVerificationCode(String message) {
        String code = null;
        int index = message.indexOf(OTP_DELIMITER);
        if (index != -1) {
            int start = index + 2;
            int length = 4;
            code = message.substring(start, start + length);
            return code;
        }
        return code;
    }
}