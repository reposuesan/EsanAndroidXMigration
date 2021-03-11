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

import android.graphics.Color;
import android.location.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.mixare.data.DataHandler;
import pe.edu.esan.appostgrado.mixare.data.DataSource;
import pe.edu.esan.appostgrado.mixare.data.DataSource.DATAFORMAT;
import pe.edu.esan.appostgrado.mixare.data.DataSource.DATASOURCE;
import pe.edu.esan.appostgrado.mixare.gui.PaintScreen;
import pe.edu.esan.appostgrado.mixare.gui.RadarPoints;
import pe.edu.esan.appostgrado.mixare.gui.ScreenLine;
import pe.edu.esan.appostgrado.mixare.render.Camera;

//import esan.pe.edu.aresan.ScheduleDetailsActivity;

public class DataView {

	public int getEstado() {
		return state.nextLStatus;
	}

	public static boolean viendoUnicoMarker = false;
	public static boolean actualizarMarkers = false; //no de BD, solo utilizar markers actuales (se utiliza cuando se ve Vmapa o VAR cuando provienen de VLista)
	public static boolean enBusqueda = false;
	public static boolean volverInicio = false;
//	public static boolean volverAFiltro = false; //si se ha buscado por categorías y estas no tienen contenido, se regresa a la pantalla de filtros y se muestra un mensaje
	
	/** current context */
	private MixContext mixContext;

	/** is the view Inited? */
	private boolean isInit;

	/** width and height of the view */
	private int width, height;

	/**
	 * _NOT_ the android camera, the class that takes care of the transformation
	 */
	private Camera cam;

	private MixState state = new MixState();

	/** The view can be "frozen" for debug purposes */
	private boolean frozen;

//	/** how many times to re-attempt download */
//	private int retry;

	private Location curFix;
	private DataHandler dataHandler = new DataHandler();
	private float radius;

	private boolean isLauncherStarted;

	private ArrayList<UIEvent> uiEvents = new ArrayList<UIEvent>();

	private RadarPoints radarPoints = new RadarPoints();
	private ScreenLine lrl = new ScreenLine();
	private ScreenLine rrl = new ScreenLine();
	private float rx = 10, ry = 20;
//	private float addX = 0, addY = 0;
//	private int frames = 0;

	private int TUltimo;

	/**
	 * Constructor
	 */
	public DataView(MixContext ctx) {
		this.mixContext = ctx;
	}

	public MixContext getContext() {
		return mixContext;
	}

	public void setMixContext(){
		mixContext = null;
	}

	public boolean isLauncherStarted() {
		return isLauncherStarted;
	}

	public boolean isFrozen() {
		return frozen;
	}

	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public void changeState(int newState) {
		int oldState = state.nextLStatus;
		state.nextLStatus = newState;
	}

	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public DataHandler getDataHandler() {
		return dataHandler;
	}

	public boolean isDetailsView() {
		return state.isDetailsView();
	}

	public void setDetailsView(boolean detailsView) {
		state.setDetailsView(detailsView);
	}

	public void doStart() {
		if (state.nextLStatus != MixState.DONE) {
			state.nextLStatus = MixState.NOT_STARTED;
		}
		mixContext.setLocationAtLastDownload(curFix);
	}

	public boolean isInited() {
		return isInit;
	}

//	public void initCamera(int widthInit, int heightInit) {
//		try {
//			width = widthInit;
//			height = heightInit;
//
//			cam = new Camera(width, height, true);
//			
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//	}
	
	public void init(int widthInit, int heightInit)  {
		try {
			width = widthInit;
			height = heightInit;
			cam = new Camera(width, height, true);
			cam.setViewAngle(Camera.DEFAULT_VIEW_ANGLE);
			lrl.set(0, -RadarPoints.RADIUS);
			lrl.rotate(Camera.DEFAULT_VIEW_ANGLE / 2);
			lrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
			rrl.set(0, -RadarPoints.RADIUS);
			rrl.rotate(-Camera.DEFAULT_VIEW_ANGLE / 2);
			rrl.add(rx + RadarPoints.RADIUS, ry + RadarPoints.RADIUS);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		frozen = false;
		isInit = true;
	}

	public void requestData(String url, DATAFORMAT dataformat,
                            DATASOURCE datasource) {
		DownloadRequest request = new DownloadRequest();
		request.format = dataformat;
		request.source = datasource;
		request.url = url;
		mixContext.getDownloader().submitJob(request);
	}

	public void reiniciarAlturas() {
		for (int i = 0; i < dataHandler.getMarkerCount(); i++) {
			Marker ma = dataHandler.getMarker(i);
			ma.addY = 0;
			ma.alturaEstablecida = false;
		}
	}
	
	public void draw(PaintScreen dw) {
		mixContext.getRM(cam.transform);
		curFix = mixContext.getCurrentLocation();
		state.calcPitchBearing(cam.transform);
//		/*frames por segundo*/
		int segundos = 3;
//		frames ++;
		int Tactual = Calendar.getInstance().get(Calendar.SECOND);
		if (Tactual < TUltimo)	{
			TUltimo = TUltimo - 60;
		}
		if (Tactual - TUltimo > segundos)	{
//			frames = 0;
////			for (int i = 0; i <= dataHandler.getMarkerCount() - 1; i++) {
////				Marker ma = dataHandler.getMarker(i);
////				if (ma.isVisible)//ma.isLookingAt
////					Log.d(ScheduleDetailsActivity.TAG,"visible:" + ma.getTitle());
////				else
////					Log.d(ScheduleDetailsActivity.TAG,"NO visible:" + ma.getTitle());
////			}
			reiniciarAlturas();
			TUltimo = Tactual;
		}
		List<Marker> mActivos = new ArrayList<Marker>();
		float tamanoCirculo = dw.getHeight() / 20f; // es el
													// tamaño del circulo,
													// obtenido de funcion
													// drawcircle
		float tamanoPOIY = (tamanoCirculo) * 2;
		float tamanoPOIX = 150;
		boolean dibujarArriba = true; // por traslape
		for (int i = 0; i < dataHandler.getMarkerCount(); i++) {
			Marker ma = dataHandler.getMarker(i);
			if (ma.isActive() && (ma.getDistance() / 1000f < radius)) {
				if (ma.alturaEstablecida)	{
					ma.calcPaint(cam, 0, ma.addY); // se actualizan las coordenadas de dibujo
				}
				else	{
					ma.calcPaint(cam, 0, 0); // se actualizan las coordenadas de dibujo
				}
				if (ma.isVisible)	{		
					if (!ma.alturaEstablecida)	{
						ma.addY = 0;
						boolean huboTraslape = false;
						for (int j = 0; j < mActivos.size(); j++) {
							if (esAproximado(tamanoPOIX, mActivos.get(j).cMarker.x,
									ma.cMarker.x)
									&& esAproximado(tamanoPOIY,
											mActivos.get(j).cMarker.y, ma.cMarker.y)) {
								// si hay traslape
								huboTraslape = true;
								// cambio las coordenadas de dibujo de este marker
								if (dibujarArriba) {
									ma.addY -= tamanoCirculo * 6;
								}
								else {
									ma.addY -= tamanoCirculo * 6;
								} 
								ma.calcPaint(cam, 0, ma.addY);
							}
						}
						if (huboTraslape)	{
							dibujarArriba = dibujarArriba ? false : true;
						}
						ma.alturaEstablecida = true;
					}
					mActivos.add(ma);
					ma.draw(dw);
				}
			}
		}
		// Draw Radar
		String dirTxt = "";
		int bearing = (int) state.getCurBearing();
		int range = (int) (state.getCurBearing() / (360f / 16f));

		if (range == 15 || range == 0)
			dirTxt = "N";
		else if (range == 1 || range == 2)
			dirTxt = "NE";
		else if (range == 3 || range == 4)
			dirTxt = "E";
		else if (range == 5 || range == 6)
			dirTxt = "SE";
		else if (range == 7 || range == 8)
			dirTxt = "S";
		else if (range == 9 || range == 10)
			dirTxt = "SO";
		else if (range == 11 || range == 12)
			dirTxt = "O";
		else if (range == 13 || range == 14)
			dirTxt = "NO";

		radarPoints.view = this;
		RadarPoints.setRadarColor(mixContext.getResources().getColor(
				R.color.color_radar));
		dw.paintObj(radarPoints, rx, ry, -state.getCurBearing(), 1, true);
		dw.setFill(false);
		dw.setColor(mixContext.getResources().getColor(R.color.lineas_radar));
		dw.paintLine(lrl.x, lrl.y, rx + RadarPoints.RADIUS, ry
				+ RadarPoints.RADIUS);
		dw.paintLine(rrl.x, rrl.y, rx + RadarPoints.RADIUS, ry
				+ RadarPoints.RADIUS);
		dw.setColor(Color.rgb(255, 255, 255));
		dw.setFontSize(12);

		radarText(dw, MixUtils.formatDist(radius * 1000), rx
				+ RadarPoints.RADIUS, ry + RadarPoints.RADIUS * 2 - 10, false);
		radarText(dw, "" + bearing + ((char) 176) + " " + dirTxt, rx
				+ RadarPoints.RADIUS, ry - 5, true);

		// Get next event
		UIEvent evt = null;
		synchronized (uiEvents) {
			if (uiEvents.size() > 0) {
				evt = uiEvents.get(0);
				uiEvents.remove(0);
			}
		}
		if (evt != null) {
			switch (evt.type) {

			case UIEvent.CLICK:
				handleClickEvent((ClickEvent) evt);
				break;
			}
		}
	}

	private boolean esAproximado(float tamanoPOI, float x, float x2) {
		return (Math.abs(x2 - x) < tamanoPOI) ? true : false;
	}

	boolean handleClickEvent(ClickEvent evt) {
		boolean evtHandled = false;

		for (int i = 0; i < dataHandler.getMarkerCount() && !evtHandled; i++) {
			Marker pm = dataHandler.getMarker(i);
			if (pm.isVisible)
				evtHandled = pm.fClick(evt.x, evt.y, mixContext, state);
		}
		return evtHandled;
	}

	void radarText(PaintScreen dw, String txt, float x, float y, boolean bg) {
		float padw = 4, padh = 2;
		float w = dw.getTextWidth(txt) + padw * 2;
		float h = dw.getTextAsc() + dw.getTextDesc() + padh * 2;
		if (bg) {
			dw.setColor(Color.rgb(0, 0, 0));
			dw.setFill(true);
			dw.paintRect(x - w / 2, y - h / 2, w, h);
			dw.setColor(Color.rgb(255, 255, 255));
			dw.setFill(false);
			dw.paintRect(x - w / 2, y - h / 2, w, h);
		}
		dw.paintText(padw + x - w / 2, padh + dw.getTextAsc() + y - h / 2, txt,
				false);
	}

	public void clickEvent(float x, float y) {
		synchronized (uiEvents) {
			uiEvents.add(new ClickEvent(x, y));
		}
	}

	public void keyEvent(int keyCode) {
		synchronized (uiEvents) {
			uiEvents.add(new KeyEvent(keyCode));
		}
	}

	public void clearEvents() {
		synchronized (uiEvents) {
			uiEvents.clear();
		}
	}

	public Marker getMarker(String idPOI, DATAFORMAT format) {

		String url = DataSource.createRequestURL(DataSource.DATASOURCE.BYID, 0,
				0, 0, radius, idPOI);
		requestData(url, format, DataSource.DATASOURCE.BYID);

		DownloadResult result = mixContext.getDownloader().getDefaultMarkers();
		if (!result.error) {
			List<Marker> lm = result.getMarkers();
			if (lm != null && lm.size() == 1) {
				return lm.iterator().next();
			}
		}

		return null;
	}

	public List<Marker> setDefaultMarkerList() {
		this.changeState(MixState.PROCESSING);
		boolean hayMarker = false;
		dataHandler.clearMarkers();
		for (DataSource.DATASOURCE source : DataSource.DATASOURCE.values()) {
			if (mixContext.isDataSourceSelected(source)) {
				// String uno =
				// DataSource.createRequestURL(source,0,0,0,radius,Locale.getDefault().getLanguage());
				// DATAFORMAT dos = DataSource.dataFormatFromDataSource(source);
				// requestData(uno,dos,source);

				requestData(DataSource.createRequestURL(source, 0, 0, 0,
						radius, Locale.getDefault().getLanguage()),
						DataSource.dataFormatFromDataSource(source), source);

				// Debug notification
				// Toast.makeText(mixContext, "Downloading from "+ source,
				// Toast.LENGTH_SHORT).show();

				DownloadResult result = mixContext.getDownloader()
						.getDefaultMarkers();
				if (!result.error) {
					hayMarker = true;
					dataHandler.addMarkers(result.getMarkers());
				}
			}
		}
		if (hayMarker) {
			dataHandler.updateActivationStatus(mixContext);
			this.changeState(MixState.DONE);
			return dataHandler.getMarkerList();
		}
		this.changeState(MixState.NOT_STARTED);
		return null;
	}

	public void valorEncontrado(){
		dataHandler.updateActivationStatus(mixContext);
		this.changeState(MixState.DONE);
	}

	public List<Marker> setSearchMarkerList(String textoBúsqueda) {

		this.changeState(MixState.PROCESSING);

		String url = DataSource.createRequestURL(DataSource.DATASOURCE.SEARCH,
				0, 0, 0, radius, textoBúsqueda);
		requestData(url, DataSource.DATAFORMAT.LUGARES,
				DataSource.DATASOURCE.SEARCH);

		DownloadResult result = mixContext.getDownloader().getDefaultMarkers();
		if (!result.error) {
			// creo nueva lista, resultado de busqueda
			dataHandler.clearMarkers();

			dataHandler.addMarkers(result.getMarkers());

			dataHandler.updateActivationStatus(mixContext);
			this.changeState(MixState.DONE);
			return dataHandler.getMarkerList();
		}

		this.changeState(MixState.NOT_STARTED);
		return null;
	}

}

class UIEvent {
	public static final int CLICK = 0;
	public static final int KEY = 1;

	public int type;
}

class ClickEvent extends UIEvent {
	public float x, y;

	public ClickEvent(float x, float y) {
		this.type = CLICK;
		this.x = x;
		this.y = y;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + ")";
	}
}

class KeyEvent extends UIEvent {
	public int keyCode;

	public KeyEvent(int keyCode) {
		this.type = KEY;
		this.keyCode = keyCode;
	}

	@Override
	public String toString() {
		return "(" + keyCode + ")";
	}
}
