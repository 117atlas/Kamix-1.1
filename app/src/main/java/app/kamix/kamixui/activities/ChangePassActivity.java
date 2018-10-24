package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
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

import app.kamix.R;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePassActivity extends AppCompatActivity {

    private EditText oldPass, newPass, confirmPass;
    private AppCompatButton modify;

    private Toolbar toolbar;

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pass);

        user = (User) getIntent().getSerializableExtra(User.class.getSimpleName());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        oldPass = findViewById(R.id.etpasswdold);
        newPass = findViewById(R.id.etpasswdnew);
        confirmPass = findViewById(R.id.etconfirmnewpasswd);
        modify = findViewById(R.id.modify);

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (oldPass.getText().toString().equalsIgnoreCase("")){
                    oldPass.setError(getString(R.string.empty_pin));
                }
                else if (newPass.getText().toString().equalsIgnoreCase("")) {
                    newPass.setError(getString(R.string.empty_pin_new));
                }
                else if (oldPass.getText().toString().equals(newPass.getText().toString())){
                    newPass.setError(getString(R.string.match_old_new_pin));
                }
                else if (!newPass.getText().toString().equals(confirmPass.getText().toString())){
                    confirmPass.setError(getString(R.string.match_new_pin_confirm));
                }
                else{
                    changePass();
                }
            }
        });
    }

    private void changePass(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.please_wait));
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);

        UserInterface.ChangePasswdBody changePasswdBody = new UserInterface.ChangePasswdBody();
        changePasswdBody.setOldPasswd(oldPass.getText().toString());
        changePasswdBody.setNewPasswd(newPass.getText().toString());

        Call<UserInterface.UserResponse> changePass = userInterface.changePasswd(changePasswdBody, user.getId());
        progressDialog.show();
        changePass.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(ChangePassActivity.this, getString(R.string.connection_error));
                    Log.e("CHANGE PASS", "Body null");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(ChangePassActivity.this, getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("CHANGE PASS", response.body().getErrorCode() + " error");
                }
                else{
                    Toast.makeText(ChangePassActivity.this, getString(R.string.change_pass_successful), Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                UiUtils.infoDialog(ChangePassActivity.this, getString(R.string.connection_error));
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
}
