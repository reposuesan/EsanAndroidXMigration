/*
	Copyright (C) 2010- Peer internet solutions
	
	This file is part of ESAN App

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

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import pe.edu.esan.appostgrado.mixare.data.DataSource.DATAFORMAT;
import pe.edu.esan.appostgrado.mixare.data.DataSource.DATASOURCE;
import pe.edu.esan.appostgrado.mixare.data.Json;
import pe.edu.esan.appostgrado.view.mas.ra.PrincipalRAActivity;

public class DownloadManager implements Runnable {

	private boolean stop = false, pause = false, proceed = false;
	public static int NOT_STARTED = 0, CONNECTING = 1, CONNECTED = 2,
			PAUSED = 3, STOPPED = 4;
	private int state = NOT_STARTED;

	private int id = 0;
	private HashMap<String, DownloadRequest> todoList = new HashMap<String, DownloadRequest>();
	private HashMap<String, DownloadResult> doneList = new HashMap<String, DownloadResult>();
	InputStream is;

	private String currJobId = null;

	MixContext ctx;

	public DownloadManager(MixContext ctx) {
		this.ctx = ctx;
	}

	public void run() {
		Log.v("HILO", "hilo - run" + Thread.currentThread().getId());
		String jobId;
		DownloadRequest request;
		DownloadResult result;

		stop = false;
		pause = false;
		proceed = false;
		state = CONNECTING;

		while (!stop) {
			jobId = null;
			request = null;
			result = null;

			// Wait for proceed
			while (!stop && !pause) {
				synchronized (this) {
					if (todoList.size() > 0) {
						jobId = getNextReqId();
						request = todoList.get(jobId);
						proceed = true;
					}
				}
				// Do proceed
				if (proceed) {
					state = CONNECTED;
					currJobId = jobId;

					result = processRequest(request);

					synchronized (this) {
						todoList.remove(jobId);
						doneList.put(jobId, result);
						proceed = false;
					}
				}
				state = CONNECTING;

				if (!stop && !pause)
					sleep(100);
			}

			// Do pause
			if (pause)
				Log.v("ARPUCP-HILO", "hilo - pause"
						+ Thread.currentThread().getId());
			while (!stop && pause) {
				state = PAUSED;
				sleep(100);
			}
			state = CONNECTING;
			if (!stop && !pause)
				Log.v("ARPUCP-HILO", "hilo - restart"
						+ Thread.currentThread().getId());
		}
		// Do stop
		state = STOPPED;
		Log.v("ARPUCP-HILO", "hilo - stop" + Thread.currentThread().getId());
	}

	public int checkForConnection() {
		return state;
	}

	private void sleep(long ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException ex) {

		}
	}

	private String getNextReqId() {
		return todoList.keySet().iterator().next();
	}

	private DownloadResult processRequest(DownloadRequest request) {
		DownloadResult result = new DownloadResult();
		// assume an error until everything is fine
		result.error = true;
		try {
			// if(ctx!=null && request!=null &&
			// ctx.getHttpGETInputStream(request.url)!=null){
			//
			// is = ctx.getHttpGETInputStream(request.url);
			if (ctx != null && request != null) {

				is = ctx.getHttpGETInputStream(request.url);
				if (is != null) {
					String tmp = ctx.getHttpInputString(is);

					Json layer = new Json();

					// try loading JSON DATA
					try {

						Log.v(PrincipalRAActivity.TAG, "try to load JSON data: "+tmp);

						JSONObject root = new JSONObject(tmp);

						Log.d(PrincipalRAActivity.TAG, "loading JSON data");

						List<Marker> markers = layer.load(root, request.format);
						result.setMarkers(markers);

						result.format = request.format;
						result.source = request.source;
						result.error = false;
						result.errorMsg = null;

					} catch (JSONException e) {

						Log.v(PrincipalRAActivity.TAG, "no JSON data");
//						Log.v(ScheduleDetailsActivity.TAG, "try to load XML data");
//
//						try {
//							DocumentBuilder builder = DocumentBuilderFactory
//									.newInstance().newDocumentBuilder();
//							// Document doc = builder.parse(is);d
//							Document doc = builder.parse(new InputSource(
//									new StringReader(tmp)));
//
//							// Document doc = builder.parse(is);
//
//							XMLHandler xml = new XMLHandler();
//
//							Log.i(ScheduleDetailsActivity.TAG, "loading XML data");
//
//							List<Marker> markers = xml.load(doc);
//							result.setMarkers(markers);
//
//							result.format = request.format;
//							result.error = false;
//							result.errorMsg = null;
//						} catch (Exception e1) {
//							e1.printStackTrace();
//						}
					}
					ctx.returnHttpInputStream(is);
					is = null;
				}
			}
		} catch (Exception ex) {
			result.errorMsg = ex.getMessage();
			result.errorRequest = request;

			try {
				ctx.returnHttpInputStream(is);
			} catch (Exception ignore) {
			}

			ex.printStackTrace();
		}

		currJobId = null;

		return result;
	}

	public synchronized void purgeLists() {
		todoList.clear();
		doneList.clear();
	}

	public synchronized String submitJob(DownloadRequest job) {
		if (job != null) {
			String jobId = "ID_" + (id++);
			todoList.put(jobId, job);
			Log.i(PrincipalRAActivity.TAG, "Submitted Job with " + jobId + ", format: "
					+ job.format + ", params: " + job.params + ", url: "
					+ job.url);
			return jobId;
		}
		return null;
	}

	public synchronized boolean isReqComplete(String jobId) {
		return doneList.containsKey(jobId);
	}

	public synchronized DownloadResult getReqResult(String jobId) {
		DownloadResult result = doneList.get(jobId);
		doneList.remove(jobId);

		return result;
	}

	public String getActiveReqId() {
		return currJobId;
	}

	public void pause() {
		pause = true;
	}

	public void restart() {
		pause = false;
	}

	public void stop() {
		stop = true;
	}

	public synchronized DownloadResult getNextResult() {
		if (!doneList.isEmpty()) {
			String nextId = doneList.keySet().iterator().next();
			DownloadResult result = doneList.get(nextId);
			doneList.remove(nextId);
			return result;
		}
		return null;
	}

	public Boolean isDone() {
		return todoList.isEmpty();
	}

	public DownloadResult getDefaultMarkers() {
		DownloadRequest request;
		String jobId;

		jobId = getNextReqId();
		request = todoList.get(jobId);
		purgeLists();
		return processRequest(request);
	}
}

class DownloadRequest {

	public DATAFORMAT format;
	public DATASOURCE source;
	String url;
	String params;
}

class DownloadResult {
	public DATAFORMAT format;
	public DATASOURCE source;
	List<Marker> markers;

	boolean error;
	String errorMsg;
	DownloadRequest errorRequest;

	public List<Marker> getMarkers() {
		return markers;
	}

	public void setMarkers(List<Marker> markers) {
		this.markers = markers;
	}

}
