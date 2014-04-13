package com.example.dszzt.network;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.PersistentCookieStore;

public class SZRApiClient 
{
    public static void getVMSPublicData(AsyncHttpResponseHandler handler)
    {
        ApiClient.getXML("/utms/data/layers/VMSPublic", null, handler);
    }
    public static void getIndex(PersistentCookieStore cookie, AsyncHttpResponseHandler handler)
    {
    	ApiClient.setCoockieStore(cookie);
        ApiClient.get("/utms/index", null, handler);
    }
    
    public static void createCameraStream(String cameraId, AsyncHttpResponseHandler handler)
    {
        ApiClient.getSsl(String.format("/utms/rtv?method=createImageConsumer&cameraId=%s&_dc=1397156967098", cameraId), null, handler, null);
    }
    
    public static void downloadImage(String cameraId, String cookie, BinaryHttpResponseHandler handler)
    {
        ApiClient.getSsl(String.format("/utms/rtvServlet?method=showImageSync&cameraId=%s", cameraId), null, handler, cookie);
    }
}
