package com.printerlogic.printerlogic;

import androidx.annotation.NonNull;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

public class DialogActivity extends Dialog {

    public DialogActivity(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}