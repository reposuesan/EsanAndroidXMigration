package pe.edu.esan.appostgrado.view.mas.ra;

import android.content.Intent;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.entidades.CustumMarker;
import pe.edu.esan.appostgrado.mixare.DataView;
import pe.edu.esan.appostgrado.mixare.Marker;
import pe.edu.esan.appostgrado.mixare.MixContext;
import pe.edu.esan.appostgrado.util.Utilitarios;

public class DetalleMarkerActivity extends AppCompatActivity {

    private MixContext mixContext;
    private static CustumMarker oneMarker;
    private static ArrayList<CustumMarker> lCustumMarker = new ArrayList<>();
    private static List<Marker> markerListTemporal;
    private HacerTiempo HacerTiempoTask = null;

    private View viewContent, viewContent_Cargando;

    private static final String LOG = "DetalleMarkerActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_marker);

        mixContext = PrincipalRAActivity.dataView.getContext();
        //mixContext = f_comedor.dataView.getContextF();

        if (oneMarker == null) {
            Toast.makeText(this, "Error, No se pudo obtener información del sitio", Toast.LENGTH_SHORT).show();
            finish();
        }

        oneMarker.setActive(true);

        TextView lblTipo, lblId, lblDetalle, lblUbicacion, lblTitulo;
        Button btnOneMarkerAR;
        ImageView imgCategoria;

        lblTipo = (TextView)findViewById(R.id.txtDataSource);
        lblId = (TextView)findViewById(R.id.txtId);
        lblTitulo = (TextView)findViewById(R.id.txtTitulo);
        lblDetalle = (TextView)findViewById(R.id.txtDetalle);
        lblUbicacion = (TextView)findViewById(R.id.txtUbicacion);
        btnOneMarkerAR = (Button)findViewById(R.id.btn_onmarker_RA);
        imgCategoria = (ImageView)findViewById(R.id.imgDetArTipo);
        viewContent = findViewById(R.id.viewContent);
        viewContent_Cargando = findViewById(R.id.viewContent_Cargando);

        final SpannableStringBuilder Titulo = new SpannableStringBuilder(String.format(getResources().getString(R.string.ra_d_nombre),oneMarker.getTitle()));
        final SpannableStringBuilder Detalle = new SpannableStringBuilder(String.format(getResources().getString(R.string.ra_d_detalle),oneMarker.getDescripcion()));
        //final SpannableStringBuilder Ubicacion = new SpannableStringBuilder(String.format(getResources().getString(R.string.d_ubicacion),oneMarker.getUbicacion()));
        final SpannableStringBuilder Ubicacion = new SpannableStringBuilder(String.format(getResources().getString(R.string.ra_d_ubicacion),"Ya no existe este parametro"));

        final StyleSpan bold = new StyleSpan(android.graphics.Typeface.BOLD);

        Titulo.setSpan(bold, 0, 7, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        Detalle.setSpan(bold, 0, 8, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        Ubicacion.setSpan(bold, 0, 10, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        lblTipo.setText(mixContext.getDataSourceName(oneMarker.getDatasource()));
        lblId.setText(oneMarker.getID());
        lblTitulo.setText(Titulo);
        lblDetalle.setText(Detalle);
        lblUbicacion.setText(Ubicacion);

        if(oneMarker.getImagen()==null){
            imgCategoria.setImageDrawable(getResources().getDrawable(mixContext.getLocalDataSourceImagen(oneMarker.getDatasource())));
        }else{
            imgCategoria.setImageBitmap(oneMarker.getImagen());
        }
        //imgCategoria.setImageDrawable(getResources().getDrawable(mixContext.getDataSourceImagen(oneMarker.getDatasource())));

        btnOneMarkerAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verAR(true);
            }
        });

        if(!Utilitarios.Companion.getMostrarDetalleMarker()){
            /**Si entra aqui carga automaticamente la pantalla de ver solo ese puntos y finalizamos este activity para que no vuelva a ingresar.**/
            //verAR(false);
            viewContent.setVisibility(View.GONE);
            viewContent_Cargando.setVisibility(View.VISIBLE);
            HacerTiempoTask = new HacerTiempo(0);/*CERO IDA*/
            HacerTiempoTask.execute((Void) null);
        }
    }

    public static void setDetalleMarker(CustumMarker oneMarker) {
        DetalleMarkerActivity.oneMarker = oneMarker;
    }

    private void verAR(boolean regresa){
        oneMarker.setUnicoMarker(true);
        List<Marker> markerList = new ArrayList<>();
        markerList.add(oneMarker);

        List<Marker> markerListActual = PrincipalRAActivity.dataView.getDataHandler().getMarkerList();

        markerListTemporal = new ArrayList<>();
        //PrincipalAulaAR.markerListTemporalPrincipal = new ArrayList<>();
        for(Marker mark : markerListActual){
            markerListTemporal.add(mark);
            //PrincipalAulaAR.markerListTemporalPrincipal.add(mark);
        }

        PrincipalRAActivity.dataView.getDataHandler().clearMarkers();
        PrincipalRAActivity.dataView.getDataHandler().setMarkerList(markerList);

        DataView.viendoUnicoMarker = true;

        /*if(!regresa){
            HacerTiempoTask = new HacerTiempo();
            HacerTiempoTask.execute((Void) null);
        }else{*/
        startActivity(new Intent(PrincipalRAActivity.getContext(), MixView.class));
        /*}*/
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (DataView.volverInicio)	{
            finish();
            return;
        }

        if (DataView.viendoUnicoMarker)	{
            PrincipalRAActivity.dataView.getDataHandler().setMarkerList(markerListTemporal);
            markerListTemporal = null;
            DataView.viendoUnicoMarker = false;
            oneMarker.setUnicoMarker(false);
            /*Regresa a lo anterior*/
            if (!Utilitarios.Companion.getMostrarDetalleMarker()) {
                HacerTiempoTask = new HacerTiempo(1);/*CERO IDA*/
                HacerTiempoTask.execute((Void) null);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!lCustumMarker.isEmpty()) {
                oneMarker = lCustumMarker.remove(lCustumMarker.size()-1);
            }
            return super.onKeyDown(keyCode, event);
        }
        return false;
    }

    public class HacerTiempo extends AsyncTask<Void, Void, Boolean> {
        //private final ProgressDialog progressDialog;
        private final int opcion;
        public HacerTiempo(int opcion) {
            this.opcion = opcion;
            //progressDialog = new ProgressDialog(DetalleMarker.this);
        }

        /*@Override
        protected void onPreExecute() {
            progressDialog.setIndeterminate(true);
            progressDialog.setMessage("Cargando ...");
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
        }*/

        @Override
        protected Boolean doInBackground(Void... params) {
            HacerTiempoTask = null;
            //progressDialog.dismiss();
            try {
                Thread.sleep(1000);
                if(opcion==0) {
                    verAR(false);
                }else{
                    finish();
                }
                return true;
            } catch (InterruptedException e) {
                //Auto-generated catch block
                e.printStackTrace();
                return false;
            }

        }

        @Override
        protected void onPostExecute(final Boolean success) {
            HacerTiempoTask = null;
            //progressDialog.dismiss();
            //startActivity(new Intent(PrincipalAulaAR.getContext(), MixView.class));
        }

        @Override
        protected void onCancelled() {
            HacerTiempoTask = null;
            //progressDialog.dismiss();
        }
    }
}
