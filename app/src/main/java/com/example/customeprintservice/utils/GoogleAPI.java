package com.example.customeprintservice.utils;


import android.content.Context;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class GoogleAPI {


   public static void getGoogleData(Context context) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://googleid.printercloud.com/api/idp";
       JsonArrayRequest jsonObjectRequest = new JsonArrayRequest (Request.Method.GET,url,null,
               new Response.Listener<JSONArray>() {
                   @Override
                   public void onResponse(JSONArray response) {

                       JSONArray GoogleApiData=response;
                       Log.d("google api data:",GoogleApiData.toString());
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Log.d("ERROR", "error => " + error.toString());
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                StringBuilder sb = new StringBuilder();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
                String formattedDate = sdf.format(new Date());

                try {
                    Log.d("formattedDate",formattedDate);
                    String s1="PrinterLogicIdpAuthentication "+formattedDate;
                    Log.d("s1",s1);
                    MessageDigest md = MessageDigest.getInstance("SHA-512");
                    byte[] digest = md.digest(s1.getBytes());

                    for (int i = 0; i < digest.length; i++) {
                        sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
                    }

                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                Map<String, String> params = new HashMap<String, String>();
                params.put("x-printerlogic-hash", sb.toString());
                params.put("x-printerlogic-datetime", formattedDate);

                return params;
            }
        };
        queue.add(jsonObjectRequest);
    }


   /*public static void generateSHA512() throws NoSuchAlgorithmException {
       SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
       String formattedDate = sdf.format(new Date());
       Log.d("formattedDate",formattedDate);
       String s1="PrinterLogicIdpAuthentication "+formattedDate;
       Log.d("s1",s1);
        MessageDigest md = MessageDigest.getInstance("SHA-512");
        byte[] digest = md.digest(s1.getBytes());
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }
       Log.d("SHA512",sb.toString());
    }*/
}
