package com.iqonic.store.activity

import android.os.Bundle
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.utils.extensions.launchActivity
import com.iqonic.store.utils.extensions.runDelayed
import android.view.WindowManager

class SplashActivity : AppBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_new)
        val w = window
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        runDelayed(1500) {
            launchActivity<WalkThroughActivity>()
            finish()
        }
    }
}