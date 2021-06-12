package com.example.customeprintservice.signin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.customeprintservice.R;
import com.example.customeprintservice.prefs.LoginPrefs;
import com.example.customeprintservice.print.BottomNavigationActivity;
import com.example.customeprintservice.print.BottomNavigationActivityForServerPrint;
import com.example.customeprintservice.utils.DataDogLogger;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GoogleLoginActivity extends AppCompatActivity {
    GoogleSignInClient googleApiClient;
    private static final int RC_GET_TOKEN = 9002;
    Button btnSignInWithOkta;
    Logger logger = LoggerFactory.getLogger(GoogleLoginActivity.class);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);


        btnSignInWithOkta =findViewById(R.id.btnSignInWithOkta);
        btnSignInWithOkta.setBackgroundResource(R.drawable.button_sign_in_google);
        btnSignInWithOkta.setText("Google");
        Drawable drawable = getResources().getDrawable(R.mipmap.icon_google);
        btnSignInWithOkta.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        btnSignInWithOkta.setGravity(Gravity.CENTER);

        @SuppressLint("WrongConstant") SharedPreferences sh = getSharedPreferences("MySharedPref", Context.MODE_APPEND);
        String clientIdForGoogleLogin = sh.getString("clientIdForGoogleLogin", "");
        Log.d("clientIdForGoogleLogin:",clientIdForGoogleLogin);
        logger.info("Devnco_Android clientIdForGoogleLogin:"+clientIdForGoogleLogin);
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("179384819622-j6vellprfrbso2soigrjbulbkqob1n87.apps.googleusercontent.com")
                .requestEmail()
                .build();
        googleApiClient = GoogleSignIn.getClient(this,googleSignInOptions);

        getIdToken();
        btnSignInWithOkta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getIdToken();
            }
        });
    }


    private void getIdToken(){
        Intent signInIntent = googleApiClient.getSignInIntent();
        startActivityForResult(signInIntent,RC_GET_TOKEN);
    }

    @Override
    public  void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GET_TOKEN) {

            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            }


    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask){
        try{
            GoogleSignInAccount account =completedTask.getResult(ApiException.class);
            String idToken=account.getIdToken();
            String username=account.getEmail();
            Log.d("idToken",idToken);
            //Toast.makeText(getApplicationContext(), idToken, Toast.LENGTH_SHORT).show();
            logger.info("Devnco_Android Papertrail idToken log management demo"+idToken);
            SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
            SharedPreferences.Editor myEdit= sharedPreferences.edit();
            myEdit.putString("idToken",idToken);
            myEdit.commit();
            LoginPrefs.Companion.saveOctaToken(this, idToken.toString());


            Intent intent = new Intent(GoogleLoginActivity.this, BottomNavigationActivity.class);
            startActivity(intent);
            signOut();
        } catch (ApiException e) {
            e.printStackTrace();
            //Toast.makeText(getApplicationContext(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            DataDogLogger.getLogger().e("Devnco_Android printer:"+ "Exception in handleSignInResult:"+e.getLocalizedMessage());
        }
    }

    private void signOut() {
        googleApiClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
            }
        });
    }
}