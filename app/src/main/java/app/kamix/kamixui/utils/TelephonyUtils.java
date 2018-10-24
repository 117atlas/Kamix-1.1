package app.kamix.kamixui.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.util.List;

public class TelephonyUtils {
    public static boolean isSIMinPhone(String mobileNumber, AppCompatActivity context) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
            SubscriptionManager subscriptionManager = SubscriptionManager.from(context);
            List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
            for (SubscriptionInfo subscriptionInfo : subsInfoList) {
                String number = subscriptionInfo.getNumber();
                return number.contains(FormatUtils.removeIndicator(mobileNumber));
            }
            return false;
        } else {
            TelephonyManager tMgr = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            @SuppressLint("MissingPermission") String mPhoneNumber = tMgr.getLine1Number();
            return mPhoneNumber.contains(FormatUtils.removeIndicator(mobileNumber));
        }
    }
}
