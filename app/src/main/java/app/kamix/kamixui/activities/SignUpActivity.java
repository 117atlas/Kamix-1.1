package app.kamix.kamixui.activities;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import app.kamix.R;
import app.kamix.kamixui.adapters.UnSwipeableViewPager;
import app.kamix.kamixui.fragments.SignUpConfirmationFragment;
import app.kamix.kamixui.fragments.SignUpFinalFragment;
import app.kamix.kamixui.fragments.SignUpFragment;
import app.kamix.models.User;

public class SignUpActivity extends AppCompatActivity {

    private UnSwipeableViewPager viewPager;

    private SignUpFragment signUpFragment;
    private SignUpConfirmationFragment signUpConfirmationFragment;
    private SignUpFinalFragment signUpFinalFragment;

    private String email = "";
    private String mobile = "";
    private int flag = 0;
    private String firstname = "";
    private String lastname = "";

    private User user;

    public void handlePinCode(String pinCodeReceived){
        //if (signUpConfirmationFragment!=null) signUpConfirmationFragment.handlePinCodeReceived(pinCodeReceived);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        viewPager = findViewById(R.id.view_pager);
        setupViewPager();
    }

    private void setupViewPager(){
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        signUpFragment = new SignUpFragment();
        signUpConfirmationFragment = new SignUpConfirmationFragment();
        signUpFinalFragment = new SignUpFinalFragment();
        viewPagerAdapter.addFragment(signUpFragment, "STEP 1");
        viewPagerAdapter.addFragment(signUpConfirmationFragment, "STEP 2");
        viewPagerAdapter.addFragment(signUpFinalFragment, "STEP 3");
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        viewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();
        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
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

    public void swipe(int dest){
        if (dest<3) {
            if (dest==1){
                if (signUpConfirmationFragment!=null) signUpConfirmationFragment.setUserInfos(mobile, email, flag);
            }
            viewPager.setCurrentItem(dest);
        }
        else {
            Intent intent = new Intent(SignUpActivity.this, SplashActivity.class);
            intent.putExtra(User.class.getSimpleName(), user);
            startActivity(intent);
            finish();
        }
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
