package app.kamix.kamixui.fragments;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import app.kamix.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class BalanceFragment extends DialogFragment {

    private TextView balance;
    private AppCompatButton close;

    private double dbalance;

    public BalanceFragment() {
        // Required empty public constructor
    }

    public static BalanceFragment newInstance(double dbalance){
        BalanceFragment balanceFragment = new BalanceFragment();
        Bundle args = new Bundle();
        args.putDouble("BALANCE", dbalance);
        balanceFragment.setArguments(args);
        return balanceFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        return inflater.inflate(R.layout.fragment_balance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        balance = view.findViewById(R.id.balance);
        close = view.findViewById(R.id.close);
        dbalance = getArguments().getDouble("BALANCE");
        balance.setText(formatBalance());
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
    }

    private String formatBalance(){
        /*String formattedBalance = "";
        String[] parts = TextUtils.split(String.valueOf(dbalance), ".");
        String ent = parts[0];
        for (int i=ent.length()-1; i>=0; i--){
            int j = ent.length()-1-i;
            if (j%3==2) formattedBalance = formattedBalance + ent.charAt(i) + " ";
            else formattedBalance = formattedBalance + ent.charAt(i);

        }*/
        double epsilon = 0.004; // 4 tenths of a cent
        if (Math.abs(Math.round(dbalance) - dbalance) < epsilon) {
            return String.format("%10.0f", dbalance) + " XAF"; // sdb
        } else {
            return String.format("%10.2f", dbalance) + " XAF"; // dj_segfault
        }
    }
}
