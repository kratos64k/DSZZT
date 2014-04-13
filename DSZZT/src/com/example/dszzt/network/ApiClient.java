package com.example.dszzt.network;

import java.security.KeyStore;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.loopj.android.http.RequestParams;

public class ApiClient {
	private static final String BASE_URL = "https://szr.szczecin.pl";
	private static AsyncHttpClient client = new AsyncHttpClient();
	private static AsyncHttpClient sslClient = new AsyncHttpClient();


	public static void setCoockieStore(PersistentCookieStore store)
	{
		client.setCookieStore(store);
	}
	public static void get(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler)
	{
		client.setTimeout(900000000);
		client.setEnableRedirects(true);
		client.addHeader("Accept", "*/*");
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}
	public static void getXML(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler)
	{
		client.get(getAbsoluteUrl(url), params, responseHandler);
	}

	public static void getSsl(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler, String cookie)
	{
		
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			MySSLSocketFactory socketFactory = new MySSLSocketFactory(trustStore);
			socketFactory.setHostnameVerifier(MySSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			sslClient.setTimeout(30*1000);
			sslClient.addHeader("Accept", "*/*");
			if(cookie != null)
			{
				sslClient.addHeader("Cookie", cookie);
			}
			sslClient.setSSLSocketFactory(socketFactory);
			sslClient.get(getAbsoluteUrl(url), params, responseHandler);
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void post(String url, RequestParams params,
			AsyncHttpResponseHandler responseHandler)
	{
		client.post(getAbsoluteUrl(url), params, responseHandler);
	}
	
	private static String getAbsoluteUrl(String relativeUrl)
	{
		return BASE_URL + relativeUrl;
	}
}