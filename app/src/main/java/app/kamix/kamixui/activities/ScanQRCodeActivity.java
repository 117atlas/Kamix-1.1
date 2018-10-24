package app.kamix.kamixui.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import app.kamix.R;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class ScanQRCodeActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {

    private ZBarScannerView mScannerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qrcode);
        mScannerView = (ZBarScannerView) findViewById(R.id.zbQr);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(me.dm7.barcodescanner.zbar.Result result) {
        // Do something with the result here
        Log.v("kkkk", result.getContents()); // Prints scan results
        Log.v("uuuu", result.getBarcodeFormat().getId() +" - "+ result.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)

        Intent intent = new Intent();
        intent.putExtra("QRCODERESULT", result.getContents());
        setResult(RESULT_OK, intent);
        onBackPressed();
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
    }
}
