package com.koalatea.sedaily

import android.app.Application
import android.content.Context
import br.com.bemobi.medescope.Medescope

class SEDApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SEDApp.appContext = getApplicationContext()
        Medescope.getInstance(this).setApplicationName("My Application Name")
    }

    companion object {

        var appContext: Context? = null
            private set
    }
}