package com.daya.mysound

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import java.io.IOException
import java.lang.ref.WeakReference

class MediaService : Service(), MediaPlayerCallback {
    private var mMediaPlayer : MediaPlayer? = null
    private var isReady :Boolean? = null

    companion object{
        const val ACTION_CREATE = "create"
        const val ACTION_DESTROY = "destroy"
        const val PLAY = 0
        const val STOP = 1
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action: String? = intent?.action

        if (action != null) {
            when (action) {
                ACTION_CREATE ->{
                    if (mMediaPlayer == null) {
                        init()
                    }

                }
                ACTION_DESTROY -> {
                    if (!mMediaPlayer!!.isPlaying) {
                        stopSelf()
                    }
                }
            }
        }
        Log.d("mediaservice","onstartcommand")
        return flags
    }

    override fun onBind(intent: Intent): IBinder{
        Log.d("tag", "onbind")
        return mMesenger.binder
    }

    override fun onPlay() {
        if ( isReady == null|| !isReady!!   ) {
            mMediaPlayer!!.prepareAsync()
        } else {
            if (mMediaPlayer!!.isPlaying) {
                mMediaPlayer!!.pause()
                showNotif()
            } else {
                mMediaPlayer!!.start()
            }
        }

    }

    override fun onStop() {
        if (mMediaPlayer!!.isPlaying || isReady!!) {
            mMediaPlayer!!.stop()
            isReady = false
            stopNotif()
        }
    }

    private fun init() {
        mMediaPlayer = MediaPlayer()
        mMediaPlayer!!.setAudioStreamType(AudioManager.STREAM_MUSIC)
        val afd: AssetFileDescriptor = applicationContext.resources.openRawResourceFd(R.raw.fancy)
        try {
            mMediaPlayer!!.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        mMediaPlayer!!.setOnPreparedListener {
            isReady = true
            mMediaPlayer!!.start()
            showNotif()
        }


        mMediaPlayer!!.setOnErrorListener { _, _, _ -> false }
    }


    val mMesenger = Messenger(IncomingHandler(this))

    class IncomingHandler(playerCallback: MediaPlayerCallback) :Handler(){
        private val mediaPlayerCallbackWeakRef: WeakReference<MediaPlayerCallback> = WeakReference(playerCallback)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                PLAY -> mediaPlayerCallbackWeakRef.get()?.onPlay()
                STOP -> mediaPlayerCallbackWeakRef.get()?.onStop()
                else -> super.handleMessage(msg)
            }
        }
    }

    private fun showNotif(){
        val notificationIntent = Intent(this, MainActivity::class.java)
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0)
        val CHANNEL_DEFAULT_IMPORTANCE = "chanel_default_importance"
        val ONGOING_NOTIFICATION_ID = 1

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
              val notification =    Notification.Builder(this,CHANNEL_DEFAULT_IMPORTANCE)
                .setContentTitle("Test1")
                .setContentText("text2")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setTicker("test3")
                .build()

              createChannel(CHANNEL_DEFAULT_IMPORTANCE)
              startForeground(ONGOING_NOTIFICATION_ID, notification)
          } else {
              val notification =Notification.Builder(this)
                .setContentTitle("Test1")
                .setContentText("text2")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setTicker("test3")
                .build()

        createChannel(CHANNEL_DEFAULT_IMPORTANCE)
        startForeground(ONGOING_NOTIFICATION_ID,notification)

        }

    }

    private fun createChannel(CHANEL_ID :String) {
        val notificationManager: NotificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(CHANEL_ID, "battery", NotificationManager.IMPORTANCE_DEFAULT)

            channel.apply {
                setShowBadge(false)
                setSound(null,null)
            }
            notificationManager.createNotificationChannel(channel)

        }
    }

    private fun stopNotif() {
        stopForeground(false)
    }
}
