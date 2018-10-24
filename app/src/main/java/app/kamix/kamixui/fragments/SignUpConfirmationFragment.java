package app.kamix.kamixui.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.kamixui.activities.SignUpActivity;
import app.kamix.kamixui.adapters.CountrySpinnerAdapter;
import app.kamix.kamixui.utils.Country;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SignUpConfirmationFragment extends Fragment {

    public EditText etEmail, etMob, etPin, etPass;
    public TextView confirm, tvCode;
    public Spinner spCountry;
    private LinearLayout resend_container;

    private String oldEmail;
    private String oldMobile;
    private int flag;

    public SignUpConfirmationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_confirmation, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //user = (User) getIntent().getSerializableExtra(User.class.getSimpleName());

        etEmail =  view.findViewById(R.id.etEmail);
        etMob =  view.findViewById(R.id.etMob);
        etPin =  view.findViewById(R.id.etPin);
        etPass =  view.findViewById(R.id.etPass);
        confirm =  view.findViewById(R.id.confirm_register);
        tvCode =  view.findViewById(R.id.tvCode);
        spCountry =  view.findViewById(R.id.spCountry);
        resend_container = view.findViewById(R.id.resend_container);

        final ArrayList<Country> cArr = new ArrayList<Country>();
        cArr.add(new Country(R.drawable.cm, "+237"));
        cArr.add(new Country(R.drawable.france_flg, "+33"));
        spCountry.setAdapter(new CountrySpinnerAdapter(getContext(), cArr)); // set country flag dropdown
        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tvCode.setText("" + cArr.get(i).getCode());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etPin.getText().toString().equalsIgnoreCase("")) {
                    etPin.setError(getString(R.string.empty_pin));
                } else if (etPin.getText().toString().length() < 4) {
                    etPin.setError(getString(R.string.invalid_pin_length));
                } else if (!( oldEmail==null || oldEmail.isEmpty()) && etPass.getText().toString().equalsIgnoreCase("")) {
                    etPass.setError(getString(R.string.invalid_passwd));
                } else {
                    //swipe();
                    verifyMobile();
                }
            }
        });

        resend_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //tvResendSuccess.setVisibility(View.VISIBLE); // show resend pin text
                redoVerification();
                Toast.makeText(getContext(), "RESEND", Toast.LENGTH_SHORT).show();
            }
        });

        bind();
    }

    @Override
    public void onResume() {
        super.onResume();
        //oldMobile = ((SignUpActivity)getActivity()).getMobile();
        //oldEmail = ((SignUpActivity)getActivity()).getEmail();
        //bind();
    }

    private void swipe(){
        ((SignUpActivity)getActivity()).swipe(2);
    }

    public void setUserInfos(String mobile, String email, int flag){
        this.oldMobile = mobile;
        this.oldEmail = email;
        this.flag = flag;
        bind();
    }

    private void bind(){
        etMob.setText(oldMobile);
        etEmail.setText(oldEmail);
        final ArrayList<Country> cArr = new ArrayList<Country>();
        cArr.add(new Country(R.drawable.cm, "+237"));
        cArr.add(new Country(R.drawable.france_flg, "+33"));
        spCountry.setSelection(((SignUpActivity)getActivity()).getFlag());
        tvCode.setText(cArr.get(((SignUpActivity)getActivity()).getFlag()).getCode() + "");
    }

    public void redoVerification(){
        String _oldmobile = oldMobile.contains("+237")?oldMobile:"+237"+oldMobile;
        String _newmobile = "+237"+etMob.getText().toString();

        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.please_wait));
        User user = ((SignUpActivity)getActivity()).getUser();

        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        Call<UserInterface.UserResponse> redoVerification = null;
        if (oldEmail!=null && !oldEmail.equalsIgnoreCase("") && !etEmail.getText().toString().equalsIgnoreCase(""))
            redoVerification = userInterface.redoverification(user.getId(), _oldmobile, _newmobile, oldEmail, etEmail.getText().toString());
        else if ((oldEmail==null || oldEmail.equalsIgnoreCase("")) &&  !etEmail.getText().toString().equalsIgnoreCase(""))
            redoVerification = userInterface.redoverification(user.getId(), _oldmobile, _newmobile, etEmail.getText().toString());
        else
            redoVerification = userInterface.redoverification(user.getId(), _oldmobile, _newmobile);

        progressDialog.show();
        redoVerification.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                    Toast.makeText(getContext(), "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(getContext(), getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("REGISTER", response.body().getErrorCode() + " error");
                }
                else{
                    //Alert
                    UiUtils.infoDialog(getContext(), getString(R.string.resend_success)); // show resend pin text
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
            }
        });
    }

    private String getMessageByErrorCode(int code){
        switch (code){
            case 1311: return getString(R.string.problem_occurs);
            case 1315: return getString(R.string.invalid_phone_or_email);
            case 171: return getString(R.string.invalid_pin);
            case 172: return getString(R.string.invalid_passwd_);
            default: return getString(R.string.connection_error);
        }
    }

    private void verifyMobile(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.verifying_mobile));
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);

        User user = ((SignUpActivity)getActivity()).getUser();

        UserInterface.VerifyMobileBody verifyMobileBody = new UserInterface.VerifyMobileBody();
        verifyMobileBody.setUserId(user.getId());
        verifyMobileBody.setMobile("+237"+etMob.getText().toString());
        verifyMobileBody.setPinCode(etPin.getText().toString());
        verifyMobileBody.setUserPin(true);

        Call<UserInterface.UserResponse> verifyMobile = userInterface.verifyMobile(verifyMobileBody, user.getId());
        progressDialog.show();
        verifyMobile.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body() == null) {
                    UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                    Toast.makeText(getContext(), "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(getContext(), getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("REGISTER", response.body().getErrorCode() + " error");
                }
                else{
                    //Success verification Mobile
                    if (!etEmail.getText().toString().equalsIgnoreCase("")){
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                                .setMessage(getString(R.string.mobile_verification_success))
                                .setPositiveButton(getString(R.string.verify_label), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        verifyEmail();
                                    }
                                })
                                .setNegativeButton(getString(R.string.skip_email_verif), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //nextActivity();
                                        swipe();
                                    }
                                });
                        AlertDialog alertDialog = builder.create();
                        alertDialog.setCancelable(false);
                        alertDialog.show();
                    }
                    else{
                        //nextActivity();
                        swipe();
                    }

                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
            }
        });
    }

    private void verifyEmail(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.verifying_mobile));
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);

        User user = ((SignUpActivity)getActivity()).getUser();

        UserInterface.VerifyEmailBody verifyEmailBody = new UserInterface.VerifyEmailBody();
        verifyEmailBody.setUserId(user.getId());
        verifyEmailBody.setEmail(etEmail.getText().toString());
        verifyEmailBody.setPasswd(etPass.getText().toString());
        verifyEmailBody.setUserPass(true);

        Call<UserInterface.UserResponse> verifyEmail = userInterface.verifyEmail(verifyEmailBody, user.getId());
        progressDialog.show();
        verifyEmail.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body() == null) {
                    UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                    Toast.makeText(getContext(), "Null Body", Toast.LENGTH_LONG).show();
                    alertErrorVerifyEmail();
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(getContext(), getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("REGISTER", response.body().getErrorCode() + " error");
                    alertErrorVerifyEmail();
                }
                else{
                    //Success email verification
                    Toast.makeText(getContext(), getString(R.string.email_verification_success), Toast.LENGTH_LONG).show();
                    //nextActivity();
                    swipe();
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));

                alertErrorVerifyEmail();
            }
        });
    }

    /*private void nextActivity(){
        Intent i = new Intent(SignUpConfirmationActivity.this, AccountActivationActivity.class); // account activation screen call
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(User.class.getSimpleName(), user.getId());
        startActivity(i);
    }*/

    private void alertErrorVerifyEmail(){
        //Skip dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.restart_verify_email))
                .setPositiveButton(getString(R.string.skip_email_verif), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //nextActivity();
                        swipe();
                    }
                })
                .setNegativeButton(getString(R.string.restart_email_verif), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        verifyEmail();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
