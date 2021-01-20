package com.example.customeprintservice.print.ui.gallery;

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

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class GalleryFragment extends Fragment {

    private GalleryViewModel galleryViewModel;
    private ViewGroup mContainer = null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_gallery, null, false);
        ViewGroup containerView = (ViewGroup) rootView.findViewById(R.id.container);

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

        String url = "https://gw.app.printercloud.com/devncookta/tree/api/node/";
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
                    List<Printer> listOfPrinters = response.body();
                    for(Printer printer:listOfPrinters) {
                        TreeNode child = new TreeNode(printer.getNode_title());
                        root.addChild(child);
                    }

                    AndroidTreeView tView = new AndroidTreeView(getActivity(), root);
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
}