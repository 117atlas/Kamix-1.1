package app.kamix.kamixui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.util.Pair;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import app.kamix.R;
import app.kamix.models.Transaction;

public class HistoryBlockAdapter extends RecyclerView.Adapter<HistoryBlockAdapter.HistoryBlockViewHolder> {

    private Context context;
    private List<Pair<String, List<? extends Transaction>>> list;

    public HistoryBlockAdapter(Context context){
        this.context = context;
    }

    public void setList(List<Pair<String, List<? extends Transaction>>> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryBlockViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new HistoryBlockViewHolder(inflater.inflate(R.layout.history_block, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryBlockViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        int count = list==null?0:list.size();
        return count;
    }

    class HistoryBlockViewHolder extends RecyclerView.ViewHolder{
        private TextView blockTitle;
        private RecyclerView history;

        public HistoryBlockViewHolder(View itemView) {
            super(itemView);
            blockTitle = itemView.findViewById(R.id.block_title);
            history = itemView.findViewById(R.id.transactions_list);
        }

        public void bind(int position){
            Pair<String, List<? extends Transaction>> blockDatas = list.get(position);
            blockTitle.setText(blockDatas.first);

            HistoryAdapter historyAdapter = new HistoryAdapter(context);
            LinearLayoutManager manager = new LinearLayoutManager(context);
            history.setAdapter(historyAdapter);
            history.setLayoutManager(manager);
            historyAdapter.setTransactions(blockDatas.second);
        }
    }
}
