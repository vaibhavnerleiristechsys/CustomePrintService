package com.printerlogic.printerlogic.utils

import android.app.ProgressDialog
import android.content.Context

class ProgressDialog {

    companion object{
        private var progressDialog: ProgressDialog? = null
        fun showLoadingDialog(context: Context, message: String) {
            if (!(progressDialog != null && progressDialog!!.isShowing)) {
                progressDialog = ProgressDialog(context)
                progressDialog!!.setMessage(message)
                progressDialog!!.setCancelable(false)
                progressDialog!!.setCanceledOnTouchOutside(false)
                progressDialog!!.show()
            }
        }

        fun cancelLoading() {
            if (progressDialog != null && progressDialog!!.isShowing)
                progressDialog!!.cancel()
        }
    }
}