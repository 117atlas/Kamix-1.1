package app.kamix.kamixui.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import app.kamix.R;
import app.kamix.kamixui.utils.FormatUtils;
import app.kamix.kamixui.utils.TelephonyUtils;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.Funding;
import app.kamix.network.BaseResponse;
import app.kamix.network.FundingInterface;
import app.kamix.network.NetworkAPI;
import app.kamix.network.models.FundingData;
import app.kamix.network.models.Provider;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FundingInstructionsActivity extends AppCompatActivity {

    public static final int REQ_PERMISSION_COMPOSE = 987;
    public static final int REQ_PERMISSION_CALL = 988;

    private Toolbar toolbar;
    private AppCompatButton compose, proceed, cancel;
    private TextView instructions;
    private EditText idtransaction;

    private FundingData fundingData;

    private Provider provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funding_instructions);

        fundingData = (FundingData) getIntent().getSerializableExtra(FundingData.class.getSimpleName());
        provider = getProvider(fundingData.getWcuResponse().getContent().getProviders(), fundingData.getFunding().getMobileNumber());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        compose = findViewById(R.id.compose);
        proceed = findViewById(R.id.proceed);
        cancel = findViewById(R.id.cancel);
        instructions = findViewById(R.id.funding_instructions);
        idtransaction = findViewById(R.id.operator_tid);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.questionDialog(FundingInstructionsActivity.this, getString(R.string.cancel_transaction_question), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        cancelFunding();
                    }
                }).show();
            }
        });

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (idtransaction.getText().toString().equalsIgnoreCase("")) {
                    idtransaction.setError(getString(R.string.empty_operator_tid));
                } else {
                    doFunding();
                }
            }
        });

        compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (provider != null) {
                    compose();
                }
                else Toast.makeText(FundingInstructionsActivity.this, "Provider null", Toast.LENGTH_SHORT).show();
            }
        });

        if (provider!=null){
            instructions.setText(provider.getInstructions());
        }
    }

    private Provider getProvider(List<Provider> providers, String mobile){
        if (providers==null || providers.size()==0 || mobile==null || mobile.equalsIgnoreCase(""))
            return null;
        String providerName = "";
        if (mobile.startsWith("+23769") || mobile.startsWith("+237655") || mobile.startsWith("+237656") || mobile.startsWith("+237657")
                || mobile.startsWith("+237658") || mobile.startsWith("+237659"))
            providerName = "Orange";
        else providerName = "MTN";
        for (Provider provider: providers){
            if (provider.getName().equalsIgnoreCase(providerName)) return provider;
        }
        return null;
    }

    private void compose(){
        if (ActivityCompat.checkSelfPermission(FundingInstructionsActivity.this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(FundingInstructionsActivity.this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(FundingInstructionsActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(FundingInstructionsActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            ActivityCompat.requestPermissions(FundingInstructionsActivity.this,
                    new String[]{Manifest.permission.READ_SMS, Manifest.permission.READ_PHONE_NUMBERS, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE},
                    1);
            return;
        }
        if (TelephonyUtils.isSIMinPhone(fundingData.getFunding().getMobileNumber(), FundingInstructionsActivity.this)) {
            call(buildUssd());
        }
        else{
            UiUtils.infoDialog(FundingInstructionsActivity.this, getString(R.string.sim_not_in_phone));
        }
    }

    private Uri buildUssd(){
        String composeUssd = "";
        if (provider.getName().equals("Orange"))
            composeUssd = "#150*1*1*"+getMobileNumberInInstructions(provider.getInstructions())+"*"+fundingData.getFunding().getAmount()+"*0000#";
        else composeUssd = "*126*1*1#";
        String uriString = "";
        if(!composeUssd.startsWith("tel:"))
            uriString += "tel:";
        for(char c : composeUssd.toCharArray()) {
            if(c == '#')
                uriString += Uri.encode("#");
            else
                uriString += c;
        }
        return Uri.parse(uriString);
    }

    @SuppressLint("MissingPermission")
    private void call(Uri uri){
        Intent intent = new Intent(Intent.ACTION_CALL, uri);
        startActivity(intent);
    }

    private String getMobileNumberInInstructions(String instructions){
        Pattern pattern = Pattern.compile(FormatUtils.CMR_MOBILE_REGEX);
        Matcher matcher = pattern.matcher(instructions);
        if (matcher.find()) return matcher.group();
        return null;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQ_PERMISSION_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    compose();
                } else {
                    Toast.makeText(FundingInstructionsActivity.this, getString(R.string.call_ussd_permissions_denied), Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void cancelFunding(){
        FundingInterface fundingInterface = NetworkAPI.getClient().create(FundingInterface.class);
        Funding funding = fundingData.getFunding();
        Call<BaseResponse> cancel = fundingInterface.cancel(funding.getId());
        cancel.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                onBackPressed();
                if (response.body()!=null && !response.body().isError()){
                    Toast.makeText(FundingInstructionsActivity.this, getString(R.string.transaction_cancelled), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                onBackPressed();
                t.printStackTrace();
            }
        });
    }

    private void doFunding(){
        FundingInterface fundingInterface = NetworkAPI.getClient().create(FundingInterface.class);
        Funding funding = fundingData.getFunding();
        funding.setTransactionConfCode(idtransaction.getText().toString());
        funding.setTransactionProvName(provider.getName());
        Call<FundingInterface.FundingResponse> begin = fundingInterface.doFunding(funding);
        final ProgressDialog progressDialog = UiUtils.loadingDialog(FundingInstructionsActivity.this, getString(R.string.processing_transaction));
        progressDialog.show();
        begin.enqueue(new Callback<FundingInterface.FundingResponse>() {
            @Override
            public void onResponse(Call<FundingInterface.FundingResponse> call, Response<FundingInterface.FundingResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(FundingInstructionsActivity.this, getString(R.string.connection_error));
                    Log.e("DO FUNDING", "Body Null");
                }
                /*else if (response.body().isError() && response.body().getErrorCode()==FundingInterface.FUNDING_FAILED){
                    incorrectPINDialog();
                }*/
                else if (response.body().isError() && response.body().getErrorCode()==FundingInterface.FUNDING_FAILED) {
                    UiUtils.questionDialog(FundingInstructionsActivity.this, getString(R.string.funding_failed), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            doFunding();
                        }
                    });
                    Log.e("DO FUNDING", "Funding Failed");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(FundingInstructionsActivity.this, getString(R.string.transaction_server_error));
                    Log.e("DO FUNDING", response.message() +" - "+response.body().getErrorCode());
                }
                else{
                    Intent intent = new Intent(FundingInstructionsActivity.this, TransactionSucessActivity.class);
                    intent.putExtra("TRANSACTION", response.body().getFunding());
                    intent.putExtra("data", "Funding");
                    startActivity(intent);
                    finish();
                }
            }
            @Override
            public void onFailure(Call<FundingInterface.FundingResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(FundingInstructionsActivity.this, getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }
}
