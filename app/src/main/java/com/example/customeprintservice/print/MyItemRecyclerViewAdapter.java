package com.example.customeprintservice.print;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.customeprintservice.R;
import com.example.customeprintservice.room.SelectedFile;
import com.example.customeprintservice.utils.ProgressDialog;

import org.spongycastle.asn1.x509.Holder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * TODO: Replace the implementation with code for your data type.
 */
public class MyItemRecyclerViewAdapter extends RecyclerView.Adapter<MyItemRecyclerViewAdapter.ViewHolder> {

    public static Set<SelectedFile> selectedServerFile = new HashSet<SelectedFile>();
    private final List<SelectedFile> mValues;
    Menu menu;
    public Context context;
    private int selectedPosition = -1;
    List<ViewHolder> holders= new ArrayList<ViewHolder>();
    public MyItemRecyclerViewAdapter(List<SelectedFile> items) {
        mValues = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        context = parent.getContext();
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_server_print_relase, parent, false);



        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.mIdView.setText(mValues.get(position).getFileName());
        holder.mContentView.setText(mValues.get(position).getFileSelectedDate());

        holders.add(holder);
        this.selectedPosition = position;

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

        holder.documenticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Log.d("click on document icon","clock on document icon");

                Log.d("file name", Objects.requireNonNull(mValues.get(position).getFileName()));
                Log.d("check value", String.valueOf(holder.checkBox.isChecked()));
                Intent intent = new Intent("menuFunctionlityDisplay");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                Intent intent1 = new Intent("qrcodefloatingbutton");
                intent1.putExtra("qrCodeScanBtn", "InActive");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);



                for(int i=0;i<holders.size();i++) {
                    ViewHolder holder = holders.get(i);
                    if (i == position) {
                        holder.checkBox.setVisibility(View.VISIBLE);
                        holder.documenticon.setVisibility(View.GONE);
                        holder.checkBox.setChecked(true);
                        holder.serverDocument.setBackgroundColor(Color.parseColor("#FFEEE5"));
                        if(!BottomNavigationActivityForServerPrint.selectedServerFile.isEmpty()) {
                            SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                            if(selectedFile.getFileName().equals(mValues.get(position).getFileName())){
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
                            }else{
                                BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                            }

                        }else{
                            BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                            BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                        }

                        if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                            SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                            if (selectedFile.isFromApi() == true) {
                                if(selectedFile.getPrinterId()!=null) {
                                    PrintersFragment.Companion.getServerSecurePrinterForHeldJob().clear();
                                    if(selectedFile.getJobType().equals("secure_release")){
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
                // holder.Documenticonchanged();
            }

        });
        holder.serverDocument.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.d("file name", Objects.requireNonNull(mValues.get(position).getFileName()));
                Log.d("check value", String.valueOf(holder.checkBox.isChecked()));
                Intent intent = new Intent("menuFunctionlityDisplay");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);

                Intent intent1 = new Intent("qrcodefloatingbutton");
                intent1.putExtra("qrCodeScanBtn", "InActive");
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent1);



               for(int i=0;i<holders.size();i++) {
                    ViewHolder holder = holders.get(i);
                    if (i == position) {
                        holder.checkBox.setVisibility(View.VISIBLE);
                        holder.documenticon.setVisibility(View.GONE);
                        holder.checkBox.setChecked(true);
                        holder.serverDocument.setBackgroundColor(Color.parseColor("#FFEEE5"));
                        if(!BottomNavigationActivityForServerPrint.selectedServerFile.isEmpty()) {
                            SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                            if(selectedFile.getFileName().equals(mValues.get(position).getFileName())){
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
                            }else{
                                BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                                BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                            }

                        }else{
                            BottomNavigationActivityForServerPrint.selectedServerFile.clear();
                            BottomNavigationActivityForServerPrint.selectedServerFile.add(mValues.get(position));
                        }

                        if(BottomNavigationActivityForServerPrint.selectedServerFile.size()>0) {
                            SelectedFile selectedFile = BottomNavigationActivityForServerPrint.selectedServerFile.get(0);
                            if (selectedFile.isFromApi() == true) {
                                if(selectedFile.getPrinterId()!=null) {
                                    PrintersFragment.Companion.getServerSecurePrinterForHeldJob().clear();
                                    if(selectedFile.getJobType().equals("secure_release")){
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
               // holder.Documenticonchanged();
            }

        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mIdView;
        public final TextView mContentView;
        public final ConstraintLayout serverDocument;
        public final ConstraintLayout checkboxanddocument;
        public final CheckBox checkBox;
        public SelectedFile mItem;
        public ImageView documenticon;
        List a =new ArrayList<View>();
        public ViewHolder(View view) {
            super(view);
            mView = view;
            mIdView = view.findViewById(R.id.item_number);
            mContentView = view.findViewById(R.id.content);
            serverDocument = view.findViewById(R.id.serverDocument);
            checkboxanddocument= view.findViewById(R.id.checkboxanddocument);
            checkBox = view.findViewById(R.id.checkbox);
            checkBox.setVisibility(View.GONE);
            documenticon =view.findViewById(R.id.documenticon);
/*

           documenticon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d( "document icon click", ":successfull");
                   // Documenticonchanged();
                }

            });

            serverDocument.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Log.d( "document click", ":successfull");
                //    Documenticonchanged();
                }

            });

*/
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }

        public void Documenticonchanged(){
            for(int i=0;i<holders.size();i++){
                ViewHolder holder1 = holders.get(i);
                holder1.checkBox.setVisibility(View.VISIBLE);
                holder1.documenticon.setVisibility(View.GONE);
            }
        }

    }


}