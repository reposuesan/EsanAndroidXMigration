package pe.edu.esan.appostgrado.appesanpostgrado.view.login

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * Created by lchang on 16/04/18.
 */
class LoginView : View {
    constructor(context: Context) : super(context)
    constructor(context: Context, attributeSet: AttributeSet) : super(context, attributeSet)
    constructor(context: Context, attributeSet: AttributeSet, defStyleAttr: Int) : super(context, attributeSet, defStyleAttr)

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //Obtenemos las dimensiones del control
        val alto = measuredHeight
        val ancho = measuredWidth

        val radioDP = 50f
        val metrics = context.resources.displayMetrics
        val radio = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radioDP, metrics)

        val red = Paint()
        red.color = Color.WHITE
        red.style = Paint.Style.FILL


        val path = Path()
        path.moveTo(0.0f, alto - alto * 0.25f)
        path.lineTo(0.0f, radio)
        path.lineTo(radio, 0.0f)

        //path.addArc(new RectF(0.0f, 0.0f, radio*2, radio*2), 180, 90);

        path.lineTo(ancho - radio, 0.0f)
        path.lineTo(ancho.toFloat(), radio)

        //path.addArc(new RectF(ancho-radio*2, 0.0f, ancho, radio*2), 0, -90);

        path.lineTo(ancho.toFloat(), alto - radio)
        path.lineTo(ancho - radio, alto.toFloat())

        //path.addArc(new RectF(ancho-radio*2, alto-radio*2, ancho, alto), 0, 90);

        path.lineTo(radio / 3, alto - alto * 0.25f - radio * 0.25f)
        path.lineTo(0.0f, alto - alto * 0.25f - radio)
        path.close()

        canvas?.drawPath(path, red)


        val paint = Paint()

        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL

        val oval = RectF()

        oval.set(0.0f,
                0.0f,
                radio * 2,
                radio * 2)

        canvas?.drawArc(oval, 180f, 90f, true, paint)

        oval.set(ancho - radio * 2,
                0.0f,
                ancho.toFloat(),
                radio * 2)

        canvas?.drawArc(oval, 270f, 90f, true, paint)

        oval.set(ancho - radio * 2,
                alto - radio * 2,
                ancho.toFloat(),
                alto.toFloat())

        canvas?.drawArc(oval, 0f, 90f, true, paint)

        oval.set(0.0f,
                alto - alto * 0.25f - 2 * radio,
                radio * 2,
                alto - alto * 0.25f)

        canvas?.drawArc(oval, 127f, 53f, true, paint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

}