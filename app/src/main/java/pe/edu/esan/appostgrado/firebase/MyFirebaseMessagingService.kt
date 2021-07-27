package pe.edu.esan.appostgrado.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.os.Build.VERSION_CODES.O
import androidx.core.app.NotificationCompat
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pe.edu.esan.appostgrado.R
import pe.edu.esan.appostgrado.view.login.LoginActivity


/**
 * Created by lventura on 26/06/18.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val LOG = MyFirebaseMessagingService::class.simpleName

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val misPreferencias = getSharedPreferences("PreferenciasUsuario", MODE_PRIVATE)
        val editor = misPreferencias.edit()
        editor.putString("tokenID", token)
        editor.apply()
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)

        if (p0.notification != null) {
            val titulo = p0.notification?.title
            val texto = p0.notification?.body

            onShowNotification(titulo, texto)
        }
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
    }

    private fun onShowNotification(titulo: String?, mensaje: String?) {

        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val channelID = "pe.edu.esan.appostgrado"

        val icon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher_foreground)
        var smallIcon = R.mipmap.ic_launcher_round

        smallIcon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            R.drawable.ic_stat_android
        } else {
            R.mipmap.ic_launcher_round
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

        notificationManager.notify(0, notificationBuilder.build())
    }
}