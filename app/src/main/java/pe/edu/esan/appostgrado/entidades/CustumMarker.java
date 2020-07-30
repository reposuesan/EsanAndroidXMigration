package pe.edu.esan.appostgrado.entidades;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;

import java.text.DecimalFormat;

import pe.edu.esan.appostgrado.mixare.Marker;
import pe.edu.esan.appostgrado.mixare.MixUtils;
import pe.edu.esan.appostgrado.mixare.data.DataSource;
import pe.edu.esan.appostgrado.mixare.gui.PaintScreen;
import pe.edu.esan.appostgrado.mixare.gui.TextObj;

/**
 * Created by lchang on 21/07/16.
 */
public class CustumMarker extends Marker {
    private static final int MAX_OBJECTS=100;

    private String custumMID;
    private String descripcion;
    //private String color;
    private Bitmap imagen;

    private int visibilidadMinima;
    private double distanciaMayorTransparencia; //en metros
    private double distanciaSinTransparencia; //en metros
    private float pendiente;


    public CustumMarker(String title, double latitude, double longitude, double altitude,
                        DataSource.DATASOURCE datasource, String custumMID,
                        String descripcion, String color, Bitmap imagen) {
        super(title, latitude, longitude, altitude, datasource, color);

        this.custumMID = custumMID;
        this.descripcion = descripcion;
        //this.color = color;
        this.imagen = imagen;

        distanciaMayorTransparencia = 700;
        distanciaSinTransparencia = 100;
        float porcVisibilidad = 0.65f;
        visibilidadMinima = (int) Math.round(porcVisibilidad*255);
        pendiente = (float) ((visibilidadMinima - 255) / (distanciaMayorTransparencia - distanciaSinTransparencia));
    }

    @Override
    public void update(Location curGPSFix) {
        super.update(curGPSFix);
    }

    @Override
    public int getMaxObjects() {
        return MAX_OBJECTS;
    }

    @Override
    public String getID() {
        return custumMID;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Bitmap getImagen(){return imagen;}

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void drawCircle(PaintScreen dw) {
        if (isVisible) {
            float maxHeight = dw.getHeight();

            if (unicoMarker)	{
                dw.setStrokeWidth(maxHeight / 100f);
                dw.setFill(false);
                radius = Math.max( ((maxHeight/2 - maxHeight/25f)/400*(400 - distance) + maxHeight/25f), maxHeight/25f);
                dw.paintCircle(cMarker.x, cMarker.y, (float)radius);
            }
            else	{
                radius = maxHeight/20f;
                //
                Bitmap bitmap = null;
                if(imagen==null){
                    bitmap = DataSource.getLocalBitmap(datasource, (int)(radius*2));
                }else{
                    bitmap = DataSource.getNewBitmap(imagen, (int)(radius*2));
                }

//				bitmap = DataSource.adjustOpacity(bitmap, getVisibilidadSegunDistancia());
                if(bitmap!=null) {
                    dw.paintBitmap(bitmap, cMarker.x - (float)radius, cMarker.y - (float)radius);
                }
                else	{
                    dw.setStrokeWidth(maxHeight / 100f);
                    dw.setFill(true);
                    dw.paintCircle(cMarker.x, cMarker.y, (float)radius);
                }
            }

        }
    }

    public void drawTextBlock(PaintScreen dw) {
        if (isVisible) {
            float maxHeight = Math.round(dw.getHeight() / 10f) + 1;

            String textStr = "";

            double d = distance;
            DecimalFormat df = new DecimalFormat("@#");
            if (d < 1000.0) {
                textStr = title + " (" + df.format(d) + "m)";
            } else {
                d = d / 1000.0;
                textStr = title + " (" + df.format(d) + "km)";
            }

            int transp = Math.round(getVisibilidadSegunDistancia());
            int bgColor = Color.argb((int) (Math.round(transp / 1.5)), 200,
                    200, 200);
            int textColor = Color.argb(transp, 10, 10, 10);
            float fontSize = Math.round(maxHeight / 2.5f);
            if (fontSize > 22f) fontSize = 22f;
            textBlock = new TextObj(textStr, fontSize, 100,
                    bgColor, bgColor, textColor, Color.argb(64, 0, 0, 0),
                    dw.getTextAsc() / 2 , dw, false);

            float currentAngle = MixUtils.getAngle(cMarker.x, cMarker.y,
                    signMarker.x, signMarker.y);

            this.txtLab.prepare(textBlock);

            dw.setStrokeWidth(1f);
            dw.setFill(true);
            if (unicoMarker)	{
                dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()/2, signMarker.y + maxHeight, currentAngle + 90, 1, false);
            }
            else	{
                dw.paintObj(txtLab, signMarker.x - txtLab.getWidth()/2, cMarker.y + maxHeight/1.5f, currentAngle + 90, 1, false);
            }

        }
    }

    public int getVisibilidadSegunDistancia() {
        if (distance > distanciaMayorTransparencia)
            return visibilidadMinima;
        else if (distance < distanciaSinTransparencia)
            return 255;
        int visibilidad = (int) (Math.round(pendiente * (distance - distanciaSinTransparencia) + 255f));
        return Math.max(visibilidad, visibilidadMinima);
    }

    protected int getDataSourceColor()	{
        int _color = Color.parseColor(color);
        //int color = DataSource.getColor(datasource);
        return Color.argb(getVisibilidadSegunDistancia(), Color.red(_color), Color.green(_color), Color.blue(_color));

    }
}
