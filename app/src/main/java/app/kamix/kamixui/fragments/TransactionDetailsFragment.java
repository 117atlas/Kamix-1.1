package app.kamix.kamixui.fragments;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.Locale;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.app.Utils;
import app.kamix.kamixui.activities.FundingInstructionsActivity;
import app.kamix.kamixui.activities.TransactionSucessActivity;
import app.kamix.kamixui.utils.FormatUtils;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.Funding;
import app.kamix.models.Payment;
import app.kamix.models.Transfert;
import app.kamix.models.User;
import app.kamix.models.Withdrawal;
import app.kamix.network.BaseResponse;
import app.kamix.network.FundingInterface;
import app.kamix.network.NetworkAPI;
import app.kamix.network.PaymentInterface;
import app.kamix.network.TransfertInterface;
import app.kamix.network.WithdrawalInterface;
import app.kamix.network.models.FundingData;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 */
public class TransactionDetailsFragment extends DialogFragment implements TransactionPINFragment.DoTransaction{

    private TextView receiver, amount, total, fees, mobile, message, title;
    private LinearLayout receiverContainer, mobileMoneyContainer;
    private AppCompatButton proceed, cancel;

    private Serializable transaction;
    private Funding funding;
    private Withdrawal withdrawal;
    private Transfert transfert;
    private Payment payment;
    private int what;
    public static final int DETAILS_FUNDING = 6;
    public static final int DETAILS_WITHDRAWAL = 7;
    public static final int DETAILS_TRANSFERT = 8;
    public static final int DETAILS_PAYMENT = 9;

    private User user;

    public TransactionDetailsFragment() {
        // Required empty public constructor
    }

    public static TransactionDetailsFragment newInstance(Serializable transaction, int what){
        TransactionDetailsFragment transactionDetailsFragment = new TransactionDetailsFragment();
        Bundle args = new Bundle();
        args.putSerializable("TRANSACTION", transaction);
        args.putInt("WHAT", what);
        transactionDetailsFragment.setArguments(args);
        return transactionDetailsFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        return inflater.inflate(R.layout.fragment_tpdetails, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        user = KamixApp.getUser(getContext());

        transaction = getArguments().getSerializable("TRANSACTION");
        what = getArguments().getInt("WHAT");
        switch (what){
            case DETAILS_FUNDING: funding = (Funding) transaction; break;
            case DETAILS_WITHDRAWAL: withdrawal = (Withdrawal) transaction; break;
            case DETAILS_TRANSFERT: transfert = (Transfert) transaction; break;
            case DETAILS_PAYMENT: payment = (Payment) transaction; break;
        }

        title = view.findViewById(R.id.transaction_details_header);
        receiver = view.findViewById(R.id.receiver);
        amount = view.findViewById(R.id.amount);
        total = view.findViewById(R.id.total);
        fees = view.findViewById(R.id.fees);
        mobile = view.findViewById(R.id.mobile_noney);
        message = view.findViewById(R.id.transaction_message);
        proceed = view.findViewById(R.id.proceed);
        cancel = view.findViewById(R.id.cancel);
        receiverContainer = view.findViewById(R.id.receiver_container);
        mobileMoneyContainer = view.findViewById(R.id.mobile_money_container);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.questionDialog(getContext(), getString(R.string.cancel_transaction_question), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (what){
                            case DETAILS_FUNDING: cancelFunding(); break;
                            case DETAILS_PAYMENT: cancelPayment(); break;
                            case DETAILS_TRANSFERT: cancelTransfert(); break;
                            case DETAILS_WITHDRAWAL: cancelWithdrawal(); break;
                        }
                    }
                }).show();
            }
        });

        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                requestPIN();
            }
        });

        switch (what){
            case DETAILS_FUNDING: {
                title.setText(getString(R.string.funding_details_label));
                receiverContainer.setVisibility(View.VISIBLE);
                mobileMoneyContainer.setVisibility(View.VISIBLE);

                receiver.setText(funding.getReceiver().name() + (funding.isForMe()?"": "(" + funding.getReceiverMobile() + ")"));
                amount.setText(FormatUtils.formatAmount(funding.getAmount()));
                fees.setText(FormatUtils.formatAmount(funding.getFees()));
                total.setText(FormatUtils.formatAmount(funding.getFees()+funding.getAmount()));
                mobile.setText(funding.getMobileNumber());
                message.setText(funding.getMessage());
            } break;
            case DETAILS_WITHDRAWAL: {
                title.setText(getString(R.string.withdrawal_details_label));
                receiverContainer.setVisibility(View.GONE);
                mobileMoneyContainer.setVisibility(View.VISIBLE);

                amount.setText(FormatUtils.formatAmount(withdrawal.getAmount()));
                fees.setText(FormatUtils.formatAmount(withdrawal.getFees()));
                total.setText(FormatUtils.formatAmount(withdrawal.getFees()+withdrawal.getAmount()));
                mobile.setText(withdrawal.getMobileNumber());
                message.setText(withdrawal.getMessage());
            } break;
            case DETAILS_TRANSFERT: {
                title.setText(getString(R.string.transfert_details_label));
                receiverContainer.setVisibility(View.VISIBLE);
                mobileMoneyContainer.setVisibility(View.GONE);

                receiver.setText(transfert.getReceiver().name() + "(" + transfert.getReceiverMobile() + ")");
                amount.setText(FormatUtils.formatAmount(transfert.getAmount()));
                fees.setText(FormatUtils.formatAmount(transfert.getFees()));
                total.setText(FormatUtils.formatAmount(transfert.getFees()+transfert.getAmount()));
                message.setText(transfert.getMessage());
            } break;
            case DETAILS_PAYMENT: {
                title.setText(getString(R.string.payment_details_label));
                receiverContainer.setVisibility(View.VISIBLE);
                mobileMoneyContainer.setVisibility(View.GONE);

                receiver.setText(payment.getMerchant().name());
                amount.setText(FormatUtils.formatAmount(payment.getAmount()));
                fees.setText(FormatUtils.formatAmount(payment.getFees()));
                total.setText(FormatUtils.formatAmount(payment.getFees()+payment.getAmount()));
                message.setText(payment.getMessage());
            } break;
        }

    }

    private void requestPIN(){
        TransactionPINFragment transactionPINFragment = TransactionPINFragment.newInstance(this);
        transactionPINFragment.show(getFragmentManager(), TransactionPINFragment.class.getSimpleName());
    }

    private void incorrectPINDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setMessage(getString(R.string.transaction_incorrect_pin_message))
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPIN();
                    }
                })
                .setNegativeButton(getString(R.string.no), null)
                .setCancelable(false);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void transactionSuccess(Serializable transaction, User user, String data){
        Intent intent = new Intent(getContext(), TransactionSucessActivity.class);
        intent.putExtra("TRANSACTION", transaction);
        intent.putExtra("data", data);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void doTransaction(String pinCode) {
        switch (what){
            case DETAILS_FUNDING: beginFunding(pinCode); break;
            case DETAILS_PAYMENT: doPayment(pinCode); break;
            case DETAILS_TRANSFERT: doTransfert(pinCode); break;
            case DETAILS_WITHDRAWAL: beginWithdrawal(pinCode); break;
        }
    }

    @Override
    public void cancel() {

    }

    private void beginFunding(final String pincode){
        FundingInterface fundingInterface = NetworkAPI.getClient().create(FundingInterface.class);
        Call<FundingInterface.BeginFundingResponse> begin = fundingInterface.begin(user.getId(), funding.getId(), pincode, Locale.getDefault().getLanguage());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.processing_transaction));
        progressDialog.show();
        begin.enqueue(new Callback<FundingInterface.BeginFundingResponse>() {
            @Override
            public void onResponse(Call<FundingInterface.BeginFundingResponse> call, Response<FundingInterface.BeginFundingResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                    Log.e("BEGIN FUNDING", "Body Null");
                }
                else if (response.body().isError() && response.body().getErrorCode()==FundingInterface.FUNDING_INCORRECT_PINCODE){
                    incorrectPINDialog();
                }
                else if (response.body().isError() && response.body().getErrorCode()==FundingInterface.FUNDING_WECASHUP_ERROR) {
                    UiUtils.questionDialog(getContext(), getString(R.string.we_cash_up_error), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            beginFunding(pincode);
                        }
                    }).show();
                    Log.e("BEGIN FUNDING", "We cash up server error");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(getContext(), getString(R.string.transaction_server_error));
                    Log.e("BEGIN FUNDING", response.message() +" - "+response.body().getErrorCode());
                }
                else{
                    String resCode = response.body().getFundingData()==null?null:String.valueOf(response.body().getFundingData().getWcuResponse().getCode());
                    if (response.body().getFundingData()!=null && response.body().getFundingData().getFunding()!=null){
                        Intent intent = new Intent(getContext(), FundingInstructionsActivity.class);
                        intent.putExtra(FundingData.class.getSimpleName(), response.body().getFundingData());
                        startActivity(intent);
                        dismiss();
                        getActivity().finish();
                    }
                    else if (resCode.startsWith("4") || resCode.startsWith("5")){
                        UiUtils.infoDialog(getContext(), getString(R.string.transaction_server_error));
                        Log.e("BEGIN FUNDING", response.body().getFundingData().getWcuResponse().getDetails());
                    }
                    else{
                        UiUtils.infoDialog(getContext(), getString(R.string.transaction_server_error));
                        Log.e("BEGIN FUNDING", "RESP NULL");
                    }
                }
            }
            @Override
            public void onFailure(Call<FundingInterface.BeginFundingResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }

    private void beginWithdrawal(final String pincode){
        WithdrawalInterface withdrawalInterface = NetworkAPI.getClient().create(WithdrawalInterface.class);
        Call<WithdrawalInterface.BeginWithdrawalResponse> begin = withdrawalInterface.begin(user.getId(), withdrawal.getId(), pincode, Locale.getDefault().getLanguage());
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.processing_transaction));
        progressDialog.show();
        begin.enqueue(new Callback<WithdrawalInterface.BeginWithdrawalResponse>() {
            @Override
            public void onResponse(Call<WithdrawalInterface.BeginWithdrawalResponse> call, Response<WithdrawalInterface.BeginWithdrawalResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                    Log.e("BEGIN WITHDRAWAL", "Body Null");
                }
                else if (response.body().isError() && response.body().getErrorCode()==WithdrawalInterface.WITHDRAWAL_INCORRECT_PINCODE){
                    incorrectPINDialog();
                }
                else if (response.body().isError() && response.body().getErrorCode()==WithdrawalInterface.WITHDRAWAL_WECASHUP_ERROR) {
                    UiUtils.questionDialog(getContext(), getString(R.string.we_cash_up_error), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            beginWithdrawal(pincode);
                        }
                    }).show();
                    Log.e("BEGIN WITHDRAWAL", "We cash up server error");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(getContext(), getString(R.string.transaction_server_error));
                    Log.e("BEGIN WITHDRAWAL", response.message() +" - "+response.body().getErrorCode());
                }
                else{
                    Withdrawal withdrawal = response.body().getWithdrawalData().getWithdrawal();
                    String resCode = response.body().getWithdrawalData()==null?null:String.valueOf(response.body().getWithdrawalData().getWcuResponse().getCode());
                    if (withdrawal!=null){
                        UiUtils.infoDialog(getContext(), getString(R.string.transaction_server_error));
                        Log.e("BEGIN WITHDRAWAL", "RESP NULL");
                    }
                    else if (resCode.startsWith("4") || resCode.startsWith("5")){
                        UiUtils.infoDialog(getContext(), getString(R.string.transaction_server_error));
                        Log.e("BEGIN WITHDRAWAL", response.body().getWithdrawalData().getWcuResponse().getDetails());
                    }
                    else{
                        transactionSuccess(withdrawal, withdrawal.getUser(),"Withdrawal");
                        //save transfert
                        dismiss();
                        getActivity().finish();
                    }
                }
            }
            @Override
            public void onFailure(Call<WithdrawalInterface.BeginWithdrawalResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }

    private void cancelFunding(){
        FundingInterface fundingInterface = NetworkAPI.getClient().create(FundingInterface.class);
        Call<BaseResponse> cancel = fundingInterface.cancel(funding.getId());
        cancel.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                dismiss();
                if (response.body()!=null && !response.body().isError()){
                    Toast.makeText(getContext(), getString(R.string.transaction_cancelled), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                dismiss();
                t.printStackTrace();
            }
        });
    }

    private void cancelWithdrawal(){
        WithdrawalInterface withdrawalInterface = NetworkAPI.getClient().create(WithdrawalInterface.class);
        Call<BaseResponse> cancel = withdrawalInterface.cancel(withdrawal.getId());
        cancel.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                dismiss();
                if (response.body()!=null && !response.body().isError()){
                    //Toast.makeText(getContext(), getString(R.string.transaction_cancelled), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                dismiss();
                t.printStackTrace();
            }
        });
    }

    private void doTransfert(String pinCode){
        TransfertInterface.DoTransfertBody doTransfertBody = new TransfertInterface.DoTransfertBody();
        doTransfertBody.setPinCode(pinCode);
        doTransfertBody.setTransfert(transfert);
        TransfertInterface transfertInterface = NetworkAPI.getClient().create(TransfertInterface.class);
        Call<TransfertInterface.TransfertResponse> doTransfert = transfertInterface.doTransfert(doTransfertBody);
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.processing_transaction));
        progressDialog.show();
        doTransfert.enqueue(new Callback<TransfertInterface.TransfertResponse>() {
            @Override
            public void onResponse(Call<TransfertInterface.TransfertResponse> call, Response<TransfertInterface.TransfertResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                    Log.e("DO TRANDFERT", "Body Null");
                }
                else if (response.body().isError() && response.body().getErrorCode()==TransfertInterface.TRANSFERT_INCORRECT_PINCODE){
                    incorrectPINDialog();
                }
                else if (response.body().isError() && response.body().getErrorCode()==TransfertInterface.TRANSFERT_SUCCESS) {
                    transactionSuccess(null, null, "Money Transfer");
                    Log.e("DO TRANSFERT", "TRANSACTION MADE");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(getContext(), getString(R.string.transaction_server_error));
                    Log.e("DO TRANSFERT", response.message() +" - "+response.body().getErrorCode());
                }
                else{
                    transactionSuccess(response.body().getTransfert(), response.body().getSender(),"Transfert");
                    //save transfert
                    dismiss();
                    getActivity().finish();
                }
            }
            @Override
            public void onFailure(Call<TransfertInterface.TransfertResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }

    private void doPayment(String pinCode){
        PaymentInterface.DoPaymentBody doPaymentBody = new PaymentInterface.DoPaymentBody();
        doPaymentBody.setPinCode(pinCode);
        doPaymentBody.setPayment(payment);
        PaymentInterface paymentInterface = NetworkAPI.getClient().create(PaymentInterface.class);
        Call<PaymentInterface.PaymentResponse> doPayment = paymentInterface.doPayment(doPaymentBody);
        final ProgressDialog progressDialog = UiUtils.loadingDialog(getContext(), getString(R.string.processing_transaction));
        progressDialog.show();
        doPayment.enqueue(new Callback<PaymentInterface.PaymentResponse>() {
            @Override
            public void onResponse(Call<PaymentInterface.PaymentResponse> call, Response<PaymentInterface.PaymentResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                    Log.e("DO PAYMENT", "Body Null");
                }
                else if (response.body().isError() && response.body().getErrorCode()==PaymentInterface.PAYMENT_INCORRECT_PINCODE){
                    incorrectPINDialog();
                }
                else if (response.body().isError() && response.body().getErrorCode()==PaymentInterface.PAYMENT_SUCCESS) {
                    transactionSuccess(null, null, "Payment");
                    Log.e("DO PAYMENT", "TRANSACTION MADE");
                }
                else if (response.body().isError()){
                    UiUtils.infoDialog(getContext(), getString(R.string.transaction_server_error));
                    Log.e("DO PAYMENT", response.message() +" - "+response.body().getErrorCode());
                }
                else{
                    //TODO: save payment
                    transactionSuccess(response.body().getPayment(), response.body().getCustomer(), "Payment");
                    dismiss();
                    getActivity().finish();
                }
            }
            @Override
            public void onFailure(Call<PaymentInterface.PaymentResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                UiUtils.infoDialog(getContext(), getString(R.string.connection_error));
                t.printStackTrace();
            }
        });
    }

    private void cancelTransfert(){
        TransfertInterface transfertInterface = NetworkAPI.getClient().create(TransfertInterface.class);
        Call<BaseResponse> cancelTransfert = transfertInterface.cancelTransfert(transfert.getId());
        cancelTransfert.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                dismiss();
                if (response.body()!=null && !response.body().isError()){
                    Toast.makeText(getContext(), getString(R.string.transaction_cancelled), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                dismiss();
                t.printStackTrace();
            }
        });
    }

    private void cancelPayment(){
        PaymentInterface paymentInterface = NetworkAPI.getClient().create(PaymentInterface.class);
        Call<BaseResponse> cancelPayment = paymentInterface.cancelPayment(payment.getId());
        cancelPayment.enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                dismiss();
                if (response.body()!=null && !response.body().isError()){
                    Toast.makeText(getContext(), getString(R.string.transaction_cancelled), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                dismiss();
                t.printStackTrace();
            }
        });
    }
}
