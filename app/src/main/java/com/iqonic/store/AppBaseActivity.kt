package com.iqonic.store

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.iqonic.store.ShopHopApp.Companion.noInternetDialog
import com.iqonic.store.activity.DashBoardActivity
import com.iqonic.store.utils.Constants.SharedPref.LANGUAGE
import com.iqonic.store.utils.Constants.THEME.DARK
import com.iqonic.store.utils.extensions.*
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.layout_sidebar.*
import java.util.*

open class AppBaseActivity : AppCompatActivity() {
    private var progressDialog: Dialog? = null
    var language: Locale? = null
    private var themeApp: Int = 0
    var onNetworkRetry: (() -> Unit)? = null

    fun disableHardwareRendering(v: View) {
        v.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
    }

    fun setToolbar(mToolbar: Toolbar) {
        setSupportActionBar(mToolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        mToolbar.setNavigationIcon(R.drawable.ic_keyboard_backspace)
        mToolbar.changeToolbarFont()
        mToolbar.navigationIcon!!.setColorFilter(resources.getColor(R.color.colorBackArrow), PorterDuff.Mode.SRC_ATOP)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        switchToDarkMode(ShopHopApp.appTheme == DARK)
        super.onCreate(savedInstanceState)
        setStatusBarGradient(this)
        noInternetDialog = null

        if (progressDialog == null) {
            progressDialog = Dialog(this)
            progressDialog!!.window!!.setBackgroundDrawable(ColorDrawable(0))
            progressDialog!!.setContentView(R.layout.custom_dialog)
        }
        themeApp = ShopHopApp.appTheme
        language = Locale(ShopHopApp.language)
        if (!isNetworkAvailable()) {
            openNetworkDialog {
                recreate();onNetworkRetry?.invoke() }
        }

    }

    private fun setStatusBarGradient(activity: Activity) {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> {
                val window = activity.window
                //val background = activity.resources.getDrawable(R.drawable.bg_toolbar_gradient)
                var flags = activity.window.decorView.systemUiVisibility // get current flag
                flags =
                        flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR   // add LIGHT_STATUS_BAR to flag
                activity.window.decorView.systemUiVisibility = flags
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorToolBarBackground)
                //window.setBackgroundDrawable(background)
            }
            else -> {
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimaryDark)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    fun showProgress(show: Boolean) {
        when {
            show -> {
                if (!isFinishing) {
                    /*  progressDialog!!.setTitle(getString(R.string.msg_loading))
                      progressDialog!!.setMessage(getString(R.string.msg_please_wait))*/
                    progressDialog!!.setCanceledOnTouchOutside(false)
                    progressDialog!!.show()
                }
            }
            else -> try {
                if (progressDialog!!.isShowing && !isFinishing) {
                    progressDialog!!.dismiss()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun changeLanguage(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        return context.createConfigurationContext(config)
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(updateBaseContextLocale(newBase!!)))
    }

    private fun updateBaseContextLocale(context: Context): Context {
        val language = getSharedPrefInstance().getStringValue(LANGUAGE, getLanguage())
        val locale = Locale(language)
        Locale.setDefault(locale)
        return changeLanguage(context, locale)

    }

    override fun onResume() {
        super.onResume()
        val locale = Locale(ShopHopApp.language)
        val appTheme = ShopHopApp.appTheme

        if (language != null && locale != language) {
            recreate()
            return
        }
        if (themeApp != 0 && themeApp != appTheme) {
            launchActivity<DashBoardActivity>()
        }
    }

}
