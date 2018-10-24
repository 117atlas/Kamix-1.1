package app.kamix.kamixui.fragments;


import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import app.kamix.R;
import app.kamix.kamixui.activities.HistoryActivity;
import app.kamix.kamixui.adapters.HistoryBlockAdapter;
import app.kamix.kamixui.utils.DateUtils;
import app.kamix.models.Funding;
import app.kamix.models.Payment;
import app.kamix.models.Transaction;
import app.kamix.models.Transfert;
import app.kamix.models.Withdrawal;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment {

    private RecyclerView blocks;
    private String what;
    public static final String WHAT_FUNDINGS = "FUNDINGS";
    public static final String WHAT_WITHDRAWALS = "WITHDRAWALS";
    public static final String WHAT_TRANSFERTS = "TRANSFERTS";
    public static final String WHAT_PAYMENTS = "PAYMENTS";
    /*private ArrayList<Funding> fundings = new ArrayList<>();
    private ArrayList<Withdrawal> withdrawals = new ArrayList<>();
    private ArrayList<Transfert> transferts = new ArrayList<>();
    private ArrayList<Payment> payments = new ArrayList<>();*/

    private HistoryBlockAdapter adapter;

    private List<Pair<String, List<? extends Transaction>>> dblocks;

    public HistoryFragment() {
        // Required empty public constructor
    }

    public static HistoryFragment newInstance(String what){
        HistoryFragment historyFragment = new HistoryFragment();
        Bundle args = new Bundle();
        args.putString("WHAT", what);
        historyFragment.setArguments(args);
        return historyFragment;
    }

    public void build(List<? extends Transaction> transactions){
        dblocks = new ArrayList<>();
        for (Transaction transaction: transactions) transaction.setTdate();
        if (transactions!=null && transactions.size()>0){
            ArrayList<Pair<String, Long>> tblocks = DateUtils.blocks(transactions.get(transactions.size()-1).getTdate(), transactions.get(0).getTdate(), getContext());
            for (int i = 0; i<tblocks.size(); i++){
                Pair<String, Long> tblock = tblocks.get(i);
                long bmax = i==0?transactions.get(transactions.size()-1).getTdate():tblocks.get(i-1).second;
                long bmin = tblock.second;
                ArrayList<Transaction> tTransactions = new ArrayList<>();
                for (Transaction transaction: transactions) if (bmin<=transaction.getTdate() && transaction.getTdate()<=bmax) tTransactions.add(transaction);
                if (tTransactions.size()>0) dblocks.add(new Pair<String, List<? extends Transaction>>(tblock.first, tTransactions));
            }
        }
        /*adapter = new HistoryBlockAdapter(getContext());
        blocks.setLayoutManager(new LinearLayoutManager(getContext()));
        blocks.setAdapter(adapter);*/
        blocks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter.setList(dblocks);

        //fundings = (ArrayList<Funding>) transactions;
        /*if (fundings!=null && fundings.size()>0){
            ArrayList<Pair<String, Long>> tblocks = DateUtils.blocks(fundings.get(0).getDate(), fundings.get(fundings.size()-1).getDate(), getContext());
            for (int i = 0; i<tblocks.size(); i++){
                Pair<String, Long> tblock = tblocks.get(i);
                long bmax = i==0?fundings.get(0).getDate():tblocks.get(i-1).second;
                long bmin = tblock.second;
                ArrayList<Funding> tFundings = new ArrayList<>();
                for (Funding funding: fundings) if (bmin<=funding.getDate() && funding.getDate()<=bmax) tFundings.add(funding);
                if (tFundings.size()>0) dblocks.add(new Pair<String, ArrayList<? extends Transaction>>(tblock.first, tFundings));
            }
        }*/

    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        blocks = view.findViewById(R.id.history_blocks);
        blocks.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new HistoryBlockAdapter(getContext());
        blocks.setAdapter(adapter);
        /*switch (what){
            case WHAT_TRANSFERTS: build(((HistoryActivity)getActivity()).getTransferts()); break;
            case WHAT_PAYMENTS: build(((HistoryActivity)getActivity()).getPayments()); break;
            case WHAT_FUNDINGS: build(((HistoryActivity)getActivity()).getFundings()); break;
            case WHAT_WITHDRAWALS: build(((HistoryActivity)getActivity()).getWithdrawals()); break;
        }*/

    }
}
