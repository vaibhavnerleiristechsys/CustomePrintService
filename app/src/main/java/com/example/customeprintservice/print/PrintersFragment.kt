package com.example.customeprintservice.print

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.FragmentPrinterListAdapter
import com.example.customeprintservice.jipp.PrinterList
import com.example.customeprintservice.jipp.PrinterModel
import com.example.customeprintservice.prefs.LoginPrefs
import com.example.customeprintservice.rest.ApiService
import com.example.customeprintservice.rest.RetrofitClient
import com.example.customeprintservice.utils.Inet
import com.example.customeprintservice.utils.ProgressDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.android.synthetic.main.fragment_printers.*
import okhttp3.ResponseBody
import org.jetbrains.anko.doAsync
import org.jsoup.Jsoup
import org.jsoup.parser.Parser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.InetAddress

class PrintersFragment : Fragment() {

    val printerList = ArrayList<PrinterModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_printers, container, false)
    }

    @SuppressLint("WrongConstant")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btnAddManuallyPrinter.setOnClickListener {
//            dialogAddManualPrinter()
            dialogSelectPrinter()
        }
        updateUi()
        getPrinterList(requireContext())
        Log.i("printer", "Login okta token" + LoginPrefs.getOCTAToken(requireContext()))
    }

    @SuppressLint("WrongConstant")
    private fun updateUi() {
        val recyclerViewPrinterLst =
            view?.findViewById<RecyclerView>(R.id.recyclerViewFragmentPrinterList)

        recyclerViewPrinterLst?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )
        val adapter = FragmentPrinterListAdapter(
            context as Activity,
            PrinterList().printerList
        )
        recyclerViewPrinterLst?.adapter = adapter
    }


    fun updatePrinterDialog() {

    }

    fun getPrinterList(
        context: Context
    ) {
        val BASE_URL =
            "https://devncookta.printercloud.com/client/gateway.php/"

        val apiService = RetrofitClient(context)
            .getRetrofitInstance(BASE_URL)
            .create(ApiService::class.java)

        val call = apiService.getPrinterList(
            "devncookta",
            "Bearer ${LoginPrefs.getOCTAToken(requireContext())}",
            "ranjeeta.balakrishnan@devnco.co",
            "saml2",
            "Okta",
            "1",
            "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" +
                    "<system driverless=\"1\">\n" +
                    "  <machine>\n" +
                    "    <ips>\n" +
                    "      <ip mask=\"255.255.255.0\"> my-ip </ip>\n" +
                    "    </ips>\n" +
                    "  </machine>\n" +
                    "  <idp>\n" +
                    "    {\"idpName\": \"Okta\",\n" +
                    "      \"username\": \"ranjeeta.balakrishnan@devnco.co\",\n" +
                    "      \"isLoggedIn\": \"true\",\n" +
                    "      \"type\": \"auth-type\",\n" +
                    "      \"token\": \"eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJqdGkiOiIyMjMxN2RlMC05NDRkLTRhNjItOGIxNy03YjYyZWQ5OGM5Y2EiLCJpZHAiOiJPa3RhIiwic2l0ZSI6ImRldm5jb29rdGEiLCJ1c2VyIjoicmFuamVldGEuYmFsYWtyaXNobmFuQGRldm5jby5jbyIsInNlc3Npb24iOiJhOGI0NmE1Yi0wYWFlLTQ4ZjUtOWUxMS04NTM5YzljZDdkMTIiLCJleHAiOjE2MzkyMTQ0MzgsImlhdCI6MTYwNzY3ODQzOCwiaXNzIjoiY29tLnByaW50ZXJsb2dpYy5zZXJ2aWNlcy5hdXRobiIsImF1ZCI6ImNvbS5wcmludGVybG9naWMuY2xpZW50cy5kZXNrdG9wLmlkcCJ9.HKiyYRd0QNql6zRsz276L6nGgiQG0GHcYpA6s6h7dOZQoAJZI5G5nZfdPARUEX3vvnEqpy4E8xDrKepk24SoKOQB4dXoSfwg0B6D1B5sz7Dl8Pf6D0N0wvXQl9cEC2LNpv3WqI_qXPYXS6ihO926XSa6f7mo2j3pwmzPZkrO_Q8PSaAjNoXhfCgVXh4oDApTb8A-kO7D67ky9w-GjoMfLdieVqoD1DcWMKkGfFKIdAHDWsEuxamR7xvmtBVvtNnOKIEAxKwf_SqL2JDpMt4PEqvcGd1Cp2_WqREHpq5UG1t0go52PCY7YqCt9e6AypWE0KcxbOo9uoauXKIn5e95sA\"}\n" +
                    "  </idp>\n" +
                    "  <memberships>\n" +
                    "    <computer />\n" +
                    "    <user>\n" +
                    "      <guid>S-1-1-0</guid>\n" +
                    "    </user>\n" +
                    "  </memberships>\n" +
                    "</system>"

        )

        call.enqueue(object : Callback<ResponseBody> {

            @RequiresApi(Build.VERSION_CODES.N)
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                ProgressDialog.cancelLoading()
                if (response.isSuccessful) {
                    try {
                        val html = response.body()?.string()

                        val document = Jsoup.parse(html, "", Parser.xmlParser())
                        val element = document.select("command")
                        val inetAddress = InetAddress.getByName("192.168.1.1")

                        PrinterList().printerList.removeIf {
                            it.fromServer == true
                        }

                        element.forEach {
                            val printerModel: PrinterModel = PrinterModel()
                            printerModel.serviceName = it.text()
                            printerModel.printerHost = inetAddress
                            printerModel.printerPort = 631
                            printerModel.fromServer = true
                            Log.i("printer", "html res=>${it.text()}")
                            PrinterList().addPrinterModel(printerModel)
                        }

                    } catch (e: Exception) {
                        Log.i("printer", "e=>${e.message.toString()}")
                    }
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                ProgressDialog.cancelLoading()
                Log.i("printer", "Error html response==>${t.message.toString()}")
            }
        })
    }

    @SuppressLint("WrongConstant")
     fun dialogSelectPrinter() {
        val dialog = Dialog(context as Activity)
        dialog.setContentView(R.layout.dialog_select_printer)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        val window = dialog.window
        window!!.setLayout(
            AbsListView.LayoutParams.MATCH_PARENT,
            AbsListView.LayoutParams.MATCH_PARENT
        )
//        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        //        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        val wlp = window.attributes
        wlp.gravity = Gravity.BOTTOM
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
        window.setDimAmount(0.5f)
        window.attributes = wlp

        val printerRecyclerView =
            dialog.findViewById<RecyclerView>(R.id.dialogSelectPrinterRecyclerView)
        val imgCancel = dialog.findViewById<ImageView>(R.id.imgDialogSelectPrinterCancel)
        val floatButton =
            dialog.findViewById<FloatingActionButton>(R.id.dialogSelectPrinterFloatingButton)

        printerRecyclerView?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )
        val adapter = FragmentPrinterListAdapter(
            context as Activity,
            PrinterList().printerList
        )
        printerRecyclerView?.adapter = adapter
        dialog.show()

        imgCancel.setOnClickListener {
            dialog.cancel()
        }

        floatButton.setOnClickListener {
            Toast.makeText(requireContext(), "Click on float btn", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("WrongConstant")
    private fun dialogAddManualPrinter() {
        val dialog = Dialog(context as Activity)
        dialog.setContentView(R.layout.dialog_add_manual_printer)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        dialog.window?.setLayout((6 * width) / 7, WindowManager.LayoutParams.WRAP_CONTENT)

        val edtAddManualPrinter = dialog.findViewById<EditText>(R.id.edtDialogAddManualPrinter)
        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnAddPrinterManually = dialog.findViewById<Button>(R.id.btnAddPrinter)
        btnCancel.setOnClickListener { dialog.dismiss() }

        val printer: PrinterModel = PrinterModel()

        btnAddPrinterManually.setOnClickListener {
            if (Inet.validIP(edtAddManualPrinter.text.toString())) {
                try {
                    var inetAddress: InetAddress? = null

                    doAsync {
                        inetAddress = InetAddress.getByName(edtAddManualPrinter.text.toString())
                    }
                    Thread.sleep(100)
                    if (inetAddress != null) {
                        printer.printerHost = inetAddress
                        printer.serviceName = "" + inetAddress
                        printer.printerPort = 631
                        Log.i("printer", "innet Address->" + inetAddress)
                    }

                } catch (e: Exception) {
                    Log.i("printer", e.toString())
                }

                var flagIsExist: Boolean = false

                PrinterList().printerList.forEach {
                    if (it.printerHost.equals(printer.printerHost)) {
                        flagIsExist = true
                    }
                }

                if (!flagIsExist) {
                    PrinterList().addPrinterModel(printer)
                    Toast.makeText(context, "Printer Added", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                    updateUi()
                } else {
                    Toast.makeText(context, "Unable to add Printer", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "IP is not valid", Toast.LENGTH_SHORT).show()
            }
        }
    }
}