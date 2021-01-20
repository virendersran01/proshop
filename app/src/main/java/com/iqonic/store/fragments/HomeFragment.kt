package com.iqonic.store.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.ShopHopApp
import com.iqonic.store.activity.*
import com.iqonic.store.adapter.BaseAdapter
import com.iqonic.store.adapter.HomeSliderAdapter
import com.iqonic.store.models.RequestModel
import com.iqonic.store.models.StoreProductModel
import com.iqonic.store.utils.CarouselEffectTransformer
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.Constants.SharedPref.CONTACT
import com.iqonic.store.utils.Constants.SharedPref.COPYRIGHT_TEXT
import com.iqonic.store.utils.Constants.SharedPref.DEFAULT_CURRENCY
import com.iqonic.store.utils.Constants.SharedPref.DEFAULT_CURRENCY_FORMATE
import com.iqonic.store.utils.Constants.SharedPref.ENABLE_COUPONS
import com.iqonic.store.utils.Constants.SharedPref.FACEBOOK
import com.iqonic.store.utils.Constants.SharedPref.INSTAGRAM
import com.iqonic.store.utils.Constants.SharedPref.LANGUAGE
import com.iqonic.store.utils.Constants.SharedPref.PAYMENT_METHOD
import com.iqonic.store.utils.Constants.SharedPref.PRIVACY_POLICY
import com.iqonic.store.utils.Constants.SharedPref.TERM_CONDITION
import com.iqonic.store.utils.Constants.SharedPref.TWITTER
import com.iqonic.store.utils.Constants.SharedPref.WHATSAPP
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.item_home_newest_product.view.*
import kotlinx.android.synthetic.main.menu_cart.view.*

class HomeFragment : BaseFragment() {
    private var mMenuCart: View? = null
    var image: String = ""
    private var isAddedTocart: Boolean = false
    private lateinit var lan: String

    private val mNewestAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_home_newest_product,
        onBind = { view, model, _ ->
            setProductItem1(view, model)
        })
    private val mFeatureAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_home_newest_product,
        onBind = { view, model, _ ->
            setProductItem1(view, model)
        })
    private val mSellingAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_home_newest_product,
        onBind = { view, model, _ ->
            setProductItem1(view, model)
        })
    private val mSaleAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_home_newest_product,
        onBind = { view, model, _ ->
            setProductItem1(view, model)
        })
    private val mDealAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_viewproductgrid,
        onBind = { view, model, _ ->
            setProductItem1(view, model, true)
        })
    private val mOfferAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_viewproductgrid,
        onBind = { view, model, _ ->
            setProductItem1(view, model, true)
        })
    private val mSuggestAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_home_newest_product,
        onBind = { view, model, _ ->
            setProductItem1(view, model)
        })
    private val mLikeAdapter = BaseAdapter<StoreProductModel>(
        R.layout.item_home_newest_product,
        onBind = { view, model, _ ->
            setProductItem1(view, model)
        })

    private fun setProductItem1(
        view: View,
        model: StoreProductModel,
        params: Boolean = false
    ) {
        if (!params) {
            view.ivProduct.layoutParams = activity?.productLayoutParams()
        } else {
            view.ivProduct.layoutParams = activity?.productLayoutParamsForDealOffer()
        }

        if (model.images!![0].src!!.isNotEmpty()) {
            view.ivProduct.loadImageFromUrl(model.images!![0].src!!)
            image = model.images!![0].src!!
        }

        val mName = model.name!!.split(",")

        view.tvProductName.text = mName[0]
        view.tvDiscountPrice.setTextColor((activity as AppBaseActivity).color(R.color.colorAccent))
        if (!model.onSale) {
            view.tvDiscountPrice.text = model.price!!.currencyFormat()
            view.tvOriginalPrice.visibility = View.VISIBLE
            view.tvSaleLabel.visibility = View.GONE
            view.tvOriginalPrice.text = ""
        } else {
            if (model.salePrice!!.isNotEmpty()) {
                view.tvSaleLabel.visibility = View.VISIBLE
                view.tvDiscountPrice.text = model.salePrice!!.currencyFormat()
                view.tvOriginalPrice.applyStrike()
                view.tvOriginalPrice.text = model.regularPrice!!.currencyFormat()
                view.tvOriginalPrice.visibility = View.VISIBLE
            } else {
                view.tvSaleLabel.visibility = View.GONE
                view.tvOriginalPrice.visibility = View.VISIBLE
                if (model.regularPrice!!.isEmpty()) {
                    view.tvOriginalPrice.text = ""
                    view.tvDiscountPrice.text = model.price!!.currencyFormat()
                } else {
                    view.tvOriginalPrice.text = ""
                    view.tvDiscountPrice.text = model.regularPrice!!.currencyFormat()
                }
            }
        }
        if (model.attributes!!.isNotEmpty()) {
            view.tvProductWeight.text = model.attributes!![0].options!![0]
            view.tvProductWeight.setTextColor((activity as AppBaseActivity).color(R.color.colorAccent))
        }
        if (model.in_stock) {
            view.tvAdd.show()
        } else {
            view.tvAdd.hide()
        }
        if (!model.purchasable) {
            view.tvAdd.hide()
        } else {
            view.tvAdd.show()
        }
        view.onClick {
            activity?.launchActivity<ProductDetailActivityNew> {
                putExtra(Constants.KeyIntent.PRODUCT_ID, model.id)
                putExtra(Constants.KeyIntent.DATA, model)
            }
        }
        view.tvAdd.onClick {
            mAddCart(model)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_home, container, false)


    private fun mAddCart(model: StoreProductModel) {
        if (isLoggedIn()) {
            val requestModel = RequestModel()
            if (model.type == "variable") {
                requestModel.pro_id = model.variations!![0]
            } else {
                requestModel.pro_id = model.id
            }
            requestModel.quantity = 1
            (activity as AppBaseActivity).addItemToCart(requestModel, onApiSuccess = {
                isAddedTocart = true
                activity!!.fetchAndStoreCartData()
            })
        } else (activity as AppBaseActivity).launchActivity<SignInUpActivity> { }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHasOptionsMenu(true)
       // lan = ShopHopApp.language

        setClickEventListener()

        setRecyclerView(rvNewProduct)
        setRecyclerView(rcvFeaturedProducts)
        setRecyclerView(rcvBestSelling)
        setRecyclerView(rcvSale)
        setRecyclerView(rcvSuggest)
        setRecyclerView(rcvLike)
        rcvDeal.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 2)
        }
        rcvOffer.apply {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(activity, 2)
        }
        if (isLoggedIn()) {
            loadApis()
        }
        rvNewProduct?.adapter = mNewestAdapter
        rcvFeaturedProducts?.adapter = mFeatureAdapter
        rcvBestSelling?.adapter = mSellingAdapter
        rcvSale?.adapter = mSaleAdapter
        rcvDeal?.adapter = mDealAdapter
        rcvOffer?.adapter = mOfferAdapter
        rcvSuggest?.adapter = mSuggestAdapter
        rcvLike?.adapter = mLikeAdapter
        listAllProducts()
        refreshLayout.setOnRefreshListener {
            mNewestAdapter.clearItems()
            mFeatureAdapter.clearItems()
            mSellingAdapter.clearItems()
            mSaleAdapter.clearItems()
            mDealAdapter.clearItems()
            mOfferAdapter.clearItems()
            mSuggestAdapter.clearItems()
            mLikeAdapter.clearItems()
            listAllProducts()
            refreshLayout.isRefreshing = false
        }
        refreshLayout.viewTreeObserver.addOnScrollChangedListener {
            refreshLayout.isEnabled = scrollView.scrollY == 0
        }
    }

    private fun showLoader() {
        rvNewProduct?.showShimmerAdapter()
        rcvFeaturedProducts?.showShimmerAdapter()
        rcvBestSelling?.showShimmerAdapter()
        rcvSale?.showShimmerAdapter()
        rcvSuggest?.showShimmerAdapter()
        rcvLike?.showShimmerAdapter()
        rcvDeal?.showShimmerAdapter()
        rcvOffer?.showShimmerAdapter()

    }

    private fun loadApis() {
        if (isNetworkAvailable()) {
            activity!!.fetchAndStoreCartData()
        }
    }

    private fun hideLoader() {
        rvNewProduct?.hideShimmerAdapter()
        rcvFeaturedProducts?.hideShimmerAdapter()
        rcvBestSelling?.hideShimmerAdapter()
        rcvSale?.hideShimmerAdapter()
        rcvSuggest?.hideShimmerAdapter()
        rcvLike?.hideShimmerAdapter()
        rcvDeal?.hideShimmerAdapter()
        rcvOffer?.hideShimmerAdapter()
    }


    private fun setClickEventListener() {

        viewPopular.onClick {
            activity?.launchActivity<ViewAllProductActivity> {
                putExtra(Constants.KeyIntent.TITLE, getString(R.string.lbl_Featured))
                putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.FEATURED)
            }
        }

        viewNewest.onClick {
            activity?.launchActivity<ViewAllProductActivity> {
                putExtra(Constants.KeyIntent.TITLE, getString(R.string.lbl_newest_product))
                putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.NEWEST)
            }
        }
        viewSelling.onClick {
            activity?.launchActivity<ViewAllProductActivity> {
                putExtra(Constants.KeyIntent.TITLE, getString(R.string.lbl_selling))
                putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.BESTSELLING)
            }
        }
        viewSale.onClick {
            activity?.launchActivity<ViewAllProductActivity> {
                putExtra(Constants.KeyIntent.TITLE, getString(R.string.lbl_sale))
                putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.SALE)
            }
        }
        viewOffer.onClick {
            activity?.launchActivity<ViewAllProductActivity> {
                putExtra(Constants.KeyIntent.TITLE, getString(R.string.title_offer))
                putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.SPECIAL_PRODUCT)
                putExtra(Constants.KeyIntent.SPECIAL_PRODUCT_KEY, "offer")
            }
        }
        viewSuggest.onClick {
            activity?.launchActivity<ViewAllProductActivity> {
                putExtra(Constants.KeyIntent.TITLE, getString(R.string.lbl_rec_product))
                putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.SPECIAL_PRODUCT)
                putExtra(Constants.KeyIntent.SPECIAL_PRODUCT_KEY, "suggested_for_you")

            }
        }
        viewlike.onClick {
            activity?.launchActivity<ViewAllProductActivity> {
                putExtra(Constants.KeyIntent.TITLE, getString(R.string.lbl_you_may_like))
                putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.SPECIAL_PRODUCT)
                putExtra(Constants.KeyIntent.SPECIAL_PRODUCT_KEY, "you_may_like")

            }
        }
        btnViewAllProduct.onClick {
            activity?.launchActivity<ViewAllProductActivity> {
                putExtra(Constants.KeyIntent.TITLE, getString(R.string.lbl_deals_of_the_day))
                putExtra(Constants.KeyIntent.VIEWALLID, Constants.viewAllCode.SPECIAL_PRODUCT)
                putExtra(Constants.KeyIntent.SPECIAL_PRODUCT_KEY, "deal_of_the_day")

            }
        }
    }

    private fun setRecyclerView(recyclerView: RecyclerView?) {
        recyclerView?.setHorizontalLayout()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.clear()
        inflater.inflate(R.menu.menu_dashboard, menu)
        val menuWishItem: MenuItem = menu.findItem(R.id.action_cart)
        menuWishItem.isVisible = true
        mMenuCart = menuWishItem.actionView
        mMenuCart?.onClick {
            if (isLoggedIn()) {
                launchActivity<MyCartActivity>()
            } else {
                launchActivity<SignInUpActivity>()
            }
        }
        setCartCount()


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_search -> {
                activity?.launchActivity<SearchActivity>()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun setCartCount() {
        val count = getCartCount()
        mMenuCart?.tvNotificationCount?.text = count
        Log.d("Count",count)
        if (count.checkIsEmpty() || count == "0") {
            mMenuCart?.tvNotificationCount?.hide()
        } else {
            mMenuCart?.tvNotificationCount?.show()
        }
    }

    private fun listAllProducts() {
        if (isNetworkAvailable()) {
            showLoader()
            loadDashboardData()
        }
    }

    private fun loadDashboardData() {
        getRestApiImpl().getDashboardData(onApiSuccess = {
            hideLoader()

            getSharedPrefInstance().apply {
                removeKey(WHATSAPP)
                removeKey(FACEBOOK)
                removeKey(TWITTER)
                removeKey(INSTAGRAM)
                removeKey(CONTACT)
                removeKey(PRIVACY_POLICY)
                removeKey(TERM_CONDITION)
                removeKey(COPYRIGHT_TEXT)
                removeKey(LANGUAGE)
                setValue(LANGUAGE, it.app_lang)
                setValue(DEFAULT_CURRENCY, it.currency_symbol.currency_symbol)
                setValue(DEFAULT_CURRENCY_FORMATE, it.currency_symbol.currency)
                setValue(WHATSAPP, it.social_link.whatsapp)
                setValue(FACEBOOK, it.social_link.facebook)
                setValue(TWITTER, it.social_link.twitter)
                setValue(INSTAGRAM, it.social_link.instagram)
                setValue(CONTACT, it.social_link.contact)
                setValue(PRIVACY_POLICY, it.social_link.privacy_policy)
                setValue(TERM_CONDITION, it.social_link.term_condition)
                setValue(COPYRIGHT_TEXT, it.social_link.copyright_text)
                setValue(ENABLE_COUPONS, it.enable_coupons)
                setValue(PAYMENT_METHOD, it.payment_method)

            }
            setNewLocale(it.app_lang)

            if (it.banner.isNotEmpty()) {
                rl_head?.visibility = View.VISIBLE
                val adapter = HomeSliderAdapter(it.banner)
                if (slideViewPager != null) {
                    adapter.setListener(object : HomeSliderAdapter.OnClickListener {
                        override fun onClick(position: Int) {
                            launchActivity<WebViewExternalProductActivity> {
                                putExtra(Constants.KeyIntent.EXTERNAL_URL, it.banner[position].url)
                            }
                        }
                    })
                    slideViewPager?.adapter = adapter
                    dots.attachViewPager(slideViewPager)
                    dots.setDotDrawable(R.drawable.bg_circle_primary, R.drawable.black_dot)
                    slideViewPager.pageMargin = resources.getDimensionPixelOffset(R.dimen._6sdp)
                    slideViewPager.setPageTransformer(
                        false,
                        object : CarouselEffectTransformer(activity) {})
                }
            } else {
                rl_head?.visibility = View.GONE
            }


            if (it.newest.isNotEmpty()) {
                rlNewest?.show()
                rvNewProduct?.show()
                mNewestAdapter.addItems(it.newest)
                mNewestAdapter.setModelSize(2)
            } else {
                rlNewest.hide()
                rvNewProduct.hide()
            }

            if (it.featured.isEmpty()) {
                rlFeatured?.hide()
                rcvFeaturedProducts?.hide()
            } else {
                rlFeatured?.show()
                rcvFeaturedProducts?.show()
                mFeatureAdapter.addItems(it.featured)
                mFeatureAdapter.setModelSize(5)
            }

            if (it.best_selling_product.isEmpty()) {
                rlBestSelling?.hide()
                rcvBestSelling?.hide()
            } else {
                rlBestSelling?.show()
                rcvBestSelling?.show()
                mSellingAdapter.addItems(it.best_selling_product)
                mSellingAdapter.setModelSize(5)

            }


            if (it.sale_product.isEmpty()) {
                rlSale?.hide()
                rcvSale?.hide()
            } else {
                rlSale?.show()
                rcvSale?.show()
                mSaleAdapter.addItems(it.sale_product)
                mSaleAdapter.setModelSize(5)

            }

            if (it.deal_of_the_day.isEmpty()) {
                llDeal?.hide()
                rcvDeal?.hide()
            } else {
                llDeal?.show()
                rcvDeal?.show()
                mDealAdapter.addItems(it.deal_of_the_day)
                mDealAdapter.setModelSize(2)
            }


            if (it.offer.isEmpty()) {
                rlOffer?.hide()
                rcvOffer?.hide()
            } else {
                rlOffer?.show()
                rcvOffer?.show()
                mOfferAdapter.addItems(it.offer)
                mOfferAdapter.setModelSize(5)

            }

            if (it.suggested_for_you.isEmpty()) {
                rlSuggest?.hide()
                rcvSuggest?.hide()
            } else {
                rlSuggest?.show()
                rcvSuggest?.show()
                mSuggestAdapter.addItems(it.suggested_for_you)
                mSuggestAdapter.setModelSize(5)

            }
            if (it.you_may_like.isEmpty()) {
                rlLike?.hide()
                rcvLike?.hide()
            } else {
                rlLike?.show()
                rcvLike?.show()
                mLikeAdapter.addItems(it.you_may_like)
                mLikeAdapter.setModelSize(5)
            }

        }, onApiError = {
            showLoader()
            snackBar(it)
        })
    }

    private fun setNewLocale(language: String) {
        ShopHopApp.changeLanguage(language)
        lan=ShopHopApp.language
        Log.e("lan", lan)
        if (lan != language) {
            (activity as AppBaseActivity).recreate()
            //(activity as AppBaseActivity).setResult(Activity.RESULT_OK)
        }
    }

}