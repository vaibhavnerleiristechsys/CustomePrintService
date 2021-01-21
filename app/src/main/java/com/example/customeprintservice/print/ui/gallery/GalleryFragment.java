package com.example.customeprintservice.print.ui.gallery;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

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
        return rootView;
    }

    private void buildTree( ViewGroup containerView )
    {
        TreeNode root = TreeNode.root();
        TreeNode parent = new TreeNode("MyParentNode");
        TreeNode child0 = new TreeNode("ChildNode0");
        TreeNode child1 = new TreeNode("ChildNode1");
        parent.addChildren(child0, child1);
        root.addChild(parent);
        AndroidTreeView tView = new AndroidTreeView(getActivity(), root);
        containerView.addView(tView.getView());
    }

    private void serverCall(ViewGroup containerView)
    {
        TreeNode root = TreeNode.root();
        TreeNode child = new TreeNode("MyParentNode");
        root.addChild(child);
        String siteId=LoginPrefs.Companion.getSiteId(requireContext());
        String url = "https://gw.app.printercloud.com/"+siteId+"/tree/api/node/";
        ApiService apiService = new RetrofitClient(requireContext())
                .getRetrofitInstance(url)
                .create(ApiService.class);

        PrintReleaseFragment prf = new PrintReleaseFragment();


        Call call = apiService.getPrintersList(
                "Bearer " + LoginPrefs.Companion.getOCTAToken(requireContext()),
                prf.decodeJWT(requireContext()),
                SignInCompanyPrefs.Companion.getIdpType(requireContext()).toString(),
                SignInCompanyPrefs.Companion.getIdpName(requireContext()).toString()
        );

        call.enqueue(new Callback<List<Printer>>() {
            public void onResponse(Call<List<Printer>> call, Response<List<Printer>> response) {
                if(response.isSuccessful())
                {
                    Map<Printer,TreeNode> mapPrinter2TreeNode = new HashMap<>();

                    List<Printer> listOfPrinters = response.body();
                    for(Printer printer:listOfPrinters) {
                        TreeNode child = new TreeNode(printer.getNode_title());
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
                            parent.addChild(child);
                        }
                    }

                    AndroidTreeView tView = new AndroidTreeView(context, root);
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
                children.add(mapPrinter2TreeNode.get(printer));
            }
        }

        return children;
    }
}