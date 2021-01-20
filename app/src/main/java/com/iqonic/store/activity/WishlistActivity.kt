package com.iqonic.store.activity

import android.os.Bundle
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.fragments.WishListFragment
import com.iqonic.store.utils.extensions.addFragment
import kotlinx.android.synthetic.main.toolbar.*

class WishlistActivity : AppBaseActivity() {

    private var myCartFragment: WishListFragment = WishListFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_cart)

        setToolbar(toolbar)
        title = getString(R.string.lbl_wish_list)

        addFragment(myCartFragment, R.id.container)
    }

}
