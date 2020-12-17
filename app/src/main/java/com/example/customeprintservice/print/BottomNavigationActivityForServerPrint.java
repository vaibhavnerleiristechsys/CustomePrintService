package com.example.customeprintservice.print;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.example.customeprintservice.room.SelectedFile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.example.customeprintservice.R;

import java.util.HashSet;
import java.util.Set;

public class BottomNavigationActivityForServerPrint extends AppCompatActivity {
    public static Set<SelectedFile> selectedServerFile =new HashSet<SelectedFile>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_navigation_for_server_print);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();

        loadFragment(new ServerPrintRelaseFragment());
      //  NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
      //  NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
     //   NavigationUI.setupWithNavController(navView, navController);
    }


    private boolean loadFragment(Fragment fragment) {
        //switching fragment
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

     /*   MenuItem item = menu.findItem(R.id.print);
        item.setVisible(false);
        if (selectedServerFile.size()>0) {
            item.setVisible(true);
        }*/

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { switch(item.getItemId()) {
        case R.id.download:
            Log.d("documents download",selectedServerFile.toString());
            return(true);
        case R.id.print:
             Log.d("documents print",selectedServerFile.toString());
            return(true);

    }
        return(super.onOptionsItemSelected(item));
    }
}