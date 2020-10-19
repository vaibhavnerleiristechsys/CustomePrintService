package com.example.customeprintservice.print

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.*
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.customeprintservice.R
import com.example.customeprintservice.adapter.FragmentSelectedFileListAdapter
import com.example.customeprintservice.jipp.FileUtils
import com.example.customeprintservice.jipp.PrinterDiscoveryActivity
import com.example.customeprintservice.model.FileAttributes
import com.example.customeprintservice.utils.PermissionHelper
import com.example.customeprintservice.utils.Permissions
import kotlinx.android.synthetic.main.fragment_print_release.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class PrintReleaseFragment : Fragment() {

    private var permissionsHelper: PermissionHelper? = null
    private val bundle = Bundle()
    private var isFileSelected: Boolean = false
    private var list = ArrayList<String>()
//    private val rxPermissions = RxPermissions(this)

    private var adapter:FragmentSelectedFileListAdapter?= null
    private var toolbar:Toolbar?= null
    private var textToolbar:TextView?= null
    private var backButton :ImageButton? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_print_release, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (requireActivity() as AppCompatActivity).supportActionBar?.hide()
        checkPermissions()

        btnFragmentSelectDoc.setOnClickListener {
            if (Permissions().checkAndRequestPermissions(context as Activity)) {
                val i = Intent(
                    Intent.ACTION_GET_CONTENT,
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                )
                i.type = "*/*"
                startActivityForResult(i, 1)

            } else {
                Toast.makeText(context as Activity, "Please accept Permissions", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        btnFragmentPrintReleaseNext.setOnClickListener {
            if (isFileSelected && list.size > 0) {
                val intent = Intent(context, PrinterDiscoveryActivity::class.java)
                intent.putExtras(bundle)
                startActivity(intent)
            } else {
                Toast.makeText(context, "Select the Document", Toast.LENGTH_SHORT).show()
            }

        }

    }




    private fun checkPermissions() {
        permissionsHelper = PermissionHelper()
        permissionsHelper!!.checkAndRequestPermissions(
            context as Activity,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionsHelper!!.onRequestPermissionsResult(
            context as Activity,
            requestCode,
            permissions,
            grantResults
        )
    }

    @SuppressLint("SimpleDateFormat")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1 && resultCode == AppCompatActivity.RESULT_OK) {
            val uri: Uri = data?.data!!
            val realPath = FileUtils.getPath(context as Activity, uri)
            val file: File = File(realPath)

            val fileAttribute = FileAttributes()
            fileAttribute.fileName = file.name
            fileAttribute.fileRealPath = realPath
            fileAttribute.fileSize = file.length() / 1024
            val c = Calendar.getInstance()
            val df = SimpleDateFormat("dd-MM HH:mm ")
            val formattedDate: String = df.format(c.time)
            fileAttribute.fileSelectedDate = formattedDate

            list.add(realPath)
            listUpdate(list, context as Activity)
            isFileSelected = true
            bundle.putStringArrayList("selectedFileList", list)
            Log.i("printer", "file choosed-->$file")
            Log.i("printer", "list of Files-->$list")
        }
    }

    @SuppressLint("WrongConstant")
    private fun listUpdate(list: ArrayList<String>, context: Context) {
        val recyclerViewDocumentList =
            view?.findViewById<RecyclerView>(R.id.recyclerViewDocumentList)
        recyclerViewDocumentList?.layoutManager =
            LinearLayoutManager(
                context,
                LinearLayout.VERTICAL,
                false
            )

         adapter = FragmentSelectedFileListAdapter(
             context as Activity,
             list
         )
        recyclerViewDocumentList?.adapter = adapter


        adapter?.setListener(object :
            FragmentSelectedFileListAdapter.ViewHolder.FragmentSelectedFileAdapterListener {
            override fun onItemClick(position: Int) {
//                enableActionMode(position, context)
            }

            override fun onItemLongClick(position: Int) {
//                enableActionMode(position, context)
            }
        })
    }

    private var actionMode: ActionMode? = null

    private fun enableActionMode(position: Int, context: Context) {

        if (actionMode == null)
            actionMode = AppCompatActivity().startSupportActionMode(object : ActionMode.Callback {
                override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                    mode.menuInflater.inflate(R.menu.menu_delete, menu)
                    return true
                }

                override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                    return false
                }

                override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                    if (item.itemId == R.id.action_delete) {

                        mode.finish()
                        return true
                    }
                    return false
                }

                override fun onDestroyActionMode(mode: ActionMode) {

                    adapter?.notifyDataSetChanged()
                    actionMode = null
                }
            })

    }

}

//https://www.youtube.com/watch?v=vPLKNsQEAEc
//https://www.youtube.com/watch?v=nC9E9dvw2eY