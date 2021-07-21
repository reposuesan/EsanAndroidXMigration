package com.example.vuforiamod.international.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.example.vuforiamod.international.adapters.IWCursoAdapter;
import com.example.vuforiamod.international.model.IWCurso;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class InternationalCourseFragment extends Fragment {

    private RecyclerView rvCursos;

    public InternationalCourseFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_international_course, container, false);

        rvCursos = (RecyclerView) view.findViewById(R.id.rvCursos_cursoiw);
        rvCursos.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvCursos.setAdapter(null);

        getCursos();

        return view;
    }

    private void getCursos() {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setIndeterminate(true);
        progressDialog.setMessage("Cargando ...");
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);

        final String urlWs = "https://wssi.ue.edu.pe/internationalweek/service.svc/getCursos.php";
        //final String urlWs = "https://tempwssi.ue.edu.pe/internationalweek/service.svc/getCursos.php";

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET,
                urlWs,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            final JSONArray jaCursos = response.getJSONArray("IWCursosResult");
                            List<IWCurso> currentLista = new ArrayList<>();
                            for(int i = 0; i < jaCursos.length(); i++) {
                                final JSONObject joCurso = jaCursos.getJSONObject(i);
                                final String categoria = joCurso.getString("categoria");
                                final JSONArray jaCurso = joCurso.getJSONArray("cursos");

                                currentLista.add(new IWCurso(0, categoria));

                                for(int z = 0; z < jaCurso.length(); z++) {
                                    final JSONObject jCurso = jaCurso.getJSONObject(z);
                                    final String curso = jCurso.getString("curso");
                                    final String descripcioncurso = jCurso.getString("descripcioncurso");
                                    final String profesor = jCurso.getString("profesor");
                                    final String descripcionprofesor = jCurso.getString("descripcionprofesor");
                                    final String horario = jCurso.getString("horario");

                                    currentLista.add(new IWCurso(1, curso, descripcioncurso, profesor, descripcionprofesor, horario));
                                }
                            }

                            IWCursoAdapter adapter = new IWCursoAdapter(currentLista, getActivity());
                            rvCursos.setAdapter(adapter);

                        }catch (JSONException ex) {
                            Log.e("Course","There was an error when retrieving response");
                        }
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e("Course", error.toString());
                    }
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

}
