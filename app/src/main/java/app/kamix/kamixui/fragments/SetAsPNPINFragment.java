package app.kamix.kamixui.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.activities.UserProfileActivity;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.localstorage.DAO;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class SetAsPNPINFragment extends DialogFragment {

    private EditText pinCode;
    private AppCompatButton consult, cancel;

    public interface SetAsPNDone extends Serializable{
        void setAsPNDone();
    }

    private SetAsPNDone setAsPNDone;
    private String mobile;

    public static SetAsPNPINFragment newInstance(SetAsPNDone setAsPNDone, String mobile){
        SetAsPNPINFragment setAsPNPINFragment = new SetAsPNPINFragment();
        Bundle args = new Bundle();
        args.putSerializable("DONE", setAsPNDone);
        args.putString("MOBILE", mobile);
        setAsPNPINFragment.setArguments(args);
        return setAsPNPINFragment;
    }

    public SetAsPNPINFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        return inflater.inflate(R.layout.fragment_set_as_pnpin, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setAsPNDone = (SetAsPNDone) getArguments().getSerializable("DONE");
        mobile = getArguments().getString("MOBILE");

        pinCode = view.findViewById(R.id.etPin);
        consult = view.findViewById(R.id.consult);
        cancel = view.findViewById(R.id.cancel);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });

        consult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (pinCode.getText().toString().equalsIgnoreCase("")){
                    pinCode.setError(getString(R.string.empty_pin));
                }
                else{
                    //simulateConsultBalance();
                    setAsPrincipalNumber(mobile);
                }
            }
        });
    }

    private void setAsPrincipalNumber(final String mobile){
        User user = KamixApp.getUser(getContext());
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        Call<UserInterface.UserResponse> setAsPNumber = userInterface.setAsPrincipalNumber(user.getId(), mobile, pinCode.getText().toString());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.please_wait));
        progressDialog.show();
        setAsPNumber.enqueue(new Callback<UserInterface.UserResponse>() {
            @Override
            public void onResponse(Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                    Toast.makeText(getContext(), "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(getContext(), getMessageByErrorCode(response.body().getErrorCode()));
                    Log.e("REDO VERIF MOBILE", response.body().getErrorCode() + " error");
                }
                else{
                    User user = response.body().getUser();
                    DAO dao = new DAO(getContext());
                    dao.storeUser(user);
                    Toast.makeText(getContext(), getString(R.string.mobile_modify_success), Toast.LENGTH_LONG).show();

                    setAsPNDone.setAsPNDone();
                    dismiss();
                }
            }
            @Override
            public void onFailure(Call<UserInterface.UserResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
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

}
