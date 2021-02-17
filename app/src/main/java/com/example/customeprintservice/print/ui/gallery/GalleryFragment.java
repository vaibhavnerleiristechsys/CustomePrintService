package com.example.customeprintservice.print.ui.gallery;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.customeprintservice.IconTreeItemHolder;
import com.example.customeprintservice.R;
import com.example.customeprintservice.prefs.LoginPrefs;
import com.example.customeprintservice.prefs.SignInCompanyPrefs;
import com.example.customeprintservice.print.PrintReleaseFragment;
import com.example.customeprintservice.printjobstatus.model.printerlist.Printer;
import com.example.customeprintservice.rest.ApiService;
import com.example.customeprintservice.rest.RetrofitClient;

import com.google.gson.Gson;
import com.unnamed.b.atv.model.TreeNode;
import com.unnamed.b.atv.view.AndroidTreeView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;
    private ViewGroup mContainer = null;
    Context context;
    public static List<Printer> listOfPrinters=new ArrayList<Printer>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_gallery, null, false);
        ViewGroup containerView = (ViewGroup) rootView.findViewById(R.id.container);
        context = rootView.getContext();
        //View root = inflater.inflate(R.layout.fragment_gallery, null, false);

        /*galleryViewModel =
                ViewModelProviders.of(this).get(GalleryViewModel.class);

        final TextView textView = root.findViewById(R.id.text_gallery);
        galleryViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });*/
        mContainer = container;
        serverCall(containerView);

        Intent intent = new Intent("qrcodefloatingbutton");
        intent.putExtra("qrCodeScanBtn", "InActive");
        LocalBroadcastManager.getInstance(requireContext()).sendBroadcast(intent);

        return rootView;
    }

    private void buildTree( ViewGroup containerView )
    {
        TreeNode root = TreeNode.root();
        TreeNode parent = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_folder, "MyParentNode"));
        TreeNode child0 = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_print,"ChildNode0"));
        TreeNode child1 = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_print,"ChildNode1"));
        parent.addChildren(child0, child1);
        root.addChild(parent);
        AndroidTreeView tView = new AndroidTreeView(getActivity(), root);
        containerView.addView(tView.getView());
    }

    private void serverCall(ViewGroup containerView)
    {
        TreeNode root = TreeNode.root();
        listOfPrinters.clear();
      //  TreeNode child = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_folder,"MyParentNode"));
       // root.addChild(child);
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
                    Map<Printer,TreeNode> mapPrinter2TreeNode = new HashMap<>();

                    listOfPrinters = response.body();
                    for(Printer printer:listOfPrinters) {
                        TreeNode child = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_folder,printer.getNode_title()));
                        mapPrinter2TreeNode.put(printer,child);
                        if(printer.getParent_id()==0) {
                            root.addChild(child);
                        }
                    }

                    for (Map.Entry<Printer,TreeNode> entry : mapPrinter2TreeNode.entrySet()) {
                        Printer printer = entry.getKey();
                        TreeNode parent = entry.getValue();
                        List<TreeNode> children = findChildren(printer.getId(), listOfPrinters, mapPrinter2TreeNode);
                        for(TreeNode child: children) {
                          //  child = new TreeNode(new IconTreeItemHolder.IconTreeItem(R.string.ic_print,child.));
                            parent.addChild(child);
                        }
                    }

                    AndroidTreeView tView = new AndroidTreeView(context, root);

                    tView.setDefaultAnimation(true);
                    tView.setDefaultContainerStyle(R.style.TreeNodeStyleCustom);
                    tView.setDefaultViewHolder(IconTreeItemHolder.class);
                    tView.setDefaultNodeClickListener(nodeClickListener);
                    tView.setDefaultNodeLongClickListener(nodeLongClickListener);

                    containerView.addView(tView.getView());

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

    private List<TreeNode> findChildren(Integer printerId, List<Printer> listOfPrinters,Map<Printer,TreeNode> mapPrinter2TreeNode)
    {
        List<TreeNode> children = new ArrayList<>();
        for(Printer printer: listOfPrinters)
        {
            if(printer.getParent_id()== printerId)
            {
                if(printer.getObject_sort_order() !=900) {
                    children.add(mapPrinter2TreeNode.get(printer));
                }

            }
        }

        return children;
    }

    private TreeNode.TreeNodeClickListener nodeClickListener = new TreeNode.TreeNodeClickListener() {
        @Override
        public void onClick(TreeNode node, Object value) {
            IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
           // status_bar.setText("Last clicked: " + item.text);
        }
    };

    private TreeNode.TreeNodeLongClickListener nodeLongClickListener = new TreeNode.TreeNodeLongClickListener() {
        @Override
        public boolean onLongClick(TreeNode node, Object value) {
            IconTreeItemHolder.IconTreeItem item = (IconTreeItemHolder.IconTreeItem) value;
            Toast.makeText(getActivity(), "Long click: " + item.text, Toast.LENGTH_SHORT).show();
            return true;
        }
    };

}