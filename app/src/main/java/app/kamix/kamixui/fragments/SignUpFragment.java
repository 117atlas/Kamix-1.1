package app.kamix.kamixui.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.activities.LoginActivity;
import app.kamix.kamixui.activities.SignUpActivity;
import app.kamix.kamixui.activities.TermsConditionsActivity;
import app.kamix.kamixui.adapters.CountrySpinnerAdapter;
import app.kamix.kamixui.utils.Country;
import app.kamix.kamixui.utils.FormatUtils;
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
public class SignUpFragment extends Fragment {

    public TextView register, tvCode;
    public EditText etEmail, etMob;
    public CheckBox cbTermsAndConditions;
    public Spinner spCountry;
    private LinearLayout loginContainer;

    private int flagSelected = 0;

    public SignUpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        register =  view.findViewById(R.id.register);
        tvCode =  view.findViewById(R.id.tvCode);
        etEmail =  view.findViewById(R.id.etEmail);
        etMob =  view.findViewById(R.id.etMob);
        spCountry = view.findViewById(R.id.spCountry);
        loginContainer = view.findViewById(R.id.login_container);
        cbTermsAndConditions = view.findViewById(R.id.cbTermsAndConditions);

        loginContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), LoginActivity.class));
                getActivity().finish();
            }
        });

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etMob.getText().toString().equalsIgnoreCase("")) {
                    etMob.setError(getString(R.string.empty_mobile));
                } else if (!FormatUtils.isValidMobile("+237"+etMob.getText().toString())){
                    etMob.setError(getString(R.string.invalid_mobile));
                } else if (!etEmail.getText().toString().isEmpty() && !FormatUtils.isValidEmail(etEmail.getText().toString())) {
                    etEmail.setText(getString(R.string.invalid_email));
                } else {
                    if (/*!NetworkUtils.checkInternetConnectivity(SignUpActivity.this)*/false){
                        //UiUtils.snackBar(root, getString(R.string.not_online));
                    }
                    else {
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

                        ((SignUpActivity)getActivity()).setMobile(etMob.getText().toString());
                        ((SignUpActivity)getActivity()).setEmail(etEmail.getText().toString());
                        ((SignUpActivity)getActivity()).setFlag(flagSelected);
                        //swipe();
                        register();
                    }
                }

            }
        });

        cbTermsAndConditions.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    startActivity(new Intent(getContext(), TermsConditionsActivity.class)); // start terms and condition once user checked the checkbox
                }
            }
        });

        final ArrayList<Country> cArr = new ArrayList<Country>();
        cArr.add(new Country(R.drawable.cm, "+237"));
        //cArr.add(new Country(R.drawable.france_flg, "+33"));

        spCountry.setAdapter(new CountrySpinnerAdapter(getContext(), cArr)); // set country flag dropdown
        spCountry.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                tvCode.setText("" + cArr.get(i).getCode());
                flagSelected = i;
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

    }

    private void swipe(){
        ((SignUpActivity)getActivity()).swipe(1);
    }

    private void register(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.please_wait));
        progressDialog.show();
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        Call<UserInterface.UserResponse> register = userInterface.register(tvCode.getText().toString()+etMob.getText().toString(), etEmail.getText().toString());
        register.enqueue(new Callback<UserInterface.UserResponse>() {
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
                    if (response.body().getErrorCode()== KamixApp.EXISTING_USER){
                        UiUtils.infoDialog(getContext(), getString(R.string.existing_user_text));
                    }
                    else{
                        User user = response.body().getUser();
                        /*DAO dao = new DAO(SignUpActivity.this);
                        dao.storeUser(user);
                        nextActivity(user);*/
                        ((SignUpActivity)getActivity()).setUser(user);
                        swipe();
                    }
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                //UiUtils.snackBar(root, getString(R.string.connection_error));
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
