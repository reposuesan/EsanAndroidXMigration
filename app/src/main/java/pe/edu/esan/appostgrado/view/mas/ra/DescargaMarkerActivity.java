package pe.edu.esan.appostgrado.view.mas.ra;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.entidades.CustumMarker;
import pe.edu.esan.appostgrado.mixare.DataView;
import pe.edu.esan.appostgrado.mixare.Marker;
import pe.edu.esan.appostgrado.mixare.MixContext;
import pe.edu.esan.appostgrado.mixare.MixState;
import pe.edu.esan.appostgrado.mixare.data.DataSource;

public class DescargaMarkerActivity extends AppCompatActivity {

    private DescargandoMarkersTask descargandoMarkersTask = null;
    private static DataView dataView;
    private MixContext mixContext;

    private List<Marker> markerList;

    private boolean detenerDescarga = false;
    private boolean exito;
    private boolean interrumpir;

    private String accion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_descarga_marker);

        dataView = PrincipalRAActivity.dataView;
        //dataView = f_comedor.dataView;
        mixContext = dataView.getContext();
        accion = getIntent().getExtras().getString(PrincipalRAActivity.PutExtraAR);
        //accion = getIntent().getExtras().getString(f_comedor.PutExtraAR);
        if(accion == null){
            finish();
        }

        if(descargandoMarkersTask == null){
            descargandoMarkersTask = new DescargandoMarkersTask(accion);
            descargandoMarkersTask.execute((Void) null);
        }
    }

    public class DescargandoMarkersTask extends AsyncTask<Void, Void, Boolean> {
        private final String accion;
        private final ProgressDialog progressDialog;

        public DescargandoMarkersTask(String accion){
            this.accion = accion;
            progressDialog = new ProgressDialog(DescargaMarkerActivity.this);
        }

        @Override
        protected  void onPreExecute()
        {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Descargando Ubicaciones ...");
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            return DescargarMarker();
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            descargandoMarkersTask = null;
            progressDialog.dismiss();
            finish();
            if(success){
                if (accion.equals("ar")) {
                    startActivity(new Intent(PrincipalRAActivity.getContext(), MixView.class));
                    //startActivity(new Intent(f_comedor.getContextF(), MixView.class));
                } else if (accion.equals("poi")) {
                    startActivity(new Intent(PrincipalRAActivity.getContext(), DetalleMarkerActivity.class));
                    //startActivity(new Intent(f_comedor.getContextF(), DetalleMarker.class));
                    //startActivity(new Intent(PrincipalAulaAR.getContext(), MixView.class));
                } else if (accion.equals("poi2")){
                    startActivity(new Intent(PrincipalRAActivity.getContext(), MixView.class));
                    //startActivity(new Intent(f_comedor.getContextF(), MixView.class));
                } else if(accion.equals("arbuscar")){
                    startActivity(new Intent(PrincipalRAActivity.getContext(), PrincipalAulaArBuscarActivity.class));
                    //startActivity(new Intent(f_comedor.getContextF(), principalAulaArBusca.class));
                }
            }else{
                Toast.makeText(mixContext.getApplicationContext(),"No se pudieron obtener las ubicaciones, compruebe la conexión a internet.",Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            descargandoMarkersTask = null;
            progressDialog.dismiss();
        }

        private boolean DescargarMarker(){
            exito = true;
            interrumpir = false;
            while (!detenerDescarga) {
                if (accion.equals("poi")) {
                    String idPoi = getIntent().getExtras().getString("idPOI");
                    if (!("setted".equals(idPoi))) {
                        Marker poi = dataView.getMarker(idPoi, DataSource.DATAFORMAT.LUGARES);
                        if (poi == null) {
                            exito = false;
                        } else {
                            List<Marker> lMark = new ArrayList<>();
                            lMark.add(poi);
                            dataView.getDataHandler().addMarkers(lMark);
                            dataView.valorEncontrado();
                            DetalleMarkerActivity.setDetalleMarker((CustumMarker) poi);
                        }
                    }
                } else if(accion.equals("poi2")){
                    String idPoi = getIntent().getExtras().getString("idPOI");
                    if (!("setted".equals(idPoi))) {
                        Marker poi = dataView.getMarker(idPoi, DataSource.DATAFORMAT.LUGARES);
                        if (poi == null) {
                            exito = false;
                        } else {
                            List<Marker> lMark = new ArrayList<>();
                            lMark.add(poi);
                            dataView.getDataHandler().addMarkers(lMark);
                            dataView.valorEncontrado();
                            DetalleMarkerActivity.setDetalleMarker((CustumMarker) poi);
                            PrincipalRAActivity.dataView.getDataHandler().clearMarkers();
                            PrincipalRAActivity.dataView.getDataHandler().setMarkerList(lMark);
                            //f_comedor.dataView.getDataHandler().clearMarkers();
                            //f_comedor.dataView.getDataHandler().setMarkerList(lMark);
                            DataView.viendoUnicoMarker = true;
                        }
                    }
                } else {
                    int estadoDescarga = PrincipalRAActivity.dataView.getEstado();
                    //int estadoDescarga = f_comedor.dataView.getEstado();
                    if (estadoDescarga != MixState.DONE) {
                        markerList = dataView.setDefaultMarkerList();
                        if ((markerList == null) || (markerList.isEmpty())) {
                            exito = false;
                        }
                    } else {
                        markerList = dataView.getDataHandler().getMarkerList();
                        if ((markerList == null) || (markerList.isEmpty())) {
                            markerList = dataView.setDefaultMarkerList();
                            if ((markerList == null) || (markerList.isEmpty())) {
                                exito = false;
                            }
                        }
                    }
                }
                detenerDescarga = true;
            }

            // ok, file is downloaded,
            if (exito && !interrumpir) {
                //Log.d(PrincipalAulaAR.TAG + "-HILO", "Terminó carga con éxito");
                return true;
            } else {
                if (!interrumpir) { //exito y !interrumpir
                    // no se interrumpio y no hubo éxito,
                    //pudo ser por error de conexion o porque no se encontraron resultados
                    if (markerList == null) { //error de conexión
                        return false;
                    } else {
                        //Log.d(PrincipalAulaAR.TAG + "-HILO", "No se encontraron resultados");
                        return true;
                    }
                }
                //Log.d(PrincipalAulaAR.TAG, "TERMINOOO");
            }
            return false;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!detenerDescarga) {
                detenerDescarga = true;
                interrumpir = true;
            }
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }
}
