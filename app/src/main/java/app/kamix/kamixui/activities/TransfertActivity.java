package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
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
import app.kamix.network.NetworkAPI;
import app.kamix.network.TransfertInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TransfertActivity extends AppCompatActivity {

    private TextView tvCode, execute;
    private EditText amount, message;
    private Spinner spCountry;
    private AutoCompleteTextView receiver;
    private Toolbar toolbar;

    private User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfert);

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


        tvCode = findViewById(R.id.tvCode);
        execute = findViewById(R.id.execute);
        receiver = findViewById(R.id.etMob);
        amount = findViewById(R.id.amount);
        message = findViewById(R.id.message);
        spCountry = findViewById(R.id.spCountry);

        execute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (amount.getText().toString().equalsIgnoreCase("")) {
                    amount.setError(getString(R.string.empty_amount));
                } else if (receiver.getText().toString().equalsIgnoreCase("")) {
                    receiver.setError(getString(R.string.empty_receiver));
                } else {
                    /*Intent in = new Intent(MoneyTransferActivity.this, TransactionSuccessfullyActivity.class);// call trnsaction sucess screen
                    in.putExtra("data", "Money Transfer");
                    startActivity(in);
                    finish();*/
                    //initTransfert();
                    //simulateTransfert();
                    initTransfert();
                }
            }
        });

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

        receiver.setInputType(InputType.TYPE_CLASS_NUMBER);
        final ReceiverMobileAdapter receiverMobileAdapter = new ReceiverMobileAdapter(this, (ArrayList<Contact>) user.getContacts());
        receiver.setAdapter(receiverMobileAdapter);
        receiver.setThreshold(1);
        receiver.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
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
    }

    public void initTransfert(){
        String receiver = this.receiver.getText().toString();
        if (!receiver.contains("+")) receiver = tvCode.getText().toString()+receiver;
        TransfertInterface transfertInterface = NetworkAPI.getClient().create(TransfertInterface.class);
        Call<TransfertInterface.TransfertResponse> init = transfertInterface.init(user.getId(), receiver, amount.getText().toString(), message.getText().toString());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.init_transaction));
        progressDialog.show();
        init.enqueue(new Callback<TransfertInterface.TransfertResponse>() {
            @Override
            public void onResponse(Call<TransfertInterface.TransfertResponse> call, Response<TransfertInterface.TransfertResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(TransfertActivity.this, getString(R.string.connection_error));
                    Log.e("TRANSFERT", "Body null");
                }
                else if (response.body().isError() && response.body().getErrorCode()==TransfertInterface.TRANSFERT_INVALID_RECEIVER){
                    UiUtils.infoDialog(TransfertActivity.this, getString(R.string.transfert_invalid_receiver));
                }
                else if (response.body().isError() && response.body().getErrorCode()==TransfertInterface.TRANSFERT_UNACTIVE_SENDER){
                    UiUtils.infoDialog(TransfertActivity.this, getString(R.string.transfert_unactive_sender));
                }
                else if (response.body().isError() && response.body().getErrorCode()==TransfertInterface.TRANSFERT_UNACTIVE_RECEIVER){
                    UiUtils.infoDialog(TransfertActivity.this, getString(R.string.transfert_unactive_receiver));
                }
                else if (response.body().isError() && response.body().getErrorCode()==TransfertInterface.TRANSFERT_UNSUFFICIENT_BALANCE){
                    UiUtils.infoDialog(TransfertActivity.this, getString(R.string.transfert_unsufficient_balance));
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(TransfertActivity.this, getString(R.string.connection_error));
                    Log.e("TRANSFERT", response.message() + " - " + response.body().getErrorCode());
                }
                else{
                    TransactionDetailsFragment transactionDetailsFragment = TransactionDetailsFragment.newInstance(response.body().getTransfert(), TransactionDetailsFragment.DETAILS_TRANSFERT);
                    transactionDetailsFragment.show(getSupportFragmentManager(), TransactionDetailsFragment.class.getSimpleName());
                }
            }
            @Override
            public void onFailure(Call<TransfertInterface.TransfertResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(TransfertActivity.this, getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }
}
