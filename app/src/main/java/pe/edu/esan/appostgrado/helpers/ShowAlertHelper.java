package pe.edu.esan.appostgrado.helpers;

import android.content.Context;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import android.widget.Toast;
import pe.edu.esan.appostgrado.R;

/**
 * Created by lchang on 14/11/18.
 */

public class ShowAlertHelper {

    private Context mContext;

    public ShowAlertHelper(Context context){
        this.mContext = context;
    }

    public void showAlertError (String title, String message, final ClickListener clickListener) {

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CustomDialog);
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_dialog_error,null,false);

        Button btnOk = view.findViewById(R.id.error_bt_ok);

        TextView tvTitle = view.findViewById(R.id.error_tv_title);
        if(title == null ){
            tvTitle.setVisibility(View.GONE);
        }else {
            tvTitle.setVisibility(View.VISIBLE);
            tvTitle.setText(title);
        }
        TextView tvMessage = view.findViewById(R.id.error_tv_message);

        tvMessage.setText(message);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        try {
            dialog.setCancelable(false);
            dialog.show();
        } catch (Exception e){
            Toast.makeText(mContext, mContext.getString(R.string.mensaje_error_servidor),Toast.LENGTH_LONG).show();
        }

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if( clickListener!= null )
                    clickListener.onClick();
            }
        });
    }

    public void alertConfirm( final ClickListener ClickListener, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext, R.style.CustomDialog);
        View view = LayoutInflater.from(mContext).inflate(R.layout.view_alert_confirm,null,false);

        Button btnAcept = view.findViewById(R.id.btn_acetp_confirm);
        ImageView imgClose = view.findViewById(R.id.img_close_confirm);

        TextView tvMessage = view.findViewById(R.id.tv_message_alert);
        tvMessage.setText(message);

        builder.setView(view);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

        btnAcept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
                ClickListener.onClick();
            }
        });

        imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.hide();
            }
        });
    }

}
