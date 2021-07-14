package com.example.vuforiamod.util

import android.content.Context
import android.graphics.Typeface
import android.hardware.Sensor
import android.hardware.SensorManager

class Utilitarios {

    /** Tipos de fuente ROBOTO
     */
    enum class TypeFont {
        THIN,
        THIN_ITALIC,
        LIGHT,
        LIGHT_ITALIC,
        REGULAR,
        REGULAR_ITALIC,
        MEDIUM,
        MEDIUM_ITALIC,
        BOLD,
        BOLD_ITALIC,
        BLACK,
        BLACK_ITALIC
    }

    companion object {
        /*FONTS*/
        /** Obtener la fuente ROBOTO
        * @param context cualquier context
        * @param type tipo de fuente deseada
        * @see TypeFont
        * @return fuente
        */
        fun getFontRoboto(context: Context, type: TypeFont): Typeface {
            when (type) {
                TypeFont.THIN -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Thin.ttf")
                TypeFont.THIN_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-ThinItalic.ttf")
                TypeFont.LIGHT -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Light.ttf")
                TypeFont.LIGHT_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-LightItalic.ttf")
                TypeFont.REGULAR -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Regular.ttf")
                TypeFont.REGULAR_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Italic.ttf")
                TypeFont.MEDIUM -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Medium.ttf")
                TypeFont.MEDIUM_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-MediumItalic.ttf")
                TypeFont.BOLD -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Bold.ttf")
                TypeFont.BOLD_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-BoldItalic.ttf")
                TypeFont.BLACK -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-Black.ttf")
                TypeFont.BLACK_ITALIC -> return Typeface.createFromAsset(context.assets, "fonts/Roboto-BlackItalic.ttf")
            }
        }

        fun comprobarSensor(context: Context): Boolean {
            val sensorMgr = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
            var sensors = sensorMgr.getSensorList(Sensor.TYPE_ACCELEROMETER)
            if (sensors.isEmpty()) {
                return false
            } else {
                sensors = sensorMgr.getSensorList(Sensor.TYPE_MAGNETIC_FIELD)
                if (sensors.isEmpty())
                    return false
            }
            return true
        }
    }
}