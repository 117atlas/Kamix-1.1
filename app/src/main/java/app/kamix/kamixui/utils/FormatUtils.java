package app.kamix.kamixui.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FormatUtils {

    public static final String CMR_MOBILE_REGEX = "[+]2376[98765432][0-9]{7}";

    public static void showToast(String msg, Context ct) {
        Toast.makeText(ct, "" + msg, Toast.LENGTH_LONG).show();
    }
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }
    public static boolean isValidMobile(CharSequence target) {
        Pattern pattern = Pattern.compile("^[+]2376[98765432][0-9]{7}$");
        Matcher matcher = pattern.matcher(target);
        return matcher.matches();
    }

    public static String formatAmount(double dbalance){
        /*String formattedBalance = "";
        String[] parts = TextUtils.split(String.valueOf(dbalance), ".");
        String ent = parts[0];
        for (int i=ent.length()-1; i>=0; i--){
            int j = ent.length()-1-i;
            if (j%3==2) formattedBalance = formattedBalance + ent.charAt(i) + " ";
            else formattedBalance = formattedBalance + ent.charAt(i);

        }*/
        double epsilon = 0.004; // 4 tenths of a cent
        if (Math.abs(Math.round(dbalance) - dbalance) < epsilon) {
            return String.format("%10.0f", dbalance).replace(" ", "") + " XAF"; // sdb
        } else {
            return String.format("%10.2f", dbalance).replace(" ", "") + " XAF"; // dj_segfault
        }
    }

    //TODO: format date
    public static String formatDate(long date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd - HH:mm");
        return sdf.format(new Date(date));
    }

    public static String removeIndicator(String mobile){
        String[] indicators = {"+237"};
        String res = mobile.toString();
        for (String indicator: indicators) if (res.indexOf(indicator)>=0) res = res.replace(indicator, "");
        return res;
    }

    public static String getIndicator(String mobile){
        String[] indicators = {"+237"};
        for (String indicator: indicators) if (mobile.indexOf(indicator)>=0) return indicator;
        return null;
    }
}
