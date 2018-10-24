package app.kamix.kamixui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.utils.FormatUtils;
import app.kamix.models.Funding;
import app.kamix.models.Payment;
import app.kamix.models.Transaction;
import app.kamix.models.Transfert;
import app.kamix.models.User;
import app.kamix.models.Withdrawal;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private Context context;
    private List<? extends Transaction> transactions;
    private User user;

    private User getUser(){
        if (user==null) user = KamixApp.getUser(context);
        return user;
    }

    public HistoryAdapter(Context context){
        this.context = context;
        getUser();
    }

    public void setTransactions(List<? extends Transaction> transactions){
        this.transactions = transactions;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new HistoryViewHolder(inflater.inflate(R.layout.history_transaction_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return transactions==null?0:transactions.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder{
        private ImageView transactionImage;
        private TextView receiver, date, message, amount;
        private int currentPosition = 0;

        public HistoryViewHolder(View itemView) {
            super(itemView);
            transactionImage = itemView.findViewById(R.id.transaction_image);
            receiver = itemView.findViewById(R.id.receiver);
            date = itemView.findViewById(R.id.date);
            message = itemView.findViewById(R.id.message);
            amount = itemView.findViewById(R.id.amount);
        }

        public void bind(int position){
            currentPosition = position;
            Transaction transaction = transactions.get(position);
            if (transaction instanceof Transfert){
                Transfert transfert = (Transfert) transaction;

                transactionImage.setImageResource(R.drawable.transfert_green);
                receiver.setText(transfert.getReceiverMobile()+" - "+transfert.getReceiver().name());
                date.setText(FormatUtils.formatDate(transfert.getDate()));
                message.setText(transfert.getMessage());

                if (transfert.getReceiver().getId().equals(user.getId())){
                    amount.setText("+ "+FormatUtils.formatAmount(transfert.getAmount()));
                    amount.setTextColor(Color.GREEN);
                }
                else{
                    amount.setText("- "+FormatUtils.formatAmount(transfert.getAmount()));
                    amount.setTextColor(Color.RED);
                }
            }
            else if (transaction instanceof Payment){
                Payment payment = (Payment) transaction;

                transactionImage.setImageResource(R.drawable.payment_green);
                receiver.setText(payment.getMerchant().name());
                date.setText(FormatUtils.formatDate(payment.getDate()));
                message.setText(payment.getMessage());

                if (payment.getMerchant().getId().equals(user.getId())){
                    amount.setText("+ "+FormatUtils.formatAmount(payment.getAmount()));
                    amount.setTextColor(Color.GREEN);
                }
                else{
                    amount.setText("- "+FormatUtils.formatAmount(payment.getAmount()));
                    amount.setTextColor(Color.RED);
                }
            }
            else if (transaction instanceof Funding){
                Funding funding = (Funding) transaction;

                transactionImage.setImageResource(R.drawable.funding_green);
                receiver.setText(funding.getUser().name()+" - "+funding.getUser().name());
                date.setText(FormatUtils.formatDate(funding.getDate()));
                message.setText(funding.getMessage());

                amount.setText("+ "+FormatUtils.formatAmount(funding.getAmount()));
                amount.setTextColor(Color.GREEN);
            }
            else{
                Withdrawal withdrawal = (Withdrawal) transaction;

                transactionImage.setImageResource(R.drawable.withdraw_green);
                receiver.setText(withdrawal.getUser().name()+" - "+withdrawal.getUser().name());
                date.setText(FormatUtils.formatDate(withdrawal.getDate()));
                message.setText(withdrawal.getMessage());

                amount.setText("- "+FormatUtils.formatAmount(withdrawal.getAmount()));
                amount.setTextColor(Color.RED);
            }

        }
    }
}
