package pe.edu.esan.appostgrado.helpers;

import java.text.DecimalFormat;

public class Utils {

    public String calcularPromedioText(int total, int value) {
        float mTotal = (float) total;
        float mValue = (float) value;
        float promedio = (mValue * 100) / mTotal;
        DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(1);
        return String.valueOf(df.format(promedio));
    }

    public float calcularPromedioNumber(int total, int value) {
        float mTotal = (float) total;
        float mValue = (float) value;
        float promedio = (mValue * 100) / mTotal;
        return promedio;
    }

    public String inToString(int value) {
        return String.valueOf(value);
    }
}
