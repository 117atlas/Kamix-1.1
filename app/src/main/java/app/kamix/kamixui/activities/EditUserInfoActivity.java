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
import android.widget.TextView;

import app.kamix.R;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.localstorage.DAO;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditUserInfoActivity extends AppCompatActivity {

    private TextView euiTitle, euiLabel;
    private EditText euiEdit;
    private AppCompatButton modify;

    private Toolbar toolbar;

    private int what;
    private String data;
    private User user;

    public static final int WHAT_FIRSTNAME = 789;
    public static final int WHAT_LASTNAME = 791;
    public static final int WHAT_ADDRESS = 795;
    public static final int WHAT_CITY = 799;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_info);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //euiTitle = findViewById(R.id.edit_dialog_title);
        euiLabel = findViewById(R.id.userinfos_label);
        euiEdit = findViewById(R.id.userinfos);
        modify = findViewById(R.id.modify);

        modify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (euiEdit.getText().toString().equalsIgnoreCase("") || euiEdit.getText().toString().equals(data)) onBackPressed();
                else updateUser();
            }
        });

        bind();
    }

    private void bind(){
        what = getIntent().getIntExtra("WHAT", WHAT_FIRSTNAME);
        data = getIntent().getStringExtra("DATA");
        user = (User) getIntent().getSerializableExtra(User.class.getSimpleName());
        switch (what){
            case WHAT_FIRSTNAME: euiLabel.setText(getString(R.string.edit_firstname_label)); break;
            case WHAT_LASTNAME: euiLabel.setText(getString(R.string.edit_lastname_label)); break;
            case WHAT_ADDRESS: euiLabel.setText(getString(R.string.edit_address_label)); break;
            case WHAT_CITY: euiLabel.setText(getString(R.string.edit_city_label)); break;
            default: euiLabel.setText(getString(R.string.edit_lastname_label)); break;
        }
        euiEdit.setText(data==null?"":data);
    }

    private void updateUser(){
        switch (what){
            case WHAT_FIRSTNAME: user.setFirstname(euiEdit.getText().toString()); break;
            case WHAT_LASTNAME: user.setLastname(euiEdit.getText().toString()); break;
            case WHAT_ADDRESS: user.setAddress(euiEdit.getText().toString()); break;
            case WHAT_CITY: user.setCity(euiEdit.getText().toString()); break;
            default: user.setFirstname(euiEdit.getText().toString()); break;
        }
        final ProgressDialog progressDialog = UiUtils.loadingDialog(EditUserInfoActivity.this, getString(R.string.please_wait));
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        Call<UserInterface.UserResponse> updateUser = userInterface.updateUser(user, user.getId());
        progressDialog.show();
        updateUser.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    Log.e("EDIT USER INFOS", "Body null");
                    errorDialog(getString(R.string.connection_error));
                }
                else if (response.body().isError()){
                    Log.e("EDIT USER INFOS", "Is Error " + response.body().getErrorCode());
                    errorDialog(getMessageByErrorCode(response.body().getErrorCode()));
                }
                else{
                    User _user = response.body().getUser();
                    DAO dao = new DAO(EditUserInfoActivity.this);
                    dao.storeUser(_user);

                    setResult(RESULT_OK);
                    onBackPressed();
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                errorDialog(getString(R.string.connection_error));
            }
        });
    }

    private String getMessageByErrorCode(int code){
        switch (code){
            case 1311: return getString(R.string.problem_occurs);
            case 1315: return getString(R.string.invalid_phone_or_email);
            default: return getString(R.string.connection_error);
        }
    }

    private void errorDialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(EditUserInfoActivity.this)
                .setMessage(message + getString(R.string.would_like_retry))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        updateUser();
                    }
                })
                .setNegativeButton(getString(R.string.no), null);
        AlertDialog alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.show();
    }
}
