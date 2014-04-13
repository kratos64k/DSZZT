package com.example.dszzt;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import android.app.ActivityManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPathExpressionException;

import org.apache.http.Header;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.example.dszzt.network.SZRApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.loopj.android.http.AsyncHttpResponseHandler;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.FIFOLimitedMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;

import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;


public class ManualMode extends Activity {
	private static final LatLng SZCZECIN = new LatLng(53.422479,14.559985);
	static final LatLng BramaPortowa = new LatLng(53.42485, 14.54833);
	static final LatLng Gdanska = new LatLng(53.41247, 14.57750);
	static final LatLng GdanskaEstakada = new LatLng(53.41141, 14.58048);
	static final LatLng PionierowMeteo = new LatLng(53.58048, 14.60949);
	static final LatLng Eskadrowa = new LatLng(53.38961, 14.62120);
	static final LatLng Hangarowa = new LatLng(53.38575, 14.64352);
	static final LatLng Struga = new LatLng(53.38446, 14.65167);
	static final LatLng Struga2 = new LatLng(53.38429, 14.65092);
	Hashtable<Integer, String> images;
	protected ImageLoader imageLoader1 = ImageLoader.getInstance();
	
	private GoogleMap map;
	protected Marker marker;
    protected Hashtable<String, String> markers;
    protected ImageLoader imageLoader;
    protected DisplayImageOptions options;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
   
        images = new Hashtable<Integer, String>();
        setContentView(R.layout.activity_manual_mode);
        initImageLoader();
        loadData();
    }
	public void startMap()
	{
		markers = new Hashtable<String, String>();
        GoogleMap map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        map.getUiSettings().setMyLocationButtonEnabled(true);
        
        Marker VMSBoard5 = map.addMarker(new MarkerOptions()
                                  .position(BramaPortowa)
                                  .title("Brama Portowa")
                                  .snippet("Tutaj sparsowane dane"));
        Marker VMSBoard4 = map.addMarker(new MarkerOptions()
							       .position(Gdanska)
							       .title("Gdañska - Stocznia Parnica")
						           .snippet("Tutaj sparsowane dane"));
        Marker VMSSign8 = map.addMarker(new MarkerOptions()
							       .position(GdanskaEstakada)
							       .title("Gdañska - Estakada")
						           .snippet("Tutaj sparsowane dane"));
        Marker VMSBoard3 = map.addMarker(new MarkerOptions()
								   .position(Eskadrowa)
								   .title("Eskadrowa")
							       .snippet("Tutaj sparsowane dane"));
        Marker VMSSign4 = map.addMarker(new MarkerOptions()
								   .position(Hangarowa)
								   .title("Hangarowa")
							       .snippet("Tutaj sparsowane dane"));
        Marker VMSBoard2 = map.addMarker(new MarkerOptions()
								   .position(Struga)
								   .title("Struga")
							       .snippet("Tutaj sparsowane dane"));
        Marker VMSSign3 = map.addMarker(new MarkerOptions()
								   .position(Struga2)
								   .title("Struga")
							       .snippet("Tutaj sparsowane dane"));
        
        
        imageLoader = ImageLoader.getInstance();
        
        options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.loader)		//	Display Stub Image
			.showImageForEmptyUri(R.drawable.loader)	//	If Empty image found
			.cacheInMemory()
			.cacheOnDisc().bitmapConfig(Bitmap.Config.RGB_565).build();
        
        if ( map != null ) {
        	
    	map.setInfoWindowAdapter(new CustomInfoWindowAdapter());
    	map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    	markers.put(VMSBoard5.getId(), this.getImgUrl(71));
    	markers.put(VMSBoard4.getId(), this.getImgUrl(72));
    	markers.put(VMSSign8.getId(), this.getImgUrl(68));
    	
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(SZCZECIN, 15));
        }
	}
	
	private void loadData()
	{
		SZRApiClient.getVMSPublicData(new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(int core, Header[] headers, byte[] data)
			{
				try 
				{
					String xmlData = new String(data, "UTF-8");
					images = parseXmlData(xmlData);
					startMap();
				}
				catch (UnsupportedEncodingException e) 
				{
					e.printStackTrace();
				}
				catch (Exception e)
				{
					//error xpath
					e.printStackTrace();
				}
			}
		});
	}
	
	private Hashtable<Integer, String> parseXmlData(String xmlData) throws XPathExpressionException
	{
		Hashtable<Integer, String> imgList = new Hashtable<Integer, String>();
		DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setValidating(false);
		domFactory.setNamespaceAware(true);
		domFactory.setIgnoringComments(true);
		domFactory.setIgnoringElementContentWhitespace(true);
		
		try {
	        DocumentBuilder builder = domFactory.newDocumentBuilder();
	        Document dDoc = builder.parse(new InputSource(new StringReader(xmlData)));

	        // This part works
	        
	        NodeList placemarksNodes = dDoc.getElementsByTagName("Placemark");
	        for(int i = 0; i < placemarksNodes.getLength(); i++)
	        {
	        	Node n = placemarksNodes.item(i);
	        	if (n instanceof Element)
	        	{
	        		Element nodeElement = (Element) n;
	        		NodeList dataNodes = nodeElement.getElementsByTagName("Data");
	        		for(int j = 0; j < dataNodes.getLength(); j++)
	        		{
	        			Node dataNode = dataNodes.item(j);
	        			Node attributeNode = dataNode.getAttributes().getNamedItem("name");
	        			if(attributeNode != null && attributeNode.getNodeValue().equalsIgnoreCase("livePreviewURL"))
	        			{
	        				NodeList childs = dataNode.getChildNodes();
	        				if(childs.getLength() > 0)
	        				{
	        					System.out.println(dataNode.getChildNodes().item(1).getNodeName());
	        					System.out.println(dataNode.getChildNodes().item(1).getTextContent());
	        					String img_name[] = null;
	        					img_name = dataNode.getChildNodes().item(1).getTextContent().split("_");
	        					
	        					imgList.put(Integer.parseInt(img_name[1]),dataNode.getChildNodes().item(1).getTextContent());	// to zwraca Twojego stringa np img_76_2014
	        				}
	        			}
	        		}
	        	}
	        }
	        
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
		return imgList;
	}
	public String getImgUrl(int imgNumber)
	{
		String Url = null;
		String baseUrl = "http://szr.szczecin.pl/utms/img_preview/";
		Url = baseUrl + images.get(imgNumber);
		
		return Url;
		
	}
	
    private class CustomInfoWindowAdapter implements InfoWindowAdapter {
    	 
        private View view;
 
        public CustomInfoWindowAdapter() {
            view = getLayoutInflater().inflate(R.layout.custom_info_window,null);
        }
 
        @Override
        public View getInfoContents(Marker marker) {
 
            if (ManualMode.this.marker != null
                    && ManualMode.this.marker.isInfoWindowShown()) {
            	ManualMode.this.marker.hideInfoWindow();
            	ManualMode.this.marker.showInfoWindow();
            }
            return null;
        }
 
        @Override
        public View getInfoWindow(final Marker marker) {
        	ManualMode.this.marker = marker;
 
            String url = null;
 
            if (marker.getId() != null && markers != null && markers.size() > 0) {
                if ( markers.get(marker.getId()) != null &&
                        markers.get(marker.getId()) != null) {
                    url = markers.get(marker.getId());
                }
            }
            final ImageView image = ((ImageView) view.findViewById(R.id.badge));
 
            if (url != null && !url.equalsIgnoreCase("null")
                    && !url.equalsIgnoreCase("")) {
                imageLoader.displayImage(url, image, options,
                        new SimpleImageLoadingListener() {
                            @Override
                            public void onLoadingComplete(String imageUri,
                                    View view, Bitmap loadedImage) {
                                super.onLoadingComplete(imageUri, view,
                                        loadedImage);
                                getInfoContents(marker);
                            }
                        });
            } else {
                image.setImageResource(R.drawable.ic_launcher);
            }
 
            final String title = marker.getTitle();
            final TextView titleUi = ((TextView) view.findViewById(R.id.title));
            if (title != null) {
                titleUi.setText(title);
            } else {
                titleUi.setText("");
            }
 
            final String snippet = marker.getSnippet();
            final TextView snippetUi = ((TextView) view
                    .findViewById(R.id.snippet));
            if (snippet != null) {
                snippetUi.setText(snippet);
            } else {
                snippetUi.setText("");
            }
 
            return view;
        }
    }
 
    private void initImageLoader() {
        int memoryCacheSize;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR) {
            int memClass = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE))
            		.getMemoryClass();
            memoryCacheSize = (memClass / 8) * 1024 * 1024;
        } else {
            memoryCacheSize = 2 * 1024 * 1024;
        }
 
        final ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(
                this).threadPoolSize(5)
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .memoryCacheSize(memoryCacheSize)
                .memoryCache(new FIFOLimitedMemoryCache(memoryCacheSize-1000000))
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
 
        ImageLoader.getInstance().init(config);
    }
} 
	

