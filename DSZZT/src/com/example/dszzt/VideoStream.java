package com.example.dszzt;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;

import com.example.dszzt.network.ApiClient;
import com.example.dszzt.network.CustomImageDownaloder;
import com.example.dszzt.network.SZRApiClient;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.MySSLSocketFactory;
import com.loopj.android.http.PersistentCookieStore;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Menu;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.VideoView;

public class VideoStream extends Activity {
private final static int STREAM_FRAME_GAP = 1000; // klatka co sekunde
	
	String cookie;
	ImageView imageView;
	Timer streamTimer; //oszukamy w�tki i zrobimy sobie cyklicznego timera. To tak naprawd� te� jest w�tek, ale z troch� prostszym API
	
	
	//TimerTask to argument kt�rego potrzebuje timer. Mo�esz w nim nadpisa� funkcje run kt�ra sie odpali po X sekundach. Ta funkcja odpala si� w INNYM w�tku!
	TimerTask streamTimerTask = new TimerTask() {
		
		@Override
		public void run()
		{
			//Z racji �e tak jak pisa�em wy�ej to inny w�tek, to nie mo�na tutaj aktualizowac UI. Trzeba wykorzysta� konstrukcj� poni�ej, Handlery, albo Eventy (np z tej biblioteki EventBus kt�ra poleca�em)
			//Skoro to tylko przyk�ad to jedziemy jak najpro�ciej �eby CI nie miesza�
			runOnUiThread(new Runnable() {
				
				@Override
				public void run()
				{
					//ambitnie wywo�amy jedn� funkcj�. Mo�na by by�o wrzuci� ca�y kod kt�ry ma si� wykona�
					//tutaj, ale to by�oby ma�o eleganckie. W ogole ten timer i funckja downloadImage powinna by� w innej klasie
					//(ba, a idalnie by by�o zrobi� klase kt�ra dziedziczy po ImageView i specjalizuje si� w wy�wietlaniu
					//tych animacji. No ale to ju� zadanie dla Ciebie b�dzie na kiedys :))
					downloadImage();
				}
			});
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_video);
		
		imageView = (ImageView)findViewById(R.id.imageView);
	}

	@Override
	protected void onPause()
	{
		//timery, w�tki, i wszystko inne trzba zabij�c jak apka wchodzi w onPause! 
		//W przeciwnym wypadku aplikacja mo�e si� crashowa� np. podczas gadania przez telefon, albo chowania apki
		if(streamTimer != null)
		{
			streamTimer.cancel();
			streamTimer = null;
		}
		super.onPause();
	}
	
	private void downloadImage()
	{
		SZRApiClient.downloadImage("536870952", cookie, new BinaryHttpResponseHandler(){

			@Override
			public void onFailure(int statusCode, Header[] headers,
					byte[] binaryData, Throwable error)
			{
				Toast.makeText(VideoStream.this, "Pobieranie obrazu nie powiodlo si�", 500).show();
			}

			@Override
			public void onSuccess(byte[] binaryData)
			{
				Bitmap b = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
				//na ka�d� zmienn� kt�rej nie sprawdzisz czy nie jest jebanym nullem znajdzie si� jeden debil z telefonem
				//sci�gnietym z jakiego� trzeciego swiata kt�remu sko�czy si� pamiec, apka sie wywali, a on wielki kurwa kr�l napisze w markecie
				//ze aplikacja to g�wno, a programista to niedouczony peda� i b�dzie Ci smutno.
				//Polecam zawsze sprawdzac nulle.
				if(b!=null)
				{
					imageView.setImageBitmap(b);
				}
				else{
					Toast.makeText(VideoStream.this, "Dekodowanie obrazu niepowiodlo si�", 500).show();
				}
			}
			
		});
	}
	
	private void connectToStream()
	{
		//id kamery na sztywno. Ty sobie to juz jakos ogarniesz �eby wiedzie� kt�r� w��czasz
		SZRApiClient.createCameraStream("536870952", new AsyncHttpResponseHandler(){

			//a ty masz przyklad jak nienazywa� argument�w. Wyobra� sobie ze ta funkcja ma 300 lini (naawet pomimo tego ze za takie cos powinno si� zabiera� ludziom klawiatury)
			//skmin p�niej o chuj komu� chdzi�o z arg0;D
			@Override
			public void onFailure(int arg0, Header[] arg1, byte[] arg2,
					Throwable arg3)
			{
				Toast.makeText(VideoStream.this, "Tworzenie obiektu kamery sie nie powiodlo", 500).show();
			}

			@Override
			public void onSuccess(int arg0, Header[] arg1, byte[] arg2)
			{
				for(Header h : arg1)
				{
					if(h.getName().equalsIgnoreCase("Set-Cookie"))
					{
						cookie = h.getValue();

						streamTimer = new Timer();
						//jak sama nazwa mowi ka�emy wywo�a� mu si� w r�wnych odst�pach czasu. 0 jako parametr to op�nienie pierwszego startu
						//Tutaj normalnie mo�na by by�o zamiast streamTimerTask robi� new TimerTask(){ ... }, 
						//i tak zawsze robi�em (tak jak to wygl�da w wywo�aniu np tej funkcji createCameraStream()
						//ale odrazu Ci powiem �e to z�yyy pomys�. Masz p�niej pi�ciokrotne zagnierzd�enie kodu. 
						//czyta� si� tego nie da za cholere. A pozatym tworzenie takich zmiennych i callback�w jako elementy klasy
						//daje mozliwo�� zdziedziczenia tego p�niej i nadpisania. Przy takim zapisie jak createCameraStrem() musisz nadpisywa� caaaa�� funkcje. Lipa!
						streamTimer.scheduleAtFixedRate(streamTimerTask, 0, STREAM_FRAME_GAP);
					}
				}
			}
		});
	}

	@Override
	protected void onResume()
	{
		//przy wznawianiu aplikacji startujemy stream
		connectToStream();
		
		//�mieszna sprawa. Nadpisuj�c onResume, onPause itp. i nie wywo�uj�c funkcji z oryginalnej klasy system rzuci b��d
		//m�wi�cy w wolnym t�umaczeniu dok�adnie to nie wywo�a�e� funkcji super.onXXX(). 
		//Skoro system potrafi stwierdzi� �e tego nie zrobi�es, to m�g�by to zrobi� za Ciebie zamiast wywala� Ci apke.
		//No ale to� na bank mia� zajebisty pow�d �eby tego tak nie zrobi� nie ? ...
		super.onResume();
	}
	//Komentarzy wysz�o wi�cej ni� kodu. Mi�ej lektury
}


