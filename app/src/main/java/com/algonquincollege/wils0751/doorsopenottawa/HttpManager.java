package com.algonquincollege.wils0751.doorsopenottawa;

import android.util.Base64;
import android.util.Log;

import com.algonquincollege.wils0751.doorsopenottawa.parsers.HttpMethod;
import com.algonquincollege.wils0751.doorsopenottawa.parsers.RequestPackage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;



/**
 * Manage HTTP connections.
 * <p>
 * Supported methods:
 * + getData() :: String
 *
 * @author David Gassner
 */

public class HttpManager {

    /**
     * Return the HTTP response from uri
     *
     * @param uri Uniform Resource Identifier
     * @return String the response; null when exception
     */
    public static String getData(RequestPackage p, String userName, String password) {

        BufferedReader reader = null;
        HttpURLConnection con = null;
       // RequestPackage p = null;

       String  uri = p.getUri();
        if (p.getMethod() == HttpMethod.GET) {
            uri += "?" + p.getEncodedParams();
        }


        byte[] loginBytes = (userName + ":" + password).getBytes();
        StringBuilder loginBuilder = new StringBuilder()
                .append("Basic ")
                .append(Base64.encodeToString(loginBytes, Base64.DEFAULT));


        try {
            URL url = new URL(uri);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod(p.getMethod().toString());

            con.addRequestProperty("Authorization", loginBuilder.toString());

            JSONObject json = new JSONObject(p.getParams());
            String params = json.toString();

            if (p.getMethod() == HttpMethod.POST || p.getMethod() == HttpMethod.PUT) {
                con.addRequestProperty("Accept", "application/json");
                con.addRequestProperty("Content-Type", "application/json");
                con.setDoOutput(true);
                OutputStreamWriter writer = new OutputStreamWriter(con.getOutputStream());
                writer.write(params);
                writer.flush();
            }

            StringBuilder sb = new StringBuilder();
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
                try {
                    int status = con.getResponseCode();
                } catch (IOException el) {
                    el.printStackTrace();
                }
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
    }

}