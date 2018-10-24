package app.kamix.kamixui.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import app.kamix.R;
import app.kamix.kamixui.activities.AcceptPaymentActivity;
import app.kamix.kamixui.activities.PaymentActivity;

/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentChoiceFragment extends BottomSheetDialogFragment {

    private AppCompatButton accept, proceed;

    public PaymentChoiceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_payment_choice, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        accept = view.findViewById(R.id.accept_payment);
        proceed = view.findViewById(R.id.proceed_payment);
        accept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AcceptPaymentActivity.class));
                dismiss();
            }
        });
        proceed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), PaymentActivity.class));
                dismiss();
            }
        });
    }
}
