package pe.edu.esan.appostgrado.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pe.edu.esan.appostgrado.R;
import pe.edu.esan.appostgrado.entidades.MarkerFiltro;
import pe.edu.esan.appostgrado.mixare.Marker;

/**
 * Created by lchang on 8/08/18.
 */

public class MarkerAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<MarkerFiltro> catMarkerlist;
    private List<MarkerFiltro> originallist;
    OnItemClickListener mItemClickListener;

    public MarkerAdapter(Context context, List<MarkerFiltro> markerlist){
        this.context = context;
        this.catMarkerlist = new ArrayList<>();
        this.catMarkerlist.addAll(markerlist);
        this.originallist = new ArrayList<>();
        this.originallist.addAll(markerlist);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition){
        List<Marker> markerLi = catMarkerlist.get(groupPosition).getLista_markers();
        return markerLi.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition){
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent){
        /**Vista Hijos**/
        final Marker marker = (Marker)getChild(groupPosition,childPosition);
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.item_detalle_ra, null);
        }

        ((TextView)view.findViewById(R.id.txt_texto2)).setText(marker.getTitle());
        (view.findViewById(R.id.viewDetalle_arbusca)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mItemClickListener.onItemClick(marker);
            }
        });

        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<Marker> markerList = catMarkerlist.get(groupPosition).getLista_markers();
        return markerList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return catMarkerlist.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return catMarkerlist.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view, ViewGroup parent) {
        /**Vista Cabecera**/
        MarkerFiltro categoriaMarker = (MarkerFiltro)getGroup(groupPosition);
        if(view == null){
            LayoutInflater layoutInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = layoutInflater.inflate(R.layout.item_cabecera_ra, null);
        }
        ((TextView)view.findViewById(R.id.txt_texto)).setText(categoriaMarker.getCategoria_marker());
        return view;
    }

    @Override
    public boolean hasStableIds(){
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface OnItemClickListener{
        public void onItemClick(Marker marker);
    }

    public void newSetOnItemClickListener(final OnItemClickListener mItemClickListener){
        this.mItemClickListener = mItemClickListener;
    }

    public void filterData(String query){
        query = query.toLowerCase();
        catMarkerlist.clear();

        if(query.isEmpty()){
            catMarkerlist.addAll(originallist);
        }else{
            for(MarkerFiltro markerCate : originallist){
                List<Marker> markerList = markerCate.getLista_markers();
                List<Marker> newMarkerList = new ArrayList<>();
                for(Marker marker : markerList){
                    if(marker.getTitle().toLowerCase().contains(query)){
                        newMarkerList.add(marker);
                    }
                }
                if(newMarkerList.size() > 0){
                    MarkerFiltro mCate = new MarkerFiltro(markerCate.getCategoria_marker(), newMarkerList);
                    catMarkerlist.add(mCate);
                }
            }
        }

        notifyDataSetChanged();
    }
}
