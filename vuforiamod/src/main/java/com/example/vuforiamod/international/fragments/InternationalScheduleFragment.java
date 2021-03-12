package com.example.vuforiamod.international.fragments;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.vuforiamod.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.vuforiamod.international.adapters.IWHorarioAdapter;
import com.example.vuforiamod.international.model.IWCabeceraHorario;
import com.example.vuforiamod.international.model.IWDetalleHorario;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by killerypa on 8/01/2019.
 * yahyrparedesarteaga@gmail.com
 */
public class InternationalScheduleFragment extends Fragment {

    private ExpandableListView listaHorario;
    private IWHorarioAdapter adapter;

    public InternationalScheduleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_international_schedule, container, false);

        listaHorario = (ExpandableListView) view.findViewById(R.id.expandible_list_international_schedule);

        getLoadHorario();
        return view;
    }


    private void getLoadHorario(){
        //final String url = "http://172.59.1.17/internationalweek/service.svc/getHorario.php";
        final String url = "http://wssi.ue.edu.pe/internationalweek/service.svc/getHorario.php";
        //final String url = Configuracion.urlServicio2 + Configuracion.method_menucomedor + "1";

        RequestQueue requestEncuestaPos = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonRequest = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final JSONArray jaHorario = response.getJSONArray("IWHorarioResult");

                            List<IWCabeceraHorario> Horario = new ArrayList<>();
                            for (int i = 0; i < jaHorario.length(); i++) {
                                final JSONObject joHorario = jaHorario.getJSONObject(i);
                                final String titulo = joHorario.getString("titulo");
                                final String descripcion = joHorario.getString("descripcion");
                                final JSONArray jaDetalle = joHorario.getJSONArray("detalle");
                                List<IWDetalleHorario> listaDetalle = new ArrayList<>();
                                for (int z = 0; z < jaDetalle.length(); z++) {
                                    final JSONObject joDetalle = jaDetalle.getJSONObject(z);
                                    final String curso = joDetalle.getString("curso");
                                    final String profesor = joDetalle.getString("profesor");
                                    final String aula = joDetalle.getString("aula");
                                    final String idioma = joDetalle.getString("idioma");

                                    listaDetalle.add(new IWDetalleHorario(curso,profesor,idioma,aula));
                                }
                                Horario.add(new IWCabeceraHorario(titulo,descripcion,listaDetalle));

                            }

                            adapter = new IWHorarioAdapter((AppCompatActivity) getActivity(),Horario);
                            listaHorario.setAdapter(adapter);

                        }catch (JSONException e) {

                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                }
        );

        requestEncuestaPos.add(jsonRequest);
    }
}