package appostgrado.esan.edu.pe.data.mapper;

import java.util.List;

/**
 * Created by killerypa
 */


public interface Mapper<T,V> {
    T transform(V vEntity);
    List<T> transformList(List<V> vList);
}
