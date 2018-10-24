package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.fragments.FWDetailsFragment;
import app.kamix.kamixui.fragments.TransactionDetailsFragment;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.User;
import app.kamix.models.Withdrawal;
import app.kamix.network.NetworkAPI;
import app.kamix.network.WithdrawalInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WithdrawalActivity extends AppCompatActivity {

    public TextView execute;
    public EditText amount, message;
    private Spinner myMob;
    private Toolbar toolbar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_withdrawal);

        user = KamixApp.getUser(this);
        if (user==null) onBackPressed();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        execute = (TextView) findViewById(R.id.execute);
        amount = (EditText) findViewById(R.id.amount);
        message = findViewById(R.id.message);
        myMob = findViewById(R.id.myMob);

        ArrayAdapter<String> myMobAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, user.getMobiles());
        myMobAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myMob.setAdapter(myMobAdapter);
        myMob.setSelection(0);


        /*final ArrayList<Country> cArr = new ArrayList<Country>();
        cArr.add(new Country(R.drawable.africanflag, "+27"));
        cArr.add(new Country(R.drawable.france_flg, "+33"));
        cArr.add(new Country(R.drawable.us_flag, "+1"));
        cArr.add(new Country(R.drawable.uk, "+44"));
        cArr.add(new Country(R.drawable.india_flag, "+91"));

        spCountry.setAdapter(new CountrySpinnerAdapter(WithDrawActivity.this, cArr));

        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tvCode.setText("" + cArr.get(i).getCode());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });*/

        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (amount.getText().toString().equalsIgnoreCase("")) {
                    amount.setError(getString(R.string.empty_amount));
                } else {
                    initWithdrawal();
                }
            }
        });
    }

    private void initWithdrawal(){
        WithdrawalInterface withdrawalInterface = NetworkAPI.getClient().create(WithdrawalInterface.class);
        Call<WithdrawalInterface.WithdrawalResponse> init = withdrawalInterface.init(user.getId(), myMob.getSelectedItem().toString(), amount.getText().toString(), message.getText().toString());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.init_transaction));
        progressDialog.show();
        init.enqueue(new Callback<WithdrawalInterface.WithdrawalResponse>() {
            @Override
            public void onResponse(Call<WithdrawalInterface.WithdrawalResponse> call, Response<WithdrawalInterface.WithdrawalResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(WithdrawalActivity.this, getString(R.string.connection_error));
                    Log.e("TRANSFERT", "Body null");
                }
                else if (response.body().isError() && response.body().getErrorCode()==WithdrawalInterface.WITHDRAWAL_INVALID_USER){
                    UiUtils.infoDialog(WithdrawalActivity.this, getString(R.string.transfert_invalid_receiver));
                }
                else if (response.body().isError() && response.body().getErrorCode()==WithdrawalInterface.WITHDRAWAL_UNACTIVE_USER){
                    UiUtils.infoDialog(WithdrawalActivity.this, getString(R.string.transfert_unactive_sender));
                }
                else if (response.body().isError() && response.body().getErrorCode()==WithdrawalInterface.WITHDRAWAL_UNSUFFICIENT_BALANCE){
                    UiUtils.infoDialog(WithdrawalActivity.this, getString(R.string.transfert_unsufficient_balance));
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(WithdrawalActivity.this, getString(R.string.connection_error));
                    Log.e("TRANSFERT", response.message() + " - " + response.body().getErrorCode());
                }
                else{
                    Withdrawal withdrawal = response.body().getWithdrawal();
                    System.out.println();
                    TransactionDetailsFragment transactionDetailsFragment =
                            TransactionDetailsFragment.newInstance(withdrawal, TransactionDetailsFragment.DETAILS_WITHDRAWAL);
                    transactionDetailsFragment.show(getSupportFragmentManager(), TransactionDetailsFragment.class.getSimpleName());
                }
            }
            @Override
            public void onFailure(Call<WithdrawalInterface.WithdrawalResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(WithdrawalActivity.this, getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }
}
