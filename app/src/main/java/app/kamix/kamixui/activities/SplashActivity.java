package app.kamix.kamixui.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import app.kamix.R;
import app.kamix.localstorage.DAO;
import app.kamix.models.User;

public class SplashActivity extends AppCompatActivity {

    public Runnable run;
    public int i = 0;
    public Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        User user = (User) getIntent().getSerializableExtra(User.class.getSimpleName());
        if (user==null) try {
            throw new Exception("User null");
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
        DAO dao = new DAO(this);
        dao.storeUser(user);

        run = new Runnable() {
            @Override
            public void run() {
                if (i == 80) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                } else {
                    i = i + 10;
                    mHandler.postDelayed(run, 200);
                }
            }
        };
        mHandler = new Handler();
        mHandler.postDelayed(run, 200);
    }
}
