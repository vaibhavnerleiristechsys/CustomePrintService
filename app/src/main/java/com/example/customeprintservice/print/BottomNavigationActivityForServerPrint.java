package com.example.customeprintservice.print;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.customeprintservice.jipp.PrinterModel;
import com.example.customeprintservice.room.SelectedFile;
import com.example.customeprintservice.signin.GoogleLoginActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.customeprintservice.R;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.util.ArrayList;

public class BottomNavigationActivityForServerPrint extends AppCompatActivity  implements BottomNavigationView.OnNavigationItemSelectedListener{
    public static ArrayList<SelectedFile> selectedServerFile =new ArrayList<SelectedFile>();
    public static PrinterModel selectedPrinter =new PrinterModel();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation_for_server_print);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        selectedServerFile.clear();
    //    printReleaseFragment.getJobStatusesForServerList(getApplicationContext());

        loadFragment(new ServerPrintRelaseFragment());
        BottomNavigationView navigation = findViewById(R.id.nav_view);
        navigation.setOnNavigationItemSelectedListener(this);
    }


    public boolean loadFragment(Fragment fragment) {
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .commit();
            return true;
        }
        return false;
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        switch (item.getItemId()) {
            case R.id.printRelease:
                fragment = new ServerPrintRelaseFragment();
                break;
            case R.id.printer:
                fragment = new PrintersFragment();
                break;

        }
        return loadFragment(fragment);    }
}