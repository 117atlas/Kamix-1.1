package app.kamix.kamixui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.fragments.HistoryFragment;
import app.kamix.kamixui.utils.UiUtils;
import app.kamix.models.Funding;
import app.kamix.models.Payment;
import app.kamix.models.Transaction;
import app.kamix.models.Transfert;
import app.kamix.models.User;
import app.kamix.models.Withdrawal;
import app.kamix.network.HistoryInterface;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HistoryActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private TabLayout tabs;
    private ViewPager viewPager;

    private HistoryFragment transfertsHistory = new HistoryFragment();
    private HistoryFragment paymentsHistory = new HistoryFragment();
    private HistoryFragment fundingsHistory = new HistoryFragment();
    private HistoryFragment withdrawalsHistory = new HistoryFragment();

    private int currentPosition = 0;

    private List<Transfert> transferts;
    private List<Payment> payments;
    private List<Funding> fundings;
    private List<Withdrawal> withdrawals;

    public List<Transfert> getTransferts() {
        return transferts;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public List<Funding> getFundings() {
        return fundings;
    }

    public List<Withdrawal> getWithdrawals() {
        return withdrawals;
    }

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        user = KamixApp.getUser(this);
        if (user==null) onBackPressed();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        viewPager = findViewById(R.id.view_pager);
        setupViewPager(viewPager);

        tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                bind(position);
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        history();
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(transfertsHistory, getString(R.string.transferts_tab));
        adapter.addFragment(paymentsHistory, getString(R.string.payments_tab));
        adapter.addFragment(fundingsHistory, getString(R.string.fundings_tab));
        adapter.addFragment(withdrawalsHistory, getString(R.string.withdrawals_tab));
        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void bind(int position){
        final ArrayList<List<? extends Transaction>> indics = new ArrayList<>();
        indics.add(transferts); indics.add(payments); indics.add(fundings); indics.add(withdrawals);
        final ArrayList<HistoryFragment> fragments = new ArrayList<>();
        fragments.add(transfertsHistory); fragments.add(paymentsHistory); fragments.add(fundingsHistory); fragments.add(withdrawalsHistory);

        //if (position-1>=0) fragments.get(position-1).build(indics.get(position-1));
        fragments.get(position).build(indics.get(position));
        //if (position+1<4) fragments.get(position+1).build(indics.get(position+1));
    }

    private void history(){
        final ProgressDialog progressDialog = UiUtils.loadingDialog(this, getString(R.string.please_wait));
        progressDialog.show();
        HistoryInterface historyInterface = NetworkAPI.getClient().create(HistoryInterface.class);
        final Call<HistoryInterface.HistoryResponse> history = historyInterface.history(user.getId());
        history.enqueue(new Callback<HistoryInterface.HistoryResponse>() {
            @Override
            public void onResponse(Call<HistoryInterface.HistoryResponse> call, Response<HistoryInterface.HistoryResponse> response) {
                UiUtils.dismissLoadingDialog(progressDialog);
                if (response.body()==null){
                    UiUtils.questionDialog(HistoryActivity.this, getString(R.string.connection_error) + getString(R.string.would_like_retry), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            history();
                        }
                    }).show();
                    Toast.makeText(HistoryActivity.this, "Null Body", Toast.LENGTH_LONG).show();
                }
                else if (response.body().isError()){
                    UiUtils.questionDialog(HistoryActivity.this, getMessageByErrorCode(response.body().getErrorCode()) + getString(R.string.would_like_retry), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            history();
                        }
                    }).show();
                    Log.e("REGISTER", response.body().getErrorCode() + " error");
                }
                else{
                    HistoryInterface.HistoryResponse response1 = response.body();
                    transferts = response.body().getTransferts();
                    payments = response.body().getPayments();
                    fundings = response.body().getFundings();
                    withdrawals = response.body().getWithdrawals();
                    bind(currentPosition);
                }
            }
            @Override
            public void onFailure(Call<HistoryInterface.HistoryResponse> call, Throwable t) {
                UiUtils.dismissLoadingDialog(progressDialog);
                t.printStackTrace();
                UiUtils.questionDialog(HistoryActivity.this, getString(R.string.connection_error)+getString(R.string.would_like_retry), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        history();
                    }
                }).show();
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
