package com.printerlogic.printerlogic;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;

import com.printerlogic.printerlogic.jipp.PrintUtils;
import com.printerlogic.printerlogic.jipp.PrinterList;
import com.printerlogic.printerlogic.jipp.PrinterModel;
import com.printerlogic.printerlogic.jipp.QRCodeScanActivity;
import com.printerlogic.printerlogic.prefs.LoginPrefs;
import com.printerlogic.printerlogic.print.BottomNavigationActivity;
import com.printerlogic.printerlogic.print.PrintersFragment;
import com.printerlogic.printerlogic.room.SelectedFile;
import com.printerlogic.printerlogic.signin.SignInCompany;
import com.printerlogic.printerlogic.utils.DataDogLogger;
import com.printerlogic.printerlogic.utils.GoogleAPI;
import com.printerlogic.printerlogic.utils.PermissionHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
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
//import org.slf4j.Logger;
import com.datadog.android.log.Logger;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

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
    private Logger mLogger;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        checkPermissions();
        setContentView(R.layout.activity_main2);
        signout = findViewById(R.id.signout);
        print=findViewById(R.id.print);
        fab = findViewById(R.id.fab);
        fab.setVisibility(View.VISIBLE);


        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(mMessageReceiver1,
                new IntentFilter("om.example.PRINT_RESPONSE"));


        Intent intent = getIntent();
        String action = intent.getAction();
        list.clear();

    Uri intent2 = intent.getData();
        if (intent2 != null) {

                String decodeUrl = intent2.getEncodedPath().replaceFirst("/", "").toString();
                BottomNavigationActivity bottomNavigationActivity = new BottomNavigationActivity();
                String decode = bottomNavigationActivity.decode(decodeUrl);

                if (decode.toLowerCase().contains("google")) {
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
                } else {
                    bottomNavigationActivity.getTokenFromMainAcitivity(decode, this);
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

                PrintersFragment printersFragment=new PrintersFragment();
                printersFragment.removePrinters(getApplicationContext());
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

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawericon1);

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

    public static String findTenantBaseUrl(String tenantBaseUrl) {
        String tenantUrl = "";

        int i =tenantBaseUrl.indexOf("/",9);
        tenantUrl =tenantBaseUrl.substring(0,i);
        return tenantUrl;
    }


    public static String getMacAddress(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        return macAddress;
    }

    public static String encodeString(String s) {
        byte[] data = new byte[0];

        try {
            data = s.getBytes("UTF-8");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } finally {
            String base64Encoded = Base64.encodeToString(data, Base64.NO_WRAP);

            return base64Encoded;

        }
    }



    public void getAttributeDeatilsForNativePrint(Context context){
         ArrayList<PrinterModel>deployedSecurePrinterListWithDetailsSharedPreflist= new ArrayList<PrinterModel>();
         ArrayList<PrinterModel>serverSecurePrinterListWithDetailsSharedPreflist= new ArrayList<PrinterModel>();
        SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson1 = new Gson();
        Type type1 = new TypeToken<ArrayList<PrinterModel>>() {}.getType();


        String json = prefs1.getString("deployedPrintersListForPrintPreivew", null);
        if (json != null) {
            deployedSecurePrinterListWithDetailsSharedPreflist = gson1.fromJson(json, type1);
        }

        String json2 = prefs1.getString("prefServerSecurePrinterListWithDetails", null);
        if (json2 != null) {
            serverSecurePrinterListWithDetailsSharedPreflist = gson1.fromJson(json2, type1);

        }
        if(deployedSecurePrinterListWithDetailsSharedPreflist!=null && deployedSecurePrinterListWithDetailsSharedPreflist.size()>0){
            serverSecurePrinterListWithDetailsSharedPreflist.addAll(deployedSecurePrinterListWithDetailsSharedPreflist);
        }

        for(int i=0;i<serverSecurePrinterListWithDetailsSharedPreflist.size();i++){
            PrinterModel printerModel= serverSecurePrinterListWithDetailsSharedPreflist.get(i);
            if(printerModel.getIsPullPrinter().equals("0") ) {
                try{
                getAttributeResponse(printerModel.getPrinterHost().toString(), printerModel.getServiceName(), context);
            }catch(Exception e){
                DataDogLogger.getLogger().e("Devnco_Android Exception (getAttributeResponse) - " + e.getMessage());
                }
            }
            if(printerModel.getIsPullPrinter().equals("0.0")){
                try{
                getAttributeResponse(printerModel.getPrinterHost().toString(), printerModel.getServiceName(), context);
                }catch(Exception e){
                    DataDogLogger.getLogger().e("Devnco_Android Exception (getAttributeResponse) - " + e.getMessage());
                }
                }
        }
    }

    public void getAttributeResponse(String hostAddress,String printerName,Context context) {
        new Thread() {

            public void run()    //Anonymous class overriding run() method of Thread class
            {
                try {
                    ArrayList<URI> ippUri = new ArrayList<URI>();

                    if (hostAddress != null) {
                        String printerHost = hostAddress;
                        ippUri.add(URI.create("ipp:/" + printerHost + ":631/ipp/print"));
                        ippUri.add(URI.create("ipp:/" + printerHost + ":631/ipp/printer"));
                        ippUri.add(URI.create("ipp:/" + printerHost + ":631/ipp/lp"));
                        ippUri.add(URI.create("ipp:/" + printerHost + "/printer"));
                        ippUri.add(URI.create("ipp:/" + printerHost + "/ipp"));
                        ippUri.add(URI.create("ipp:/" + printerHost + "/ipp/print"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/ipp"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/ipp/print"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/ipp/printer"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/print"));
                        ippUri.add(URI.create("http:/" + printerHost + "/ipp/print"));
                        ippUri.add(URI.create("http:/" + printerHost));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/printers/lp1"));
                        ippUri.add(URI.create("https:/" + printerHost));
                        ippUri.add(URI.create("https:/" + printerHost + ":443/ipp/print"));
                        ippUri.add(URI.create("ipps:/" + printerHost + ":443/ipp/print"));
                        ippUri.add(URI.create("http:/" + printerHost + ":631/ipp/lp"));
                    }
                    PrintUtils printUtils = new PrintUtils();
                    Map<String, String> resultMap = printUtils.getAttributesCall(ippUri, context);
                    if (!resultMap.containsKey("status")) {

                    }
                    if(resultMap.get("status").equals("successful-ok")){
                        String orientation =resultMap.get("orientation-requested-supported");
                        String sides =resultMap.get("sides-supported");
                        String color =resultMap.get("print-color-mode-supported");
                        String media=resultMap.get("media-supported");
                        String document = resultMap.get("document-format-supported");
                        String[] orientationSupported = orientation.split("=");
                        String[] sidesSupported = sides.split("=");
                        String[] colorSupported = color.split("=");
                        String[] mediaSupported = media.split("=");
                        String[] documentSupported =document.split("=");
                        String orientationStrigified="";
                        String sidesStrigified="";
                        String colorStrigified="";
                        String mediaStrigified="";
                        String documentStrigified="";
                        List<String> orientationSupportList =new ArrayList<String>();
                        List<String> sidesSupportList =new ArrayList();
                        List<String> colorSupportList =new ArrayList();
                        List<String> mediaSupportList =new ArrayList();
                        List<String> documentSupportList =new ArrayList();
                        if(orientationSupported.length>1){
                            orientationStrigified  =orientationSupported[1];
                            if(orientationStrigified.contains(",")){
                                String removeSplChar =orientationStrigified.replaceAll("\\[", "");
                                String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                String removeSplChar2 =removeSplChar1.replaceAll(" ","");
                                String[] orientationSupportedArray  = removeSplChar2.split(",");
                                orientationSupportList = Arrays.asList(orientationSupportedArray);
                            }else{
                                orientationSupportList.add(orientationStrigified);
                            }

                        }

                        if(documentSupported.length>1){
                            documentStrigified  =documentSupported[1];
                            if(documentStrigified.contains(",")){
                                String removeSplChar =documentStrigified.replaceAll("\\[", "");
                                String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                String removeSplChar2 =removeSplChar1.replaceAll(" ","");
                                String[] documentSupportedArray  = removeSplChar2.split(",");
                                documentSupportList = Arrays.asList(documentSupportedArray);
                            }else{
                                documentSupportList.add(documentStrigified);
                            }

                        }


                        if(sidesSupported.length>1){
                            sidesStrigified =sidesSupported[1];
                            if(sidesStrigified.contains(",")){
                                String removeSplChar =sidesStrigified.replaceAll("\\[", "");
                                String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                String removeSplChar2 =removeSplChar1.replaceAll(" ","");
                                String[]  sidesSupportedArray  = removeSplChar2.split(",");
                                sidesSupportList = Arrays.asList(sidesSupportedArray);
                            }else{
                                sidesSupportList.add(sidesStrigified);
                            }
                        }
                        if(colorSupported.length>1){
                            colorStrigified =colorSupported[1];
                            if(colorStrigified.contains(",")){
                                String removeSplChar =colorStrigified.replaceAll("\\[", "");
                                String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                String removeSplChar2=removeSplChar1.replaceAll(" ","");
                                String[] colorSupportedArray  = removeSplChar2.split(",");
                                colorSupportList = Arrays.asList(colorSupportedArray);
                            }else{
                                colorSupportList.add(colorStrigified);
                            }
                        }
                        if(mediaSupported.length>1){
                            mediaStrigified =mediaSupported[1];
                            if(mediaStrigified.contains(",")){
                                String removeSplChar =mediaStrigified.replaceAll("\\[", "");
                                String removeSplChar1 =removeSplChar.replaceAll("\\]", "");
                                String removeSplChar2 =removeSplChar1.replaceAll(" ","");
                                String[] mediaSupportedArray  = removeSplChar2.split(",");
                                mediaSupportList = Arrays.asList(mediaSupportedArray);
                            }else{
                                mediaSupportList.add(mediaStrigified);

                            }
                        }

                        Log.d("orientationSupported:",orientationStrigified);
                        Log.d("sidesSupported:",sidesStrigified);
                        Log.d("colorSupported:",colorStrigified);
                        Log.d("mediaSupported:",mediaStrigified);
                        ArrayList<String> items = new ArrayList<String>();
                        ArrayList<String>ColorSupportedlist = new ArrayList<String>();
                        HashSet<String> colorSupportedList =new HashSet<String>();
                        ArrayList<String>documentSupportedlist = new ArrayList<String>();
                        ArrayList<String>sideSupportedlist = new ArrayList<String>();


                        for(int i=0;i<orientationSupportList.size();i++){
                            Log.d("orientationSupported:",orientationSupportList.get(i));
                            items.add(orientationSupportList.get(i));
                        }
                        for(int i=0;i<sidesSupportList.size();i++){
                            Log.d("sidesSupported:",sidesSupportList.get(i));
                            sideSupportedlist.add(sidesSupportList.get(i));
                        }
                        for(int i=0;i<colorSupportList.size();i++){
                            Log.d("colorSupported:",colorSupportList.get(i));
                            if(colorSupportList.get(i).toLowerCase().equals("monochrome")){
                                colorSupportedList.add("Monochrome");
                            }else if(colorSupportList.get(i).toLowerCase().equals("color")){
                                colorSupportedList.add("Color");
                            }

                        }
                        ArrayList<String>listOfColorSupported = new ArrayList<String>(colorSupportedList);

                        ColorSupportedlist.addAll(listOfColorSupported);
                        for(int i=0;i<mediaSupportList.size();i++){
                            Log.d("mediaSupported:",mediaSupportList.get(i));

                        }

                        for(int i=0;i<documentSupportList.size();i++){
                            Log.d("documentSupported:",documentSupportList.get(i));
                            documentSupportedlist.add(documentSupportList.get(i));
                            if(documentSupportList.get(i).toLowerCase().contains("pdf")){

                            }
                        }


                        updatePrinterSupportedData(printerName,orientationSupportList,sidesSupportList,colorSupportList,mediaSupportList,documentSupportList,context);
                    }

                } catch (Exception exp) {
                    DataDogLogger.getLogger().e("Devnco_Android Exception - " + exp.getMessage());

                }
            }
        }.start();

    }


    public void updatePrinterSupportedData(String printerName,List<String> orientationSupportList,List<String> sidesSupportList,List<String> colorSupportList,List<String> mediaSupportList,List<String> documentSupportList,Context context){
         ArrayList<PrinterModel>deployedSecurePrinterListWithDetailsSharedPreflist= new ArrayList<PrinterModel>();
         ArrayList<PrinterModel>serverSecurePrinterListWithDetailsSharedPreflist= new ArrayList<PrinterModel>();

        SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson1 = new Gson();
        Type type1 = new TypeToken<ArrayList<PrinterModel>>() {}.getType();


        String json = prefs1.getString("deployedPrintersListForPrintPreivew", null);
        if (json != null) {
            deployedSecurePrinterListWithDetailsSharedPreflist = gson1.fromJson(json, type1);
        }

        String json2 = prefs1.getString("prefServerSecurePrinterListWithDetails", null);
        if (json2 != null) {
            serverSecurePrinterListWithDetailsSharedPreflist = gson1.fromJson(json2, type1);

        }

        for(int i=0;i<deployedSecurePrinterListWithDetailsSharedPreflist.size();i++){
            PrinterModel printerModel =deployedSecurePrinterListWithDetailsSharedPreflist.get(i);
            if(printerName.equals(printerModel.getServiceName())){
                printerModel.setOrientationSupportList(orientationSupportList);
                printerModel.setSidesSupportList(sidesSupportList);
                printerModel.setColorSupportList(colorSupportList);
                printerModel.setMediaSupportList(mediaSupportList);
                printerModel.setDocumentSupportList(documentSupportList);
                deployedSecurePrinterListWithDetailsSharedPreflist.add(printerModel);
                break;
            }
        }
        for(int i=0;i<serverSecurePrinterListWithDetailsSharedPreflist.size();i++){
            PrinterModel printerModel =serverSecurePrinterListWithDetailsSharedPreflist.get(i);
            if(printerName.equals(printerModel.getServiceName())){
                printerModel.setOrientationSupportList(orientationSupportList);
                printerModel.setSidesSupportList(sidesSupportList);
                printerModel.setColorSupportList(colorSupportList);
                printerModel.setMediaSupportList(mediaSupportList);
                printerModel.setDocumentSupportList(documentSupportList);
                serverSecurePrinterListWithDetailsSharedPreflist.add(printerModel);
                break;
            }
        }




        SharedPreferences.Editor editor = prefs1.edit();

        String json3 = gson1.toJson(deployedSecurePrinterListWithDetailsSharedPreflist);
        editor.putString("deployedPrintersListForPrintPreivew", json3);


        String json4 = gson1.toJson(serverSecurePrinterListWithDetailsSharedPreflist);
        editor.putString("prefServerSecurePrinterListWithDetails", json4);
        editor.apply();

    }

    public void addNotification(Context context,String message) {
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_baseline_print) //set icon for notification
                        .setContentTitle("Print Status") //set title of notification
                        .setContentText(message)//this is notification message
                        .setAutoCancel(true) // makes auto cancel of notification
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT); //set priority of notification


        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //notification message will get at NotificationView
        notificationIntent.putExtra("message", "This is a notification message");

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        // Add as notification
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
            builder.setChannelId(channelId);
        }
        manager.notify(0, builder.build());
    }
}


