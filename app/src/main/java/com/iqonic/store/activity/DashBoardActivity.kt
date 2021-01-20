package com.iqonic.store.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.ShopHopApp
import com.iqonic.store.fragments.HomeFragment
import com.iqonic.store.fragments.ViewAllProductFragment
import com.iqonic.store.fragments.WishListFragment
import com.iqonic.store.utils.BroadcastReceiverExt
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.Constants.AppBroadcasts.CART_COUNT_CHANGE
import com.iqonic.store.utils.Constants.AppBroadcasts.PROFILE_UPDATE
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.layout_sidebar.*
import kotlinx.android.synthetic.main.toolbar.*


class DashBoardActivity : AppBaseActivity() {

    private var count: String = ""
    private val mHomeFragment = HomeFragment()
    private val mWishListFragment = WishListFragment()
    private val mViewAllProductFragment = ViewAllProductFragment()
    private var selectedFragment: Fragment? = null

    private var mModeFlag: Boolean = false

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        mModeFlag = ShopHopApp.appTheme == Constants.THEME.DARK

        setToolbar(toolbar)
        setUpDrawerToggle()
        setListener()

        if (isLoggedIn()) {
            cartCount()
            setCartCountFromPref()
        }

        setUserInfo()
        tvHome.onClick {
            loadFragment(mHomeFragment)
            title = getString(R.string.home)
            closeDrawer()
        }
        BroadcastReceiverExt(this) {
            onAction(CART_COUNT_CHANGE) {
                setCartCountFromPref()
            }
            onAction(PROFILE_UPDATE) {
                setUserInfo(); changeProfile()
            }
        }
        tvWishlist.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>()
                return@onClick
            }
            closeDrawer()
            launchActivity<WishlistActivity>()
        }
        tvCategories.onClick {
            launchActivity<CategoryActivity> {
            }
        }
        tvCart.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>()
                return@onClick
            }
            closeDrawer()
            launchActivity<MyCartActivity>()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.RequestCode.ACCOUNT) {
                loadWishlistFragment()
            } else if (requestCode == Constants.RequestCode.LOGIN) {
                setUserInfo()
            }
        }
    }

    private fun setUserInfo() {
        txtDisplayName.text = getDisplayName()
        txtDisplayEmail.text = getEmail()
        when {
            isLoggedIn() -> {
                tvSignIn.text = getString(R.string.btn_sign_out)
                tvCart.show()
                tvWishlist.show()
                tvOrder.show()
                tvChangePwd.show()
                rlProfile.show()
            }
            else -> {
                tvSignIn.text = getString(R.string.lbl_sign_in_link)
                tvCart.hide()
                tvWishlist.hide()
                tvOrder.hide()
                tvChangePwd.hide()
                rlProfile.hide()
            }
        }

        when {
            mModeFlag -> tvMode.text = getString(R.string.lbl_light_mode)
            else -> tvMode.text = getString(R.string.lbl_night_mode)
        }

    }

    private fun closeDrawer() {
        if (drawerLayout.isDrawerOpen(llLeftDrawer))
            drawerLayout.closeDrawer(llLeftDrawer)
    }

    private fun setUpDrawerToggle() {
        val toggle = object :
            ActionBarDrawerToggle(
                this, drawerLayout,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close
            ) {
            private val scaleFactor = 4f
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                super.onDrawerSlide(drawerView, slideOffset)
                val slideX = drawerView.width * slideOffset

                when {
                    ShopHopApp.language == "ar" -> main.translationX = -slideX
                    else -> main.translationX = slideX
                }
                main.scaleX = 1 - slideOffset / scaleFactor
                main.scaleY = 1 - slideOffset / scaleFactor
            }
        }
        drawerLayout.setScrimColor(Color.TRANSPARENT)
        drawerLayout.drawerElevation = 0f
        toggle.isDrawerIndicatorEnabled = false
        toolbar.setNavigationIcon(R.drawable.ic_drawer)
        toolbar.setNavigationOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
    }

    private fun setListener() {
        loadHomeFragment()
        rlProfile.onClick {
            when {
                isLoggedIn() -> launchActivity<EditProfileActivity>()
                else -> launchActivity<SignInUpActivity>()
            }
        }

        tvOrder.onClick {
            when {
                !isLoggedIn() -> launchActivity<SignInUpActivity>()
                else -> launchActivity<OrderActivity>()
            }
            closeDrawer()

        }
        tvAbout.onClick { launchActivity<AboutActivity>(); closeDrawer() }

        tvChangePwd.onClick {
            when {
                !isLoggedIn() -> launchActivity<SignInUpActivity>()
                else -> launchActivity<ChangePwdActivity>()
            }
            closeDrawer()
        }
        tvSignIn.onClick {
            when {
                isLoggedIn() -> {
                    val dialog = getAlertDialog(
                        getString(R.string.lbl_logout_confirmation),
                        onPositiveClick = { _, _ ->
                            clearLoginPref()
                            launchActivityWithNewTask<DashBoardActivity>()
                        },
                        onNegativeClick = { dialog, _ ->
                            dialog.dismiss()
                        })
                    dialog.show()
                    closeDrawer()
                }
                else -> launchActivity<SignInUpActivity>(Constants.RequestCode.LOGIN)
            }
        }
        tvMode.onClick {
            when {
                mModeFlag -> {
                    ShopHopApp.changeAppTheme(false)
                    switchToDarkMode(false)
                }
                else -> {
                    ShopHopApp.changeAppTheme(true)
                    switchToDarkMode(true)
                }
            }
            closeDrawer()
        }
    }

    private fun loadFragment(aFragment: Fragment) {

        when {
            selectedFragment != null -> {
                if (selectedFragment == aFragment) {
                    return
                }
                hideFragment(selectedFragment!!)
            }
        }
        when {
            aFragment.isAdded -> {
                showFragment(aFragment)
            }
            else -> {
                addFragment(aFragment, R.id.container)
            }
        }
        selectedFragment = aFragment
    }

    fun loadHomeFragment() {
        loadFragment(mHomeFragment)
        title = getString(R.string.home)
    }

    fun loadWishlistFragment() {
        loadFragment(mWishListFragment)
        title = getString(R.string.lbl_wish_list)
    }

    override fun onBackPressed() {
        when {
            drawerLayout.isDrawerOpen(GravityCompat.START) -> drawerLayout.closeDrawer(GravityCompat.START)
            mViewAllProductFragment.isVisible -> {
                loadHomeFragment()
            }

            else -> super.onBackPressed()
        }

    }

    private fun cartCount() {
        setCartCountFromPref()
        sendCartCountChangeBroadcast()
    }

    private fun setCartCountFromPref() {
        count = getCartCount()
        mHomeFragment.setCartCount()
    }

    fun changeProfile() {
        if (isLoggedIn()) {
            civProfile.loadImageFromUrl(getUserProfile(), aPlaceHolderImage = R.drawable.ic_profile)
        }
    }
}