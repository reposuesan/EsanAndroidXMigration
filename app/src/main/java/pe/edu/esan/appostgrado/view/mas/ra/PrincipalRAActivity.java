package pe.edu.esan.appostgrado.view.mas.ra;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import java.io.File;
import java.util.Date;

import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.control.ControlUsuario;
import pe.edu.esan.appostgrado.mixare.DataView;
import pe.edu.esan.appostgrado.mixare.DownloadManager;
import pe.edu.esan.appostgrado.mixare.MixContext;
import pe.edu.esan.appostgrado.mixare.MixState;

public class PrincipalRAActivity extends AppCompatActivity {

    private static final String LOG = "PrincipalRAActivity";

    public static final String PutExtraAR = "valorREA";
    public static DataView dataView;
    public static final String TAG = "MainActivity_AR";
    public static String JSonCabecera = "";
    public static final String PREFS_NAME = "MyPrefsFileForMenuItems";
    private boolean isInited;
    private String buscar, accion;
    private MixContext mixContext;
    //public static List<Marker> markerListTemporalPrincipal;

    private static final int PERMISO_CAM_GPS = 0;
    private static final int PERMISO_CAMARA = 1;
    private static final int PERMISO_GPS = 2;

    public static MixContext getContext() {
        return dataView.getContext();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_ra);

        String valor = "";

        accion = getIntent().getExtras().getString("accion");
        if(accion.equals("individual")){
            valor = getIntent().getExtras().getString("buscar");
        }

        buscar = valor;

        if (!isInited) {
            mixContext = new MixContext(this);

            mixContext.downloadManager = new DownloadManager(mixContext);

            dataView = new DataView(mixContext);
            //ScheduleDetailsActivity.dataView.changeState(MixState.NOT_STARTED);
            isInited = true;
        }

        DataView.viendoUnicoMarker = false;
        DataView.enBusqueda = false;
        DataView.volverInicio = false;

        /*onValidaPermisosNecesarios();*/

        if(ValidaVisualizacion()){
            onRealizarAcciones();
        } else {
            Button btnAR = (Button) findViewById(R.id.btn_realidadaumentada_principalar);
            final CheckBox ckNoMostrar = (CheckBox) findViewById(R.id.ck_nomostrar_principalar);

            btnAR.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    /***Mostrar o no el activity de ra la próxima vez dependiendo del checkbox***/
                    if (ckNoMostrar.isChecked()) {
                        GuardarPreferencia();
                    }
                    onRealizarAcciones();

                }
            });
        }


    }

    /*private void onValidaPermisosNecesarios() {
        int iCamara = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int iGPS = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (iCamara != PackageManager.PERMISSION_GRANTED || iGPS != PackageManager.PERMISSION_GRANTED) {
            boolean pCamara = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA);
            boolean pGps = ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION);
            if (pCamara || pGps) {
                if (pCamara && pGps) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_CAM_GPS);
                } else {
                    if (pCamara) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISO_CAMARA);
                    }

                    if (pGps) {
                        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_GPS);
                    }
                }
            } else {
                //ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION), PERMISO_CAM_GPS);
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION}, PERMISO_CAM_GPS);
            }
        } else {
            ControlUsuario.Companion.getInstance().setAccesoCamara(true);
            ControlUsuario.Companion.getInstance().setAccesoGPS(true);
        }
    }*/


    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISO_CAM_GPS: {
                if (grantResults.length > 0) {
                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                        ControlUsuario.Companion.getInstance().setAccesoCamara(true);
                    } else {
                        ControlUsuario.Companion.getInstance().setAccesoCamara(false);
                        Snackbar.make(findViewById(android.R.id.content), "No se podrá hacer uso de la cámara", Snackbar.LENGTH_LONG).show();
                    }

                    if(grantResults[1] == PackageManager.PERMISSION_GRANTED){
                        ControlUsuario.Companion.getInstance().setAccesoGPS(true);
                    } else {
                        ControlUsuario.Companion.getInstance().setAccesoGPS(false);
                        Snackbar.make(findViewById(android.R.id.content), "No se podrá hacer uso del gps", Snackbar.LENGTH_LONG).show();
                    }
                } else {
                    Snackbar.make(findViewById(android.R.id.content), getString(R.string.no_camera_and_no_gps_usage_message), Snackbar.LENGTH_LONG).show();
                }
                return;
            }

            case PERMISO_CAMARA: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ControlUsuario.Companion.getInstance().setAccesoCamara(true);
                } else {
                    ControlUsuario.Companion.getInstance().setAccesoCamara(false);
                    Snackbar.make(findViewById(android.R.id.content), "No se podrá hacer uso de la cámara", Snackbar.LENGTH_LONG).show();
                }
            }

            case PERMISO_GPS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ControlUsuario.Companion.getInstance().setAccesoGPS(true);
                } else {
                    ControlUsuario.Companion.getInstance().setAccesoGPS(false);
                    Snackbar.make(findViewById(android.R.id.content), "No se podrá hacer uso del gps", Snackbar.LENGTH_LONG).show();
                }
            }

        }

    }

    private void onRealizarAcciones(){
        limpiar();

        if (accion.equals("individual")) {
            //Mostrar un solo punto
            JSonCabecera = "ListarDescripcionAmbienteResult";
            Log.i(TAG, buscar);
            Intent i = new Intent(getContext(), DescargaMarkerActivity.class);
            i.putExtra(PutExtraAR, "poi2");
            i.putExtra("idPOI", buscar);
            //i.putExtra(PutExtraAR, "RA");
            startActivity(i);
            finish();
        } else if(accion.equals("busqueda")){
            //Descarga todos los puntos importantes para mostrarlos posteriormente
            JSonCabecera = "ListarDescripcionAmbienteGrupoResult";
            Log.i(TAG, "MOSTRAR TODOS LOS PUNTOS IMPORTANTES");
            Intent i = new Intent(getContext(), DescargaMarkerActivity.class);
            i.putExtra(PutExtraAR, "arbuscar");
            startActivity(i);
            finish();
        }else {
            //Mostrar todos los puntos importantes
            JSonCabecera = "ListarDescripcionAmbienteGrupoResult";
            Log.i(TAG, "MOSTRAR TODOS LOS PUNTOS IMPORTANTES");
            Intent i = new Intent(getContext(), DescargaMarkerActivity.class);
            i.putExtra(PutExtraAR, "ar");
            startActivity(i);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (DataView.volverInicio)
            DataView.volverInicio = false;
    }

    @Override
    protected void onDestroy() {
        clearCache(mixContext, 0);
        /*if(dataView.getContext() != null){
            dataView.setMixContext();
            Log.d(LOG,"MixContext is null now");
        }*/
        super.onDestroy();
    }

    private void limpiar(){
        try {
            dataView.changeState(MixState.NOT_STARTED);
            dataView.getDataHandler().clearMarkers();
        }catch (Exception e){
            Log.e(LOG,"ERROR: " + e.getMessage());
        }
    }

    private boolean ValidaVisualizacion(){
        SharedPreferences mispreferencias = getSharedPreferences("MensajeAR", Context.MODE_PRIVATE);
        return mispreferencias.getBoolean("mostrar", false);
    }

    private void GuardarPreferencia(){
        SharedPreferences mispreferencias = getSharedPreferences("MensajeAR", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mispreferencias.edit();
        editor.putBoolean("mostrar", true);
        editor.commit();
    }

    //Helper method for clearCache() , recursive
    //It returns the number of deleted files
    static int clearCacheFolder(final File dir, final int numDays) {

        int deletedFiles = 0;
        if (dir != null && dir.isDirectory()) {
            try {
                for (File child : dir.listFiles()) {

                    // first delete subdirectories recursively
                    if (child.isDirectory()) {
                        deletedFiles += clearCacheFolder(child, numDays);
                    }

                    // then delete the files and subdirectories in this dir
                    // only empty directories can be deleted, so subdirs have
                    // been done first
                    if (child.lastModified() < new Date().getTime() - numDays * DateUtils.DAY_IN_MILLIS) {
                        if (child.delete()) {
                            deletedFiles++;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(LOG, String.format("Failed to clean the cache, error %s", e.getMessage()));
            }
        }
        return deletedFiles;
    }

    /*
     * Delete the files older than numDays days from the application cache 0
     * means all files.
     */
    public static void clearCache(final Context context, final int numDays) {
        /*Log.i(TAG, String.format(
                "Starting cache prune, deleting files older than %d days",
                numDays));*/
        int numDeletedFiles = clearCacheFolder(context.getCacheDir(), numDays);
        /*Log.i(TAG, String.format("Cache pruning completed, %d files deleted",
                numDeletedFiles));*/
    }
}
