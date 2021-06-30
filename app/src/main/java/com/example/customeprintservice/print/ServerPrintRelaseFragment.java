package com.example.customeprintservice.print;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.pdf.PdfRenderer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.example.customeprintservice.MainActivity;
import com.example.customeprintservice.R;
import com.example.customeprintservice.adapter.FragmentPrinterAlphabetsListAdapter;
import com.example.customeprintservice.adapter.FragmentPrinterListAdapter;
import com.example.customeprintservice.jipp.PrintActivity;
import com.example.customeprintservice.jipp.PrintRenderUtils;
import com.example.customeprintservice.jipp.PrinterList;
import com.example.customeprintservice.jipp.PrinterModel;
import com.example.customeprintservice.prefs.LoginPrefs;
import com.example.customeprintservice.prefs.SignInCompanyPrefs;
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.GetJobStatusesResponse;
import com.example.customeprintservice.printjobstatus.model.getjobstatuses.PrintQueueJobStatusItem;
import com.example.customeprintservice.printjobstatus.model.printerlist.Printer;
import com.example.customeprintservice.rest.ApiService;
import com.example.customeprintservice.rest.RetrofitClient;
import com.example.customeprintservice.room.SelectedFile;
import com.example.customeprintservice.utils.DataDogLogger;
import com.example.customeprintservice.utils.ProgressDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.jipp.model.Sides;
import com.unnamed.b.atv.model.TreeNode;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;

public class ServerPrintRelaseFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static ArrayList serverDocumentlist = new ArrayList<SelectedFile>();
    private int mColumnCount = 1;
    Context context;
    Dialog dialog;
    RecyclerView printerRecyclerView;
    View v = null;
    FloatingActionButton floatingActionButton;
    public static String localPrinturl;
    public static String selectedPrinterId;
    public static String selectedPrinterToken;
    public static String selectedPrinterHost;
    public static String selectedPrinterServiceName;
    public static int secure_release;
    public  ArrayList<SelectedFile> localdocumentFromsharedPrefences =new ArrayList<>();
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerViewList;
    private ConstraintLayout noDataMessage;
    public  List<Printer> listOfPrinters=new ArrayList<Printer>();
    //Logger logger = LoggerFactory.getLogger(ServerPrintRelaseFragment.class);
    public static ArrayList<PrinterModel> printerList=new ArrayList<PrinterModel>();
    public static boolean getholdJobAPIStart=false;
    private Context mContext;
    public ServerPrintRelaseFragment() {
    }

    public static ServerPrintRelaseFragment newInstance(int columnCount) {
        ServerPrintRelaseFragment fragment = new ServerPrintRelaseFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_COLUMN_COUNT, columnCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        if (getArguments() != null) {
            mColumnCount = getArguments().getInt(ARG_COLUMN_COUNT);
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver1, new IntentFilter("menuFunctionlityDisplay"));
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver2, new IntentFilter("menuFunctionlityDisplayhidden"));




    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_print_relase_list, container, false);
        recyclerView = view.findViewById(R.id.list);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        recyclerViewList =(RecyclerView) view.findViewById(R.id.list);
        noDataMessage =(ConstraintLayout) view.findViewById(R.id.empty_view);
        context = view.getContext();
        ProgressDialog.Companion.showLoadingDialog(context, "please wait");
        if(getholdJobAPIStart==false){
            ServerPrintRelaseFragment.getJobUpdateCall(context);
            getholdJobAPIStart=true;
        }


        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                MainActivity mainActivity=new MainActivity();

                mainActivity.getAttributeDeatilsForNativePrint(requireContext());

            }
        }, 7000);



       if(((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeButtonEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawericon1);
        }

        BottomNavigationActivity bottomNavigationActivity1 = new BottomNavigationActivity();
 String isStartedJobUpdateMethod=LoginPrefs.Companion.getStartJobIdMethod(context);

     if(LoginPrefs.Companion.getOCTAToken(context)==null) {
         @SuppressLint("WrongConstant") SharedPreferences prefs = context.getSharedPreferences("MySharedPref", Context.MODE_APPEND);
         String IsLdap = prefs.getString("IsLdap", "");
         if(IsLdap.equals("LDAP")) {
             getjobListStatus();
             serverCallForGettingAllPrinters(requireContext());
             PrintersFragment printersFragment1 = new PrintersFragment();
             printersFragment1.getPrinterList(context, bottomNavigationActivity1.decodeJWT(context));
         }else {
             //final Handler handler = new Handler();
             handler.postDelayed(new Runnable() {
                 @Override
                 public void run() {
                     getjobListStatus();
                     serverCallForGettingAllPrinters(context);
                     PrintersFragment printersFragment1 = new PrintersFragment();
                     printersFragment1.getPrinterList(context, bottomNavigationActivity1.decodeJWT(context));
                /*     if(isStartedJobUpdateMethod==null) {
                         getJobUpdateCall();
                     }

                 */
                 }
             }, 5000);
         }
     }else{
         getjobListStatus();
      //   serverCallForGettingAllPrinters(requireContext());
         /*if(isStartedJobUpdateMethod==null) {
             getJobUpdateCall();
         }

          */
         PrintersFragment printersFragment1 = new PrintersFragment();
         printersFragment1.getPrinterList(context, bottomNavigationActivity1.decodeJWT(context));
     }


            LoginPrefs.Companion.setStartJobIdMethod(context);


            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

                 recyclerView.setItemViewCacheSize(50);

            Intent intent = new Intent("qrcodefloatingbutton");
            intent.putExtra("qrCodeScanBtn","Active");
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver,
                    new IntentFilter("message_subject_intent"));

            LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(mMessageReceiver3,new IntentFilter("moveRecyclerView"));

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getjobListStatus();
            }
        });


        return view;
    }



    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.menu_main, menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download:
                SelectedFile selectedFile1=new SelectedFile();
                if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                    selectedFile1 = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                }
                if(selectedFile1.isFromApi()==true) {
                    PrintReleaseFragment printReleaseFragment = new PrintReleaseFragment();
                    deleteConfirmationDialog(selectedFile1);
                }else if(selectedFile1.isFromApi()==false){
                    deleteConfirmationDialog(selectedFile1);
                }
                return (true);
            case R.id.print:
                SelectedFile selectedFile=new SelectedFile();
                if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                    selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                }
                  if(selectedFile.isFromApi()==true && selectedFile.getJobType().equals("pull_print")){

                     /*  PrinterList printerList = new PrinterList();
                      PrinterModel printerModel;
                     for(int i=0;i<printerList.getPrinterList().size();i++){
                          Boolean isAvailable=false;
                          printerModel=printerList.getPrinterList().get(i);
                          if(printerModel.getIsPullPrinter() !=null) {
                              if (printerModel.getManual() == true && printerModel.getIsPullPrinter().equals("0.0")) {

                                  for (int j = 0; j < PrintersFragment.Companion.getServerSecurePrinterListWithDetails().size(); j++) {
                                      PrinterModel printer = PrintersFragment.Companion.getServerSecurePrinterListWithDetails().get(j);
                                      if (printer.getPrinterHost().equals(printerModel.getPrinterHost())) {
                                          isAvailable = true;
                                      }
                                  }
                                  if (isAvailable == false && printerModel != null) {
                                      PrintersFragment.Companion.getServerSecurePrinterListWithDetails().add(printerModel);
                                  }
                              }
                          }
                      }
*/
                      Collections.sort(PrintersFragment.Companion.getAllPrintersForPullHeldJob(), new Comparator<PrinterModel>() {
                          @Override
                          public int compare(PrinterModel item, PrinterModel t1) {
                              String s1 = item.getServiceName();
                              String s2 = t1.getServiceName();
                              return s1.compareToIgnoreCase(s2);
                          }

                      });
                    ArrayList<PrinterModel> RecentPrinterAddedList =  addRecentPrintersToDisplay(PrintersFragment.Companion.getAllPrintersForPullHeldJob());
                    //  selectePrinterDialog(PrintersFragment.Companion.getAllPrintersForPullHeldJob());
                      selectePrinterDialog(RecentPrinterAddedList);

                  }else if (selectedFile.isFromApi()==true && selectedFile.getJobType().equals("secure_release")){
                      selectePrinterDialog(PrintersFragment.Companion.getServerSecurePrinterForHeldJob());
                  }
                  else if(selectedFile.isFromApi()==false){
                      PrinterList printerList = new PrinterList();
                      PrinterModel printerModel;
                      for(int i=0;i<printerList.getPrinterList().size();i++){
                          Boolean isAvailable=false;
                           printerModel=printerList.getPrinterList().get(i);
                           if(printerModel.getIsPullPrinter() !=null) {
                               if (printerModel.getManual() == true && printerModel.getIsPullPrinter().equals("0.0")) {
                                   for (int j = 0; j < PrintersFragment.Companion.getServerSecurePrinterListWithDetails().size(); j++) {
                                       PrinterModel printer = PrintersFragment.Companion.getServerSecurePrinterListWithDetails().get(j);
                                       if (printer.getPrinterHost().equals(printerModel.getPrinterHost())) {
                                           isAvailable = true;
                                       }
                                   }
                                   if (isAvailable == false && printerModel != null) {
                                       PrintersFragment.Companion.getServerSecurePrinterListWithDetails().add(printerModel);
                                   }
                               }
                           }
                      }

                      Collections.sort(PrintersFragment.Companion.getServerSecurePrinterListWithDetails(), new Comparator<PrinterModel>() {
                          @Override
                          public int compare(PrinterModel item, PrinterModel t1) {
                              String s1 = item.getServiceName();
                              String s2 = t1.getServiceName();
                              return s1.compareToIgnoreCase(s2);
                          }

                      });

                      selectePrinterDialog(PrintersFragment.Companion.getServerSecurePrinterListWithDetails());
                  }


                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }


    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(floatingActionButton!=null) {
                floatingActionButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.bloodOrange));
            }
        }
    };



    private void selectePrinterDialog(ArrayList<PrinterModel> list) {
        dialog = new Dialog(context);
        printerList.clear();
        printerList.addAll(list);
        SwipeRefreshLayout swipeContainer;


        v = LayoutInflater.from(context).inflate(R.layout.dialog_select_printer, null);
        dialog.setContentView(v);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        Window window = dialog.getWindow();
        assert window != null;
        window.setLayout(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.MATCH_PARENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.BOTTOM;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
       window.setDimAmount(0.5f);
        window.setAttributes(wlp);




        printerRecyclerView = dialog.findViewById(R.id.dialogSelectPrinterRecyclerView);
        ImageView imgCancel = dialog.findViewById(R.id.imgDialogSelectPrinterCancel);
        floatingActionButton = dialog.findViewById(R.id.dialogSelectPrinterFloatingButton);
        floatingActionButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.paleGray));
        EditText searchPrinter=dialog.findViewById(R.id.searchPrinter);

        swipeContainer = (SwipeRefreshLayout) dialog.findViewById(R.id.swipeContainer);

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                SelectedFile selectedFile=new SelectedFile();
                if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                    selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                }
                if(selectedFile.isFromApi()==true && selectedFile.getJobType().equals("pull_print")) {
                    serverCallForGettingAllPrinters(context);
                }

                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.d("refresh:","refresh printer list");
                        swipeContainer.setRefreshing(false);
                        SelectedFile selectedFile=new SelectedFile();
                        if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                            selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                        }
                        if(selectedFile.isFromApi()==true && selectedFile.getJobType().equals("pull_print")) {
                            Collections.sort(PrintersFragment.Companion.getAllPrintersForPullHeldJob(), new Comparator<PrinterModel>() {
                                @Override
                                public int compare(PrinterModel item, PrinterModel t1) {
                                    String s1 = item.getServiceName();
                                    String s2 = t1.getServiceName();
                                    return s1.compareToIgnoreCase(s2);
                                }

                            });
                            ArrayList<PrinterModel> RecentPrinterAddedList =  addRecentPrintersToDisplay(PrintersFragment.Companion.getAllPrintersForPullHeldJob());
                            printerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                            printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context,RecentPrinterAddedList,"selectPrinter"));
                            printerRecyclerView.setItemViewCacheSize(50);
                        }
                        else if (selectedFile.isFromApi()==true && selectedFile.getJobType().equals("secure_release")){

                            printerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
                            printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context, PrintersFragment.Companion.getServerSecurePrinterForHeldJob(),"selectPrinter"));
                            printerRecyclerView.setItemViewCacheSize(50);
                        }

                    }
                }, 30000);

            }
        });



        printerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context,list,"selectPrinter"));
        printerRecyclerView.setItemViewCacheSize(50);

          ArrayList<String> alphabetsList = new ArrayList<String>(
                Arrays.asList("A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z","#"));

        RecyclerView recyclerViewAlphabetsList = dialog.findViewById(R.id.alphabetsRecyclerView);
        recyclerViewAlphabetsList.setLayoutManager(new LinearLayoutManager(context));
        recyclerViewAlphabetsList.setAdapter(new FragmentPrinterAlphabetsListAdapter(context,alphabetsList));


        TextWatcher watcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
               Log.d("text:",s.toString());
                DataDogLogger.getLogger().i("Devnco_Android text:"+s.toString());

                ArrayList<PrinterModel> filterList =new ArrayList<PrinterModel>();
               for(int i=0;i<list.size();i++){
                   PrinterModel printerModel=list.get(i);
                   if(printerModel.getServiceName().toLowerCase().contains(s.toString().toLowerCase())){
                       filterList.add(printerModel);
                   }
               }
                Collections.sort(filterList, new Comparator<PrinterModel>() {
                    @Override
                    public int compare(PrinterModel item, PrinterModel t1) {
                        String s1 = item.getServiceName();
                        String s2 = t1.getServiceName();
                        return s1.compareToIgnoreCase(s2);
                    }

                });
               if(s.toString().equals("")) {
                   ArrayList<PrinterModel> RecentPrinterAddedList = addRecentPrintersToDisplay(filterList);
                   printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context, RecentPrinterAddedList, "selectPrinter"));
               }else{
                   printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context, filterList, "selectPrinter"));
               }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
        };

        searchPrinter.addTextChangedListener(watcher);

        imgCancel.setOnClickListener(v ->
                dialog.cancel()
        );

        floatingActionButton.setOnClickListener(v -> {
            PrintReleaseFragment printReleaseFragment=new PrintReleaseFragment();
            SelectedFile selectedFile=new SelectedFile();

            if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                 selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
            }



           if(selectedFile.isFromApi()==true){
               if(selectedFile.getJobType().equals("pull_print")){
                   String release_t="";
                   if(selectedPrinterId!=null && selectedPrinterToken!=null) {
                        release_t = selectedPrinterId + "," + selectedPrinterToken;
                   }
                   //Toast.makeText(requireContext(), "print release", Toast.LENGTH_LONG).show();
                   printReleaseFragment.releaseJob(context,release_t);
                   dialog.cancel();
               }else{
                 //  Toast.makeText(requireContext(), "print release", Toast.LENGTH_LONG).show();
                   printReleaseFragment.releaseJob(context,"null");
                   dialog.cancel();
               }

            }else{

               if(secure_release == 0 || secure_release ==1 || secure_release==2){
                    String FilePath =selectedFile.getFilePath();
                    PrintActivity printActivity =new PrintActivity();
                    printActivity.locaPrint(FilePath,localPrinturl,context);
                 //   Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                    dialog.cancel();
                    Intent myIntent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(myIntent);

               }else if(secure_release == 3 || secure_release ==4){
                  // Toast.makeText(context, "print hold", Toast.LENGTH_LONG).show();
                   dialog.cancel();
                   Intent myIntent = new Intent(getActivity(), MainActivity.class);
                   getActivity().startActivity(myIntent);
               }
               else if(secure_release == 5 || secure_release ==6){
                   String FilePath =selectedFile.getFilePath();
                   PrintActivity printActivity =new PrintActivity();
                   printActivity.locaPrint(FilePath,localPrinturl,context);

                 //  Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
                   dialog.cancel();
                   Intent myIntent = new Intent(getActivity(), MainActivity.class);
                   getActivity().startActivity(myIntent);
               }

            }

           //**********************
            /*
                        if(selectedFile.getSourceMachine().equals("Mobile")){
                ArrayList<SelectedFile> localDocumentSharedPreflist = new ArrayList<SelectedFile>();
                String FileName=selectedFile.getFileName();
                String filePath="";
                SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
                Gson gson1 = new Gson();
                String json2 = prefs1.getString("holdlocaldocumentlist", null);
                Type type1 = new TypeToken<ArrayList<SelectedFile>>() {
                }.getType();
                localDocumentSharedPreflist = gson1.fromJson(json2, type1);
                if(localDocumentSharedPreflist != null) {
                    for (int i = 0; i < localDocumentSharedPreflist.size(); i++) {
                        SelectedFile selectedPrefFile = localDocumentSharedPreflist.get(i);
                        if (selectedPrefFile.getFileName().contains(FileName)) {
                            File file = new File(selectedPrefFile.getFilePath());
                            Log.d("File::", file.getName() + "" + file.getAbsolutePath());
                            filePath =file.getAbsolutePath();

                        }
                    }
                }

                try {
                    sendLocalPrintHoldJob(filePath,context,selectedPrinterHost,selectedPrinterServiceName,selectedPrinterId);
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

             */
            //*******************





        });
        dialog.show();
    }

   public void removeDocumentFromSharedPreferences(Context context){
       SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
       Gson gson = new Gson();
       String json = prefs.getString("localdocumentlist", null);
       Type type = new TypeToken<ArrayList<SelectedFile>>() {}.getType();
       localdocumentFromsharedPrefences = gson.fromJson(json, type);
       SelectedFile selectedFile=new SelectedFile();

       if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
           selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
       }


       if(localdocumentFromsharedPrefences !=null) {
           for (int i = 0; i < localdocumentFromsharedPrefences.size(); i++) {
               SelectedFile selectedFile1 = localdocumentFromsharedPrefences.get(i);
               if (selectedFile1.getFilePath().equals(selectedFile.getFilePath())) {
                   localdocumentFromsharedPrefences.remove(i);
               }
           }

           SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
           Gson gson1 = new Gson();
           SharedPreferences.Editor editor = prefs1.edit();
           String json1 = gson1.toJson(localdocumentFromsharedPrefences);
           editor.putString("localdocumentlist", json1);
           editor.apply();
       }

    }


    public void getjobListStatus(){
        @SuppressLint("WrongConstant") SharedPreferences prefs = context.getSharedPreferences("MySharedPref", Context.MODE_APPEND);
        String IsLdap = prefs.getString("IsLdap", "");
        String LdapUsername= prefs.getString("LdapUsername", "");
        String LdapPassword= prefs.getString("LdapPassword", "");
        Log.d("IsLdap:", IsLdap);
        DataDogLogger.getLogger().i("Devnco_Android IsLdap:"+ IsLdap);
        ProgressDialog.Companion.showLoadingDialog(context, "Loading");
        PrintReleaseFragment printReleaseFragment=new PrintReleaseFragment();
        PrintReleaseFragment.Companion.getGetdocumentList().clear();
        String siteId= LoginPrefs.Companion.getSiteId(context);
        String tanentUrl =LoginPrefs.Companion.getTenantUrl(context);
        //String BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/";
        String BASE_URL =""+tanentUrl+"/"+siteId+"/pq/api/job-statuses/";
        ApiService apiService = new RetrofitClient(context).getRetrofitInstance(BASE_URL).create(ApiService.class);
        Call call;
        if(IsLdap.equals("LDAP")){
            call = apiService.getPrintJobStatusesForLdap(
                    siteId.toString(),
                    LdapUsername.toString(),
                    LdapPassword.toString(),
                    "printerDeviceQueue.printers"
            );
        }else if(siteId.contains("google")){
            DataDogLogger.getLogger().i("Devnco_Android API call: "+BASE_URL.toString()+" Token: "+LoginPrefs.Companion.getOCTAToken(context)+" username: "+printReleaseFragment.decodeJWT(context));
            call = apiService.getPrintJobStatusesForGoogle(
                    "Bearer " + LoginPrefs.Companion.getOCTAToken(context),
                    printReleaseFragment.decodeJWT(context),
                    SignInCompanyPrefs.Companion.getIdpType(context).toString(),
                    SignInCompanyPrefs.Companion.getIdpName(context).toString(),
                    "serverId",
                    printReleaseFragment.decodeJWT(context),
                    "printerDeviceQueue.printers"
            );
        }
        else {

            DataDogLogger.getLogger().i("Devnco_Android API call: "+BASE_URL.toString()+" Token: "+LoginPrefs.Companion.getOCTAToken(context)+" username: "+printReleaseFragment.decodeJWT(context));
             call = apiService.getPrintJobStatuses(
                    "Bearer " + LoginPrefs.Companion.getOCTAToken(context),
                     printReleaseFragment.decodeJWT(context),
                    SignInCompanyPrefs.Companion.getIdpType(context).toString(),
                    SignInCompanyPrefs.Companion.getIdpName(context).toString(),
                    printReleaseFragment.decodeJWT(context),
                    "printerDeviceQueue.printers"
            );
        }
        call.enqueue(new Callback<GetJobStatusesResponse>() {
            public void onResponse(Call<GetJobStatusesResponse> call, Response<GetJobStatusesResponse> response) {
                if(response.isSuccessful()){
                    DataDogLogger.getLogger().i("Devnco_Android Geeting held job response ** : "+response.body().toString()+" **====>");
                    DataDogLogger.getLogger().i("Devnco_Android Geeting held job response: "+response.body().getPrintQueueJobStatus().toString()+"====>");
                List<PrintQueueJobStatusItem> getJobStatusesResponse = response.body().getPrintQueueJobStatus();
                    PrintReleaseFragment.Companion.getGetdocumentList().clear();
                    DataDogLogger.getLogger().i("Devnco_Android Geeting held job size: "+getJobStatusesResponse.size()+"====>");
                for (int i = 0; i < getJobStatusesResponse.size(); i++) {
                    PrintQueueJobStatusItem PrintQueueJobStatusItem = getJobStatusesResponse.get(i);
                    SelectedFile selectedFile = new SelectedFile();
                    selectedFile.setFromApi(true);
                    selectedFile.setFileName(PrintQueueJobStatusItem.getDocumentTitle());
                    selectedFile.setFileSelectedDate(PrintQueueJobStatusItem.getSubmittedAtRelative());
                    selectedFile.setFilePath(PrintQueueJobStatusItem.getDocumentTitle());
                    selectedFile.setJobNum(PrintQueueJobStatusItem.getJobNumber());
                    selectedFile.setJobType(PrintQueueJobStatusItem.getJobType());
                    selectedFile.setQueueId(PrintQueueJobStatusItem.getPrinterDeviceQueueId());
                    selectedFile.setUserName(PrintQueueJobStatusItem.getUserName());
                    selectedFile.setPages(PrintQueueJobStatusItem.getPages());
                    Integer sizeInKb =PrintQueueJobStatusItem.getJobSize() /1024;
                    String fileSize=sizeInKb.toString()+"KB";
                    selectedFile.setJobSize(fileSize);
                    selectedFile.setSourceMachine(PrintQueueJobStatusItem.getSourceMachine());
                    selectedFile.setWorkStationId(PrintQueueJobStatusItem.getWorkstationId());
                    DataDogLogger.getLogger().i("Devnco_Android Geeting held job :"+i+" : Title: "+PrintQueueJobStatusItem.getDocumentTitle()
                            +"username: "+PrintQueueJobStatusItem.getUserName()+"====>");

                    if(PrintQueueJobStatusItem.getPrinterDeviceQueue() !=null) {
                        if (PrintQueueJobStatusItem.getPrinterDeviceQueue().getPrinters() != null) {
                            if (PrintQueueJobStatusItem.getPrinterDeviceQueue().getPrinters().size() > 0) {
                                selectedFile.setPrinterId(PrintQueueJobStatusItem.getPrinterDeviceQueue().getPrinters().get(0).getId());
                                DataDogLogger.getLogger().i("Devnco_Android Geeting held job :"+i+" : Title: "+PrintQueueJobStatusItem.getDocumentTitle()
                                        +"printer id: "+PrintQueueJobStatusItem.getPrinterDeviceQueue().getPrinters().get(0).getId()+"====>");
                            }
                        }
                    }




                    PrintReleaseFragment.Companion.getGetdocumentList().add(selectedFile);
                }
                if(localdocumentFromsharedPrefences!=null) {
                    localdocumentFromsharedPrefences.clear();
                }
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                Gson gson = new Gson();
                String json = prefs.getString("localdocumentlist", null);
                Type type = new TypeToken<ArrayList<SelectedFile>>() {
                }.getType();
                localdocumentFromsharedPrefences = gson.fromJson(json, type);
                if(localdocumentFromsharedPrefences!=null) {
                    PrintReleaseFragment.Companion.getGetdocumentList().addAll(localdocumentFromsharedPrefences);
                }

                if(PrintReleaseFragment.Companion.getGetdocumentList().size()>0){
                    recyclerViewList.setVisibility(View.VISIBLE);
                    noDataMessage.setVisibility(View.GONE);
                }else{
                    recyclerViewList.setVisibility(View.GONE);
                    noDataMessage.setVisibility(View.VISIBLE);
                }
                recyclerView.setAdapter(new MyItemRecyclerViewAdapter(PrintReleaseFragment.Companion.getGetdocumentList(),"Print Release"));

                    ProgressDialog.Companion.cancelLoading();
                    swipeContainer.setRefreshing(false);
            }else{
                    ProgressDialog.Companion.cancelLoading();
                    swipeContainer.setRefreshing(false);
                    DataDogLogger.getLogger().i("Devnco_Android Geeting held job response message** : "+response.raw().toString()+" **====>");
                    DataDogLogger.getLogger().i("Devnco_Android Geeting held job response error code** : "+response.code()+" **====>");
                }
        }

            @Override
            public void onFailure(Call<GetJobStatusesResponse> call, Throwable t) {
                ProgressDialog.Companion.cancelLoading();
                DataDogLogger.getLogger().i("Devnco_Android Geeting held job failure message** : "+t.getLocalizedMessage()+" **====>");
                DataDogLogger.getLogger().i("Devnco_Android Geeting held job failure error body message** : "+t.getMessage().toString()+" **====>");
                swipeContainer.setRefreshing(false);
                call.cancel();
            }
        });

    }

    public BroadcastReceiver mMessageReceiver1 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setHasOptionsMenu(true);
        }
    };

    public BroadcastReceiver mMessageReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            setHasOptionsMenu(false);
        }
    };

    public BroadcastReceiver mMessageReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           String character  = intent.getStringExtra("Character");

                if (character != "") {
                    boolean isAvailable = false;
                    int position = 0;
                    for (int i = 0; i < printerList.size(); i++) {
                        PrinterModel printer = printerList.get(i);
                        if (printer.getServiceName().toLowerCase().startsWith(character.toLowerCase())) {
                            if (printerRecyclerView != null) {
                                //  recyclerViewPrinterLst.scrollToPosition(position)
                                isAvailable= true;
                                printerRecyclerView.smoothScrollToPosition(position);

                            }
                        }
                        if(character.equals("#")){
                            isAvailable= true;
                            printerRecyclerView.smoothScrollToPosition(1);
                            break;
                        }
                        position++;
                    }
                    if(isAvailable == true){

                    }else{
                        Toast.makeText(context, "Printer is Not available", Toast.LENGTH_LONG).show();
                    }

                }

            //printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context,printerList,"selectPrinter"));
            //printerRecyclerView.setItemViewCacheSize(50);
        }
    };


    private void deleteConfirmationDialog(SelectedFile selectedFile){
        Dialog dialog1 = new Dialog(context);
        dialog1.setContentView(R.layout.dialog_confirmation_delete_job);
        dialog1.setCancelable(false);
        Button ok = dialog1.findViewById(R.id.ok);
        Button cancel = dialog1.findViewById(R.id.cancel);
        dialog1.setCanceledOnTouchOutside(true);
        Window window = dialog1.getWindow();
        assert window != null;
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setLayout(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.5f);
        window.setAttributes(wlp);
        dialog1.show();

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(selectedFile.isFromApi()==true) {
                    PrintReleaseFragment printReleaseFragment = new PrintReleaseFragment();
                    printReleaseFragment.cancelJob(requireContext());
                }else if(selectedFile.isFromApi()==false){
                    removeDocumentFromSharedPreferences(requireContext());
                }
                dialog1.cancel();
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(myIntent);

            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog1.cancel();
            }
        });

    }


    public void serverCallForGettingAllPrinters(Context context)
    {
        TreeNode root = TreeNode.root();
        listOfPrinters.clear();
        PrintersFragment.Companion.getAllPrintersForPullHeldJob().clear();
        @SuppressLint("WrongConstant") SharedPreferences prefs = context.getSharedPreferences("MySharedPref", Context.MODE_APPEND);
        String IsLdap = prefs.getString("IsLdap", "");
        String LdapUsername= prefs.getString("LdapUsername", "");
        String LdapPassword= prefs.getString("LdapPassword", "");
        Log.d("IsLdap:", IsLdap);
        String siteId=LoginPrefs.Companion.getSiteId(context);
        String tanentUrl =LoginPrefs.Companion.getTenantUrl(context);

        String url = ""+tanentUrl+"/"+siteId+"/tree/api/node/";
        //String url = "https://gw.app.printercloud.com/"+siteId+"/tree/api/node/";

        ApiService apiService = new RetrofitClient(context).getRetrofitInstance(url).create(ApiService.class);
        PrintReleaseFragment prf = new PrintReleaseFragment();
        Call call;
        if(IsLdap.equals("LDAP")){
            call = apiService.getPrintersListForLdap(
                    siteId.toString(),
                    LdapUsername.toString(),
                    LdapPassword.toString()
            );
        }else if(siteId.contains("google")){

            DataDogLogger.getLogger().i("Devnco_Android API call: "+url.toString()+" Token: "+LoginPrefs.Companion.getOCTAToken(context)+" username: "+prf.decodeJWT(context));
            call = apiService.getPrintersListForGoogle(
                    "Bearer " + LoginPrefs.Companion.getOCTAToken(context),
                    prf.decodeJWT(context),
                    SignInCompanyPrefs.Companion.getIdpType(context).toString(),
                    SignInCompanyPrefs.Companion.getIdpName(context).toString(),
                    "serverId"
            );
        }
        else {
            DataDogLogger.getLogger().i("Devnco_Android API call: "+url.toString()+" Token: "+LoginPrefs.Companion.getOCTAToken(context)+" username: "+prf.decodeJWT(context));
            call = apiService.getPrintersList(
                    "Bearer " + LoginPrefs.Companion.getOCTAToken(context),
                    prf.decodeJWT(context),
                    SignInCompanyPrefs.Companion.getIdpType(context).toString(),
                    SignInCompanyPrefs.Companion.getIdpName(context).toString()
            );
        }

        call.enqueue(new Callback<List<Printer>>() {
            public void onResponse(Call<List<Printer>> call, Response<List<Printer>> response) {
                if(response.isSuccessful())
                {
                    listOfPrinters = response.body();
                    for(Printer printer:listOfPrinters) {

                        if(printer.getObject_sort_order()==1000) {
                            PrintersFragment printersFragment=new PrintersFragment();
                            Thread thread = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try  {
                                        printersFragment.getPrinterListByPrinterId(context,printer.getObject_id().toString() ,"getPrinterDetailsForPullJob");
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();
                        }
                    }

                }
                else
                {
                    int code = response.code();

                }
            }

            @Override
            public void onFailure(Call<List<Printer>> call, Throwable t) {
                int code  = call.hashCode();

            }
        });
    }

public ArrayList<PrinterModel> addRecentPrintersToDisplay(ArrayList<PrinterModel> originalList){
    ArrayList<PrinterModel> removeRecentPrinters=new ArrayList<PrinterModel>();
    removeRecentPrinters.addAll(originalList);
    originalList.clear();
    for(int i=0;i<removeRecentPrinters.size();i++){
        PrinterModel removeRecentTagPrinter = removeRecentPrinters.get(i);
        removeRecentTagPrinter.setRecentUsed(false);
        originalList.add(removeRecentTagPrinter);
    }



    SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
    ArrayList<PrinterModel> recentPrinters=new ArrayList<PrinterModel>();
    Gson gson1 = new Gson();
    String json2 = prefs1.getString("recentUsedPrinters", null);
    Type type1 = new TypeToken<ArrayList<PrinterModel>>() {}.getType();
    if (json2 != null) {
        recentPrinters = gson1.fromJson(json2, type1);
        for(int i=0;i<recentPrinters.size();i++){
            PrinterModel printerModel= recentPrinters.get(i);
           for(int j=0;j<originalList.size();j++){
               PrinterModel OriginalPrinterModel = originalList.get(j);
               if(OriginalPrinterModel.getServiceName().equals(printerModel.getServiceName())){
                   originalList.remove(OriginalPrinterModel);
                   OriginalPrinterModel.setRecentUsed(true);
                   originalList.add(i,OriginalPrinterModel);
               }
           }

        }
    }
    return originalList;
}

public void sendLocalPrintHoldJob(String filePath ,Context context,String hostAddress,String PrinterName,String PrinterId) throws IOException {
    {
        File file = new File(filePath);
        ArrayList<URI> ippUri =new ArrayList<URI>();
        if( hostAddress != null){
            String printerHost = hostAddress.toString();
            ippUri.add(URI.create("ipp:/"+printerHost+":631/ipp/print"));
            ippUri.add(URI.create("ipp:/"+printerHost+":631/ipp/printer"));
            ippUri.add(URI.create("ipp:/"+printerHost+":631/ipp/lp"));
            ippUri.add(URI.create("ipp:/"+printerHost+"/printer"));
            ippUri.add(URI.create("ipp:/"+printerHost+"/ipp"));
            ippUri.add(URI.create("ipp:/"+printerHost+"/ipp/print"));
            ippUri.add(URI.create("http:/"+printerHost+":631/ipp"));
            ippUri.add(URI.create("http:/"+printerHost+":631/ipp/print"));
            ippUri.add(URI.create("http:/"+printerHost+":631/ipp/printer"));
            ippUri.add(URI.create("http:/"+printerHost+":631/print"));
            ippUri.add(URI.create("http:/"+printerHost+"/ipp/print"));
            ippUri.add(URI.create("http:/"+printerHost));
            ippUri.add(URI.create("http:/"+printerHost+":631/printers/lp1"));
            ippUri.add(URI.create("https:/"+printerHost));
            ippUri.add(URI.create("https:/"+printerHost+":443/ipp/print"));
            ippUri.add(URI.create("ipps:/"+printerHost+":443/ipp/print"));
            ippUri.add(URI.create("http:/"+printerHost+":631/ipp/lp"));
        }

        if( filePath != null && hostAddress != null){
            BottomNavigationActivityForServerPrint.selectedServerFile.clear();
            SelectedFile selectedFile=new SelectedFile();
            selectedFile.setFileName(file.getName());
            selectedFile.setFilePath(file.getAbsolutePath());
            BottomNavigationActivityForServerPrint.selectedServerFile.add(selectedFile);
            String host =hostAddress.replaceAll("/","");
            BottomNavigationActivityForServerPrint.selectedPrinter.setPrinterHost(InetAddress.getByName(host));
            BottomNavigationActivityForServerPrint.selectedPrinter.setServiceName(PrinterName);
            BottomNavigationActivityForServerPrint.selectedPrinter.setId(PrinterId);
        }



        if(file.getName().contains(".pdf")) {
            if ( hostAddress != null && filePath != null ) {
                String finalLocalurl = "http" + ":/" + hostAddress.toString() + ":631/ipp/print";
                PrintRenderUtils printRenderUtils = new PrintRenderUtils();
                ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(file, MODE_READ_ONLY);
                PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                final int pageCount = renderer.getPageCount();

                printRenderUtils.renderPageUsingDefaultPdfRendererForSelectedPages(file, finalLocalurl, context, 0, pageCount, 1,ippUri,pageCount,true,"portrait","");
             //   Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();
            }

        }else if(file.getName().contains(".docx") || file.getName().contains(".doc")){

        }else{
            if(hostAddress !=null && filePath !=null ) {

                String finalLocalurl = "http" + ":/" + hostAddress.toString() + ":631/ipp/print";
                PrintRenderUtils printRenderUtils = new PrintRenderUtils();
                printRenderUtils.printNoOfCOpiesJpgOrPngAndPdfFiles(file, finalLocalurl, context, 1,ippUri,true,"portrait","", Sides.oneSided);
              //  Toast.makeText(context, "print release", Toast.LENGTH_LONG).show();

            }
        }


    }
}


public static void getJobUpdateCall(Context context){
   /* new Timer().scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
            PrintReleaseFragment printReleaseFragment =new PrintReleaseFragment();
            new Handler(Looper.getMainLooper()).post(
                    new Runnable() {
                        @Override
                        public void run() {
                            printReleaseFragment.gettingHeldJobStatus(context);
                            handler.postDelayed(this, 30000);
                        }
                    });


        }
    }, 30000, 30000);*/
    PrintReleaseFragment printReleaseFragment =new PrintReleaseFragment();
     Handler handler = new Handler();
     new Handler(Looper.getMainLooper()).post(
            new Runnable() {
                @Override
                public void run() {
                    printReleaseFragment.gettingHeldJobStatus(context,"");
                    handler.postDelayed(this, 15000);
                }
            });


}
public static void getjobFromSharedPreferences(Context context,String jobId,String printerId) throws IOException {
    SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
    ArrayList<SelectedFile> documentSharedPreflist = new ArrayList<SelectedFile>();
    Gson gson1 = new Gson();
    String json2 = prefs1.getString("holdlocaldocumentlist", null);
    Type type1 = new TypeToken<ArrayList<SelectedFile>>() {
    }.getType();
    documentSharedPreflist = gson1.fromJson(json2, type1);
    if(documentSharedPreflist != null) {
        PrinterList list =new PrinterList();
        String filePath="";
        ArrayList<PrinterModel> printerList = list.getPrinterList();
        for (int i = 0; i < documentSharedPreflist.size(); i++) {
            SelectedFile selectedPrefFile = documentSharedPreflist.get(i);
            if (selectedPrefFile.getJobId().equals(jobId)) {
                Log.d("job Id:",jobId);
                        Log.d("document name:",selectedPrefFile.getFileName());
                                Log.d("filter jobs for print","*************");
                filePath =selectedPrefFile.getFilePath();

                  for(int j=0;j<printerList.size();j++){
                    PrinterModel printerModel =  printerList.get(j);
                    if(printerModel.getId().equals(printerId)){
                        Log.d("printer Id:",printerModel.getId().toString());
                        Log.d("printer name:",printerModel.getServiceName().toString());
                        Log.d("printer host:",printerModel.getPrinterHost().toString());

                        ServerPrintRelaseFragment serverPrintRelaseFragment =new ServerPrintRelaseFragment();
                        String filePaths =filePath;
                        Thread thread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try  {
                                    serverPrintRelaseFragment.sendLocalPrintHoldJob(filePaths,context,printerModel.getPrinterHost().toString(),printerModel.getServiceName(),printerModel.getId());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                        thread.start();


                    }
                  }
            break;
            }
        }
    }
}


    public static void getjobFromSharedPreferencesForPullJobs(Context context,String jobId,String printerId,String printerName,String printerHost) throws IOException {
        SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(context);
        ArrayList<SelectedFile> documentSharedPreflist = new ArrayList<SelectedFile>();
        Gson gson1 = new Gson();
        String json2 = prefs1.getString("holdlocaldocumentlist", null);
        Type type1 = new TypeToken<ArrayList<SelectedFile>>() {
        }.getType();
        documentSharedPreflist = gson1.fromJson(json2, type1);
        if(documentSharedPreflist != null) {
            PrinterList list =new PrinterList();
            String filePath="";
            ArrayList<PrinterModel> printerList = list.getPrinterList();
            for (int i = 0; i < documentSharedPreflist.size(); i++) {
                SelectedFile selectedPrefFile = documentSharedPreflist.get(i);
                if (selectedPrefFile.getJobId().equals(jobId)) {
                    Log.d("job Id:",jobId);
                    Log.d("document name:",selectedPrefFile.getFileName());
                    Log.d("filter jobs for print","*************");
                    filePath =selectedPrefFile.getFilePath();

                            Log.d("printer Id:",printerId);
                            Log.d("printer name:",printerName);
                             Log.d("printer host:",printerHost);
                             String modifyHostName="/"+printerHost;
                             Log.d("printer modifyHost:",modifyHostName);



                            ServerPrintRelaseFragment serverPrintRelaseFragment =new ServerPrintRelaseFragment();
                            String filePaths =filePath;
                            Thread thread = new Thread(new Runnable() {

                                @Override
                                public void run() {
                                    try  {
                                        serverPrintRelaseFragment.sendLocalPrintHoldJob(filePaths,context,modifyHostName,printerName,printerId);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            thread.start();

                    break;
                }
            }
        }
    }

    public static String getMacAddress(Context context){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wInfo = wifiManager.getConnectionInfo();
        String macAddress = wInfo.getMacAddress();
        return macAddress;
    }

}

