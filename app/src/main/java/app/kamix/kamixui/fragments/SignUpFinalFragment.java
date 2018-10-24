package app.kamix.kamixui.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.activities.SignUpActivity;
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
public class SignUpFinalFragment extends Fragment {

    private EditText firstname;
    private EditText lastname;
    private TextView finalize;

    public SignUpFinalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_sign_up_final, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        firstname = view.findViewById(R.id.firstname);
        lastname = view.findViewById(R.id.lastname);
        finalize = view.findViewById(R.id.finalize);

        finalize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (firstname.getText().toString().equalsIgnoreCase("")){
                    firstname.setError(getString(R.string.enter_firstname));
                }
                else if (lastname.getText().toString().equalsIgnoreCase("")){
                    lastname.setError(getString(R.string.enter_lastname));
                }
                else{
                    ((SignUpActivity)getActivity()).setFirstname(firstname.getText().toString());
                    ((SignUpActivity)getActivity()).setLastname(lastname.getText().toString());
                    //swipe();
                    finalizeInscription();
                }
            }
        });
    }

    private void swipe(){
        ((SignUpActivity)getActivity()).swipe(3);
    }

    private void finalizeInscription(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.please_wait));
        progressDialog.show();
        User user = ((SignUpActivity)getActivity()).getUser();
        UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
        Call<UserInterface.UserResponse> register = userInterface.finalizeRegister(user.getId(), firstname.getText().toString(), lastname.getText().toString(), "Cameroon");
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
