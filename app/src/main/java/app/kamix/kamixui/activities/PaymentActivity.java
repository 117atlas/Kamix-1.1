package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.app.Utils;
import app.kamix.kamixui.fragments.TransactionDetailsFragment;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.PaymentInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentActivity extends AppCompatActivity {

    private EditText merchant, amount, message;
    private TextView execute;
    private Toolbar toolbar;

    private User user;

    public static final int REQ_QR_SCAN = 808;
    private String qrCodeResult = "";
    private String merchantId = "";
    private String merchantName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        user = KamixApp.getUser(this);
        if (user==null) finish();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        merchant = findViewById(R.id.merchant);
        amount = findViewById(R.id.amount);
        message = findViewById(R.id.message);
        execute = findViewById(R.id.execute);
        toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (amount.getText().toString().equalsIgnoreCase("")) {
                    amount.setText(getString(R.string.empty_amount));
                } else {
                    initPayment();
                }
            }
        });

        merchant.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(PaymentActivity.this, ScanQRCodeActivity.class), REQ_QR_SCAN);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQ_QR_SCAN && resultCode==RESULT_OK){
            qrCodeResult = data.getStringExtra("QRCODERESULT");
            Log.d("QRCODERESULT", qrCodeResult);
            try{
                ArrayList<String> qrcodeDatas = Utils.split(qrCodeResult, "|"); //TextUtils.split(qrCodeResult, "|");

                if (qrcodeDatas==null || qrcodeDatas.size()!=2){
                    UiUtils.infoDialog(this, getString(R.string.invalid_qrcode));
                }
                else{
                    merchantName = qrcodeDatas.get(1);
                    merchant.setText(qrcodeDatas.get(1));
                    merchantId = qrcodeDatas.get(0);
                }
            } catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("MERCHANT_NAME", merchantName);
        outState.putString("MERCHANT_ID", merchantId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        merchantName = savedInstanceState.getString("MERCHANT_NAME");
        savedInstanceState.getString("MERCHANT_ID");
        if (merchant!=null && merchantName!=null) merchant.setText(merchantName);
    }

    private void initPayment(){
        PaymentInterface paymentInterface = NetworkAPI.getClient().create(PaymentInterface.class);
        Call<PaymentInterface.PaymentResponse> init = paymentInterface.init(user.getId(), merchantId, amount.getText().toString(), message.getText().toString(), "0");
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.init_transaction));
        progressDialog.show();
        init.enqueue(new Callback<PaymentInterface.PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentInterface.PaymentResponse> call, Response<PaymentInterface.PaymentResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(PaymentActivity.this, getString(R.string.connection_error));
                    Log.e("TRANSFERT", "Body null");
                }
                else if (response.body().isError() && response.body().getErrorCode()==PaymentInterface.PAYMENT_INVALID_RECEIVER){
                    UiUtils.infoDialog(PaymentActivity.this, getString(R.string.transfert_invalid_receiver));
                }
                else if (response.body().isError() && response.body().getErrorCode()==PaymentInterface.PAYMENT_UNACTIVE_SENDER){
                    UiUtils.infoDialog(PaymentActivity.this, getString(R.string.transfert_unactive_sender));
                }
                else if (response.body().isError() && response.body().getErrorCode()==PaymentInterface.PAYMENT_UNACTIVE_RECEIVER){
                    UiUtils.infoDialog(PaymentActivity.this, getString(R.string.transfert_unactive_receiver));
                }
                else if (response.body().isError() && response.body().getErrorCode()==PaymentInterface.PAYMENT_UNSUFFICIENT_BALANCE){
                    UiUtils.infoDialog(PaymentActivity.this, getString(R.string.transfert_unsufficient_balance));
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(PaymentActivity.this, getString(R.string.connection_error));
                    Log.e("TRANSFERT", response.message() + " - " + response.body().getErrorCode());
                }
                else{
                    TransactionDetailsFragment transactionDetailsFragment = TransactionDetailsFragment.newInstance(response.body().getPayment(), TransactionDetailsFragment.DETAILS_PAYMENT);
                    transactionDetailsFragment.show(getSupportFragmentManager(), TransactionDetailsFragment.class.getSimpleName());
                }
            }
            @Override
            public void onFailure(Call<PaymentInterface.PaymentResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(PaymentActivity.this, getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }
}
