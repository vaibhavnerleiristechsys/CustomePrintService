package com.printerlogic.printerlogic.print;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;
import com.printerlogic.printerlogic.R;
import com.printerlogic.printerlogic.room.SelectedFile;
import com.printerlogic.printerlogic.utils.DataDogLogger;
import com.printerlogic.printerlogic.utils.ProgressDialog;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    private final List<SelectedFile> mValues;
    public Context context;
    String locationPlaced;
    List<ViewHolder> holders= new ArrayList<ViewHolder>();
  //  Print Release
   // QrCodeScan
    public MyItemRecyclerViewAdapter(List<SelectedFile> items,String location) {
        mValues = items;
        locationPlaced=location;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_server_print_relase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getFileName());
        holder.mContentView.setText(mValues.get(position).getFileSelectedDate());
        holder.mFileSize.setText(mValues.get(position).getJobSize());
        holders.add(holder);

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.documenticon.setVisibility(View.VISIBLE);
                holder.checkBox.setVisibility(View.GONE);
                holder.serverDocument.setBackgroundColor(Color.parseColor("#FFFFFF"));
                BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                Intent intent2 = new Intent("menuFunctionlityDisplayhidden");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                Intent intent3 = new Intent("qrcodefloatingbutton");
                intent3.putExtra("qrCodeScanBtn", "Active");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
            }
            });

        if(locationPlaced.equals("Print Release")) {
            holder.documenticon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("check value", String.valueOf(holder.checkBox.isChecked()));
                    DataDogLogger.getLogger().i("Devnco_Android check value" + String.valueOf(holder.checkBox.isChecked()));
                    Intent intent = new Intent("menuFunctionlityDisplay");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    Intent intent1 = new Intent("qrcodefloatingbutton");
                    intent1.putExtra("qrCodeScanBtn", "InActive");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
                    try {
                        for (int i = 0; i < holders.size(); i++) {

                            ViewHolder holder = holders.get(i);
                            if (i == position) {
                                holder.checkBox.setVisibility(View.VISIBLE);
                                holder.documenticon.setVisibility(View.GONE);
                                holder.checkBox.setChecked(true);
                                holder.serverDocument.setBackgroundColor(Color.parseColor("#FFEEE5"));
                                if (!BottomNavigationActivityForServerPrint.selectedServerFile.isEmpty()) {
                                    SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                                    if (selectedFile.getFileName().equals(mValues.get(position).getFileName())) {
                                        holder.checkBox.setChecked(false);
                                        holder.documenticon.setVisibility(View.VISIBLE);
                                        holder.checkBox.setVisibility(View.GONE);
                                        holder.serverDocument.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                        BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                        Intent intent2 = new Intent("menuFunctionlityDisplayhidden");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                                        Intent intent3 = new Intent("qrcodefloatingbutton");
                                        intent3.putExtra("qrCodeScanBtn", "Active");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
                                    } else {
                                        BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                        BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                                    }

                                } else {
                                    BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                    BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                                }

                                if (BottomNavigationActivityForServerPrint.selectedServerFile.size() > 0) {
                                    SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                                    if (selectedFile.isFromApi() == true) {
                                        if (selectedFile.getPrinterId() != null) {
                                            PrintersFragment.Companion.getServerSecurePrinterForHeldJob().clear();
                                            if (selectedFile.getJobType().equals("secure_release")) {
                                                ProgressDialog.Companion.showLoadingDialog(context, "please wait");
                                            }
                                            new PrintersFragment().getPrinterListByPrinterId(context, selectedFile.getPrinterId().toString(), "forSecureRelase");
                                        }
                                    }
                                }

                            } else {
                                holder.documenticon.setVisibility(View.VISIBLE);
                                holder.checkBox.setVisibility(View.GONE);
                                holder.checkBox.setChecked(false);
                                holder.serverDocument.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("File SelectionException", e.getMessage());
                        DataDogLogger.getLogger().i("Devnco_Android File SelectionException" + e.getMessage());
                    }
                }

            });
            holder.serverDocument.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("check value", String.valueOf(holder.checkBox.isChecked()));
                    DataDogLogger.getLogger().i("Devnco_Android check value" + String.valueOf(holder.checkBox.isChecked()));
                    Intent intent = new Intent("menuFunctionlityDisplay");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    Intent intent1 = new Intent("qrcodefloatingbutton");
                    intent1.putExtra("qrCodeScanBtn", "InActive");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
                    try {
                        for (int i = 0; i < holders.size(); i++) {
                            ViewHolder holder = holders.get(i);
                            if (i == position) {
                                holder.checkBox.setVisibility(View.VISIBLE);
                                holder.documenticon.setVisibility(View.GONE);
                                holder.checkBox.setChecked(true);
                                holder.serverDocument.setBackgroundColor(Color.parseColor("#FFEEE5"));
                                if (!BottomNavigationActivityForServerPrint.selectedServerFile.isEmpty()) {
                                    SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                                    if (selectedFile.getFileName().equals(mValues.get(position).getFileName())) {
                                        holder.checkBox.setChecked(false);
                                        holder.documenticon.setVisibility(View.VISIBLE);
                                        holder.checkBox.setVisibility(View.GONE);
                                        holder.serverDocument.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                        BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                        Intent intent2 = new Intent("menuFunctionlityDisplayhidden");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                                        Intent intent3 = new Intent("qrcodefloatingbutton");
                                        intent3.putExtra("qrCodeScanBtn", "Active");
                                        LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
                                    } else {
                                        BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                        BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                                    }

                                } else {
                                    BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                    BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                                }

                                if (BottomNavigationActivityForServerPrint.selectedServerFile.size() > 0) {
                                    SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                                    if (selectedFile.isFromApi() == true) {
                                        if (selectedFile.getPrinterId() != null) {
                                            PrintersFragment.Companion.getServerSecurePrinterForHeldJob().clear();
                                            if (selectedFile.getJobType().equals("secure_release")) {
                                                ProgressDialog.Companion.showLoadingDialog(context, "please wait");
                                            }
                                            new PrintersFragment().getPrinterListByPrinterId(context, selectedFile.getPrinterId().toString(), "forSecureRelase");
                                        }
                                    }
                                }

                            } else {
                                holder.documenticon.setVisibility(View.VISIBLE);
                                holder.checkBox.setVisibility(View.GONE);
                                holder.checkBox.setChecked(false);
                                holder.serverDocument.setBackgroundColor(Color.parseColor("#FFFFFF"));
                            }
                        }
                    } catch (Exception e) {
                        Log.e("File SelectionException", e.getMessage());
                        DataDogLogger.getLogger().e("Devnco_Android File SelectionException" + e.getMessage());
                    }
                }

            });

        }
        if(locationPlaced.equals("QrCodeScan")){
            holder.documenticon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("check value", String.valueOf(holder.checkBox.isChecked()));
                    DataDogLogger.getLogger().i("Devnco_Android check value" + String.valueOf(holder.checkBox.isChecked()));
                    Intent intent = new Intent("menuFunctionlityDisplay");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    Intent intent1 = new Intent("qrcodefloatingbutton");
                    intent1.putExtra("qrCodeScanBtn", "InActive");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
                    try {
                        for (int i = 0; i < holders.size(); i++) {

                            ViewHolder holder = holders.get(i);
                            if (i == position) {
                                holder.checkBox.setVisibility(View.VISIBLE);
                                holder.documenticon.setVisibility(View.GONE);
                                holder.checkBox.setChecked(true);
                                holder.serverDocument.setBackgroundColor(Color.parseColor("#FFEEE5"));
                                if (!BottomNavigationActivityForServerPrint.selectedServerFile.isEmpty()) {
                                    SelectedFile selectedFileforRemove = null;
                                    for(int j=0;i<BottomNavigationActivityForServerPrint.selectedServerFile.size();j++) {
                                        SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(j);
                                        if (selectedFile.getFileName().equals(mValues.get(position).getFileName())) {
                                            holder.checkBox.setChecked(false);
                                            holder.documenticon.setVisibility(View.VISIBLE);
                                            holder.checkBox.setVisibility(View.GONE);
                                            holder.serverDocument.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                            selectedFileforRemove =selectedFile;
                                            Intent intent2 = new Intent("menuFunctionlityDisplayhidden");
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                                            Intent intent3 = new Intent("qrcodefloatingbutton");
                                            intent3.putExtra("qrCodeScanBtn", "Active");
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
                                         }
                                    }
                                    if(selectedFileforRemove !=null) {
                                        BottomNavigationActivityForServerPrint.selectedServerFile.remove(selectedFileforRemove);
                                    }else{
                                        BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                                    }
                                } else {
                                    BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                    BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                                }

                            } else {

                            }
                        }
                    } catch (Exception e) {
                        Log.e("File SelectionException", e.getMessage());
                        DataDogLogger.getLogger().i("Devnco_Android File SelectionException in Qr Code scan :" + e.getMessage());
                    }
                    for(int i=0;i<BottomNavigationActivityForServerPrint.selectedServerFile.size();i++){
                        SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(i);
                        Log.e("File Name:", selectedFile.getFileName());
                    }
                }

            });

            //****************************************************************************************************************************

            holder.serverDocument.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("check value", String.valueOf(holder.checkBox.isChecked()));
                    DataDogLogger.getLogger().i("Devnco_Android check value" + String.valueOf(holder.checkBox.isChecked()));
                    Intent intent = new Intent("menuFunctionlityDisplay");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    Intent intent1 = new Intent("qrcodefloatingbutton");
                    intent1.putExtra("qrCodeScanBtn", "InActive");
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);
                    try {
                        for (int i = 0; i < holders.size(); i++) {
                            ViewHolder holder = holders.get(i);
                            if (i == position) {
                                holder.checkBox.setVisibility(View.VISIBLE);
                                holder.documenticon.setVisibility(View.GONE);
                                holder.checkBox.setChecked(true);
                                holder.serverDocument.setBackgroundColor(Color.parseColor("#FFEEE5"));
                                if (!BottomNavigationActivityForServerPrint.selectedServerFile.isEmpty()) {
                                    SelectedFile  selectedFileforRemove = null;
                                    for(int j=0;j<BottomNavigationActivityForServerPrint.selectedServerFile.size();j++){
                                        SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(j);
                                        if (selectedFile.getFileName().equals(mValues.get(position).getFileName())) {
                                            holder.checkBox.setChecked(false);
                                            holder.documenticon.setVisibility(View.VISIBLE);
                                            holder.checkBox.setVisibility(View.GONE);
                                            holder.serverDocument.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                            selectedFileforRemove=selectedFile;
                                            Intent intent2 = new Intent("menuFunctionlityDisplayhidden");
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent2);
                                            Intent intent3 = new Intent("qrcodefloatingbutton");
                                            intent3.putExtra("qrCodeScanBtn", "Active");
                                            LocalBroadcastManager.getInstance(context).sendBroadcast(intent3);
                                        }
                                    }
                                    if(selectedFileforRemove !=null) {
                                        BottomNavigationActivityForServerPrint.selectedServerFile.remove(selectedFileforRemove);
                                    }else{
                                        BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                                    }

                                } else {
                                    BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                    BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                                }

                            } else {

                            }
                        }
                    } catch (Exception e) {
                        Log.e("File SelectionException", e.getMessage());
                        DataDogLogger.getLogger().e("Devnco_Android File SelectionException" + e.getMessage());
                    }
                    for(int i=0;i<BottomNavigationActivityForServerPrint.selectedServerFile.size();i++){
                        SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(i);
                        Log.e("File Name:", selectedFile.getFileName());
                    }
                }


            });




        }
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final TextView mFileSize;
        public final ConstraintLayout serverDocument;
        public final ConstraintLayout checkboxanddocument;
        public final CheckBox checkBox;
        public SelectedFile mItem;
        public ImageView documenticon;
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mContentView = view.findViewById(R.id.content);
            mFileSize= view.findViewById(R.id.filesize);
            serverDocument = view.findViewById(R.id.serverDocument);
            checkboxanddocument= view.findViewById(R.id.checkboxanddocument);
            checkBox = view.findViewById(R.id.checkbox);
            checkBox.setVisibility(View.GONE);
            documenticon =view.findViewById(R.id.documenticon);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

    }


}