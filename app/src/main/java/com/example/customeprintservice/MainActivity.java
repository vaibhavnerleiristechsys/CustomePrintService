package com.example.customeprintservice;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import com.example.customeprintservice.jipp.FileUtils;
import com.example.customeprintservice.jipp.PrinterList;
import com.example.customeprintservice.jipp.QRCodeScanActivity;
import com.example.customeprintservice.model.TokenResponse;
import com.example.customeprintservice.prefs.LoginPrefs;
import com.example.customeprintservice.print.BottomNavigationActivity;
import com.example.customeprintservice.print.PrintPreview;
import com.example.customeprintservice.print.PrintersFragment;
import com.example.customeprintservice.rest.ApiService;
import com.example.customeprintservice.rest.RetrofitClient;
import com.example.customeprintservice.room.SelectedFile;
import com.example.customeprintservice.signin.SignInCompany;
import com.example.customeprintservice.utils.GoogleAPI;
import com.example.customeprintservice.utils.PermissionHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.navigation.NavigationView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import android.view.Menu;
import android.widget.Button;
import android.widget.Toast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final float END_SCALE = 0.85f;
    private AppBarConfiguration mAppBarConfiguration;
    private NavController navController;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavView;
    private CoordinatorLayout contentView;
    private Button signout,print;
    private FloatingActionButton fab;
    public static ArrayList<SelectedFile> list = new ArrayList<SelectedFile>();
    public ArrayList<SelectedFile> localDocumentSharedPreflist = new ArrayList<SelectedFile>();
    public PrintService app;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        setContentView(R.layout.activity_main2);
        signout = findViewById(R.id.signout);
        print=findViewById(R.id.print);
        fab = findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);
       // BottomNavigationActivity bottomNavigationActivity1 = new BottomNavigationActivity();
     //   PrintersFragment printersFragment1 = new PrintersFragment();
      //  printersFragment1.getPrinterList(this, bottomNavigationActivity1.decodeJWT(this));

        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver1,
                new IntentFilter("om.example.PRINT_RESPONSE"));


        Intent intent = getIntent();
        String action = intent.getAction();
        list.clear();

        if (action != null) {
            switch (action) {
                case Intent.ACTION_SEND_MULTIPLE:
                    ArrayList<Uri> imageUris = new ArrayList<Uri>();
                    for (int i = 0; i < intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM).size(); i++) {

                        imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    }
                    for (int j = 0; j < imageUris.size(); j++) {
                        Uri imageUri = imageUris.get(j);

                        if (imageUri != null) {
                            String realPath = FileUtils.getPath(this, imageUri);
                            SelectedFile selectedFile = new SelectedFile();
                            File file = new File(realPath);
                            selectedFile.setFileName(file.getName());
                            selectedFile.setFilePath(realPath);
                            selectedFile.setFromApi(false);
                            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                            Date date = new Date();
                            String strDate = dateFormat.format(date);
                            selectedFile.setFileSelectedDate(strDate);
                            list.add(selectedFile);
                        }
                    }

                    SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(this);
                    Gson gson1 = new Gson();
                    String json2 = prefs1.getString("localdocumentlist", null);
                    Type type1 = new TypeToken<ArrayList<SelectedFile>>() {
                    }.getType();
                    localDocumentSharedPreflist = gson1.fromJson(json2, type1);
                    if (localDocumentSharedPreflist != null) {
                        list.addAll(localDocumentSharedPreflist);
                    }
                    SharedPreferences.Editor editor1 = prefs1.edit();
                    String convertedJson = gson1.toJson(list);
                    editor1.putString("localdocumentlist", convertedJson);
                    editor1.apply();
                    Toast.makeText(this, "file added", Toast.LENGTH_LONG).show();

                    if (LoginPrefs.Companion.getOCTAToken(this) == null) {
                        Intent intent1 = new Intent(getApplicationContext(), SignInCompany.class);
                        startActivity(intent1);
                    }


                case Intent.ACTION_SEND:
                    Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    if (imageUri != null) {
                        String realPath = FileUtils.getPath(this, imageUri);
                        SelectedFile selectedFile = new SelectedFile();
                        File file = new File(realPath);
                        selectedFile.setFileName(file.getName());
                        selectedFile.setFilePath(realPath);
                        selectedFile.setFromApi(false);
                        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                        Date date = new Date();
                        String strDate = dateFormat.format(date);
                        selectedFile.setFileSelectedDate(strDate);
                                Intent intent1 = new Intent(getApplicationContext(), PrintPreview.class);
                                Bundle bundle = new Bundle();
                                bundle.putString("filePath", realPath);
                                intent1.putExtras(bundle);
                                startActivity(intent1);

                    }
                    if (LoginPrefs.Companion.getOCTAToken(this) == null) {
                        Intent intent1 = new Intent(getApplicationContext(), SignInCompany.class);
                        startActivity(intent1);
                    }

            }

        }
    Uri intent2 = intent.getData();
        if (intent2 != null) {
            String decodeUrl = intent2.getEncodedPath().replaceFirst("/", "").toString();
            BottomNavigationActivity bottomNavigationActivity = new BottomNavigationActivity();
            String decode = bottomNavigationActivity.decode(decodeUrl);

            if(decode.contains("google")) {
                String decodeGoogleUrl = decode.replaceAll("\\\\", "");
                decodeGoogleUrl = decodeGoogleUrl.replaceAll("\"", "").toString();
                decodeGoogleUrl = decodeGoogleUrl.replaceAll("\\{", "").toString();
                decodeGoogleUrl = decodeGoogleUrl.replaceAll("\\}", "").toString();


                String[] pairs = decodeGoogleUrl.split(",");
                String code = "";
                String requestUri = "";
                for (int i = 0; i < pairs.length; i++) {
                    String pair = pairs[i];
                    if (pair.contains("code")) {
                        code = pair.substring(5, pair.length());
                    }
                    if (pair.contains("requestUri")) {
                        requestUri = pair.substring(11, pair.length());
                    }
                }

                GoogleAPI googleApi = new GoogleAPI();
                googleApi.getData(code, requestUri, this);
            }else{
                bottomNavigationActivity.getTokenFromMainAcitivity(decode,this);
            }

        }
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver, new IntentFilter("qrcodefloatingbutton"));

        initToolbar();
        initFab();
        initNavigation();


        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LoginPrefs.Companion.deleteToken(getApplicationContext());
                PrinterList printerList =new PrinterList();
                printerList.removePrinters();
                SharedPreferences sharedPreferences = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor myEdit = sharedPreferences.edit();
                myEdit.putString("IsLdap", "Others");
                myEdit.commit();
                Intent intent = new Intent(getApplicationContext(), SignInCompany.class);
                startActivity(intent);
            }
        });

        print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });

    }

    private void checkPermissions() {
        PermissionHelper permissionsHelper = new PermissionHelper();
        permissionsHelper.checkAndRequestPermissions(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        );
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void initFab() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), QRCodeScanActivity.class);
                startActivity(intent);
            }
        });

    }


    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String qrCodeScanBtn = intent.getStringExtra("qrCodeScanBtn");
            if (fab != null) {
                fab.setVisibility(View.INVISIBLE);
            }

            if (qrCodeScanBtn.equals("Active")) {
                fab.setVisibility(View.VISIBLE);
            }
        }
    };


    private void initNavigation() {
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setItemIconTintList(null);
        bottomNavView = findViewById(R.id.bottom_nav_view);
        contentView = findViewById(R.id.content_view);

        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow,
                R.id.nav_tools, R.id.nav_share, R.id.nav_send,
                R.id.bottom_home, R.id.bottom_dashboard, R.id.bottom_notifications)
                .setDrawerLayout(drawer)
                .build();

        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);

        NavigationUI.setupWithNavController(navigationView, navController);
        NavigationUI.setupWithNavController(bottomNavView, navController);

        Menu menuNav = navigationView.getMenu();
        MenuItem workspace = menuNav.findItem(R.id.nav_home);
        MenuItem reports = menuNav.findItem(R.id.nav_gallery);
        MenuItem storage = menuNav.findItem(R.id.nav_slideshow);
        MenuItem forms = menuNav.findItem(R.id.nav_tools);

        animateNavigationDrawer();
    }


    private void animateNavigationDrawer() {

        drawer.addDrawerListener(new DrawerLayout.SimpleDrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                final float diffScaledOffset = slideOffset * (1 - END_SCALE);
                final float offsetScale = 1 - diffScaledOffset;
                contentView.setScaleX(offsetScale);
                contentView.setScaleY(offsetScale);


                final float xOffset = drawerView.getWidth() * slideOffset;
                final float xOffsetDiff = contentView.getWidth() * diffScaledOffset / 2;
                final float xTranslation = xOffset - xOffsetDiff;
                contentView.setTranslationX(xTranslation);
            }
        });
    }

    public BroadcastReceiver mMessageReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("getPrintResponse") != null) {
                String printResponseStatus = intent.getStringExtra("getPrintResponse").toString();
                Toast.makeText(context, printResponseStatus, Toast.LENGTH_LONG).show();
            }

        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    public static String findSiteId(String completeUrl) {
        String siteId = "";
        int periodLocation = completeUrl.indexOf(".");
        if (periodLocation != -1) {
            siteId = completeUrl.substring(0, periodLocation);
        }
        return siteId;
    }

}



