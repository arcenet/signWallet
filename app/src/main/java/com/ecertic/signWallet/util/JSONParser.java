package com.ecertic.signWallet.util;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;

public class JSONParser {

        static InputStream is = null;
        static JSONObject jObj = null;
        static String json = "";

        // constructor
        public JSONParser() {

        }

        public JSONObject getJSONFromUrl(String url) throws JSONException {

            // Making HTTP request
            try {
                // defaultHttpClient
                DefaultHttpClient httpClient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet();
                URI uri = new URI(url);

                httpGet.setURI(uri);


                HttpResponse httpResponse = httpClient.execute(httpGet);
                HttpEntity httpEntity = httpResponse.getEntity();
                is = httpEntity.getContent();

            } catch (IOException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(
                        is, "iso-8859-1"), 8);
                StringBuilder sb = new StringBuilder();
                String line = null;
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("n");
                }
                is.close();
                Log.d("Result", sb.toString());
                json = sb.toString();
            } catch (Exception e) {
                Log.e("Buffer Error", "Error converting result " + e.toString());
            }

            // try parse the string to a JSON object
            jObj = new JSONObject(json);

            // return JSON String
            return jObj;

        }
}

