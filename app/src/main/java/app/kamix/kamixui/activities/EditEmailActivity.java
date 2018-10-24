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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.app.Utils;
import app.kamix.kamixui.fragments.VerifyEmailFragment;
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

public class EditEmailActivity extends AppCompatActivity {

    private TextView labelOldEmail, labelPass, passPurpose;
    private EditText etEmailOld, etEmailNew, passwd;
    private AppCompatButton modify;

    private Toolbar toolbar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_email);

        user = (User) getIntent().getSerializableExtra(User.class.getSimpleName());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        etEmailOld = findViewById(R.id.etEmailOld);
        etEmailNew = findViewById(R.id.etEmailNew);
        passwd = findViewById(R.id.etPass);
        modify = findViewById(R.id.modify);

        labelOldEmail = findViewById(R.id.labelEmailOld);
        labelPass = findViewById(R.id.labelPass);
        passPurpose = findViewById(R.id.pass_purpose);

        final ArrayList<Country> cArr = new ArrayList<Country>();
        cArr.add(new Country(R.drawable.cm, "+237"));

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (etEmailNew.getText().toString().equalsIgnoreCase("")){
                    etEmailNew.setError(getString(R.string.empty_email));
                }
                else if (!FormatUtils.isValidEmail(etEmailNew.getText().toString())){
                    etEmailNew.setError(getString(R.string.invalid_email));
                }
                else if (user.getEmail()!=null && !user.getEmail().equalsIgnoreCase("") && passwd.getText().toString().equalsIgnoreCase("")){
                    passwd.setText(getString(R.string.empty_passwd));
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(EditEmailActivity.this)
                            .setMessage(getString(R.string.edit_mobile_confirm).replace("?????", etEmailOld.getText().toString()).replace("????", etEmailNew.getText().toString()))
                            .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    modifyEmail();
                                }
                            })
                            .setNegativeButton(getString(R.string.no), null);
                    AlertDialog alertDialog = builder.create();
                    alertDialog.setCancelable(false);
                    alertDialog.show();
                }
            }
        });

        etEmailOld.setText(user.getEmail());

        if (user.getEmail()==null || user.getEmail().equalsIgnoreCase("")){
            labelPass.setVisibility(View.GONE);
            labelOldEmail.setVisibility(View.GONE);
            passPurpose.setVisibility(View.GONE);
            etEmailOld.setVisibility(View.GONE);
            passwd.setVisibility(View.GONE);
        }
        else{
            labelPass.setVisibility(View.VISIBLE);
            labelOldEmail.setVisibility(View.VISIBLE);
            passPurpose.setVisibility(View.VISIBLE);
            etEmailOld.setVisibility(View.VISIBLE);
            passwd.setVisibility(View.VISIBLE);
        }

    }

    private void modifyEmail(){
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        UserInterface.ModifyEmailBody modifyEmailBody = new UserInterface.ModifyEmailBody();
        modifyEmailBody.setOldEmail(etEmailOld.getText().toString());
        modifyEmailBody.setNewEmail(etEmailNew.getText().toString());
        modifyEmailBody.setPasswd(passwd.getText().toString());
        Call<UserInterface.UserResponse> modifyEmail = userInterface.modifyEmail(modifyEmailBody, user.getId());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.please_wait));
        progressDialog.show();
        modifyEmail.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(EditEmailActivity.this, getString(R.string.connection_error));
                    Toast.makeText(EditEmailActivity.this, "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError() && response.body().getErrorCode()== KamixApp.PASSWD_INCORRECT){
                    AlertDialog dialog = UiUtils.infoDialog(EditEmailActivity.this, getString(R.string.incorrect_pin));
                    Log.e("EDIT EMAIL ACT", response.body().getErrorCode() + " error");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(EditEmailActivity.this, getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("EDIT EMAIL ACT", response.body().getErrorCode() + " error");
                }
                else{
                    User user = response.body().getUser();
                    DAO dao = new DAO(EditEmailActivity.this);
                    dao.storeUser(user);
                    Toast.makeText(EditEmailActivity.this, getString(R.string.mobile_modify_success), Toast.LENGTH_LONG).show();

                    VerifyEmailFragment verifyEmailFragment = VerifyEmailFragment.newInstance(user, etEmailNew.getText().toString());
                    verifyEmailFragment.show(getSupportFragmentManager(), VerifyEmailFragment.class.getSimpleName());
                    verifyEmailFragment.setOnDismissListener(new DialogInterface.OnDismissListener() {
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
                UiUtils.infoDialog(EditEmailActivity.this, getString(R.string.connection_error));
                Log.e("EDIT EMAIL ACT", t.getMessage() + " error");
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
