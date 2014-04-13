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
	Timer streamTimer; //oszukamy w¹tki i zrobimy sobie cyklicznego timera. To tak naprawdê te¿ jest w¹tek, ale z trochê prostszym API
	
	
	//TimerTask to argument którego potrzebuje timer. Mo¿esz w nim nadpisaæ funkcje run która sie odpali po X sekundach. Ta funkcja odpala siê w INNYM w¹tku!
	TimerTask streamTimerTask = new TimerTask() {
		
		@Override
		public void run()
		{
			//Z racji ¿e tak jak pisa³em wy¿ej to inny w¹tek, to nie mo¿na tutaj aktualizowac UI. Trzeba wykorzystaæ konstrukcjê poni¿ej, Handlery, albo Eventy (np z tej biblioteki EventBus która poleca³em)
			//Skoro to tylko przyk³ad to jedziemy jak najproœciej ¿eby CI nie mieszaæ
			runOnUiThread(new Runnable() {
				
				@Override
				public void run()
				{
					//ambitnie wywo³amy jedn¹ funkcjê. Mo¿na by by³o wrzuciæ ca³y kod który ma siê wykonaæ
					//tutaj, ale to by³oby ma³o eleganckie. W ogole ten timer i funckja downloadImage powinna byæ w innej klasie
					//(ba, a idalnie by by³o zrobiæ klase która dziedziczy po ImageView i specjalizuje siê w wyœwietlaniu
					//tych animacji. No ale to ju¿ zadanie dla Ciebie bêdzie na kiedys :))
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
		//timery, w¹tki, i wszystko inne trzba zabij¹c jak apka wchodzi w onPause! 
		//W przeciwnym wypadku aplikacja mo¿e siê crashowaæ np. podczas gadania przez telefon, albo chowania apki
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
				Toast.makeText(VideoStream.this, "Pobieranie obrazu nie powiodlo siê", 500).show();
			}

			@Override
			public void onSuccess(byte[] binaryData)
			{
				Bitmap b = BitmapFactory.decodeByteArray(binaryData, 0, binaryData.length);
				//na ka¿d¹ zmienn¹ której nie sprawdzisz czy nie jest jebanym nullem znajdzie siê jeden debil z telefonem
				//sci¹gnietym z jakiegoœ trzeciego swiata któremu skoñczy siê pamiec, apka sie wywali, a on wielki kurwa król napisze w markecie
				//ze aplikacja to gówno, a programista to niedouczony peda³ i bêdzie Ci smutno.
				//Polecam zawsze sprawdzac nulle.
				if(b!=null)
				{
					imageView.setImageBitmap(b);
				}
				else{
					Toast.makeText(VideoStream.this, "Dekodowanie obrazu niepowiodlo siê", 500).show();
				}
			}
			
		});
	}
	
	private void connectToStream()
	{
		//id kamery na sztywno. Ty sobie to juz jakos ogarniesz ¿eby wiedzieæ któr¹ w³¹czasz
		SZRApiClient.createCameraStream("536870952", new AsyncHttpResponseHandler(){

			//a ty masz przyklad jak nienazywaæ argumentów. WyobraŸ sobie ze ta funkcja ma 300 lini (naawet pomimo tego ze za takie cos powinno siê zabieraæ ludziom klawiatury)
			//skmin póŸniej o chuj komuœ chdzi³o z arg0;D
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
						//jak sama nazwa mowi ka¿emy wywo³aæ mu siê w równych odstêpach czasu. 0 jako parametr to opóŸnienie pierwszego startu
						//Tutaj normalnie mo¿na by by³o zamiast streamTimerTask robiæ new TimerTask(){ ... }, 
						//i tak zawsze robi³em (tak jak to wygl¹da w wywo³aniu np tej funkcji createCameraStream()
						//ale odrazu Ci powiem ¿e to z³yyy pomys³. Masz póŸniej piêciokrotne zagnierzd¿enie kodu. 
						//czytaæ siê tego nie da za cholere. A pozatym tworzenie takich zmiennych i callbacków jako elementy klasy
						//daje mozliwoœæ zdziedziczenia tego póŸniej i nadpisania. Przy takim zapisie jak createCameraStrem() musisz nadpisywaæ caaaa³¹ funkcje. Lipa!
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
		
		//œmieszna sprawa. Nadpisuj¹c onResume, onPause itp. i nie wywo³uj¹c funkcji z oryginalnej klasy system rzuci b³¹d
		//mówi¹cy w wolnym t³umaczeniu dok³adnie to nie wywo³a³eœ funkcji super.onXXX(). 
		//Skoro system potrafi stwierdziæ ¿e tego nie zrobi³es, to móg³by to zrobiæ za Ciebie zamiast wywalaæ Ci apke.
		//No ale toœ na bank mia³ zajebisty powód ¿eby tego tak nie zrobiæ nie ? ...
		super.onResume();
	}
	//Komentarzy wysz³o wiêcej ni¿ kodu. Mi³ej lektury
}


