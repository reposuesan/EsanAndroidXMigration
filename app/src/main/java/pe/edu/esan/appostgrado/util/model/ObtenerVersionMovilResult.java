package pe.edu.esan.appostgrado.util.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ObtenerVersionMovilResult {
    @SerializedName("VerAndroid")
    @Expose
    private int verAndroid;
    @SerializedName("VerOIS")
    @Expose
    private int verOIS;

    public int getVerAndroid() {
        return verAndroid;
    }

    public void setVerAndroid(int verAndroid) {
        this.verAndroid = verAndroid;
    }

    public int getVerOIS() {
        return verOIS;
    }

    public void setVerOIS(int verOIS) {
        this.verOIS = verOIS;
    }

}
