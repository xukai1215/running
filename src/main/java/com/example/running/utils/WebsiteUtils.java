package com.example.running.utils;

import java.net.HttpURLConnection;
import java.net.URL;

public class WebsiteUtils {

    public static int visitWebSite(String address){
        HttpURLConnection conn = null;
        try {
            URL realUrl = new URL(address);
            conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setUseCaches(false);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            conn.setInstanceFollowRedirects(false);
            conn.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64; rv:46.0) Gecko/20100101 Firefox/46.0");
            return conn.getResponseCode();
        }catch (Exception e){
            System.out.println("error");
        }
        return -1;
    }

}
