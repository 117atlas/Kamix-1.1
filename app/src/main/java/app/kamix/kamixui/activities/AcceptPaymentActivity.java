package app.kamix.kamixui.activities;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import net.glxn.qrgen.android.QRCode;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.models.User;

public class AcceptPaymentActivity extends AppCompatActivity {
    private ImageView qrCodeContainer;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accept_payment);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        qrCodeContainer = findViewById(R.id.qrcode_container);

        User me = KamixApp.getUser(this);
        if (me.noName()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage(getString(R.string.complete_profile_name))
                    .setNeutralButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            onBackPressed();
                        }
                    })
                    .setCancelable(false);
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
        else{
            String qrCodeText = me.getId()+"|"+me.name();
            Bitmap bitmap = QRCode.from(qrCodeText).withSize(1000, 1000).bitmap();
            qrCodeContainer.setImageBitmap(bitmap);
        }

    }
}
