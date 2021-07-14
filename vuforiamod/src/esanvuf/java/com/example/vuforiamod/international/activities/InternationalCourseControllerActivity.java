package com.example.vuforiamod.international.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.TextView;
import com.example.vuforiamod.R;


public class InternationalCourseControllerActivity extends AppCompatActivity {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_international_course_controller);

        toolbar = (Toolbar) findViewById(R.id.toolbar_iwcursodetalle);
        //TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);

        final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_material);
        upArrow.setColorFilter(getResources().getColor(R.color.md_white_1000), PorterDuff.Mode.SRC_ATOP);
        getSupportActionBar().setHomeAsUpIndicator(upArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        TextView curso = (TextView) findViewById(R.id.lblCurso_iwcursodetalle);
        TextView cursodetalle = (TextView) findViewById(R.id.lblCursoDetalle_iwcursodetalle);
        TextView profesor = (TextView) findViewById(R.id.lblProfesor_iwcursodetalle);
        TextView profesordetalle = (TextView) findViewById(R.id.lblDescripcionProfesor_iwcursodetalle);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            curso.setText(extras.getString("curso"));
            cursodetalle.setText(extras.getString("cursodetalle"));
            profesor.setText(extras.getString("profesor"));
            profesordetalle.setText(extras.getString("profesordetalle"));
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            //noinspection SimplifiableIfStatement
            /*case R.id.action_settings:
                return true;*/
            case android.R.id.home:
                finish();
                this.overridePendingTransition(R.anim.right_in, R.anim.right_out);
                return true;
            /*case R.id.action_next_itinerario:
                //validarItinerarioSelect();
                return true;*/
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            this.overridePendingTransition(R.anim.right_in, R.anim.right_out);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
