package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.adapters.CountrySpinnerAdapter;
import app.kamix.kamixui.adapters.ReceiverMobileAdapter;
import app.kamix.kamixui.fragments.TransactionDetailsFragment;
import app.kamix.kamixui.utils.Country;
import app.kamix.kamixui.utils.FormatUtils;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.Contact;
import app.kamix.models.User;
import app.kamix.network.FundingInterface;
import app.kamix.network.NetworkAPI;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FundingActivity extends AppCompatActivity {

    private SwitchCompat fundSwitch;
    private TextView tvExecute, tvCode;
    private EditText etAmount, message;
    private Spinner myMob;
    private Spinner spCountry;
    private AutoCompleteTextView receiverMob;
    private LinearLayout receiverContainer;

    private Toolbar toolbar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funding);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        user = KamixApp.getUser(this);
        if (user==null) onBackPressed();

        fundSwitch = findViewById(R.id.fund_switch);
        tvExecute = (TextView) findViewById(R.id.execute);
        tvCode = (TextView) findViewById(R.id.tvCode);
        etAmount = (EditText) findViewById(R.id.amount);
        message = findViewById(R.id.message);
        myMob = findViewById(R.id.myMob);
        receiverMob = findViewById(R.id.etMob);
        receiverContainer = findViewById(R.id.receiver_container);
        spCountry = findViewById(R.id.spCountry);

        fundSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) receiverContainer.setVisibility(View.VISIBLE);
                else {
                    receiverMob.setText("");
                    receiverContainer.setVisibility(View.GONE);
                }
            }
        });
        receiverContainer.setVisibility(View.GONE);

        final ArrayList<Country> cArr = new ArrayList<Country>();
        cArr.add(new Country(R.drawable.cm, "+237"));
        //cArr.add(new Country(R.drawable.france_flg, "+33"));

        spCountry.setAdapter(new CountrySpinnerAdapter(this, cArr)); // set country flag dropdown
        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tvCode.setText("" + cArr.get(i).getCode());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        ArrayAdapter<String> myMobAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, user.getMobiles());
        myMobAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myMob.setAdapter(myMobAdapter);
        myMob.setSelection(0);

        receiverMob.setInputType(InputType.TYPE_CLASS_NUMBER);
        final ReceiverMobileAdapter receiverMobileAdapter = new ReceiverMobileAdapter(this, (ArrayList<Contact>) user.getContacts());
        receiverMob.setAdapter(receiverMobileAdapter);
        receiverMob.setThreshold(1);
        receiverMob.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Contact contact = receiverMobileAdapter.getSuggestions().get(i);
                int indexInCountries = Country.indexInCountriesList(cArr, FormatUtils.getIndicator(contact.getMobile()));
                if (indexInCountries>0){
                    spCountry.setSelection(indexInCountries);
                    tvCode.setText(FormatUtils.getIndicator(contact.getMobile()));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etAmount.getText().toString().equalsIgnoreCase("")) {
                    etAmount.setError(getString(R.string.invalid_amount));
                } else if (fundSwitch.isChecked() && (receiverMob.getText().toString().equalsIgnoreCase("") || !FormatUtils.isValidMobile(tvCode.getText().toString()+receiverMob.getText().toString()))) {
                    receiverMob.setError(getString(R.string.invalid_receiver_mobile_num));
                } else {
                    /*Intent in = new Intent(FundingActivity.this, TransactionSuccessfullyActivity.class); // call trnsaction sucess screen
                    in.putExtra("data", "Funding");
                    startActivity(in);
                    finish();*/
                    initFunding();
                }
            }
        });
    }

    private void initFunding(){
        String receiver = receiverMob.getText().toString();
        if (!receiver.contains("+237")) receiver = "+237"+receiver;
        FundingInterface fundingInterface = NetworkAPI.getClient().create(FundingInterface.class);
        Call<FundingInterface.FundingResponse> init = fundingInterface.init(user.getId(), receiver, myMob.getSelectedItem().toString(), etAmount.getText().toString(), message.getText().toString(), !fundSwitch.isChecked());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.init_transaction));
        progressDialog.show();
        init.enqueue(new Callback<FundingInterface.FundingResponse>() {
            @Override
            public void onResponse(Call<FundingInterface.FundingResponse> call, Response<FundingInterface.FundingResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(FundingActivity.this, getString(R.string.connection_error));
                    Log.e("TRANSFERT", "Body null");
                }
                else if (response.body().isError() && response.body().getErrorCode()==FundingInterface.FUNDING_INVALID_USER){
                    UiUtils.infoDialog(FundingActivity.this, getString(R.string.transfert_invalid_receiver));
                }
                else if (response.body().isError() && response.body().getErrorCode()==FundingInterface.FUNDING_UNACTIVE_USER){
                    UiUtils.infoDialog(FundingActivity.this, getString(R.string.transfert_unactive_sender));
                }
                else if (response.body().isError() && response.body().getErrorCode()==FundingInterface.FUNDING_INVALID_USER){
                    UiUtils.infoDialog(FundingActivity.this, getString(R.string.transfert_invalid_receiver));
                }
                else if (response.body().isError() && response.body().getErrorCode()==FundingInterface.FUNDING_UNACTIVE_RECEIVER){
                    UiUtils.infoDialog(FundingActivity.this, getString(R.string.transfert_unactive_receiver));
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(FundingActivity.this, getString(R.string.connection_error));
                    Log.e("TRANSFERT", response.message() + " - " + response.body().getErrorCode());
                }
                else{
                    TransactionDetailsFragment fwDetailsFragment = TransactionDetailsFragment.newInstance(response.body().getFunding(), TransactionDetailsFragment.DETAILS_FUNDING);
                    fwDetailsFragment.show(getSupportFragmentManager(), TransactionDetailsFragment.class.getSimpleName());
                }
            }
            @Override
            public void onFailure(Call<FundingInterface.FundingResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(FundingActivity.this, getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }
}
