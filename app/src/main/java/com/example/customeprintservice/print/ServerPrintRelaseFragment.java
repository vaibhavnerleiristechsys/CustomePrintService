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
import android.os.Bundle;
import android.os.Handler;
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
import com.example.customeprintservice.adapter.FragmentPrinterListAdapter;
import com.example.customeprintservice.jipp.PrintActivity;
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
import com.example.customeprintservice.utils.ProgressDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.unnamed.b.atv.model.TreeNode;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A fragment representing a list of Items.
 */
public class ServerPrintRelaseFragment extends Fragment {
    private static final String ARG_COLUMN_COUNT = "column-count";
    public static ArrayList serverDocumentlist = new ArrayList<SelectedFile>();
    private int mColumnCount = 1;
    Context context;
    Dialog dialog;
    View v = null;
    FloatingActionButton floatingActionButton;
    public static String localPrinturl;
    public static String selectedPrinterId;
    public static String selectedPrinterToken;
    public static int secure_release;
    public  ArrayList<SelectedFile> localdocumentFromsharedPrefences =new ArrayList<>();
    RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private RecyclerView recyclerViewList;
    private ConstraintLayout noDataMessage;
    public  List<Printer> listOfPrinters=new ArrayList<Printer>();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
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
        ProgressDialog.Companion.showLoadingDialog(requireContext(), "please wait");

    //    PrintReleaseFragment printReleaseFragment = new PrintReleaseFragment();
     //   printReleaseFragment.getJobStatusesForServerList(requireContext());

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver1,
                new IntentFilter("menuFunctionlityDisplay"));

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver2,
                new IntentFilter("menuFunctionlityDisplayhidden"));


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_print_relase_list, container, false);
        recyclerView = view.findViewById(R.id.list);
        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
        recyclerViewList =(RecyclerView) view.findViewById(R.id.list);
        noDataMessage =(ConstraintLayout) view.findViewById(R.id.empty_view);
        // Set the adapter
      //  if (view instanceof RecyclerView) {
             context = view.getContext();
             //recyclerView = (RecyclerView) view;


     if(LoginPrefs.Companion.getOCTAToken(context)==null) {
         final Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
             @Override
             public void run() {
                 getjobListStatus();
                 serverCallForGettingAllPrinters(requireContext());
             }
         }, 5000);
     }else{
         getjobListStatus();
         serverCallForGettingAllPrinters(requireContext());
     }




            if (mColumnCount <= 1) {
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
            } else {
                recyclerView.setLayoutManager(new GridLayoutManager(context, mColumnCount));
            }

                 recyclerView.setItemViewCacheSize(50);
            //setMaxsetMaxViewPoolSize(MAX_TYPE_ITEM, Int.MAX_VALUE)

              //   recyclerView.setAdapter(new MyItemRecyclerViewAdapter(DummyContent.ITEMS));


            Intent intent = new Intent("qrcodefloatingbutton");
            intent.putExtra("qrCodeScanBtn","Active");
            LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);

            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(mMessageReceiver,
                    new IntentFilter("message_subject_intent"));



            //    recyclerView.setAdapter(new MyItemRecyclerViewAdapter(PrintReleaseFragment.Companion.getGetdocumentList()));

      //  }
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
        // Inflate the menu; this adds items to the action bar if it is present.
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
                   // printReleaseFragment.cancelJob(requireContext());
                   // Intent myIntent = new Intent(getActivity(), MainActivity.class);
                //    getActivity().startActivity(myIntent);
                }else if(selectedFile1.isFromApi()==false){
                 //   removeDocumentFromSharedPreferences(requireContext());
                  //  Intent myIntent = new Intent(getActivity(), MainActivity.class);
                  //  getActivity().startActivity(myIntent);
                    deleteConfirmationDialog(selectedFile1);
                }
                return (true);
            case R.id.print:
                SelectedFile selectedFile=new SelectedFile();
                if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                    selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                }
                  if(selectedFile.isFromApi()==true && selectedFile.getJobType().equals("pull_print")){
                     /* PrinterList printerList = new PrinterList();
                      PrinterModel printerModel=new PrinterModel();
                      for(int i=0;i<printerList.getPrinterList().size();i++){
                          Boolean isAvailable=false;
                          printerModel=printerList.getPrinterList().get(i);
                          if(printerModel.getIsPullPrinter() !=null) {
                              if (printerModel.getManual() == true && printerModel.getIsPullPrinter().equals("1.0")) {

                                  for (int j = 0; j < PrintersFragment.Companion.getServerPullPrinterListWithDetails().size(); j++) {
                                      PrinterModel printer = PrintersFragment.Companion.getServerPullPrinterListWithDetails().get(j);
                                      if (printer.getPrinterHost().equals(printerModel.getPrinterHost())) {
                                          isAvailable = true;
                                      }
                                  }
                                  if (isAvailable == false && printerModel != null) {
                                      PrintersFragment.Companion.getServerPullPrinterListWithDetails().add(printerModel);
                                  }
                              }
                          }
                      }
                      selectePrinterDialog(PrintersFragment.Companion.getServerPullPrinterListWithDetails());
                      */
                      PrinterList printerList = new PrinterList();
                      // selectePrinterDialog(PrintersFragment.Companion.getServerPullPrinterListWithDetails());
                      PrinterModel printerModel=new PrinterModel();
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

                      // selectePrinterDialog(printerList.getPrinterList());
                     // selectePrinterDialog(PrintersFragment.Companion.getServerSecurePrinterListWithDetails());
                      selectePrinterDialog(PrintersFragment.Companion.getAllPrintersForPullHeldJob());

                  }else if (selectedFile.isFromApi()==true && selectedFile.getJobType().equals("secure_release")){
                     // PrintReleaseFragment printReleaseFragment1=new PrintReleaseFragment();
                      //printReleaseFragment1.releaseJob(requireContext(),"null");
                     // Intent myIntent1 = new Intent(getActivity(), MainActivity.class);
                    //  getActivity().startActivity(myIntent1);
                      selectePrinterDialog(PrintersFragment.Companion.getServerSecurePrinterForHeldJob());
                  }
                  else if(selectedFile.isFromApi()==false){
                      PrinterList printerList = new PrinterList();
                     // selectePrinterDialog(PrintersFragment.Companion.getServerPullPrinterListWithDetails());
                      PrinterModel printerModel=new PrinterModel();
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

                     // selectePrinterDialog(printerList.getPrinterList());
                      selectePrinterDialog(PrintersFragment.Companion.getServerSecurePrinterListWithDetails());
                  }


                return (true);
        }
        return (super.onOptionsItemSelected(item));
    }


    public BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String name= intent.getStringExtra("name");
            if(floatingActionButton!=null) {
                floatingActionButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.bloodOrange));
            }
        }
    };



    private void selectePrinterDialog(ArrayList<PrinterModel> list) {
        dialog = new Dialog(context);
      //  dialog.setContentView(R.layout.dialog_select_printer);
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

        RecyclerView printerRecyclerView = dialog.findViewById(R.id.dialogSelectPrinterRecyclerView);
        ImageView imgCancel = dialog.findViewById(R.id.imgDialogSelectPrinterCancel);
        floatingActionButton = dialog.findViewById(R.id.dialogSelectPrinterFloatingButton);
        floatingActionButton.setBackgroundTintList(ContextCompat.getColorStateList(context, R.color.paleGray));
        EditText searchPrinter=dialog.findViewById(R.id.searchPrinter);

        printerRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        PrinterList printerList = new PrinterList();
        printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context,list,"selectPrinter"));
        printerRecyclerView.setItemViewCacheSize(50);

        TextWatcher watcher = new TextWatcher() {
            public void afterTextChanged(Editable s) {
               Log.d("text:",s.toString());
                ArrayList<PrinterModel> filterList =new ArrayList<>();
               for(int i=0;i<list.size();i++){
                   PrinterModel printerModel=list.get(i);
                   if(printerModel.getServiceName().toLowerCase().contains(s.toString().toLowerCase())){
                       filterList.add(printerModel);
                   }
               }
                printerRecyclerView.setAdapter(new FragmentPrinterListAdapter(context,filterList,"selectPrinter"));
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
                   Toast.makeText(requireContext(), "print release", Toast.LENGTH_LONG).show();
                   printReleaseFragment.releaseJob(context,release_t);
                   dialog.cancel();
               }else{
                   Toast.makeText(requireContext(), "print release", Toast.LENGTH_LONG).show();
                   printReleaseFragment.releaseJob(context,"null");
                   dialog.cancel();
               }

/*
               if(secure_release == 0 || secure_release ==1 || secure_release==2){
                   Toast.makeText(requireContext(), "print release", Toast.LENGTH_LONG)
                           .show();
                //   printReleaseFragment.releaseJob(context);
                   dialog.cancel();

               }else if(secure_release == 3 || secure_release ==4){
                   Toast.makeText(requireContext(), "print hold", Toast.LENGTH_LONG)
                           .show();
                   dialog.cancel();
                   Intent myIntent = new Intent(getActivity(), MainActivity.class);
                   getActivity().startActivity(myIntent);

               }
               else if(secure_release == 5 || secure_release ==6){

                   //dialogPromptPrinter();
                   Toast.makeText(requireContext(), "print release", Toast.LENGTH_LONG)
                           .show();
                //   printReleaseFragment.releaseJob(context);
                   dialog.cancel();
               }*/

            }else{

               if(secure_release == 0 || secure_release ==1 || secure_release==2){
                     String FilePath =selectedFile.getFilePath();
                    PrintActivity printActivity =new PrintActivity();
                    printActivity.locaPrint(FilePath,localPrinturl,context);
                   //removeDocumentFromSharedPreferences();
                   Toast.makeText(context, "print release", Toast.LENGTH_LONG)
                           .show();
                       dialog.cancel();
                     Intent myIntent = new Intent(getActivity(), MainActivity.class);
                      getActivity().startActivity(myIntent);

               }else if(secure_release == 3 || secure_release ==4){
                   Toast.makeText(context, "print hold", Toast.LENGTH_LONG)
                           .show();
                   dialog.cancel();
                   Intent myIntent = new Intent(getActivity(), MainActivity.class);
                   getActivity().startActivity(myIntent);
               }
               else if(secure_release == 5 || secure_release ==6){

                //   dialogPromptPrinter();

                   String FilePath =selectedFile.getFilePath();
                   PrintActivity printActivity =new PrintActivity();
                   printActivity.locaPrint(FilePath,localPrinturl,context);
                   //removeDocumentFromSharedPreferences();
                   Toast.makeText(context, "print release", Toast.LENGTH_LONG)
                           .show();
                   dialog.cancel();
                   Intent myIntent = new Intent(getActivity(), MainActivity.class);
                   getActivity().startActivity(myIntent);
               }

            }

        });
        dialog.show();
    }



    private void dialogPromptPrinter(){
        Dialog dialog1 = new Dialog(context);
        //  dialog.setContentView(R.layout.dialog_select_printer);
        v = LayoutInflater.from(context).inflate(R.layout.dialog_printer_prompt, null);
        dialog1.setContentView(v);
        dialog1.setCancelable(false);

        Button hold = dialog1.findViewById(R.id.hold);
        Button release= dialog1.findViewById(R.id.release);
        dialog1.setCanceledOnTouchOutside(true);
        Window window = dialog1.getWindow();
        assert window != null;
        window.setLayout(AbsListView.LayoutParams.WRAP_CONTENT, AbsListView.LayoutParams.WRAP_CONTENT);
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setDimAmount(0.5f);
        window.setAttributes(wlp);
        dialog1.show();

        hold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.cancel();
                dialog1.cancel();
                Intent myIntent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(myIntent);
            }
        });

        release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SelectedFile selectedFile=new SelectedFile();

                if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                    selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                }
                if(selectedFile.isFromApi()==true) {
                    PrintReleaseFragment printReleaseFragment=new PrintReleaseFragment();
                   // printReleaseFragment.releaseJob(context);
                    dialog.cancel();
                    dialog1.cancel();
                    Intent myIntent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(myIntent);
                }else{
                      String FilePath =selectedFile.getFilePath();
                     PrintActivity printActivity =new PrintActivity();
                     printActivity.locaPrint(FilePath,localPrinturl,context);
                   // removeDocumentFromSharedPreferences();
                    dialog.cancel();
                    dialog1.cancel();
                    Intent myIntent = new Intent(getActivity(), MainActivity.class);
                    getActivity().startActivity(myIntent);
                }

                }

        });

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



        ProgressDialog.Companion.showLoadingDialog(context, "Loading");
        PrintReleaseFragment printReleaseFragment=new PrintReleaseFragment();
        PrintReleaseFragment.Companion.getGetdocumentList().clear();
       String siteId= LoginPrefs.Companion.getSiteId(context);
        String BASE_URL = "https://gw.app.printercloud.com/"+siteId+"/pq/api/job-statuses/";
        ApiService apiService = new RetrofitClient(context)
                .getRetrofitInstance(BASE_URL)
                .create(ApiService.class);
        Call call;
        if(IsLdap.equals("LDAP")){
            call = apiService.getPrintJobStatusesForLdap(
                    siteId.toString(),
                    LdapUsername.toString(),
                    LdapPassword.toString()
            );
        }else if(siteId.contains("google")){
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
                List<PrintQueueJobStatusItem> getJobStatusesResponse = response.body().getPrintQueueJobStatus();
                    PrintReleaseFragment.Companion.getGetdocumentList().clear();
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
                    Integer sizeInKb =PrintQueueJobStatusItem.getJobSize() /1024;
                    String fileSize=sizeInKb.toString()+"KB";
                    selectedFile.setJobSize(fileSize);
                    selectedFile.setWorkStationId(PrintQueueJobStatusItem.getWorkstationId());
                    if(PrintQueueJobStatusItem.getPrinterDeviceQueue().getPrinters().size()>0) {
                        selectedFile.setPrinterId(PrintQueueJobStatusItem.getPrinterDeviceQueue().getPrinters().get(0).getId());
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
                recyclerView.setAdapter(new MyItemRecyclerViewAdapter(PrintReleaseFragment.Companion.getGetdocumentList()));

                    ProgressDialog.Companion.cancelLoading();
                    swipeContainer.setRefreshing(false);
            }else{
                    ProgressDialog.Companion.cancelLoading();
                    swipeContainer.setRefreshing(false);
                }
        }

            @Override
            public void onFailure(Call<GetJobStatusesResponse> call, Throwable t) {
                ProgressDialog.Companion.cancelLoading();
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


    private void serverCallForGettingAllPrinters(Context context)
    {
        TreeNode root = TreeNode.root();
        listOfPrinters.clear();
        PrintersFragment.Companion.getAllPrintersForPullHeldJob().clear();
        @SuppressLint("WrongConstant") SharedPreferences prefs = context.getSharedPreferences("MySharedPref", Context.MODE_APPEND);
        String IsLdap = prefs.getString("IsLdap", "");
        String LdapUsername= prefs.getString("LdapUsername", "");
        String LdapPassword= prefs.getString("LdapPassword", "");
        Log.d("IsLdap:", IsLdap);


        String siteId=LoginPrefs.Companion.getSiteId(requireContext());
        String url = "https://gw.app.printercloud.com/"+siteId+"/tree/api/node/";
        ApiService apiService = new RetrofitClient(requireContext())
                .getRetrofitInstance(url)
                .create(ApiService.class);

        PrintReleaseFragment prf = new PrintReleaseFragment();

        Call call;
        if(IsLdap.equals("LDAP")){
            call = apiService.getPrintersListForLdap(
                    siteId.toString(),
                    LdapUsername.toString(),
                    LdapPassword.toString()
            );
        }else if(siteId.contains("google")){
            call = apiService.getPrintersListForGoogle(
                    "Bearer " + LoginPrefs.Companion.getOCTAToken(requireContext()),
                    prf.decodeJWT(requireContext()),
                    SignInCompanyPrefs.Companion.getIdpType(requireContext()).toString(),
                    SignInCompanyPrefs.Companion.getIdpName(requireContext()).toString(),
                    "serverId"
            );
        }
        else {
            call = apiService.getPrintersList(
                    "Bearer " + LoginPrefs.Companion.getOCTAToken(requireContext()),
                    prf.decodeJWT(requireContext()),
                    SignInCompanyPrefs.Companion.getIdpType(requireContext()).toString(),
                    SignInCompanyPrefs.Companion.getIdpName(requireContext()).toString()
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


}
