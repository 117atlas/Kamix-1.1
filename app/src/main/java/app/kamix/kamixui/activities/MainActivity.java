package app.kamix.kamixui.activities;

import android.Manifest;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import app.kamix.R;
import app.kamix.app.KamixApp;
import app.kamix.kamixui.fragments.BalancePINFragment;
import app.kamix.kamixui.fragments.PaymentChoiceFragment;
import app.kamix.localstorage.DAO;
import app.kamix.models.Contact;
import app.kamix.models.User;
import app.kamix.network.NetworkAPI;
import app.kamix.network.UserInterface;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int READ_CONTACT_PERM_CODE = 444;
    private User user;
    public TextView payment, transfert, funding, withdrawal, userInfos, history, share, balance;
    //private LinearLayout root;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        user = KamixApp.getUser(this);
        if (user==null){
            startActivity(new Intent(this, SignUpActivity.class));
            finish();
        }

        payment = (TextView) findViewById(R.id.payment);
        transfert = (TextView) findViewById(R.id.transfert);
        funding = (TextView) findViewById(R.id.funding);
        withdrawal= (TextView) findViewById(R.id.withdrawal);
        userInfos= (TextView) findViewById(R.id.userinfos);
        history= (TextView) findViewById(R.id.history);
        share= (TextView) findViewById(R.id.share);
        balance = findViewById(R.id.balance);

        payment.setOnClickListener(this);
        transfert.setOnClickListener(this);
        funding.setOnClickListener(this);
        withdrawal.setOnClickListener(this);
        userInfos.setOnClickListener(this);
        history.setOnClickListener(this);
        share.setOnClickListener(this);
        balance.setOnClickListener(this);

        if (user!=null) updateContacts();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.balance:{
                BalancePINFragment balancePINFragment = new BalancePINFragment();
                balancePINFragment.show(getSupportFragmentManager(), BalancePINFragment.class.getSimpleName());
            } break;
            case R.id.payment:{
                //startActivity(new Intent(MainActivity.this, PaymentActivity.class)); // call payment transaction screen
                PaymentChoiceFragment paymentChoiceFragment = new PaymentChoiceFragment();
                paymentChoiceFragment.show(getSupportFragmentManager(), PaymentChoiceFragment.class.getSimpleName());
            } break;
            case R.id.transfert:
                startActivity(new Intent(MainActivity.this, TransfertActivity.class)); // call money transfer screen
                break;
            case R.id.funding:
                startActivity(new Intent(MainActivity.this, FundingActivity.class)); // call funding screen
                break;
            case R.id.withdrawal:
                startActivity(new Intent(MainActivity.this, WithdrawalActivity.class)); // call withdraw screen
                break;
            case R.id.userinfos:
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class)); // call user profile screen
                break;
            case R.id.history:
                startActivity(new Intent(MainActivity.this, HistoryActivity.class)); // call transaction screen
                break;
            case R.id.share:
                //startActivity(new Intent(MainActivity.this, MainActivity.class)); // send invitation screen
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share));
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                break;
        }
    }

    private void updateContacts(){
        if (readContactsPermission()){
            new AsyncTask<Void, Void, List<Contact>>(){
                @Override
                protected List<Contact> doInBackground(Void... voids) {
                    return readContacts();
                }
                @Override
                protected void onPostExecute(List<Contact> contacts) {
                    super.onPostExecute(contacts);
                    UserInterface userInterface = NetworkAPI.getClient().create(UserInterface.class);
                    UserInterface.UpdateContactBody updateContactBody = new UserInterface.UpdateContactBody();
                    updateContactBody.setContacts(contacts);
                    retrofit2.Call<UserInterface.UserResponse> updateContacts = userInterface.updateContacts(updateContactBody, user.getId());
                    updateContacts.enqueue(new Callback<UserInterface.UserResponse>() {
                        @Override
                        public void onResponse(retrofit2.Call<UserInterface.UserResponse> call, Response<UserInterface.UserResponse> response) {
                            if (response.body()!=null && !response.body().isError()){
                                DAO dao = new DAO(MainActivity.this);
                                if (response.body().getUser()!=null) dao.storeUser(response.body().getUser());
                            }
                        }
                        @Override
                        public void onFailure(retrofit2.Call<UserInterface.UserResponse> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            }.execute();
        }

    }

    private List<Contact> readContacts(){
        List<Contact> phoneContacts= new ArrayList<>();
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            int i = 1;
            while (cur != null && cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        String phoneNo = pCur.getString(pCur.getColumnIndex(
                                ContactsContract.CommonDataKinds.Phone.NUMBER));
                        //Log.i("Phone Contacts", "Name: " + name);
                        //Log.i("Phone Contacts", "Phone Number: " + phoneNo);
                        if (phoneNo!=null && !phoneNo.isEmpty()) {
                            phoneNo = phoneNo.replace(" ", "").replace("-", "").replace("+237", "")
                                    .replace("00237", "");
                            if (phoneNo.length()==8) phoneNo = "6"+phoneNo;
                            Contact contact = new Contact(name, "+237"+phoneNo);
                            if (!phoneContacts.contains(contact)) phoneContacts.add(contact);
                        }
                    }
                    pCur.close();
                }
            }
        }
        if(cur!=null){
            cur.close();
        }
        return phoneContacts;
    }

    private boolean readContactsPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.READ_CONTACTS)!= PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)){
                    // Show an alert dialog
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("Read external storage permission is required.");
                    builder.setTitle("Please grant permission");
                    builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(
                                    MainActivity.this,
                                    new String[]{Manifest.permission.READ_CONTACTS},
                                    READ_CONTACT_PERM_CODE
                            );
                        }
                    });
                    builder.setNeutralButton("Cancel",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    // Request permission
                    ActivityCompat.requestPermissions(
                            this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            READ_CONTACT_PERM_CODE
                    );
                }
                return false;
            }else {
                // Permission already granted
                return true;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 117: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    updateContacts();
                } else {
                    Toast.makeText(this, getString(R.string.read_contacts_permissions_denied), Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }
}
