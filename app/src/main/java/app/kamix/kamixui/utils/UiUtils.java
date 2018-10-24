package app.kamix.kamixui.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.view.View;

import app.kamix.R;

public class UiUtils {
    public static ProgressDialog loadingDialog(Context context, String message){
        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        return progressDialog;
    }

    public static void dismissLoadingDialog(ProgressDialog progressDialog){
        if (progressDialog!=null && progressDialog.isShowing()) progressDialog.dismiss();
    }

    public static Snackbar snackBar(View view, String message){
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
        return snackbar;
    }

    public static AlertDialog infoDialog(Context context, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setNeutralButton(context.getString(R.string.close), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(true);
        alertDialog.show();
        return alertDialog;
    }

    public static AlertDialog questionDialog(Context context, String message, DialogInterface.OnClickListener yeslistener){
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setMessage(message)
                .setPositiveButton(context.getString(R.string.yes), yeslistener)
                .setNeutralButton(context.getString(R.string.no), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        return alertDialog;
    }

    public static void simulateLoading(Context context, final long time, String message, final OnSimulateLoadingFinished onSimulateLoadingFinished){
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setCancelable(false);
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage(message);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                int t = 0;
                while (t<time){
                    try {
                        Thread.sleep(13);
                        t += 13;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
                progressDialog.dismiss();
                onSimulateLoadingFinished.onFinished();
            }
        }).start();
    }

    public static interface OnSimulateLoadingFinished{
        void onFinished();
    }

}
