package app.kamix.kamixui.utils;

import android.content.Context;
import android.support.v4.util.Pair;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import app.kamix.R;

public class DateUtils {

    public static long beginDay(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long yesterday(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTimeInMillis();
    }

    public static long week(long time){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.add(Calendar.DAY_OF_WEEK, -5);
        return calendar.getTimeInMillis();
    }

    public static long month(long time, int distance){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.MONTH, -distance);
        return calendar.getTimeInMillis();
    }

    public static ArrayList<Pair<String, Long>> blocks(long maxTime, long minTime, Context context){
        Calendar min = Calendar.getInstance();
        min.setTimeInMillis(minTime);
        Calendar max = Calendar.getInstance();
        max.setTimeInMillis(maxTime);
        //int diffy = max.get(Calendar.YEAR) - min.get(Calendar.YEAR);
        //int diffm = max.get(Calendar.MONTH) - min.get(Calendar.MONTH);
        int diffy = Calendar.getInstance().get(Calendar.YEAR) - min.get(Calendar.YEAR);
        int diffm = Calendar.getInstance().get(Calendar.MONTH) - min.get(Calendar.MONTH);

        int diff = diffy*12 + diffm;
        long bday = beginDay(maxTime);
        long yesterday = yesterday(maxTime);
        long bweek = week(maxTime);
        long month0 = month(maxTime, 0);

        ArrayList<Pair<String, Long>> blocks = new ArrayList<>();
        blocks.add(new Pair<>(context.getString(R.string.today), bday));
        blocks.add(new Pair<String, Long>(context.getString(R.string.yesterday), yesterday));
        if (bweek<yesterday) blocks.add(new Pair<>(context.getString(R.string.thisweek), bweek));
        if (month0<bweek) blocks.add(new Pair<>(formatMonth(month0, context), month0));

        for (int i=0; i<diff; i++){
            long monthi = month(maxTime, i+1);
            blocks.add(new Pair<>(formatMonth(monthi, context), monthi));
        }

        return blocks;
    }

    private static String formatMonth(long time, Context context){
        String[] months = context.getResources().getStringArray(R.array.months);
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(time);
        return months[cal.get(Calendar.MONTH)] + " " + cal.get(Calendar.YEAR);
    }
}
