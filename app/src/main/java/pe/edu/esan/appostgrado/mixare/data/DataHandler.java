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

import android.location.Location;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pe.edu.esan.appostgrado.mixare.Marker;
import pe.edu.esan.appostgrado.mixare.MixContext;

/**
 * DataHandler is the model which provides the Marker Objects.
 * 
 * DataHandler is also the Factory for new Marker objects.
 */
public class DataHandler {
	private static final String TAG = "DataHandler";
	private List<Marker> markerList = new ArrayList<Marker>();

	public void addMarkers(List<Marker> markers) {

		Log.v(TAG, "Marker before: " + markerList.size());
		for (Marker ma : markers) {
			if((!markerList.contains(ma)) && (!contieneCodigo(ma)))
					markerList.add(ma);
		}

		Log.d(TAG, "Marker count: " + markerList.size());
	}

	public void sortMarkerList() {
		Collections.sort(markerList);
	}

	public void updateDistances(Location location) {
		for (Marker ma : markerList) {
			float[] dist = new float[3];
			Location.distanceBetween(ma.getLatitude(), ma.getLongitude(),
					location.getLatitude(), location.getLongitude(), dist);
			ma.setDistance(dist[0]);
		}
	}

	public void updateActivationStatus(MixContext mixContext) {
		if (markerList.size() == 1)	{
			Marker ma = markerList.get(0);
			ma.setActive(true);
			ma.setUnicoMarker(true);
		}
		else	{
			for (Marker ma : markerList) {
				ma.setActive(true);
				ma.setUnicoMarker(false);
			}
		}
	}

	public void onLocationChanged(Location location) {
		updateDistances(location);
		sortMarkerList();
		for (Marker ma : markerList) {
			ma.update(location);
		}
	}

	/**
	 * @deprecated Nobody should get direct access to the list
	 */
	public List<Marker> getMarkerList() {
		return markerList;
	}

	public void setMarkerList(List<Marker> markerList) {
		this.markerList = markerList;
	}

	public void clearMarkers() {
		markerList.clear();
	}
	
	public int getMarkerCount() {
		return markerList.size();
	}

	public Marker getMarker(int index) {
		return markerList.get(index);
	}

	private boolean contieneCodigo(Marker ma) {
		for (int i = 0; i < markerList.size(); i++) {
			if (markerList.get(i).getID().equals(ma.getID())) {
				return true;
			}
		}
		return false;
	}
}
