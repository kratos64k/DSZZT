package com.example.dszzt;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	
	//zmienilem troche formatowanie tej funckji. Staraj sie zachowac jeden punkt wejscia i jeden punkt wyjscia z funkcji.
	//W tym wypadaku wyjsciem jest odpalanie intentu. robienie tego w 3 meijscach jest troche nieeleganckie :D
	public void onClick(View v) 
	{						
		Intent intentToStart = null;

		switch (v.getId())
		{
			case R.id.manual:
				intentToStart = new Intent(this, ManualMode.class);
				break;
			case R.id.automat:
				intentToStart = new Intent(this, Automat_mode.class);
				break;
			case R.id.videostream:
				intentToStart = new Intent(this,VideoStream.class);

		}
		
		if(intentToStart != null)
		{
			startActivity(intentToStart);
		}
	}
	public void activityButtonClick(final View target) {
		finish();
	}
}
