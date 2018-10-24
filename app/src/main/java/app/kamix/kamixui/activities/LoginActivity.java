package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.adapters.CountrySpinnerAdapter;
import app.kamix.kamixui.utils.Country;
import app.kamix.kamixui.utils.FormatUtils;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.NetworkUtils;
import app.kamix.network.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public TextView login, tvCode, forgotPin;
    public EditText etEmail, etMob, etPin, etPass;
    public Spinner spCountry;
    private LinearLayout signupContainer, loginWithMobile, loginWithEmail;
    private SwitchCompat loginWith;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        login = (TextView) findViewById(R.id.login);
        tvCode = (TextView) findViewById(R.id.tvCode);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etMob = (EditText) findViewById(R.id.etMob);
        etPin = findViewById(R.id.etPin);
        etPass = findViewById(R.id.etPass);
        spCountry = (Spinner) findViewById(R.id.spCountry);
        forgotPin = findViewById(R.id.forgot_pin);
        signupContainer = findViewById(R.id.signup_container);
        loginWith = findViewById(R.id.login_with);
        loginWithEmail = findViewById(R.id.login_with_email);
        loginWithMobile = findViewById(R.id.login_with_mobile);

        signupContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
                finish();
            }
        });

        loginWith.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    loginWithEmail.setVisibility(View.VISIBLE);
                    loginWithMobile.setVisibility(View.GONE);
                    forgotPin.setVisibility(View.GONE);
                }
                else {
                    loginWithEmail.setVisibility(View.GONE);
                    loginWithMobile.setVisibility(View.VISIBLE);
                    forgotPin.setVisibility(View.VISIBLE);
                }
            }
        });
        
        forgotPin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etMob.getText().toString().equalsIgnoreCase("")) {
                    etMob.setError(getString(R.string.empty_mobile));
                } else if (!FormatUtils.isValidMobile(tvCode.getText().toString()+etMob.getText().toString())){
                    etMob.setError(getString(R.string.invalid_mobile));
                }
                else{
                    UiUtils.questionDialog(LoginActivity.this, getString(R.string.pin_forgotten_message), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(LoginActivity.this, "FORGOT PIN", Toast.LENGTH_SHORT).show();
                            resetPin();
                        }
                    }).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginWith.isChecked()){
                    if (etEmail.getText().toString().equalsIgnoreCase("")) {
                        etEmail.setError(getString(R.string.empty_email));
                    } else if (!FormatUtils.isValidEmail(etEmail.getText().toString())){
                        etEmail.setError(getString(R.string.invalid_email));
                    } else if (etPass.getText().toString().equalsIgnoreCase("")) {
                        etPass.setError(getString(R.string.empty_passwd));
                    } else {
                        if (/*!NetworkUtils.checkInternetConnectivity(SignUpActivity.this)*/false){
                            //UiUtils.snackBar(root, getString(R.string.not_online));
                        }
                        else {
                            next();
                        }
                    }
                }
                else{

                    if (etMob.getText().toString().equalsIgnoreCase("")) {
                        etMob.setError(getString(R.string.empty_mobile));
                    } else if (!FormatUtils.isValidMobile(tvCode.getText().toString()+etMob.getText().toString())){
                        etMob.setError(getString(R.string.invalid_mobile));
                    } else if (etPin.getText().toString().equalsIgnoreCase("")) {
                        etPin.setError(getString(R.string.empty_pin));
                    } else {
                        if (!NetworkUtils.checkInternetConnectivity(LoginActivity.this)){
                            //UiUtils.snackBar(root, getString(R.string.not_online));
                        }
                        else {
                            //next();
                            login();
                        }
                    }

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
    }

    private void loginDialog(){
        /*AlertDialog.Builder builder = new AlertDialog.Builder(SignUpActivity.this);
                builder.setMessage(getString(R.string.are_you_sure_register).replace("?????", "+237"+etMob.getText().toString()).replace("????", etEmail.getText().toString()));
                builder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        register();
                    }
                });
                builder.setNegativeButton(getString(R.string.no), null);
                AlertDialog alertDialog = builder.create();
                alertDialog.setCancelable(false);
                alertDialog.show();*/
    }

    private void next(){
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    private void login(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.please_wait));
        progressDialog.show();
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        Call<UserInterface.UserResponse> register = userInterface.login(loginWith.isChecked(), tvCode.getText().toString()+etMob.getText().toString(), etPin.getText().toString(), etEmail.getText().toString(), etPass.getText().toString());
        register.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(LoginActivity.this, getString(R.string.connection_error));
                    Toast.makeText(LoginActivity.this, "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(LoginActivity.this, getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("LOGIN", response.body().getErrorCode() + " error");
                }
                else{
                    if (response.body().getErrorCode()== KamixApp.INEXISTING_USER){
                        UiUtils.infoDialog(LoginActivity.this, getString(R.string.inexisting_user_text));
                    }
                    else{
                        User user = response.body().getUser();
                        /*DAO dao = new DAO(SignUpActivity.this);
                        dao.storeUser(user);
                        nextActivity(user);*/
                        //swipe();
                        next(user);
                    }
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                UiUtils.infoDialog(LoginActivity.this, getString(R.string.connection_error));
                //UiUtils.snackBar(root, getString(R.string.connection_error));
            }
        });
    }

    private void resetPin(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.please_wait));
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        Call<UserInterface.UserResponse> resetPin = userInterface.forgotPin(etPin.getText().toString(), tvCode.getText().toString()+etMob.getText().toString());
        progressDialog.show();
        resetPin.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    AlertDialog dialog = UiUtils.infoDialog(LoginActivity.this, getString(R.string.connection_error));
                    dialog.show();
                    Toast.makeText(LoginActivity.this, "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError()){
                    AlertDialog dialog = UiUtils.infoDialog(LoginActivity.this, getMessageByErrorCode(response.body().getErrorCode()));
                    dialog.show();
                    Log.e("LOGIN EXIS-RESET PIN", response.body().getErrorCode() + " error");
                }
                else{
                    User user = response.body().getUser();
                    UiUtils.infoDialog(LoginActivity.this, getString(R.string.pin_reset_success));
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(LoginActivity.this, getString(R.string.connection_error));
                Log.e("LOGIN EXIS USER", t.getMessage() + " error");
                t.printStackTrace();
            }
        });
    }

    private String getMessageByErrorCode(int code){
        switch (code){
            case 1311: return getString(R.string.problem_occurs);
            case 1315: return getString(R.string.invalid_phone_or_email);
            case 1900: return getString(R.string.existing_user);
            case 1910: return getString(R.string.inexisting_user_text);
            case 171: return getString(R.string.invalid_pin);
            case 172: return getString(R.string.invalid_passwd_);
            default: return getString(R.string.connection_error);
        }
    }

    private void next(User user){
        Intent intent = new Intent(this, SplashActivity.class);
        intent.putExtra(User.class.getSimpleName(), user);
        startActivity(intent);
        finish();
    }
}
