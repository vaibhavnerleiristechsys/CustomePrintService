package com.printerlogic.printerlogic.utils;


import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.printerlogic.printerlogic.prefs.LoginPrefs;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class GoogleAPI {
    static String clientId;
    static String client_secret;


    public static void getGoogleData(Context context) {


        RequestQueue queue = Volley.newRequestQueue(context);
        String url = "https://googleid.printercloud.com/api/idp";
       JsonArrayRequest jsonObjectRequest = new JsonArrayRequest (Request.Method.GET,url,null,
               new Response.Listener<JSONArray>() {
                   @Override
                   public void onResponse(JSONArray response) {

                       JSONArray GoogleApiData=response;
                       Log.d("google api data:",GoogleApiData.toString());
                       JSONObject jsonObject = null;
                       try {
                           jsonObject = GoogleApiData.getJSONObject(0);
                            clientId =  jsonObject.getString("server_id");
                           client_secret=jsonObject.getString("server_secret");
                           LoginPrefs.Companion.saveClientId(context,clientId);
                           LoginPrefs.Companion.saveClientSecret(context,client_secret);


                           Log.d("client_id:",clientId);


                       } catch (JSONException e) {
                           e.printStackTrace();
                       }



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
                    String s1="PrinterLogicIdpAuthentication"+formattedDate;
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


    public void getData(String code, String requestUri,Context context){
        String url = LoginPrefs.Companion.getgoogleTokenUrl(context);
        RequestQueue queue = Volley.newRequestQueue(context);
        String serverSecret = LoginPrefs.Companion.getClientSecret(context);
        StringRequest jsonObjRequest = new StringRequest(
                Request.Method.POST,
                url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("google api response",response.toString());
                        Gson g = new Gson();
                        JsonObject jsonObject = g.fromJson(response, JsonObject.class);
                        JsonElement idToken=jsonObject.get("id_token");
                        Log.d("idToken",idToken.toString());
                        LoginPrefs.Companion.saveOctaToken(context, idToken.getAsString());


                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("volley", "Error: " + error.getMessage());
                        error.printStackTrace();

                    }
                }) {

            @Override
            public String getBodyContentType() {
                return "application/x-www-form-urlencoded; charset=UTF-8";
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("code", code);
                params.put("client_id", clientId);
                params.put("client_secret",serverSecret);
                params.put("redirect_uri",requestUri);
                params.put("grant_type","authorization_code");
                return params;
            }

        };
        queue.add(jsonObjRequest);

    }


}

