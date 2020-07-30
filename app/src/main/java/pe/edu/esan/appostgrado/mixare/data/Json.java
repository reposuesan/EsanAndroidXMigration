package pe.edu.esan.appostgrado.mixare.data;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pe.edu.esan.appostgrado.entidades.CustumMarker;
import pe.edu.esan.appostgrado.mixare.Marker;
import pe.edu.esan.appostgrado.mixare.data.DataSource.DATAFORMAT;
import pe.edu.esan.appostgrado.mixare.data.DataSource.DATASOURCE;
import pe.edu.esan.appostgrado.util.Utilitarios;
import pe.edu.esan.appostgrado.view.mas.ra.PrincipalRAActivity;

public class Json extends DataHandler {
	public static final String TAG = "JSON";

	public static final int MAX_JSON_OBJECTS=1000;
	
	public List<Marker> load(JSONObject root, DATAFORMAT dataformat) {
		JSONObject jo = null;
		JSONArray dataArray = null;
    	List<Marker> markers=new ArrayList<Marker>();

		try {
			// Twitter & own schema
			String JSonRoot = PrincipalRAActivity.JSonCabecera;
			//String JSonRoot = f_comedor.JSonCabecera;
			if(root.has(JSonRoot))
				dataArray = root.getJSONArray(JSonRoot);
			if (dataArray != null) {

				Log.i(TAG, "processing "+dataformat+" JSON Data Array");
				int top = Math.min(MAX_JSON_OBJECTS, dataArray.length());

				for (int i = 0; i < top; i++) {					
					
					jo = dataArray.getJSONObject(i);
					Marker ma = null;
					ma = processLugaresJSONObject(jo, markers);
					if(ma!=null)
						markers.add(ma);
				}
			}
		}
		catch (JSONException e) {
			Log.e(TAG,e.getMessage());
			e.printStackTrace();
		}
		return markers;
	}
	
	public Marker processLugaresJSONObject(JSONObject jo, List<Marker> markers) throws JSONException {
		Marker ma = null;
//		parametros obligatorios:
		
		if (jo.has("title") && jo.has("lat") && jo.has("lng") && jo.has("id") && jo.has("filtro") ) {
			
			Log.i(TAG, "processing Lugar JSON object");
			
			String filtro = unescapeHTML(jo.getString("filtro"), 0);

			/*JSONArray jarrEnlaces = null;
			if(jo.has("enlace"))	{
				jarrEnlaces = jo.getJSONArray("enlace");
				if (jarrEnlaces != null )	{
					enlaces = new ArrayList<String>();
					
					int top = jarrEnlaces.length();
					for (int i = 0; i < top; i++) {	
						JSONObject obj = jarrEnlaces.getJSONObject(i);
						enlaces.add(obj.getString("enlace"));
					}
				}
			}*/

			ma = new CustumMarker(
					unescapeHTML(jo.getString("title"), 0),
					jo.getDouble("lat"),
					jo.getDouble("lng"),
					jo.getDouble("elevation"),
					getDataSource(filtro),
					jo.getString("id"),
					jo.getString("description"),
					jo.getString("color"),
					getImagen(jo.getString("urlimg"))
					);
			markers.add(ma);
		}
		return null;		
		
	}

	private Bitmap getImagen(String url){
		String newUrl = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_IMAGEN) + url;
		URL imageUrl = null;
		HttpURLConnection conn = null;

		try {

			imageUrl = new URL(newUrl);
			conn = (HttpURLConnection) imageUrl.openConnection();
			conn.connect();

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 2; // el factor de escala a minimizar la imagen, siempre es potencia de 2

			Bitmap imagen = BitmapFactory.decodeStream(conn.getInputStream(), new Rect(0, 0, 0, 0), options);
			return imagen;

		} catch (IOException e) {
			Log.e(TAG,"ERRROR:"+e.getMessage());
			return null;
		}
	}
	
	private DATASOURCE getDataSource(String filtro) {
		if (filtro.equals("FACULTADES"))	{
			return DataSource.DATASOURCE.FACULTADES;
		}
		else if (filtro.equals("EDIFICIOS"))	{
			return DataSource.DATASOURCE.EDIFICIOS;
		}
		else if (filtro.equals("CAFETERIAS"))	{
			return DataSource.DATASOURCE.CAFETERIAS;
		}
		else if (filtro.equals("DEPORTES"))	{
			return DATASOURCE.DEPORTES;
		}
		else if (filtro.equals("BIBLIOTECAS"))	{
			return DATASOURCE.BIBLIOTECAS;
		}
		else if (filtro.equals("LABORATORIOS"))	{
			return DATASOURCE.LABORATORIOS;
		}
		else if (filtro.equals("AUDITORIOS"))	{
			return DATASOURCE.AUDITORIOS;
		}
		else if (filtro.equals("LIBRERIAS"))	{
			return DATASOURCE.LIBRERIAS;
		}
		else if (filtro.equals("OFICINAS"))	{
			return DATASOURCE.OFICINAS;
		}
		else if (filtro.equals("AULAS"))	{
			return DATASOURCE.AULAS;
		}
		else if (filtro.equals("VARIOS"))	{
			return DATASOURCE.VARIOS;
		}
		return DATASOURCE.VARIOS;//nunca debe entrar aqui, si lo hace se producirán errores
	}
	
	private static HashMap<String, String> htmlEntities;
	static {
		htmlEntities = new HashMap<String, String>();
		htmlEntities.put("&lt;", "<");
		htmlEntities.put("&gt;", ">");
		htmlEntities.put("&amp;", "&");
		htmlEntities.put("&quot;", "\"");
		htmlEntities.put("&agrave;", "à");
		htmlEntities.put("&Agrave;", "À");
		htmlEntities.put("&acirc;", "â");
		htmlEntities.put("&auml;", "ä");
		htmlEntities.put("&Auml;", "Ä");
		htmlEntities.put("&Acirc;", "Â");
		htmlEntities.put("&aring;", "å");
		htmlEntities.put("&Aring;", "Å");
		htmlEntities.put("&aelig;", "æ");
		htmlEntities.put("&AElig;", "Æ");
		htmlEntities.put("&ccedil;", "ç");
		htmlEntities.put("&Ccedil;", "Ç");
		htmlEntities.put("&eacute;", "é");
		htmlEntities.put("&Eacute;", "É");
		htmlEntities.put("&egrave;", "è");
		htmlEntities.put("&Egrave;", "È");
		htmlEntities.put("&ecirc;", "ê");
		htmlEntities.put("&Ecirc;", "Ê");
		htmlEntities.put("&euml;", "ë");
		htmlEntities.put("&Euml;", "Ë");
		htmlEntities.put("&iuml;", "ï");
		htmlEntities.put("&Iuml;", "Ï");
		htmlEntities.put("&ocirc;", "ô");
		htmlEntities.put("&Ocirc;", "Ô");
		htmlEntities.put("&ouml;", "ö");
		htmlEntities.put("&Ouml;", "Ö");
		htmlEntities.put("&oslash;", "ø");
		htmlEntities.put("&Oslash;", "Ø");
		htmlEntities.put("&szlig;", "ß");
		htmlEntities.put("&ugrave;", "ù");
		htmlEntities.put("&Ugrave;", "Ù");
		htmlEntities.put("&ucirc;", "û");
		htmlEntities.put("&Ucirc;", "Û");
		htmlEntities.put("&uuml;", "ü");
		htmlEntities.put("&Uuml;", "Ü");
		htmlEntities.put("&nbsp;", " ");
		htmlEntities.put("&copy;", "\u00a9");
		htmlEntities.put("&reg;", "\u00ae");
		htmlEntities.put("&euro;", "\u20a0");
	}

	public String unescapeHTML(String source, int start) {
		int i, j;

		i = source.indexOf("&", start);
		if (i > -1) {
			j = source.indexOf(";", i);
			if (j > i) {
				String entityToLookFor = source.substring(i, j + 1);
				String value = (String) htmlEntities.get(entityToLookFor);
				if (value != null) {
					source = new StringBuffer().append(source.substring(0, i))
					.append(value).append(source.substring(j + 1))
					.toString();
					return unescapeHTML(source, i + 1); // recursive call
				}
			}
		}
		return source;
	}
}