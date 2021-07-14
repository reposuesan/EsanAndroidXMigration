package com.example.vuforiamod.international.adapters;

import android.content.Context;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.example.vuforiamod.R;
import com.example.vuforiamod.international.model.IWCabeceraHorario;
import com.example.vuforiamod.international.model.IWDetalleHorario;

import java.util.List;

public class IWHorarioAdapter extends BaseExpandableListAdapter {
    private AppCompatActivity context;
    private List<IWCabeceraHorario> listHorario;

    public IWHorarioAdapter(AppCompatActivity context, List<IWCabeceraHorario> listHorario) {
        this.context = context;
        this.listHorario = listHorario;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<IWDetalleHorario> detalle = listHorario.get(groupPosition).getDetalle();
        return detalle.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final IWDetalleHorario detalleHorario = (IWDetalleHorario) getChild(groupPosition, childPosition);
        if(convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.iwitem_detalle_horario, null);
        }

        ((TextView) convertView.findViewById(R.id.lblCurso_itemhorariodetalleiw)).setText(detalleHorario.getCurso());
        ((TextView) convertView.findViewById(R.id.lblProfesor_itemhorariodetalleiw)).setText(detalleHorario.getProfesor());
        ((TextView) convertView.findViewById(R.id.lblAula_itemhorariodetalleiw)).setText(detalleHorario.getAula());
        ((TextView) convertView.findViewById(R.id.lblIdioma_itemhorariodetalleiw)).setText(detalleHorario.getIdioma());

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<IWDetalleHorario> detalleHorarios = listHorario.get(groupPosition).getDetalle();
        return detalleHorarios.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return listHorario.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return listHorario.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final IWCabeceraHorario horario = (IWCabeceraHorario) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            //
            convertView = layoutInflater.inflate(R.layout.iwitem_cabecera_horario, null);
        }

        ((TextView) convertView.findViewById(R.id.lblHorario_itemhorariocabeceraiw)).setText(horario.getTitulo());
        ((TextView) convertView.findViewById(R.id.lblDescripcion_itemhorariocabeceraiw)).setText(horario.getDescripcion());

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
