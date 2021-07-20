package com.printerlogic.printerlogic.print.ui.gallery;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class GalleryViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public GalleryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Service portal fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}