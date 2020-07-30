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
package pe.edu.esan.appostgrado.mixare.data;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import pe.edu.esan.appostgrado.util.Utilitarios;
import pe.edu.esan.appostgrado.view.mas.ra.PrincipalRAActivity;

/**
 * @author hannes
 *
 */
public class DataSource {
	
	// Datasource and dataformat are not the same. datasource is where the data comes from
	// and dataformat is how the data is formatted. 
	// this is necessary for example when you have multiple datasources with the same
	// dataformat
	public enum DATASOURCE {
		FACULTADES,
		EDIFICIOS,
		CAFETERIAS,
		DEPORTES,
		BIBLIOTECAS,
		LABORATORIOS,
		AUDITORIOS,
		LIBRERIAS,
		OFICINAS,
		AULAS,
		VARIOS,
		BYID,
		SEARCH,
	}

	public enum DATAFORMAT {
		LUGARES
	}

	//private static final String lugaresURL = "http://172.59.1.17/mapa/lugares.php";

	public DataSource() {
		
	}
	
	public static Bitmap getLocalBitmap(DATASOURCE ds, int dim) {
		Bitmap bitmap;
		bitmap = drawableToBitmap(PrincipalRAActivity.getContext().getResources().getDrawable(PrincipalRAActivity.getContext().getLocalDataSourceImagen(ds)));
//		//resize bitmap
//		int bitmapDim = Math.min(bitmap.getWidth(), dim);
		bitmap = Bitmap.createScaledBitmap(bitmap, dim, dim, true);
		return bitmap;
	}

	public static Bitmap getNewBitmap(Bitmap imagen, int dim){
		Bitmap bitmap = Bitmap.createScaledBitmap(imagen, dim, dim, true);
		return bitmap;
	}
	
	public static Bitmap drawableToBitmap (Drawable drawable) {
	    if (drawable instanceof BitmapDrawable) {
	        return ((BitmapDrawable)drawable).getBitmap();
	    }

	    Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
	    Canvas canvas = new Canvas(bitmap);
	    drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
	    drawable.draw(canvas);

	    return bitmap;
//	    return adjustOpacity(bitmap, 20);
	}

	
	public static DATAFORMAT dataFormatFromDataSource(DATASOURCE ds) {
		return DATAFORMAT.LUGARES;
	}
	
	public static String createRequestURL(DATASOURCE source, double lat, double lon, double alt, float radius, String locale) {
		String ret="";
		switch(source) {
			case FACULTADES:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_FACULTADES);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "facultades";
				break;
			case EDIFICIOS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_EDIFICIOS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "edificios";
				break;
			case CAFETERIAS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_CAFETERIAS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "cafeterias";
				break;
			case DEPORTES:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_DEPORTES);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "deportes";
				break;
			case BIBLIOTECAS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_BIBLIOTECAS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "bibliotecas";
				break;
			case LABORATORIOS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_LABORATORIOS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "laboratorios";
				break;
			case AUDITORIOS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_AUDITORIOS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "auditorios";
				break;
			case LIBRERIAS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_LIBRERIAS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "librerias";
				break;
			case OFICINAS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_OFICINAS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "oficinas";
				break;
			case AULAS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_AULAS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "aulas";
				break;
			case VARIOS:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_VARIOS);//Configuracion.urlServicio + Configuracion.method_principalesambientes + "varios";
				break;
			case BYID:
				ret = Utilitarios.Companion.getUrl(Utilitarios.URL.RA_BYID) + locale;
				break;
		}
		
		return ret;
	}
	/*
	public static int getColor(DATASOURCE datasource) {
		int ret;
		switch(datasource) {
			case FACULTADES:		ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_facultades); break;
			case EDIFICIOS:			ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_edificios); break;
			case CAFETERIAS:		ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_cafeterias); break;
			case DEPORTES:			ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_deportes); break;
			case BIBLIOTECAS:		ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_bibliotecas); break;
			case LABORATORIOS:		ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_laboratorios); break;
			case AUDITORIOS:		ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_auditorios); break;
			case LIBRERIAS:			ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_librerias); break;
			case OFICINAS:			ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_oficinas); break;
			case AULAS:				ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_aulas); break;
			case VARIOS:			ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_varios); break;
			default:				ret= PrincipalAulaAR.getContext().getResources().getColor(R.color.c_varios); break;
		}
		return ret;
	}*/
}
