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

import android.content.Intent;

import pe.edu.esan.appostgrado.entidades.CustumMarker;
import pe.edu.esan.appostgrado.view.mas.ra.DescargaMarkerActivity;
import pe.edu.esan.appostgrado.view.mas.ra.DetalleMarkerActivity;
import pe.edu.esan.appostgrado.view.mas.ra.PrincipalRAActivity;
import pe.edu.esan.appostgrado.mixare.render.Matrix;
import pe.edu.esan.appostgrado.mixare.render.MixVector;

public class MixState {

	public static int NOT_STARTED = 0; 
	public static int PROCESSING = 1; 
	public static int READY = 2; 
	public static int DONE = 3; 

	int nextLStatus = MixState.NOT_STARTED;
	String downloadId;

	private float curBearing;
	private float curPitch;

	private boolean detailsView;

	public boolean handleEvent(MixContext ctx, Marker m) {
		try {
			if (DataView.viendoUnicoMarker)	{
				ctx.var.finish();
			}
			else	{
				Intent i = new Intent(PrincipalRAActivity.getContext(), DescargaMarkerActivity.class);
				i.putExtra(PrincipalRAActivity.PutExtraAR, "poi");
				DetalleMarkerActivity.setDetalleMarker((CustumMarker) m);
				i.putExtra("idPOI",  "setted");
				ctx.var.startActivity(i);
			}
		} catch (Exception ex) {
		}
		return true;
	}

	public boolean handleEvent_buscar(MixContext ctx, Marker m) {
		try {
			if (DataView.viendoUnicoMarker)	{
				ctx.var.finish();
			}
			else	{
				Intent i = new Intent(ctx, DescargaMarkerActivity.class);
				i.putExtra(PrincipalRAActivity.PutExtraAR, "poi");
				DetalleMarkerActivity.setDetalleMarker((CustumMarker) m);
				i.putExtra("idPOI",  "setted");
				ctx.startActivity(i);
				//ctx.var.startActivity(i);
			}
		} catch (Exception ex) {
		}
		return true;
	}

	public float getCurBearing() {
		return curBearing;
	}

	public float getCurPitch() {
		return curPitch;
	}
	
	public boolean isDetailsView() {
		return detailsView;
	}
	
	public void setDetailsView(boolean detailsView) {
		this.detailsView = detailsView;
	}

	public void calcPitchBearing(Matrix rotationM) {
		MixVector looking = new MixVector();
		rotationM.transpose();
		looking.set(1, 0, 0);
		looking.prod(rotationM);
		this.curBearing = (int) (MixUtils.getAngle(0, 0, looking.x, looking.z)  + 360 ) % 360 ;

		rotationM.transpose();
		looking.set(0, 1, 0);
		looking.prod(rotationM);
		this.curPitch = -MixUtils.getAngle(0, 0, looking.y, looking.z);
	}
}
