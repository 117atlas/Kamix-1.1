package app.kamix.kamixui.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import app.kamix.R;
import app.kamix.kamixui.activities.AddMobileActivity;
import app.kamix.kamixui.activities.EditMobileActivity;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.localstorage.DAO;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import app.kamix.receiver.SmsReceiver;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class VerifyMobileFragment extends DialogFragment {

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener!=null) onDismissListener.onDismiss(dialog);
    }

    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    public void handleCodeReceived(String codeReceived){
        confirmCode.setText(codeReceived);
        Toast.makeText(getContext(), getString(R.string.code_detected_automatically), Toast.LENGTH_SHORT).show();
    }

    private TextView confirmMobileMessage;
    private EditText confirmCode;
    private AppCompatButton confirm, confirmLater;

    private User user;
    private String mobile;

    public static VerifyMobileFragment newInstance(User user, String mobile){
        VerifyMobileFragment verifyMobileFragment = new VerifyMobileFragment();
        Bundle args = new Bundle();
        args.putSerializable(User.class.getSimpleName(), user);
        args.putString("MOBILE", mobile);
        verifyMobileFragment.setArguments(args);
        return verifyMobileFragment;
    }

    public VerifyMobileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        return inflater.inflate(R.layout.fragment_verify_mobile, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SmsReceiver smsReceiver = new SmsReceiver(this);
        getActivity().registerReceiver(smsReceiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));

        confirmMobileMessage = view.findViewById(R.id.confirm_mobile_address_message);
        confirmCode = view.findViewById(R.id.etPin);
        confirm = view.findViewById(R.id.yes);
        confirmLater = view.findViewById(R.id.no);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmCode.getText().toString().equalsIgnoreCase("")){
                    confirmCode.setError(getString(R.string.empty_confirm_code));
                }
                else verifyMobile();
            }
        });
        confirmLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
                if (getActivity()!=null && (getActivity() instanceof EditMobileActivity || getActivity() instanceof AddMobileActivity)) getActivity().onBackPressed();
            }
        });

        user = (User) getArguments().getSerializable(User.class.getSimpleName());
        mobile = getArguments().getString("MOBILE");

        confirmMobileMessage.setText(confirmMobileMessage.getText().toString().replace("????", "+237"+mobile));
    }

    private void verifyMobile(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.verifying_mobile));
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);

        UserInterface.VerifyMobileBody verifyMobileBody = new UserInterface.VerifyMobileBody();
        verifyMobileBody.setUserId(user.getId());
        verifyMobileBody.setMobile(mobile.contains("+237")?mobile:"+237"+mobile);
        verifyMobileBody.setPinCode(confirmCode.getText().toString());
        verifyMobileBody.setUserPin(false);

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
                    Log.e("MODIFY MOBILE FRAG", response.body().getErrorCode() + " error");
                }
                else{
                    //Success verification Mobile
                    User user = response.body().getUser();
                    DAO dao = new DAO(getContext());
                    dao.storeUser(user);

                    Toast.makeText(getContext(), getString(R.string.mobile_verification_success_small), Toast.LENGTH_LONG).show();
                    dismiss();
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

}
