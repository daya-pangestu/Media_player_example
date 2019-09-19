package com.daya.mysound

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.AssetFileDescriptor
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity(),View.OnClickListener {

    private var mService: Messenger? = null
    private var mBoundService : Intent? =null
    private var mServiceBound = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_play.setOnClickListener(this)
        btn_stop.setOnClickListener(this)

        mBoundService = Intent(this, MediaService::class.java)
        mBoundService!!.action = MediaService.ACTION_CREATE

        startService(mBoundService)
        bindService(mBoundService,mServiceConection, Context.BIND_AUTO_CREATE)
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_play -> {
                if (!mServiceBound) return
                try {
                    mService?.send(Message.obtain(null, MediaService.PLAY, 0, 0))
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
            R.id.btn_stop->{
                if (!mServiceBound) return
                try {
                    mService?.send(Message.obtain(null, MediaService.STOP, 0, 0))
                } catch (e: RemoteException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("tag", "ondestroy")
        unbindService(mServiceConection)
        mBoundService?.setAction(MediaService.ACTION_DESTROY)
        startService(mBoundService)
    }


    private val mServiceConection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            mService = null
            mServiceBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            mService = Messenger(service)
            mServiceBound = true
        }

    }





    }

