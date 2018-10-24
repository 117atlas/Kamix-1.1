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

public class ChangePINActivity extends AppCompatActivity {

    private EditText oldPin, newPin, confirmPin;
    private AppCompatButton modify;

    private Toolbar toolbar;

    private User user;

    public void handlePinCodeReceived(String pinCodeReceived){
        newPin.setText(pinCodeReceived);
        confirmPin.setText(pinCodeReceived);
        if (!oldPin.getText().toString().equalsIgnoreCase("") && oldPin.getText().toString().length()==4){
            changePin();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pin);

        user = (User) getIntent().getSerializableExtra(User.class.getSimpleName());

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        oldPin = findViewById(R.id.etpinold);
        newPin = findViewById(R.id.etpinnew);
        confirmPin = findViewById(R.id.etconfirmnewpin);
        modify = findViewById(R.id.modify);

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (oldPin.getText().toString().equalsIgnoreCase("")){
                    oldPin.setError(getString(R.string.empty_pin));
                }
                else if (newPin.getText().toString().equalsIgnoreCase("")) {
                    newPin.setError(getString(R.string.empty_pin_new));
                }
                else if (oldPin.getText().toString().equals(newPin.getText().toString())){
                    newPin.setError(getString(R.string.match_old_new_pin));
                }
                else if (!newPin.getText().toString().equals(confirmPin.getText().toString())){
                    confirmPin.setError(getString(R.string.match_new_pin_confirm));
                }
                else{
                    changePin();
                }
            }
        });

    }

    private void changePin(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.please_wait));
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);

        UserInterface.ChangePINBody changePINBody = new UserInterface.ChangePINBody();
        changePINBody.setOldPin(oldPin.getText().toString());
        changePINBody.setNewPin(newPin.getText().toString());

        Call<UserInterface.UserResponse> changePin = userInterface.changePin(changePINBody, user.getId());
        progressDialog.show();
        changePin.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(ChangePINActivity.this, getString(R.string.connection_error));
                    Log.e("CHANGE PIN", "Body null");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(ChangePINActivity.this, getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("CHANGE PIN", response.body().getErrorCode() + " error");
                }
                else{
                    Toast.makeText(ChangePINActivity.this, getString(R.string.change_pin_successful), Toast.LENGTH_LONG).show();
                    onBackPressed();
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                UiUtils.infoDialog(ChangePINActivity.this, getString(R.string.connection_error));
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
