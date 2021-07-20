package com.printerlogic.printerlogic.print.ui.share;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ShareViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public ShareViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is print fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}