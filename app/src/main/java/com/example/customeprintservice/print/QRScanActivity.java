package com.example.customeprintservice.print;

import androidx.appcompat.app.AppCompatActivity;
import com.example.customeprintservice.R;
import com.example.customeprintservice.jipp.QRCodeScanActivity;
import com.example.customeprintservice.prefs.LoginPrefs;
import com.example.customeprintservice.signin.SignInCompany;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

public class QRScanActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_q_r_scan);

        Intent intent = getIntent();
        String action = intent.getAction();

        Uri intent2 = intent.getData();
        if (intent2 != null) {
            if (intent2.getScheme().equals("vasion-print")) {
                if (LoginPrefs.Companion.getOCTAToken(this) == null) {
                    @SuppressLint("WrongConstant") SharedPreferences prefs = getSharedPreferences("MySharedPref", Context.MODE_APPEND);
                    String IsLdap = prefs.getString("IsLdap", "");
                    if (!IsLdap.equals("LDAP")) {
                        Intent intent1 = new Intent(getApplicationContext(), SignInCompany.class);
                        startActivity(intent1);
                    }
                } else {

                    Log.d("path", intent2.getPath());
                    QRCodeScanActivity qrCodeScanActivity = new QRCodeScanActivity();
                    String printerId = qrCodeScanActivity.getdigit(intent2.getPath().toString());
                    Log.d("printerId:", printerId);


                    Intent intent1 = new Intent(getApplicationContext(), QRCodeScanActivity.class);
                    intent1.putExtra("startqrcodescan", "startqrcodescan");
                    intent1.putExtra("printerId", printerId);
                    startActivity(intent1);

                }

            }
        }
    }
}