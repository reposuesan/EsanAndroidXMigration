package appostgrado.esan.edu.pe.domain.callbacks;

public interface BaseCallback {

    void onServicesSuccess(Object object);
    void onServicesError(String message);
    void onErrorConection();
    void onAccessErrorNotAuthorized();

}
