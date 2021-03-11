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

import java.text.DecimalFormat;

import pe.edu.esan.appostgrado.mixare.data.DataSource;
import pe.edu.esan.appostgrado.mixare.gui.PaintScreen;
import pe.edu.esan.appostgrado.mixare.gui.ScreenLine;
import pe.edu.esan.appostgrado.mixare.gui.TextObj;
import pe.edu.esan.appostgrado.mixare.reality.PhysicalPlace;
import pe.edu.esan.appostgrado.mixare.render.Camera;
import pe.edu.esan.appostgrado.mixare.render.MixVector;

//import esan.pe.edu.aresan.ScheduleDetailsActivity;

abstract public class Marker implements Comparable<Marker> {

	private String ID;
	protected String title;
	protected String color;
	private boolean underline = false;
	//private ArrayList<String> URL;
	protected PhysicalPlace mGeoLoc;
	// distance from user to mGeoLoc in meters
	protected double distance;
	// From which datasource does this marker originate
	protected DataSource.DATASOURCE datasource;
	private boolean active;
	protected boolean unicoMarker;

	// Draw properties
	protected boolean isVisible;
	protected boolean isLookingAt;

//	private float deltaCenter;
	public MixVector cMarker = new MixVector();
	protected MixVector signMarker = new MixVector();
	
	protected MixVector locationVector = new MixVector();
	private MixVector origin = new MixVector(0, 0, 0);
	private MixVector upV = new MixVector(0, 1, 0);
	private ScreenLine pPt = new ScreenLine();

	protected Label txtLab = new Label();
	protected TextObj textBlock;
	protected double radius;
	
	public float addY;
	public boolean alturaEstablecida = false;
	
	
	public Marker(String title, double latitude, double longitude, double altitude, /*ArrayList<String> enlaces,*/ DataSource.DATASOURCE datasource, String color) {
		super();

		this.active = false;
		this.title = title;
		this.mGeoLoc = new PhysicalPlace(latitude,longitude,altitude);
		
		//this.URL = enlaces;
		this.color = color;
		this.datasource = datasource;
		
		this.ID=datasource+"##"+title; //mGeoLoc.toString();
		
		this.alturaEstablecida = false;
	}
	
	public String getTitle(){
		return title;
	}
	/*
	public int getIcon()	{
		return ScheduleDetailsActivity.getContext().getDataSourceIcon(datasource);
	}
	
	public int getCircleIcon()	{
		return ScheduleDetailsActivity.getContext().getDataSourceCircleIcon(datasource);
	}

	public ArrayList<String> getURL(){
		return URL;
	}*/

	public double getLatitude() {
		return mGeoLoc.getLatitude();
	}
	
	public double getLongitude() {
		return mGeoLoc.getLongitude();
	}
	
	public double getAltitude() {
		return mGeoLoc.getAltitude();
	}
	
	public MixVector getLocationVector() {
		return locationVector;
	}
	
	public String getColor() { return color; }
	
	public DataSource.DATASOURCE getDatasource() {
		return datasource;
	}

	public void setDatasource(DataSource.DATASOURCE datasource) {
		this.datasource = datasource;
	}

	private void cCMarker(MixVector originalPoint, Camera viewCam, float addX, float addY) {

		// Temp properties
		MixVector tmpa = new MixVector(originalPoint);
		MixVector tmpc = new MixVector(upV);
		tmpa.add(locationVector); //3 
		tmpc.add(locationVector); //3
		tmpa.sub(viewCam.lco); //4
		tmpc.sub(viewCam.lco); //4
		tmpa.prod(viewCam.transform); //5
		tmpc.prod(viewCam.transform); //5

		MixVector tmpb = new MixVector();
		viewCam.projectPoint(tmpa, tmpb, addX, addY); //6
		cMarker.set(tmpb); //7
		viewCam.projectPoint(tmpc, tmpb, addX, addY); //6
		signMarker.set(tmpb); //7
	}

	private void calcV(Camera viewCam) {
		isVisible = false;
		//isLooking AT me dice si esta enfocando al punto, no borrar, 
		//puede utilizarce en un futuro...
//		isLookingAt = false;
//		deltaCenter = Float.MAX_VALUE;

		if (cMarker.z < -1f) {
//			isVisible = true;

//			if (MixUtils.pointInside(cMarker.x, cMarker.y, 0, 0,
//					viewCam.width, viewCam.height)) {
			float extra = viewCam.height / 25f * 7f;
			if (MixUtils.pointInsideX(cMarker.x, 0 - extra,viewCam.width + extra * 2)) {
				isVisible = true;
//				float xDist = cMarker.x - viewCam.width / 2;
//				float yDist = cMarker.y - viewCam.height / 2;
//				float dist = xDist * xDist + yDist * yDist;
//
//				deltaCenter = (float) Math.sqrt(dist);
//
//				if (dist < 50 * 50) {
//					isLookingAt = true;
//				}
			}
		}
	}

	public void update(Location curGPSFix) {
//		// An elevation of 0.0 probably means that the elevation of the
//		// POI is not known and should be set to the users GPS height
//		// Note: this could be improved with calls to 
//		// http://www.geonames.org/export/web-services.html#astergdem 
//		// to estimate the correct height with DEM models like SRTM, AGDEM or GTOPO30
//		if(mGeoLoc.getAltitude()==0.0)
//			mGeoLoc.setAltitude(curGPSFix.getAltitude());
		 
		// compute the relative position vector from user position to POI location
		PhysicalPlace.convLocToVec(curGPSFix, mGeoLoc, locationVector);
	}

	public void calcPaint(Camera viewCam, float addX, float addY) {
		cCMarker(origin, viewCam, addX, addY);
		calcV(viewCam);
	}

	private boolean isClickValid(float x, float y) {		
		float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
		signMarker.x, signMarker.y);
		float probError = 1.5f;
		pPt.x = x - cMarker.x;
		pPt.y = y - cMarker.y;
		pPt.rotate(Math.toRadians(-(currentAngle + 90)));
		
		pPt.x = Math.abs(pPt.x);
		pPt.y = Math.abs(pPt.y);
//		distX = Math.abs(distX);
//		distY = Math.abs(distY);
		if ((pPt.x <= radius*probError) && (pPt.y <= radius*probError)){
			return true;
		} else {
			return false;
		}
	}
	
	public void draw(PaintScreen dw) {
		dw.setColor(Color.parseColor(color));
		//dw.setColor(DataSource.getColor(datasource));
		drawCircle(dw);
		drawTextBlock(dw);
	}

	public void drawCircle(PaintScreen dw) {

		if (isVisible) {
			//float maxHeight = Math.round(dw.getHeight() / 10f) + 1;
			float maxHeight = dw.getHeight();
			dw.setStrokeWidth(maxHeight / 100f);
			dw.setFill(false);
//			dw.setColor(DataSource.getColor(datasource));
			
			//draw circle with radius depending on distance
			//0.44 is approx. vertical fov in radians 
			double angle = 2.0* Math.atan2(10,distance);
			radius = Math.max(Math.min(angle/0.44 * maxHeight, maxHeight),maxHeight/25f);
			//double radius = angle/0.44d * (double)maxHeight;
			
			dw.paintCircle(cMarker.x, cMarker.y, (float)radius);
		}
	}
	//deprecated
	public void drawTextBlock(PaintScreen dw) {
		//grandezza cerchi e trasparenza
		float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

		//change textblock only when distance changes
		String textStr="";

		double d = distance;
		DecimalFormat df = new DecimalFormat("@#");
		if(d<1000.0) {
			textStr = title + " ("+ df.format(d) + "m)";
		}
		else {
			d=d/1000.0;
			textStr = title + " (" + df.format(d) + "km)";
		}
		
		textBlock = new TextObj(textStr, Math.round(maxHeight / 2f) + 1,
				250, dw, underline);

		if (isVisible) {
			
//			dw.setColor(DataSource.getColor(datasource));

			float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y, signMarker.x, signMarker.y);

			txtLab.prepare(textBlock);

			dw.setStrokeWidth(1f);
			dw.setFill(true);
			dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()
					/ 2, signMarker.y + maxHeight, currentAngle + 90, 1, false);
		}

	}
	//deprecated
	public boolean fClick(float x, float y, MixContext ctx, MixState state) {
		boolean evtHandled = false;

		if (isClickValid(x, y)) {
//			evtHandled = state.handleEvent(ctx, URL);
			evtHandled = state.handleEvent(ctx, this);
		}
		return evtHandled;
	}

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}
	

	public String getID() {
		return ID;
	}

	public void setID(String iD) {
		ID = iD;
	}

	@Override
	public int compareTo(Marker another) {

		Marker leftPm = this;
		Marker rightPm = another;

		return Double.compare(leftPm.getDistance(), rightPm.getDistance());

	}

	@Override
	public boolean equals (Object marker) {
		return this.ID.equals(((Marker) marker).getID());
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isUnicoMarker() {
		return unicoMarker;
	}

	public void setUnicoMarker(boolean unicoMarker) {
		this.unicoMarker = unicoMarker;
	}

	abstract public int getMaxObjects();
 
}