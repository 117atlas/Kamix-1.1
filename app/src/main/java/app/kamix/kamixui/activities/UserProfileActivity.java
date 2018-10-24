package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.app.Utils;
import app.kamix.kamixui.adapters.MobileNumbersAdapter;
import app.kamix.kamixui.fragments.SetAsPNPINFragment;
import app.kamix.kamixui.fragments.VerifyEmailFragment;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.localstorage.DAO;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener, SetAsPNPINFragment.SetAsPNDone{

    public static final int REQ_MODIFY = 8888;

    private User user;

    private TextView userAccountStatus, email, emailStatus, firstName, lastName, address, city, country;
    private AppCompatButton addMobile, changePin, changePass;
    private RecyclerView mobileNumbers;

    private Toolbar toolbar;

    public User getUser(){
        if (user==null) user = KamixApp.getUser(this);
        return user;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

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

        userAccountStatus = findViewById(R.id.user_account_status);
        email = findViewById(R.id.email);
        emailStatus = findViewById(R.id.email_status);
        firstName = findViewById(R.id.firstname);
        lastName = findViewById(R.id.lastname);
        address = findViewById(R.id.address);
        city = findViewById(R.id.city);
        country = findViewById(R.id.country);
        addMobile = findViewById(R.id.add_mobile);
        changePin = findViewById(R.id.change_pin);
        changePass = findViewById(R.id.change_pass);
        mobileNumbers = findViewById(R.id.mobile_numbers);

        email.setOnClickListener(this);
        emailStatus.setOnClickListener(this);
        firstName.setOnClickListener(this);
        lastName.setOnClickListener(this);
        city.setOnClickListener(this);
        address.setOnClickListener(this);
        addMobile.setOnClickListener(this);
        changePin.setOnClickListener(this);
        changePass.setOnClickListener(this);

        bind();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.email:{
                Intent intent = new Intent(this, EditEmailActivity.class);
                intent.putExtra(User.class.getSimpleName(), user);
                startActivityForResult(intent, REQ_MODIFY);
            } break;
            case R.id.email_status:{
                if (user.getEmail()!=null && !user.getEmail().equalsIgnoreCase("")){
                    AlertDialog alertDialog = UiUtils.questionDialog(this,
                            getString(R.string.verify_question).replace("?????", getString(R.string.email_verif_label)).replace("????", email.getText().toString()),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    redoVerificationEmail();
                                }
                            });
                    alertDialog.show();
                }
            } break;
            case R.id.firstname:{
                Intent intent = new Intent(this, EditUserInfoActivity.class);
                intent.putExtra("WHAT", EditUserInfoActivity.WHAT_FIRSTNAME);
                intent.putExtra("DATA", user.getFirstname());
                intent.putExtra(User.class.getSimpleName(), user);
                startActivityForResult(intent, REQ_MODIFY);
            } break;
            case R.id.lastname:{
                Intent intent = new Intent(this, EditUserInfoActivity.class);
                intent.putExtra("WHAT", EditUserInfoActivity.WHAT_LASTNAME);
                intent.putExtra("DATA", user.getLastname());
                intent.putExtra(User.class.getSimpleName(), user);
                startActivityForResult(intent, REQ_MODIFY);
            } break;
            case R.id.address:{
                Intent intent = new Intent(this, EditUserInfoActivity.class);
                intent.putExtra("WHAT", EditUserInfoActivity.WHAT_ADDRESS);
                intent.putExtra("DATA", user.getAddress());
                intent.putExtra(User.class.getSimpleName(), user);
                startActivityForResult(intent, REQ_MODIFY);
            } break;
            case R.id.city:{
                Intent intent = new Intent(this, EditUserInfoActivity.class);
                intent.putExtra("WHAT", EditUserInfoActivity.WHAT_CITY);
                intent.putExtra("DATA", user.getCity());
                intent.putExtra(User.class.getSimpleName(), user);
                startActivityForResult(intent, REQ_MODIFY);
            } break;
            case R.id.country:{

            } break;
            case R.id.add_mobile:{
                Intent intent = new Intent(this, AddMobileActivity.class);
                intent.putExtra(User.class.getSimpleName(), user);
                startActivityForResult(intent, REQ_MODIFY);
            } break;
            case R.id.change_pin:{
                Intent intent = new Intent(this, ChangePINActivity.class);
                intent.putExtra(User.class.getSimpleName(), user);
                startActivity(intent);
            } break;
            case R.id.change_pass:{
                Intent intent = new Intent(this, ChangePassActivity.class);
                intent.putExtra(User.class.getSimpleName(), user);
                startActivity(intent);
            } break;
        }
    }

    private void bind(){
        userAccountStatus.setText(user.getStatus()==0?getString(R.string.account_active):getString(R.string.account_inactive));
        userAccountStatus.setTextColor(user.getStatus()==0? Color.GREEN:Color.RED);
        emailStatus.setText(user.isEmailVerified()?getString(R.string.verified):getString(R.string.unverified));
        emailStatus.setTextColor(user.isEmailVerified()?Color.GREEN:Color.RED);
        email.setText(user.getEmail()!=null?user.getEmail():"");
        firstName.setText(user.getFirstname());
        lastName.setText(user.getLastname());
        if (Utils.empty(user.getAddress())){
            address.setText(getString(R.string.empty_address));
            address.setTypeface(null, Typeface.ITALIC);
        }
        else{
            address.setText(user.getAddress());
            address.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        if (Utils.empty(user.getCity())){
            city.setText(getString(R.string.empty_city));
            city.setTypeface(null, Typeface.ITALIC);
        }
        else{
            city.setText(user.getCity());
            city.setTypeface(null, Typeface.BOLD_ITALIC);
        }
        country.setText(user.getCountry());

        MobileNumbersAdapter mobileNumbersAdapter = new MobileNumbersAdapter(this);
        mobileNumbers.setLayoutManager(new LinearLayoutManager(this));
        mobileNumbers.setAdapter(mobileNumbersAdapter);
        mobileNumbersAdapter.setData(user.getMobiles(), user.getMobilesVerified());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQ_MODIFY && resultCode== RESULT_OK){
            user = KamixApp.getUser(this);
            bind();
        }
    }

    private void redoVerificationEmail(){
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        Call<UserInterface.UserResponse> redoVerifEmail = userInterface.redoVerificationEmail(email.getText().toString(), user.getId());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.please_wait));
        progressDialog.show();
        redoVerifEmail.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(UserProfileActivity.this, getString(R.string.connection_error));
                    Toast.makeText(UserProfileActivity.this, "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(UserProfileActivity.this, getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("REDO VERIF MOBILE", response.body().getErrorCode() + " error");
                }
                else{
                    User user = response.body().getUser();
                    DAO dao = new DAO(UserProfileActivity.this);
                    dao.storeUser(user);
                    Toast.makeText(UserProfileActivity.this, getString(R.string.mobile_modify_success), Toast.LENGTH_LONG).show();

                    VerifyEmailFragment verifyEmailFragment = VerifyEmailFragment.newInstance(user, email.getText().toString());
                    verifyEmailFragment.show(getSupportFragmentManager(), VerifyEmailFragment.class.getSimpleName());
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(UserProfileActivity.this, getString(R.string.connection_error));
                Log.e("REDO VERIF MOBILE", t.getMessage() + " error");
                t.printStackTrace();
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

    @Override
    public void setAsPNDone() {
        onActivityResult(REQ_MODIFY, RESULT_OK, null);
    }
}
