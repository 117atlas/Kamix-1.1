package app.kamix.kamixui.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import app.kamix.R;

public class TransactionSucessActivity extends AppCompatActivity {

    private String textContain = "";
    private TextView tvText, transactionConfirmMessage, hbs;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_sucess);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        textContain = getIntent().getStringExtra("data");
        tvText = (TextView) findViewById(R.id.tvText);
        transactionConfirmMessage = findViewById(R.id.transaction_confirm_message);
        hbs = findViewById(R.id.hbs);
        tvText.setText("Your " + textContain + " has");

        switch (textContain){
            case "Transfert": {
                transactionConfirmMessage.setVisibility(View.GONE);
                hbs.setText(getString(R.string.hbs_executed));
            } break;
            case "Payment": {
                transactionConfirmMessage.setVisibility(View.GONE);
                hbs.setText(getString(R.string.hbs_executed));
            } break;
            case "Funding":{
                transactionConfirmMessage.setVisibility(View.VISIBLE);
                hbs.setText(getString(R.string.hbs_initied));
            } break;
            case "Withdrawal":{
                transactionConfirmMessage.setVisibility(View.VISIBLE);
                hbs.setText(getString(R.string.hbs_initied));
            } break;
            default:{
                transactionConfirmMessage.setVisibility(View.VISIBLE);
                hbs.setText(getString(R.string.hbs_submitted));
            } break;
        }
    }
}
