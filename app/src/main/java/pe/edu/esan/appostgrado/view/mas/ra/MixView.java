package pe.edu.esan.appostgrado.view.mas.ra;

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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.provider.Settings;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Iterator;
import java.util.List;

import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.mixare.Compatibility;
import pe.edu.esan.appostgrado.mixare.DataView;
import pe.edu.esan.appostgrado.mixare.MixContext;
import pe.edu.esan.appostgrado.mixare.MixState;
import pe.edu.esan.appostgrado.mixare.gui.PaintScreen;
import pe.edu.esan.appostgrado.mixare.render.Matrix;

import static android.hardware.SensorManager.SENSOR_DELAY_GAME;

public class MixView extends AppCompatActivity implements SensorEventListener,LocationListener {

    static final int DESCARGA_POI = 0;

    private CameraSurface camScreen;
    private AugmentedView augScreen;

    private MixContext mixContext;
    public static PaintScreen dWindow;

    private float RTmp[] = new float[9];
    private float Rot[] = new float[9];
    private float I[] = new float[9];
    private float grav[] = new float[3];
    private float mag[] = new float[3];

    private SensorManager sensorMgr;
    private List<Sensor> sensors;
    private Sensor sensorGrav, sensorMag;
    private LocationManager locationMgr;

    private int rHistIdx = 0;
    private Matrix tempR = new Matrix();
    private Matrix finalR = new Matrix();
    private Matrix smoothR = new Matrix();
    private Matrix histR[] = new Matrix[60];
    private Matrix m1 = new Matrix();
    private Matrix m2 = new Matrix();
    private Matrix m3 = new Matrix();
    private Matrix m4 = new Matrix();

    private SeekBar myZoomBar;
    private ImageButton btnZoomRadar;
    private WakeLock mWakeLock;

    private boolean fError;

    private String zoomLevel;
    private int zoomProgress;

    private ProgressDialog pd;

    private CountDownTimer timeOut;

    public boolean isZoombarVisible() {
        return myZoomBar != null && myZoomBar.getVisibility() == View.VISIBLE;
    }

    public String getZoomLevel() {
        return zoomLevel;
    }

    public int getZoomProgress() {
        return zoomProgress;
    }

    public void doError(Exception ex1) {
        if (!fError) {
            fError = true;

            setErrorDialog();

            ex1.printStackTrace();
            try {
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }

        try {
            augScreen.invalidate();
        } catch (Exception ignore) {
        }
    }

    public void killOnError() throws Exception {
        if (fError)
            throw new Exception();
    }

    public void repaint() {
        PrincipalRAActivity.dataView = new DataView(mixContext);
        dWindow = new PaintScreen();
        setZoomLevel();
    }

    public void setErrorDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.connection_error_dialog));
        builder.setCancelable(false);

		/*Retry*/
        builder.setPositiveButton(R.string.connection_error_dialog_button1, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                fError=false;
                finish();
                Intent i = new Intent(PrincipalRAActivity.getContext(), DescargaMarkerActivity.class);
                i.putExtra(PrincipalRAActivity.PutExtraAR, "ar");
                startActivity(i);
            }
        });
		/*Open settings*/
        builder.setNeutralButton(R.string.connection_error_dialog_button2, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Intent intent1 = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(intent1, 42);
            }
        });
		/*Close application*/
        builder.setNegativeButton(R.string.connection_error_dialog_button3, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void setProgressDialog(){
        pd = new ProgressDialog(this);
        pd.setCancelable(false);
        pd.setMessage("Espere: Obteniendo ubicación");

        pd.setButton("Cancelar", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int which)
            {
                timeOut.cancel();
                String nmc = mixContext.noMarkersCheck();
                if (nmc != null)	{
                    Toast.makeText(mixContext, nmc, Toast.LENGTH_LONG).show();
                }
                else	{
                    int radioMetros = (int) Math.round(calcZoomLevel() * 1000);
                    if (mixContext.noMarkerInRadius(radioMetros))	{
                        Toast.makeText(mixContext, "Ajuste el radio de visibilidad presionando sobre el ícono de Radar", Toast.LENGTH_LONG).show();
                    }
                }

            }
        });
        pd.show();
        timeOut = new CountDownTimer(40000, 1000) {

            public void onTick(long millisUntilFinished) {
                //mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
                Log.i("CUENTA","seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                //mTextField.setText("done!");
                if(pd != null){
                    pd.dismiss();
                }
                String nmc = mixContext.noMarkersCheck();
                if (nmc != null)	{
                    if(pd != null) {
                        Toast.makeText(mixContext, nmc, Toast.LENGTH_LONG).show();
                    }
                }
                else	{
                    int radioMetros = (int) Math.round(calcZoomLevel() * 1000);
                    if (mixContext.noMarkerInRadius(radioMetros))	{
                        Toast.makeText(mixContext, "Ajuste el radio de visibilidad presionando sobre el ícono de Radar", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }.start();
    }



    @Override
    protected void onDestroy() {
        pd.dismiss();
        pd = null;
        super.onDestroy();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "myapp:mywakelocktag");
            locationMgr=(LocationManager)getSystemService(Context.LOCATION_SERVICE);
//			locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,10, this);

            killOnError();
            //requestWindowFeature(Window.FEATURE_NO_TITLE);

            myZoomBar = new SeekBar(this);
            myZoomBar.setVisibility(View.GONE);
            //myZoomBar.setBackgroundColor(getResources().getColor(R.color.EsanColor));
            //myZoomBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress));
            myZoomBar.setMax(100);
            myZoomBar.setProgress(20); //x*20 = 200m
            myZoomBar.setOnSeekBarChangeListener(myZoomBarOnSeekBarChangeListener);



            btnZoomRadar = new ImageButton(this);
            btnZoomRadar.setVisibility(View.VISIBLE);
            btnZoomRadar.setImageResource(R.mipmap.btnradar);
            btnZoomRadar.setClickable(true);
            //btnZoomRadar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            btnZoomRadar.setOnClickListener(myClicListenerRadar);

            //FrameLayout frmLayoutButton = new FrameLayout(this);
            //frmLayoutButton.addView(btnZoomRadar);


            FrameLayout frameLayout = new FrameLayout(this);
            frameLayout.addView(myZoomBar);

            FrameLayout frameLayout2 = new FrameLayout(this);
            frameLayout2.addView(btnZoomRadar);

            //frameLayout.addView(btnZoomRadar);
            frameLayout.setPadding(10, 10, 10, 10);
            frameLayout2.setPadding(25, 25, 25, 25);
            dWindow = new PaintScreen();
            camScreen = new CameraSurface(this);
            augScreen = new AugmentedView(this);
            setContentView(camScreen);

            addContentView(augScreen, new LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

            addContentView(frameLayout, new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,
                    Gravity.BOTTOM));

            addContentView(frameLayout2, new FrameLayout.LayoutParams(
                    LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
                    Gravity.RIGHT));

            mixContext = PrincipalRAActivity.getContext();
            //mixContext = f_comedor.getContextF();
            mixContext.var = this;

            setZoomLevel();

            setProgressDialog();
        } catch (Exception ex) {
            doError(ex);
        }
    }



    @Override
    protected void onPause() {
        super.onPause();

        try {
            this.mWakeLock.release();

            try {
                sensorMgr.unregisterListener(this, sensorGrav);
            } catch (Exception ignore) {
            }
            try {
                sensorMgr.unregisterListener(this, sensorMag);
            } catch (Exception ignore) {
            }
            sensorMgr = null;

            try {
                locationMgr.removeUpdates(this);
            } catch (Exception ignore) {
            }
            locationMgr = null;

            try {
                mixContext.downloadManager.stop();
            } catch (Exception ignore) {
            }

            if (fError) {
                finish();
            }
        } catch (Exception ex) {
            doError(ex);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (DataView.volverInicio)	{
            finish();
            return;
        }

        int estadoDescarga = PrincipalRAActivity.dataView.getEstado();
        if ((estadoDescarga != MixState.DONE) || (DataView.actualizarMarkers)) {
            DataView.actualizarMarkers = false;
            Intent i = new Intent(PrincipalRAActivity.getContext(), DescargaMarkerActivity.class);
            i.putExtra(PrincipalRAActivity.PutExtraAR, "ar");
            startActivity(i);
            this.finish();
            return;
        }

        try {
            this.mWakeLock.acquire();

            killOnError();
            mixContext.var = this;
            double angleX, angleY;

            int marker_orientation = -90;
            int rotation = Compatibility.getRotation(this);

            //display text from left to right and keep it horizontal
            angleX = Math.toRadians(marker_orientation);
            m1.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX),
                    (float) -Math.sin(angleX), 0f, (float) Math.sin(angleX),
                    (float) Math.cos(angleX));

            angleX = Math.toRadians(marker_orientation);
            angleY = Math.toRadians(marker_orientation);
            if (rotation == 1) {
                m2.set(1f, 0f, 0f, 0f, (float) Math.cos(angleX),
                        (float) -Math.sin(angleX), 0f,
                        (float) Math.sin(angleX), (float) Math.cos(angleX));
                m3.set((float) Math.cos(angleY), 0f, (float) Math.sin(angleY),
                        0f, 1f, 0f, (float) -Math.sin(angleY), 0f,
                        (float) Math.cos(angleY));
            } else {
                m2.set((float) Math.cos(angleX), 0f, (float) Math.sin(angleX),
                        0f, 1f, 0f, (float) -Math.sin(angleX), 0f,
                        (float) Math.cos(angleX));
                m3.set(1f, 0f, 0f, 0f, (float) Math.cos(angleY),
                        (float) -Math.sin(angleY), 0f,
                        (float) Math.sin(angleY), (float) Math.cos(angleY));
            }
            m4.toIdentity();

            for (int i = 0; i < histR.length; i++) {
                histR[i] = new Matrix();
            }

            sensorMgr = (SensorManager) getSystemService(SENSOR_SERVICE);

            sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER);
            if (!sensors.isEmpty()) {
                if (sensors.size() > 0) {
                    sensorGrav = sensors.get(0);
                }
            }else{
                Toast toast  = Toast.makeText(mixContext,"Sensor acelerometro no disponible", Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.error_toast);
                TextView text = (TextView) view.findViewById(android.R.id.message);
                text.setTextColor(getResources().getColor(R.color.md_white_1000));
                toast.show();
            }

            sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
            if (!sensors.isEmpty()) {
                if (sensors.size() > 0) {
                    sensorMag = sensors.get(0);
                }
            }else{
                Toast toast = Toast.makeText(mixContext,"Sensor brujula no disponible", Toast.LENGTH_LONG);
                View view = toast.getView();
                view.setBackgroundResource(R.drawable.error_toast);
                TextView text = (TextView) view.findViewById(android.R.id.message);
                text.setTextColor(getResources().getColor(R.color.md_white_1000));
                toast.show();
            }

            sensorMgr.registerListener(this, sensorGrav, SENSOR_DELAY_GAME);
            sensorMgr.registerListener(this, sensorMag, SENSOR_DELAY_GAME);

            try {
                Criteria c = new Criteria();

                c.setAccuracy(Criteria.ACCURACY_FINE);
                //c.setBearingRequired(true);

                locationMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                locationMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000,10, this);
                locationMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000,10, this);

                if ( (!locationMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) &&
                        (!locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) )	{
                    timeOut.cancel();
                    pd.dismiss();
                    setErrorDialog();
                }
                try {
                    Location gps=locationMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    Location network=locationMgr.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                    if(gps!=null && network!=null){
                        if(gps.getTime()>network.getTime())
                            mixContext.curLoc = gps;
                        else
                            mixContext.curLoc = network;
                    }
                    else if (gps != null)
                        mixContext.curLoc = gps;
                    else if (network != null)
                        mixContext.curLoc = network;

                } catch (SecurityException ex2) {
                    ex2.printStackTrace();
                }

                mixContext.setLocationAtLastDownload(mixContext.curLoc);

                GeomagneticField gmf = new GeomagneticField((float) mixContext.curLoc
                        .getLatitude(), (float) mixContext.curLoc.getLongitude(),
                        (float) mixContext.curLoc.getAltitude(), System
                        .currentTimeMillis());

                angleY = Math.toRadians(-gmf.getDeclination());
                m4.set((float) Math.cos(angleY), 0f,
                        (float) Math.sin(angleY), 0f, 1f, 0f, (float) -Math
                                .sin(angleY), 0f, (float) Math.cos(angleY));
                mixContext.declination = gmf.getDeclination();
            } catch (SecurityException ex) {
                Log.d("mixare", "GPS Initialize Error", ex);
            }

        } catch (Exception ex) {
            doError(ex);
            try {
                if (sensorMgr != null) {
                    sensorMgr.unregisterListener(this, sensorGrav);
                    sensorMgr.unregisterListener(this, sensorMag);
                    sensorMgr = null;
                }
                if (locationMgr != null) {
                    locationMgr.removeUpdates(this);
                    locationMgr = null;
                }
                if (mixContext != null) {
                    if (mixContext.downloadManager != null)
                        mixContext.downloadManager.stop();
                }
            } catch (Exception ignore) {
            }
        }
    }

    public float calcZoomLevel(){
        float myout = (float) ((myZoomBar.getProgress() / 100f) * 2f);
        return myout;
    }

    private void setZoomLevel() {
        float myout = calcZoomLevel();

        PrincipalRAActivity.dataView.setRadius(myout);
        btnZoomRadar.setVisibility(View.VISIBLE);
        myZoomBar.setVisibility(View.GONE);
        zoomLevel = String.valueOf(myout);
    };

    private ImageButton.OnClickListener myClicListenerRadar = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            btnZoomRadar.setVisibility(View.GONE);
            myZoomBar.setVisibility(View.VISIBLE);
            zoomProgress = myZoomBar.getProgress();
        }
    };

    private SeekBar.OnSeekBarChangeListener myZoomBarOnSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        Toast t;

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            float myout = calcZoomLevel();

            zoomLevel = String.valueOf(myout);
            zoomProgress = myZoomBar.getProgress();

            if (myout < 1f)	{
                int radioMetros = (int) Math.round(myout * 1000);
                t.setText("Radio del Radar: " + String.valueOf(radioMetros) + " m");
            }
            else	{
                t.setText("Radio del Radar: " + String.format("%.2g%n", myout) + " km");
            }

            t.show();
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            Context ctx = seekBar.getContext();
            t = Toast.makeText(ctx, "Radio del Radar: ", Toast.LENGTH_LONG);
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            myZoomBar.setVisibility(View.GONE);
            btnZoomRadar.setVisibility(View.VISIBLE);

            myZoomBar.getProgress();

            t.cancel();
            setZoomLevel();

            int radioMetros = (int) Math.round(calcZoomLevel() * 1000);
            String nmc = mixContext.noMarkersCheck();
            if ( (mixContext.noMarkerInRadius(radioMetros)) && (nmc==null) )	{
                Toast.makeText(mixContext, "Ajuste el radio de visión para ver más lugares presionando sobre el ícono verde con forma de radar", Toast.LENGTH_LONG).show();
            }
        }

    };


    public void onSensorChanged(SensorEvent evt) {
        try {
            if (evt.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                grav[0] = evt.values[0];
                grav[1] = evt.values[1];
                grav[2] = evt.values[2];

                augScreen.postInvalidate();
            } else if (evt.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                mag[0] = evt.values[0];
                mag[1] = evt.values[1];
                mag[2] = evt.values[2];

                augScreen.postInvalidate();
            }

            SensorManager.getRotationMatrix(RTmp, I, grav, mag);
            int rotation = Compatibility.getRotation(this);
            if (rotation == 1) {
                SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_X,
                        SensorManager.AXIS_MINUS_Z, Rot);
            } else {
                SensorManager.remapCoordinateSystem(RTmp, SensorManager.AXIS_Y,
                        SensorManager.AXIS_MINUS_Z, Rot);
            }
            tempR.set(Rot[0], Rot[1], Rot[2], Rot[3], Rot[4], Rot[5], Rot[6], Rot[7],
                    Rot[8]);

            finalR.toIdentity();
            finalR.prod(m4);
            finalR.prod(m1);
            finalR.prod(tempR);
            finalR.prod(m3);
            finalR.prod(m2);
            finalR.invert();

            histR[rHistIdx].set(finalR);
            rHistIdx++;
            if (rHistIdx >= histR.length)
                rHistIdx = 0;

            smoothR.set(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f);
            for (int i = 0; i < histR.length; i++) {
                smoothR.add(histR[i]);
            }
            smoothR.mult(1 / (float) histR.length);

            synchronized (mixContext.rotationM) {
                mixContext.rotationM.set(smoothR);
            }
        } catch (Exception ex) {

            ex.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        try {
            killOnError();

            Rect rectgle= new Rect();
            Window window= getWindow();
            window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
            int statusBarHeight= rectgle.top;

            float xPress = me.getRawX();
            float yPress = me.getRawY() - statusBarHeight;
            if (me.getAction() == MotionEvent.ACTION_UP) {
                PrincipalRAActivity.dataView.clickEvent(xPress, yPress);
            }

            return true;
        } catch (Exception ex) {
            //doError(ex);
            ex.printStackTrace();
            return super.onTouchEvent(me);
        }
    }


    public void onProviderDisabled(String provider) {
//		isGpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void onProviderEnabled(String provider) {
//		isGpsEnabled = locationMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    public void onLocationChanged(Location location) {
        try {
            killOnError();
            location.setAltitude(0.0f);

            mixContext.setLocationAtLastDownload(location);

            if (mixContext.isBetterLocation(location, mixContext.curLoc))	{
                synchronized (mixContext.curLoc) {
                    mixContext.curLoc = location;
                }

                if (!PrincipalRAActivity.dataView.isFrozen())
                    PrincipalRAActivity.dataView.getDataHandler().onLocationChanged(location);

            }
            // setGpsEnabled(true);
            if ((!DataView.viendoUnicoMarker) && (pd.isShowing()))	{
                timeOut.cancel();
                pd.dismiss();
            }

            if (pd.isShowing())	{
                timeOut.cancel();
                pd.dismiss();
                String nmc = mixContext.noMarkersCheck();
                int radioMetros = (int) Math.round(calcZoomLevel() * 1000);
                if ( (mixContext.noMarkerInRadius(radioMetros)) && (nmc == null) )	{
                    Toast.makeText(mixContext, "Ajuste el radio de visibilidad mediante el menú de opciones para ver los lugares.", Toast.LENGTH_LONG).show();
                }
            }
            //actualización de altura de markers

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}
















/**
 * @author daniele
 *
 */
class CameraSurface extends SurfaceView implements SurfaceHolder.Callback {
    MixView app;
    SurfaceHolder holder;
    Camera camera;

    CameraSurface(Context context) {
        super(context);

        try {
            app = (MixView) context;

            holder = getHolder();
            holder.addCallback(this);
            holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        } catch (Exception ex) {

        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (Exception ignore) {
                }
                try {
                    camera.release();
                } catch (Exception ignore) {
                }
                camera = null;
            }

            camera = Camera.open();
            camera.setPreviewDisplay(holder);
        } catch (Exception ex) {
            try {
                if (camera != null) {
                    try {
                        camera.stopPreview();
                    } catch (Exception ignore) {
                    }
                    try {
                        camera.release();
                    } catch (Exception ignore) {
                    }
                    camera = null;
                }
            } catch (Exception ignore) {

            }
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        try {
            if (camera != null) {
                try {
                    camera.stopPreview();
                } catch (Exception ignore) {
                }
                try {
                    camera.release();
                } catch (Exception ignore) {
                }
                camera = null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        try {
            Camera.Parameters parameters = camera.getParameters();
            try {
                List<Camera.Size> supportedSizes = null;
                //On older devices (<1.6) the following will fail
                //the camera will work nevertheless
                supportedSizes = Compatibility.getSupportedPreviewSizes(parameters);

                //preview form factor
                float ff = (float)w/h;

                //holder for the best form factor and size
                float bff = 0;
                int bestw = 0;
                int besth = 0;
                Iterator<Camera.Size> itr = supportedSizes.iterator();

                //we look for the best preview size, it has to be the closest to the
                //screen form factor, and be less wide than the screen itself
                //the latter requirement is because the HTC Hero with update 2.1 will
                //report camera preview sizes larger than the screen, and it will fail
                //to initialize the camera
                //other devices could work with previews larger than the screen though
                while(itr.hasNext()) {
                    Camera.Size element = itr.next();
                    //current form factor
                    float cff = (float)element.width/element.height;
                    //check if the current element is a candidate to replace the best match so far
                    //current form factor should be closer to the bff
                    //preview width should be less than screen width
                    //preview width should be more than current bestw
                    //this combination will ensure that the highest resolution will win
                    if ((ff-cff <= ff-bff) && (element.width <= w) && (element.width >= bestw)) {
                        bff=cff;
                        bestw = element.width;
                        besth = element.height;
                    }
                }
                //Some Samsung phones will end up with bestw and besth = 0 because their minimum preview size is bigger then the screen size.
                //In this case, we use the default values: 480x320
                if ((bestw == 0) || (besth == 0)){
                    bestw = 480;
                    besth = 320;
                }
                parameters.setPreviewSize(bestw, besth);
            } catch (Exception ex) {
                parameters.setPreviewSize(480 , 320);
            }

            camera.setParameters(parameters);
            camera.startPreview();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}














class AugmentedView extends View {
    MixView app;
    int xSearch=200;
    int ySearch = 10;
    int searchObjWidth = 0;
    int searchObjHeight=0;

    public AugmentedView(Context context) {
        super(context);

        try {
            app = (MixView) context;

            app.killOnError();
        } catch (Exception ex) {
            app.doError(ex);
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {

            app.killOnError();

            MixView.dWindow.setWidth(canvas.getWidth());
            MixView.dWindow.setHeight(canvas.getHeight());

            MixView.dWindow.setCanvas(canvas);


            PrincipalRAActivity.dataView.init(MixView.dWindow.getWidth(), MixView.dWindow.getHeight());

            if (app.isZoombarVisible()){
                Paint zoomPaint = new Paint();
                zoomPaint.setColor(Color.WHITE);
                zoomPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                zoomPaint.setTextSize(14);
                String startKM, endKM;
                endKM = "2km";
                startKM = "0m";
                canvas.drawText(startKM, canvas.getWidth()/100*4, canvas.getHeight()/100*90, zoomPaint);
                canvas.drawText(endKM, canvas.getWidth()/100*93, canvas.getHeight()/100*90, zoomPaint);
            }

            PrincipalRAActivity.dataView.draw(MixView.dWindow);

        } catch (Exception ex) {
            app.doError(ex);
        }
    }
}