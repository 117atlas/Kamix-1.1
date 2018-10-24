package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.app.Utils;
import app.kamix.kamixui.adapters.CountrySpinnerAdapter;
import app.kamix.kamixui.fragments.VerifyMobileFragment;
import app.kamix.kamixui.utils.Country;
import app.kamix.kamixui.utils.FormatUtils;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.localstorage.DAO;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddMobileActivity extends AppCompatActivity {

    private Spinner spCountry;
    private TextView tvCode;
    private EditText etMob, pinCode;
    private AppCompatButton add;

    private Toolbar toolbar;

    private User user = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mobile);

        user = (User) getIntent().getSerializableExtra(User.class.getSimpleName());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        spCountry = findViewById(R.id.spCountry);
        tvCode = findViewById(R.id.tvCode);
        etMob = findViewById(R.id.etMob);
        pinCode = findViewById(R.id.etPin);
        add = findViewById(R.id.add);

        final ArrayList<Country> cArr = new ArrayList<Country>();
        cArr.add(new Country(R.drawable.cm, "+237"));
        spCountry.setAdapter(new CountrySpinnerAdapter(this, cArr));
        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tvCode.setText("" + cArr.get(i).getCode());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etMob.getText().toString().equalsIgnoreCase("")){
                    etMob.setError(getString(R.string.empty_mobile));
                }
                else if (!FormatUtils.isValidMobile(tvCode.getText().toString()+etMob.getText().toString())){
                    etMob.setError(getString(R.string.invalid_mobile));
                }
                else if (user.getMobiles().contains(tvCode.getText().toString()+etMob.getText().toString())){
                    etMob.setError(getString(R.string.differents_mobiles));
                }
                else if (pinCode.getText().toString().equalsIgnoreCase("")){
                    pinCode.setError(getString(R.string.empty_pin));
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddMobileActivity.this)
                            .setMessage(getString(R.string.add_mobile_mobile).replace("?????", etMob.getText().toString()))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    addMobile();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
        });
    }

    private void addMobile(){
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        UserInterface.ModifyMobileBody modifyMobileBody = new UserInterface.ModifyMobileBody();
        modifyMobileBody.setOldMobile(tvCode.getText().toString()+etMob.getText().toString());
        modifyMobileBody.setNewMobile(tvCode.getText().toString()+etMob.getText().toString());
        modifyMobileBody.setPinCode(pinCode.getText().toString());
        Call<UserInterface.UserResponse> modifyMobile = userInterface.modifyMobile(modifyMobileBody, user.getId());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.please_wait));
        progressDialog.show();
        modifyMobile.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(AddMobileActivity.this, getString(R.string.connection_error));
                    Toast.makeText(AddMobileActivity.this, "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError() && response.body().getErrorCode()== KamixApp.PIN_CODE_INCORRECT){
                    AlertDialog dialog = UiUtils.infoDialog(AddMobileActivity.this, getString(R.string.incorrect_pin));
                    Log.e("ADD MOBILE ACT", response.body().getErrorCode() + " error");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(AddMobileActivity.this, getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("ADD MOBILE ACT", response.body().getErrorCode() + " error");
                }
                else{
                    User user = response.body().getUser();
                    DAO dao = new DAO(AddMobileActivity.this);
                    dao.storeUser(user);
                    Toast.makeText(AddMobileActivity.this, getString(R.string.mobile_modify_success), Toast.LENGTH_LONG).show();

                    VerifyMobileFragment verifyMobileFragment = VerifyMobileFragment.newInstance(user, etMob.getText().toString());
                    verifyMobileFragment.show(getSupportFragmentManager(), VerifyMobileFragment.class.getSimpleName());
                    verifyMobileFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            setResult(RESULT_OK);
                            onBackPressed();
                        }
                    });
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                //UiUtils.snackBar(root, getString(R.string.connection_error));
                UiUtils.infoDialog(AddMobileActivity.this, getString(R.string.connection_error));
                Log.e("ADD MOBILE ACT", t.getMessage() + " error");
                t.printStackTrace();
            }
        });
    }

    private String getMessageByErrorCode(int code){
        switch (code){
            case 1311: return getString(R.string.problem_occurs);
            case 1315: return getString(R.string.invalid_phone_or_email);
            case 1900: return getString(R.string.existing_user);
            default: return getString(R.string.connection_error);
        }
    }
}
