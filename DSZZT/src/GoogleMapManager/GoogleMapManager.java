package GoogleMapManager;


import com.example.dszzt.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GoogleMapManager{
	
	public GoogleMap googleMap;
	
	static final LatLng SZCZECIN = new LatLng(53.422479,14.559985);
	static final LatLng BramaPortowa = new LatLng(53.42485, 14.54833);
	static final LatLng Gdanska = new LatLng(53.41247, 14.57750);
	static final LatLng GdanskaEstakada = new LatLng(53.41141, 14.58048);
	static final LatLng PionierowMeteo = new LatLng(53.58048, 14.60949);
	static final LatLng Eskadrowa = new LatLng(53.38961, 14.62120);
	static final LatLng Hangarowa = new LatLng(53.38575, 14.64352);
	static final LatLng Struga = new LatLng(53.38446, 14.65167);
	static final LatLng Struga2 = new LatLng(53.38429, 14.65092);
	public Marker VMSBoard2, VMSBoard3, VMSBoard4, VMSBoard5, VMSSign3, VMSSign4, VMSSign8 ;
	
	public GoogleMapManager(GoogleMap googleMap)
	{
		this.googleMap = googleMap;
	}
	
	public void initMap()
	{
		googleMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
		googleMap.getUiSettings().setMyLocationButtonEnabled(true);
	}
	
	public void initMarkers()
	{
		this.VMSBoard5 = this.googleMap.addMarker(new MarkerOptions()
	               .position(BramaPortowa)
			       .title("Brama Portowa")
			       .snippet(""));
		this.VMSBoard4 = this.googleMap.addMarker(new MarkerOptions()
			       .position(Gdanska)
			       .title("Gdañska - Stocznia Parnica")
			       .snippet(""));
		this.VMSSign8 = this.googleMap.addMarker(new MarkerOptions()
			       .position(GdanskaEstakada)
			       .title("Gdañska - Estakada")
			       .snippet(""));
		this.VMSBoard3 = this.googleMap.addMarker(new MarkerOptions()
				   .position(Eskadrowa)
				   .title("Eskadrowa")
			       .snippet(""));
		this.VMSSign4 = this.googleMap.addMarker(new MarkerOptions()
				   .position(Hangarowa)
				   .title("Hangarowa")
			       .snippet(""));
		this.VMSBoard2 = this.googleMap.addMarker(new MarkerOptions()
				   .position(Struga)
				   .title("Struga")
			       .snippet(""));
		this.VMSSign3 = this.googleMap.addMarker(new MarkerOptions()
				   .position(Struga2)
				   .title("Struga")
			       .snippet(""));
	}

}
