package appostgrado.esan.edu.pe.data.mapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by killerypa
 */

public class BaseMapper<T,V> implements Mapper<T,V> {

    @Override
    public T transform(V vEntity) {
        return null;
    }

    @Override
    public List<T> transformList(List<V> vs) {
        List<T> list= new ArrayList<>();
        if(vs==null)return list;

        for (V entity:vs) {
            list.add(transform(entity));
        }
        return list;
    }
}
