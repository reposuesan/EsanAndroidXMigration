package com.example.vuforiamod.international.adapters;

import android.content.Intent;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.vuforiamod.R;
import com.example.vuforiamod.international.activities.InternationalCourseControllerActivity;
import com.example.vuforiamod.international.model.IWCurso;

import java.util.List;

/**
 * Created by lchang on 12/06/17.
 */

public class IWCursoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<IWCurso> lista;
    private FragmentActivity context;

    public IWCursoAdapter(List<IWCurso> lista, FragmentActivity context) {
        this.lista = lista;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        final IWCurso curso = lista.get(position);
        switch (curso.getTipo()) {
            case CABECERA:
                return CABECERA;
            default:
                return DETALLE;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        RecyclerView.ViewHolder holder;
        View v;

        if(viewType == CABECERA) {
            v = inflater.inflate(R.layout.iwitem_curso_cabecera, parent, false);
            holder = new CabeceraCursoHolder(v);
        } else {
            v = inflater.inflate(R.layout.iwitem_curso_detalle, parent, false);
            holder = new DetalleCursoHolder(v);
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final IWCurso curso = lista.get(position);

        if(holder instanceof CabeceraCursoHolder) {

            final CabeceraCursoHolder cabeceraView = (CabeceraCursoHolder) holder;
            cabeceraView.getLblCategoria().setText(curso.getCategoria());

        } else if(holder instanceof DetalleCursoHolder) {

            final DetalleCursoHolder detalleView = (DetalleCursoHolder) holder;
            detalleView.getLblCurso().setText(curso.getCurso());
            detalleView.getLblHorario().setText(curso.getHorario());
            detalleView.getVista().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent detalle = new Intent().setClass(context, InternationalCourseControllerActivity.class);
                    detalle.putExtra("curso", curso.getCurso());
                    detalle.putExtra("cursodetalle", curso.getDescripcioncurso());
                    detalle.putExtra("profesor", curso.getProfesor());
                    detalle.putExtra("profesordetalle", curso.getDescripcionprofesor());

                    context.overridePendingTransition(R.anim.left_in, R.anim.left_out);
                    context.startActivity(detalle);
                }
            });
           /* detalleView.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Intent detalle = new Intent().setClass(context.getApplicationContext(), IWCursoDetalleController.class);
                    /*detalle.putExtra("curso", curso.getCurso());
                    detalle.putExtra("cursodetalle", curso.getDescripcioncurso());
                    detalle.putExtra("profesor", curso.getProfesor());
                    detalle.putExtra("profesordetalle", curso.getDescripcionprofesor());*/

                    //((Activity)context).overridePendingTransition(R.anim.left_in, R.anim.left_out);
                /*    //context.startActivity(detalle);
                }
            });*/
        }
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    private class CabeceraCursoHolder extends RecyclerView.ViewHolder {
        private  final TextView lblCategoria;

        public CabeceraCursoHolder(View v) {
            super(v);
            lblCategoria = (TextView) v.findViewById(R.id.lblCategoria_iwcursocabecera);
        }

        public TextView getLblCategoria(){ return lblCategoria; }
    }

    private class DetalleCursoHolder extends RecyclerView.ViewHolder {
        private final View vista;
        private final TextView lblCurso;
        private final TextView lblHorario;

        public DetalleCursoHolder(View v) {
            super(v);
            vista = v.findViewById(R.id.viewDetalle_iwitemcurso);
            lblCurso = (TextView) v.findViewById(R.id.lblCurso_iwitemcurso);
            lblHorario = (TextView) v.findViewById(R.id.lblHorario_iwitemcurso);
        }

        public View getVista(){ return vista; }
        public TextView getLblCurso() { return lblCurso; }
        public TextView getLblHorario() { return lblHorario; }
    }

    private final int CABECERA = 0;
    private final int DETALLE = 1;
}
