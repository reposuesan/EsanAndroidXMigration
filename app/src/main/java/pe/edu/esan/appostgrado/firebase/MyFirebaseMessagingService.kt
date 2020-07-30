package pe.edu.esan.appostgrado.firebase

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.content.Intent
import android.media.RingtoneManager
import android.util.Log
import org.json.JSONObject
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.view.login.LoginActivity
import pe.edu.esan.appostgrado.view.mas.mensaje.MensajeActivity
import android.app.NotificationChannel
import android.os.Build.VERSION_CODES.O
import android.os.Build
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId
import android.graphics.BitmapFactory
import android.graphics.Bitmap
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T


/**
 * Created by lventura on 26/06/18.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val LOG = MyFirebaseMessagingService::class.simpleName

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if(!task.isSuccessful){
                    Log.w(LOG, "getInstanceId failed", task.exception)
                    return@OnCompleteListener
                }

                val token = task.result?.token
                Log.i(LOG,"New token created: $token")

                val misPreferencias = getSharedPreferences("PreferenciasUsuario", Context.MODE_PRIVATE)
                val editor = misPreferencias.edit()
                editor.putString("tokenID", token ?: "")
                editor.apply()
            })
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        /*val data = p0?.data?.get("data")

        try{
            val obj = JSONObject(data)
            val title = obj.getString("title")
            val text = obj.getString("message")

            onShowNotification(title, text)
        } catch (e: Exception){
           Log.e("Error", e.message.toString())
        }*/

        if (p0?.notification != null) {
            val titulo = p0.notification?.title
            val texto = p0.notification?.body

            /*println("MENSAJE")
            println(titulo)
            println(texto)*/

            Log.i(LOG, "Notificación Push recibida de Firebase")
            Log.i(LOG, "Título de Notificación: $titulo")
            Log.i(LOG, "Texto de Notificación: $texto")

            onShowNotification(titulo, texto)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    private fun onShowNotification(titulo: String?, mensaje: String?) {
        //val intent = Intent(this, MensajeActivity::class.java)
        Log.i(LOG, "onShowNotification() was called")
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channelID = "pe.edu.esan.appostgrado"

        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground)
        var smallIcon = R.mipmap.ic_launcher_round

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            smallIcon = R.drawable.ic_stat_android
        } else {
            smallIcon = R.mipmap.ic_launcher_round
        }

        val notificationBuilder = NotificationCompat.Builder(this,channelID)
            .setSmallIcon(smallIcon)
            .setLargeIcon(icon)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= O) {
            val channelId = "Your_channel_id"
            val channel = NotificationChannel(channelId, "Channel human readable title", NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(channel)
            notificationBuilder.setChannelId(channelId)
        }

        //val counter = (0..10000).random()
        //Log.i(LOG,"Counter value is: $counter")
        notificationManager.notify(0, notificationBuilder.build())
    }
}