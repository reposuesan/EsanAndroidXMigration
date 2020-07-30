package pe.edu.esan.appostgrado.view.mas.ra;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.adapter.MarkerAdapter;
import pe.edu.esan.appostgrado.entidades.MarkerFiltro;
import pe.edu.esan.appostgrado.mixare.DataView;
import pe.edu.esan.appostgrado.mixare.Marker;
import pe.edu.esan.appostgrado.mixare.MixContext;
import pe.edu.esan.appostgrado.mixare.MixState;
import pe.edu.esan.appostgrado.mixare.data.DataSource;

public class PrincipalAulaArBuscarActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {

    private Button btnMostrarTodos;
    private SearchView buscar;
    private ExpandableListView listaLugares;
    private MarkerAdapter adapter;
    private ExpandableListView expandibleList;

    private MixContext mixContext;
    private static DataView dataView;
    private List<MarkerFiltro> listaCategorias= new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal_aula_ar_buscar);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_realidad_aumentada);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        mTitle.setText(getResources().getString(R.string.ra));
        setSupportActionBar(toolbar);

        final Drawable upArrow = getResources().getDrawable(R.drawable.ic_back);
        upArrow.setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        dataView = PrincipalRAActivity.dataView;
        //dataView = f_comedor.dataView;
        mixContext = dataView.getContext();

        Log.i("principalAulaArBusca","CANTIDAD: " + dataView.getDataHandler().getMarkerCount());
        List<Marker> listaMarker = dataView.getDataHandler().getMarkerList();

        for(int i = 0; i< dataView.getDataHandler().getMarkerCount(); i++){
            Log.i("PUNTOS: ","Nombre: "+listaMarker.get(i).getTitle());
        }

        btnMostrarTodos = (Button)findViewById(R.id.btn_mostrarTodosMarker_arbuscar);
        btnMostrarTodos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PrincipalRAActivity.getContext(), MixView.class));
                //startActivity(new Intent(f_comedor.getContextF(), MixView.class));
            }
        });

        SearchManager searchManager = (SearchManager)getSystemService(Context.SEARCH_SERVICE);
        buscar = (SearchView)findViewById(R.id.search_arbuscar);
        buscar.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        buscar.setIconifiedByDefault(false);
        buscar.setOnQueryTextListener(this);
        buscar.setOnCloseListener(this);

        cargarLista();

        expandibleList = (ExpandableListView)findViewById(R.id.expandableList);
        adapter = new MarkerAdapter(getApplicationContext(), listaCategorias);
        expandibleList.setAdapter(adapter);

        adapter.newSetOnItemClickListener(new MarkerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Marker marker) {
                Toast.makeText(getApplicationContext(), "Ubicando: "+ marker.getTitle(), Toast.LENGTH_LONG).show();
                MixState mixState = new MixState();
                mixState.handleEvent_buscar(mixContext, marker);
            }
        });
    }

    private void cargarLista(){
        listaCategorias = new ArrayList<>();
        DataSource.DATASOURCE categoria = null;
        List<Marker> newLista = null;


        for(int i = 0; i < dataView.getDataHandler().getMarkerCount(); i++){
            Marker currentMarker = dataView.getDataHandler().getMarkerList().get(i);
            if(i==0){
                categoria = currentMarker.getDatasource();
                newLista = new ArrayList<>();
                newLista.add(currentMarker);
            }else{
                if(currentMarker.getDatasource().equals(categoria)){
                    newLista.add(currentMarker);
                }else{
                    MarkerFiltro markFiltro = new MarkerFiltro(mixContext.getDataSourceName(categoria), newLista);
                    listaCategorias.add(markFiltro);

                    categoria = currentMarker.getDatasource();
                    newLista = new ArrayList<>();
                    newLista.add(currentMarker);
                }
            }
        }

        MarkerFiltro markFiltro = new MarkerFiltro(mixContext.getDataSourceName(categoria), newLista);
        listaCategorias.add(markFiltro);
    }

    private void expandirTodo(String query){
        int cont = adapter.getGroupCount();
        if(!TextUtils.isEmpty(query)) {
            for (int i = 0; i < cont; i++) {
                expandibleList.expandGroup(i);
            }
        }else{
            for (int i = 0; i < cont; i++) {
                expandibleList.collapseGroup(i);
            }
        }
    }

    @Override
    public boolean onClose() {
        adapter.filterData("");
        expandirTodo("");
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {
        adapter.filterData(query);
        expandirTodo(query);
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        adapter.filterData(query);
        expandirTodo(query);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            //noinspection SimplifiableIfStatement
            /*case R.id.action_settings:
                return true;*/
            case android.R.id.home:
                finish();
                //this.overridePendingTransition(R.anim.right_in, R.anim.right_out);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            //this.overridePendingTransition(R.anim.right_in, R.anim.right_out);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
