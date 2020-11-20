/*        
	Copyright (C) 2010- Peer internet solutions

	This file is part of ESAN App.
	
	This program is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with This program.  If not, see <http://www.gnu.org/licenses/>.
*/
package pe.edu.esan.appostgrado.mixare;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;

import pe.edu.esan.appostgrado.view.mas.ra.MixView;
import pe.edu.esan.appostgrado.view.mas.ra.PrincipalRAActivity;
import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.mixare.data.DataSource;
import pe.edu.esan.appostgrado.mixare.data.DataSource.DATASOURCE;
import pe.edu.esan.appostgrado.mixare.render.Matrix;


public class MixContext extends ContextWrapper {

	public MixView var;
	Context ctx;
	boolean isURLvalid = true;
	Random rand;
	public DownloadManager downloadManager;
	public Location curLoc;
	Location locationAtLastDownload;
	private static final int TWO_MINUTES = 1000 * 60 * 2;
	public Matrix rotationM = new Matrix();
	public float declination = 0f;
	private boolean actualLocation=false;
	LocationManager locationMgr;
	private HashMap<DataSource.DATASOURCE,Boolean> selectedDataSources=new HashMap<DataSource.DATASOURCE,Boolean>();

	private static final String LOG = MixContext.class.getSimpleName();
	
	public MixContext(Context appCtx) {
	
		super(appCtx);

		this.ctx = appCtx.getApplicationContext();

		SharedPreferences settings = getSharedPreferences(PrincipalRAActivity.PREFS_NAME, 0);
		//SharedPreferences settings = getSharedPreferences(f_comedor.PREFS_NAME, 0);
		boolean atLeastOneDatasourceSelected=false;
		
		for(DataSource.DATASOURCE source: DataSource.DATASOURCE.values()) {
			// fill the selectedDataSources HashMap with saved settings
			selectedDataSources.put(source, settings.getBoolean(source.toString(), false));
			if(selectedDataSources.get(source))
				atLeastOneDatasourceSelected=true;
		}
		// if nothing was previously selected  
		if(!atLeastOneDatasourceSelected)	{
			setDataSource(DATASOURCE.FACULTADES, true);
			setDataSource(DATASOURCE.EDIFICIOS, true);
			setDataSource(DATASOURCE.CAFETERIAS, true);
			setDataSource(DATASOURCE.DEPORTES, true);
			setDataSource(DATASOURCE.BIBLIOTECAS, true);
			setDataSource(DATASOURCE.LABORATORIOS, true);
			setDataSource(DATASOURCE.AUDITORIOS, true);
			setDataSource(DATASOURCE.LIBRERIAS, true);
			setDataSource(DATASOURCE.OFICINAS, true);
			setDataSource(DATASOURCE.AULAS, true);
			setDataSource(DATASOURCE.VARIOS, true);
		}
			

		rotationM.toIdentity();

		int locationHash = 0;
		try {

			locationMgr = (LocationManager) appCtx.getSystemService(Context.LOCATION_SERVICE);
			Location lastFix = null;

			try {
				lastFix = locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			} catch(SecurityException e){
				Log.e(LOG,e.getMessage());
			}
			
			if (lastFix == null){
				try {
					lastFix = locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				} catch (SecurityException e) {
					Log.e(LOG,e.getMessage());
				}
			}

			if (lastFix != null){
				locationHash = ("HASH_" + lastFix.getLatitude() + "_" + lastFix.getLongitude()).hashCode();

				long actualTime= new Date().getTime();
				long lastFixTime = lastFix.getTime();
				long timeDifference = actualTime-lastFixTime;

				actualLocation = timeDifference <= 1200000;	//20 min --- 300000 milliseconds = 5 min
			}
			else
				actualLocation = false;
			
			
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		rand = new Random(System.currentTimeMillis() + locationHash);
	}
	
	public Location getCurrentGPSInfo() {
		try{
			return curLoc != null ? curLoc : locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		} catch (SecurityException e){
			return null;
		}
	}

	public boolean isActualLocation(){
		return actualLocation;
	}

	public DownloadManager getDownloader() {
		return downloadManager;
	}
	
	public void setLocationManager(LocationManager locationMgr){
		this.locationMgr = locationMgr;
	}
	
	public LocationManager getLocationManager(){
		return locationMgr;
	}

	public void getRM(Matrix dest) {
		synchronized (rotationM) {
			dest.set(rotationM);
		}
	}

	public Location getCurrentLocation() {
		if (curLoc != null)	{
			synchronized (curLoc) {
				return curLoc;
			}
		}
		
		return locationAtLastDownload;
	}

	public InputStream getHttpGETInputStream(String urlStr)
	throws Exception {
		InputStream is = null;
		URLConnection conn = null;

		if (urlStr.startsWith("file://"))			
			return new FileInputStream(urlStr.replace("file://", ""));

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, null);

		if (urlStr.startsWith("https://")) {
			HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier(){
    			public boolean verify(String hostname, SSLSession session) {
    				return true;
    			}});
		SSLContext context = SSLContext.getInstance("TLS");
		context.init(null, new X509TrustManager[]{new X509TrustManager(){
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {}
			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {}
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}}}, new SecureRandom());
		HttpsURLConnection.setDefaultSSLSocketFactory(
				context.getSocketFactory());
		}
		
		try {
			URL url = new URL(urlStr);
			conn =  url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			is = conn.getInputStream();
			
			return is;
		} catch (Exception ex) {
			try {
				is.close();
			} catch (Exception ignore) {
			}
			try {
				if(conn instanceof HttpURLConnection)
					((HttpURLConnection)conn).disconnect();
			} catch (Exception ignore) {
			}
			
			throw ex;				

		}
	}

	public String getHttpInputString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is), 8 * 1024);
		StringBuilder sb = new StringBuilder();

		try {
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}

	public InputStream getHttpPOSTInputStream(String urlStr,
                                              String params) throws Exception {
		InputStream is = null;
		OutputStream os = null;
		HttpURLConnection conn = null;

		if (urlStr.startsWith("content://"))
			return getContentInputStream(urlStr, params);

		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000);
			conn.setConnectTimeout(10000);

			if (params != null) {
				conn.setDoOutput(true);
				os = conn.getOutputStream();
				OutputStreamWriter wr = new OutputStreamWriter(os);
				wr.write(params);
				wr.close();
			}

			is = conn.getInputStream();
			
			return is;
		} catch (Exception ex) {

			try {
				is.close();
			} catch (Exception ignore) {

			}
			try {
				os.close();
			} catch (Exception ignore) {

			}
			try {
				conn.disconnect();
			} catch (Exception ignore) {
			}

			if (conn != null && conn.getResponseCode() == 405) {
				return getHttpGETInputStream(urlStr);
			} else {		

				throw ex;
			}
		}
	}

	public InputStream getContentInputStream(String urlStr, String params)
	throws Exception {
		ContentResolver cr = var.getContentResolver();
		Cursor cur = cr.query(Uri.parse(urlStr), null, params, null, null);

		cur.moveToFirst();
		int mode = cur.getInt(cur.getColumnIndex("MODE"));

		if (mode == 1) {
			String result = cur.getString(cur.getColumnIndex("RESULT"));
			cur.deactivate();

			return new ByteArrayInputStream(result
					.getBytes());
		} else {
			cur.deactivate();

			throw new Exception("Invalid content:// mode " + mode);
		}
	}

	public void returnHttpInputStream(InputStream is) throws Exception {
		if (is != null) {
			is.close();
		}
	}

	public void returnResourceInputStream(InputStream is) throws Exception {
		if (is != null)
			is.close();
	}

	public void loadWebPage(String url, Context context) throws Exception {
		WebView webview = new WebView(context);
		
		webview.setWebViewClient(new WebViewClient() {
			public boolean  shouldOverrideUrlLoading  (WebView view, String url) {
			     view.loadUrl(url);
				return true;
			}

		});
				
		Dialog d = new Dialog(context) {
			public boolean onKeyDown(int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK)
					this.dismiss();
				return true;
			}
		};
		d.requestWindowFeature(Window.FEATURE_NO_TITLE);
		d.getWindow().setGravity(Gravity.BOTTOM);
		d.addContentView(webview, new FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
				Gravity.BOTTOM));

		d.show();
		
		webview.loadUrl(url);
	}

	public void setDataSource(DataSource.DATASOURCE source, Boolean selection){
		Log.i("MIXCONTEXT","Seleccion: "+source.toString());
		selectedDataSources.put(source,selection);
		SharedPreferences settings = getSharedPreferences(PrincipalRAActivity.PREFS_NAME, 0);
		//SharedPreferences settings = getSharedPreferences(f_comedor.PREFS_NAME, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(source.toString(), selection);
		editor.commit();
	}
               
	public Boolean isDataSourceSelected(DataSource.DATASOURCE source) {
		return selectedDataSources.get(source);
	}
	
	public String getDataSourceName(DataSource.DATASOURCE source) {
		switch(source) {
			case FACULTADES: 		return "Facultades";
			case EDIFICIOS: 		return "Pabellones";
			case CAFETERIAS: 		return "Cafeterías y Comedores";
			case DEPORTES:	 		return "Polideportivos";
			case BIBLIOTECAS: 		return "Bibliotecas";
			case LABORATORIOS: 		return "Laboratorios";
			case AUDITORIOS: 		return "Auditorios";
			case LIBRERIAS: 		return "Librerias";
			case OFICINAS: 			return "Oficinas Administrativas";
			case AULAS: 			return "Aulas";
			case VARIOS: 			return "Diversos Lugares";
		}
		return "";
	}

	//public int getDataSourceCircleIconRA(DataSource.DATASOURCE source) {
	public int getLocalDataSourceImagen(DataSource.DATASOURCE source) {
		switch(source) {
			case FACULTADES:		return R.mipmap.n_facultades;
			case EDIFICIOS:			return R.mipmap.m_edificios;
			case CAFETERIAS:		return R.mipmap.n_cafeterias;
			case DEPORTES:			return R.mipmap.n_deportes;
			case BIBLIOTECAS:		return R.mipmap.n_bibliotecas;
			case LABORATORIOS:		return R.mipmap.n_laboratorios;
			case AUDITORIOS:		return R.mipmap.n_auditorio;
			case LIBRERIAS:			return R.mipmap.n_librerias;
			case OFICINAS:			return R.mipmap.n_oficinas;
			case AULAS:				return R.mipmap.n_aulas;
			case VARIOS:			return R.mipmap.n_varios;
		}
		return R.mipmap.n_varios;
	}
	
	public void toogleDataSource(DataSource.DATASOURCE source) {
		setDataSource(source, !selectedDataSources.get(source));
	}
	
	public ArrayList<String> getDataSourcesStringList() {
		ArrayList<String> selectedList =  new ArrayList<String>();
		for(DataSource.DATASOURCE source: DataSource.DATASOURCE.values()) {
			if(isDataSourceSelected(source)) {
				selectedList.add(getDataSourceName(source));
			}	
		}
		return selectedList;
	}
	
	/*
	 * Devuelve una lista con las categorías activas que no tienen markers, si todas las activas tienen markers, devuelve null*/
	public String noMarkersCheck() {
		List<Marker> markerList = PrincipalRAActivity.dataView.getDataHandler().getMarkerList();
		//List<Marker> markerList = f_comedor.dataView.getDataHandler().getMarkerList();
		if ((markerList == null) || (markerList.isEmpty())) {
			ArrayList<String> selectedEmptyList = getDataSourcesStringList();
			String selectedEmptyListString = "";
			for (int i=0; i<selectedEmptyList.size(); i++)	{
				if (i==0)
					selectedEmptyListString += "\"" + selectedEmptyList.get(i) + "\"";
				else if (i==selectedEmptyList.size()-1)
					selectedEmptyListString += " y \"" + selectedEmptyList.get(i) + "\"";
				else
					selectedEmptyListString += ", \"" + selectedEmptyList.get(i) + "\"";
			}
			if (selectedEmptyList.size()==1)	{
				return "Por el momento la categoría " + selectedEmptyListString + " no tiene contenido. Seleccione otras categorías.";
			}
			else if (selectedEmptyList.size()>1)	{
				return "Por el momento las categorías: " + selectedEmptyListString + " no tienen contenido. Seleccione otras categorías.";
			}
			return null;
		}
		return null;
	}
	
	/*
	 * Return true si no hay markers dentro del radio de visibilidad establecido, false si hay al menos 1 marker dentro de él*/
	public boolean noMarkerInRadius(int radioMetros) {
		List<Marker> markerList = PrincipalRAActivity.dataView.getDataHandler().getMarkerList();
		//List<Marker> markerList = f_comedor.dataView.getDataHandler().getMarkerList();
		for (int i=0; i < markerList.size(); i++)	{
			if (markerList.get(i).getDistance() < radioMetros)
				return false;
		}
		return true;
	}

	public Location getLocationAtLastDownload() {
		return locationAtLastDownload;
	}

	public void setLocationAtLastDownload(Location locationAtLastDownload) {
		this.locationAtLastDownload = locationAtLastDownload;
	}
	
	public boolean isBetterLocation(Location location, Location currentBestLocation) {
	    if (currentBestLocation == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - currentBestLocation.getTime();
	    boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
	    boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(),
	            currentBestLocation.getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	public boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
}
