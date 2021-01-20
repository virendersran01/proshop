package com.iqonic.store.activity

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.BuildConfig
import com.iqonic.store.R
import com.iqonic.store.models.*
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.Constants.SharedPref.USER_PICODE
import com.iqonic.store.utils.ImagePicker
import com.iqonic.store.utils.extensions.*
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.ByteArrayOutputStream
import java.io.File

class EditProfileActivity : AppBaseActivity() {

    private var uri: Uri? = null
    private var encodedImage: String? = null
    private var mShippingCountryAdapter: ArrayAdapter<String>? = null
    private var mBillingCountryAdapter: ArrayAdapter<String>? = null
    private var mShippingStateAdapter: ArrayAdapter<String>? = null
    private var mBillingStateAdapter: ArrayAdapter<String>? = null
    private val mShippingCountryList = ArrayList<String>()
    private val mBillingCountryList = ArrayList<String>()
    private val mShippingStateList = ArrayList<String>()
    private val mBillingStateList = ArrayList<String>()
    var countryList = ArrayList<CountryModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        setToolbar(toolbar)
        title = getString(R.string.lbl_edit_profile)
        ivProfile.loadImageFromUrl(getUserProfile(), aPlaceHolderImage = R.drawable.ic_profile)
        tvCountWishList.text = getWishListCount()
        tvOrderCount.text = getOrderCount()

        cbCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                edtShippingFName.setText(edtBillingFName.text.toString())
                edtShippingLName.setText(edtBillingLName.text.toString())
                edtShippingCompany.setText(edtBillingCompany.text.toString())
                edtShippingAdd1.setText(edtBillingAdd1.text.toString())
                edtShippingAdd2.setText(edtBillingAdd2.text.toString())
                edtShippingCity.setText(edtBillingCity.text.toString())
                edtShippingPinCode.setText(edtBillingPinCode.text.toString())
                spState.setSelection(spBillingState.selectedItemPosition)
                spCountry.setSelection(spBillingCountry.selectedItemPosition)
            } else {
                edtShippingFName.text.clear()
                edtShippingLName.text.clear()
                edtShippingCompany.text.clear()
                edtShippingAdd1.text.clear()
                edtShippingAdd2.text.clear()
                edtShippingCity.text.clear()
                edtShippingPinCode.text.clear()
            }
        }
        mShippingCountryAdapter = ArrayAdapter(this, R.layout.spinner_items, mShippingCountryList)
        spCountry.adapter = this.mShippingCountryAdapter

        mBillingCountryAdapter = ArrayAdapter(this, R.layout.spinner_items, mBillingCountryList)
        spBillingCountry.adapter = this.mBillingCountryAdapter

        mBillingStateAdapter = ArrayAdapter(this, R.layout.spinner_items, mBillingStateList)
        spBillingState.adapter = this.mBillingStateAdapter

        mShippingStateAdapter = ArrayAdapter(this, R.layout.spinner_items, mShippingStateList)
        spState.adapter = this.mShippingStateAdapter
        if (isLoggedIn()) {
            getData()
        }
        setUpListener()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                val resultUri = result.uri
                ivProfile.setImageURI(resultUri)
                val imageStream = this.contentResolver.openInputStream(resultUri)
                val selectedImage = BitmapFactory.decodeStream(imageStream)
                encodedImage = encodeImage(selectedImage)
                if (encodedImage != null) {
                    updateProfilePhoto()
                }
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                val error = result.error
                if (error?.message != null) {
                    snackBar(error.message!!)
                }
            }
        } else {
            if (data != null && data.data != null) ivProfile.setImageURI(data.data)
            val path: String? =
                ImagePicker.getImagePathFromResult(this, requestCode, resultCode, data) ?: return
            val uri = FileProvider.getUriForFile(
                this,
                BuildConfig.APPLICATION_ID + ".provider",
                File(path)
            )
            CropImage.activity(uri)
                .setOutputCompressQuality(40)
                .start(this)
        }
    }

    private fun encodeImage(bm: Bitmap): String {
        val baos = ByteArrayOutputStream()
        bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val b = baos.toByteArray()
        return Base64.encodeToString(b, Base64.DEFAULT)
    }

    private fun setUpListener() {
        btnSaveProFile.onClick {
            if (validate()) {
                updateProfile()
            }
        }
        editProfileImage.onClick {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), onResult = {
                    if (it) {
                        CropImage.activity()
                            .setAspectRatio(1, 1)
                            .setGuidelines(CropImageView.Guidelines.OFF)
                            .setRequestedSize(300, 300)
                            .setOutputCompressQuality(40)
                            .start(this@EditProfileActivity)
                    } else {
                        showPermissionAlert(this)
                    }
                })
        }

        llOrder.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>()
                return@onClick
            }
            launchActivity<OrderActivity>()
        }
        llWishList.onClick {
            if (!isLoggedIn()) {
                launchActivity<SignInUpActivity>()
                return@onClick
            }
            launchActivity<WishlistActivity>()
        }
        spBillingCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                onBillingCountryChanged(countryList[position].states)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }
        spCountry.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                onShippingCountryChanged(countryList[position].states)
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
        spBillingState.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(
                parent: AdapterView<*>, view: View,
                position: Int, id: Long
            ) {
                if (cbCheck.isChecked) {
                    spState.setSelection(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
            }
        }


    }

    private fun updateProfile() {
        val mBilling = Billing()
        mBilling.first_name = edtBillingFName.textToString()
        mBilling.last_name = edtBillingLName.textToString()
        mBilling.address_1 = edtBillingAdd1.textToString()
        mBilling.address_2 = edtBillingAdd2.textToString()
        mBilling.city = edtBillingCity.textToString()
        mBilling.company = edtBillingCompany.textToString()
        mBilling.postcode = edtBillingPinCode.textToString()
        mBilling.country = spBillingCountry.selectedItem.toString()
        if (mBillingStateList.isNotEmpty()){
            mBilling.state = spBillingState.selectedItem.toString()
        }
        mBilling.phone = edtBillingPhone.textToString()
        mBilling.email = edtBillingEmail.textToString()

        val mShipping = Shipping()
        mShipping.first_name = edtShippingFName.textToString()
        mShipping.last_name = edtShippingLName.textToString()
        mShipping.address_1 = edtShippingAdd1.textToString()
        mShipping.company = edtShippingCompany.textToString()
        mShipping.address_2 = edtShippingAdd2.textToString()
        mShipping.city = edtShippingCity.textToString()
        mShipping.postcode = edtShippingPinCode.textToString()
        mShipping.country = spCountry.selectedItem.toString()
        if (mShippingStateList.isNotEmpty()){
            mShipping.state = spState.selectedItem.toString()
        }

        val requestModel = RequestModel()
        requestModel.userEmail = edtEmail.textToString()
        requestModel.firstName = edtFirstName.textToString()
        requestModel.lastName = edtLastName.textToString()
        requestModel.billing = mBilling
        requestModel.shipping = mShipping
        if (uri != null) {
            requestModel.image = uri.toString()
        }
        updateCustomer(requestModel) {
            snackBar(getString(R.string.lbl_profile_saved_successfully))
            showProgress(false)
            getSharedPrefInstance().removeKey(Constants.SharedPref.BILLING)
            getSharedPrefInstance().removeKey(Constants.SharedPref.SHIPPING)
            getSharedPrefInstance().setValue(
                Constants.SharedPref.BILLING,
                Gson().toJson(it.billing)
            )
            getSharedPrefInstance().setValue(
                Constants.SharedPref.SHIPPING,
                Gson().toJson(it.shipping)
            )
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    private fun validate(): Boolean {
        return when {
            edtFirstName.checkIsEmpty() -> {
                edtFirstName.showError(getString(R.string.error_field_required))
                false
            }
            !edtFirstName.isValidText() -> {
                edtFirstName.showError(getString(R.string.error_validText))
                false
            }
            edtLastName.checkIsEmpty() -> {
                edtLastName.showError(getString(R.string.error_field_required))
                false
            }
            !edtLastName.isValidText() -> {
                edtLastName.showError(getString(R.string.error_validText))
                false
            }
            edtEmail.checkIsEmpty() -> {
                edtEmail.showError(getString(R.string.error_field_required))
                false
            }
            !edtEmail.isValidEmail() -> {
                edtEmail.showError(getString(R.string.error_enter_valid_email))
                false
            }
            edtShippingFName.checkIsEmpty() -> {
                edtShippingFName.showError(getString(R.string.error_field_required))
                false
            }
            !edtShippingFName.isValidText() -> {
                edtShippingFName.showError(getString(R.string.error_validText))
                false
            }
            edtShippingLName.checkIsEmpty() -> {
                edtShippingLName.showError(getString(R.string.error_field_required))
                false
            }
            !edtShippingLName.isValidText() -> {
                edtShippingLName.showError(getString(R.string.error_validText))
                false
            }
            edtShippingCompany.checkIsEmpty() -> {
                edtShippingCompany.showError(getString(R.string.error_field_required))
                false
            }
            edtShippingPinCode.checkIsEmpty() -> {
                edtShippingPinCode.showError(getString(R.string.error_field_required))
                false
            }

            edtShippingAdd1.checkIsEmpty() -> {
                edtShippingAdd1.showError(getString(R.string.error_field_required))
                false
            }
            edtShippingAdd2.checkIsEmpty() -> {
                edtShippingAdd2.showError(getString(R.string.error_field_required))
                false
            }
            edtShippingCity.checkIsEmpty() -> {
                edtShippingCity.showError(getString(R.string.error_field_required))
                false
            }

            edtBillingFName.checkIsEmpty() -> {
                edtBillingFName.showError(getString(R.string.error_field_required))
                false
            }
            !edtBillingFName.isValidText() -> {
                edtBillingFName.showError(getString(R.string.error_validText))
                false
            }
            edtBillingLName.checkIsEmpty() -> {
                edtBillingLName.showError(getString(R.string.error_field_required))
                false
            }
            !edtBillingLName.isValidText() -> {
                edtBillingLName.showError(getString(R.string.error_validText))
                false
            }
            edtBillingAdd1.checkIsEmpty() -> {
                edtBillingAdd1.showError(getString(R.string.error_field_required))
                false
            }
            edtBillingAdd2.checkIsEmpty() -> {
                edtBillingAdd2.showError(getString(R.string.error_field_required))
                false
            }
            edtBillingCity.checkIsEmpty() -> {
                edtBillingCity.showError(getString(R.string.error_field_required))
                false
            }
            edtBillingPinCode.checkIsEmpty() -> {
                edtBillingPinCode.showError(getString(R.string.error_field_required))
                false
            }
            edtBillingPhone.checkIsEmpty() -> {
                edtBillingPhone.showError(getString(R.string.error_field_required))
                false
            }
            edtBillingEmail.checkIsEmpty() -> {
                edtBillingEmail.showError(getString(R.string.error_field_required))
                false
            }
            !edtBillingEmail.isValidEmail() -> {
                edtBillingEmail.showError(getString(R.string.error_enter_valid_email))
                false
            }
            else -> true
        }

    }

    private fun getData() {
        if (isNetworkAvailable()) {
            showProgress(true)
            getRestApiImpl().retrieveCustomer(onApiSuccess = { it ->
                showProgress(false)
                getSharedPrefInstance().setValue(Constants.SharedPref.SHOW_SWIPE, true)
                edtFirstName.setText(getSharedPrefInstance().getStringValue(Constants.SharedPref.USER_FIRST_NAME))
                edtLastName.setText(getSharedPrefInstance().getStringValue(Constants.SharedPref.USER_LAST_NAME))

                if (it.first_name.isNotEmpty()) {
                    getSharedPrefInstance().setValue(
                        Constants.SharedPref.USER_LAST_NAME,
                        it.first_name
                    )
                }

                if (it.last_name.isNotEmpty()) {
                    getSharedPrefInstance().setValue(
                        Constants.SharedPref.USER_LAST_NAME,
                        it.last_name
                    )
                }

                getSharedPrefInstance().setValue(Constants.SharedPref.USER_ROLE, it.role)
                getSharedPrefInstance().setValue(
                    Constants.SharedPref.BILLING,
                    Gson().toJson(it.billing)
                )
                getSharedPrefInstance().setValue(
                    Constants.SharedPref.SHIPPING,
                    Gson().toJson(it.shipping)
                )
                getSharedPrefInstance().setValue(USER_PICODE, it.shipping.postcode)

                tvUserName.text = it.username
                tvEmail.text = getEmail()
                edtEmail.setText(getEmail())

                edtFirstName.setSelection(edtFirstName.text.length)

                edtBillingFName.setText(it.billing.first_name)
                edtBillingLName.setText(it.billing.last_name)
                edtBillingAdd1.setText(it.billing.address_1)
                edtBillingAdd2.setText(it.billing.address_2)
                edtBillingCompany.setText(it.billing.company)
                edtBillingCity.setText(it.billing.city)
                edtBillingPinCode.setText(it.billing.postcode)
                edtBillingPhone.setText(it.billing.phone)
                edtBillingEmail.setText(it.billing.email)

                edtShippingFName.setText(it.shipping.first_name)
                edtShippingLName.setText(it.shipping.last_name)
                edtShippingAdd1.setText(it.shipping.address_1)
                edtShippingCompany.setText(it.shipping.company)
                edtShippingAdd2.setText(it.shipping.address_2)
                edtShippingCity.setText(it.shipping.city)
                edtShippingPinCode.setText(it.shipping.postcode)

                fetchCountry(onApiSuccess = { its ->
                    countryList.clear()
                    countryList.addAll(its)
                    mShippingCountryList.clear()
                    mBillingCountryList.clear()
                    mBillingStateList.clear()
                    mShippingStateList.clear()
                    var billingCountryPosition = 0
                    var billingStatePosition = 0
                    var shippingCountryPosition = 0
                    var shippingStatePosition = 0
                    its.forEachIndexed { i, attr ->
                        mShippingCountryList.add(attr.name.getHtmlString().toString())
                        mBillingCountryList.add(attr.name.getHtmlString().toString())
                        if (it.billing.country == attr.name) {
                            billingCountryPosition = i
                        }
                        if (it.shipping.country == attr.name) {
                            shippingCountryPosition = i
                        }
                    }
                    countryList[billingCountryPosition].states.forEachIndexed { index, state ->
                        if (it.billing.state == state.name) {
                            billingStatePosition = index
                        }
                        mBillingStateList.add(state.name)
                    }
                    countryList[shippingCountryPosition].states.forEachIndexed { index, state ->
                        if (it.shipping.state == state.name) {
                            shippingStatePosition = index
                        }
                        mShippingStateList.add(state.name)
                    }
                    mBillingCountryAdapter?.notifyDataSetChanged()
                    mShippingCountryAdapter?.notifyDataSetChanged()
                    mBillingStateAdapter?.notifyDataSetChanged()
                    mShippingStateAdapter?.notifyDataSetChanged()
                    spBillingCountry.setSelection(billingCountryPosition)
                    spBillingState.setSelection(billingStatePosition)
                    spCountry.setSelection(shippingCountryPosition)
                    spState.setSelection(shippingStatePosition)
                    if (mBillingStateList.isEmpty()){
                        spBillingState.hide()
                    }
                    if (mShippingStateList.isEmpty()){
                        spState.hide()
                    }


                }, onApiError = {
                    showProgress(false)
                })

            }, onApiError = {
                showProgress(false)
            })
        }

    }

    private fun onBillingCountryChanged(its: ArrayList<State>) {
        mBillingStateList.clear()
        if (cbCheck.isChecked) {
            mShippingStateList.clear()
        }
        its.forEach {
            mBillingStateList.add(it.name)
            if (cbCheck.isChecked) {
                mShippingStateList.add(it.name)
            }
        }
        mBillingStateAdapter?.notifyDataSetChanged()
        if (cbCheck.isChecked) {
            mShippingStateAdapter?.notifyDataSetChanged()
        }
        if (mBillingStateList.isEmpty()){
            spBillingState.hide()
        }else{
            spBillingState.show()
        }
        if (mShippingStateList.isEmpty()){
            spState.hide()
        }else{
            spState.show()
        }
    }

    private fun onShippingCountryChanged(its: ArrayList<State>) {
        mShippingStateList.clear()
        its.forEach {
            mShippingStateList.add(it.name)
        }

        mShippingStateAdapter?.notifyDataSetChanged()
        if (mShippingStateList.isEmpty()){
            spState.hide()
        }else{
            spState.show()
        }
    }

    private fun updateProfilePhoto() {
        showProgress(true)
        val requestModel = RequestModel()
        requestModel.base64_img = encodedImage
        saveProfileImage(requestModel, onApiSuccess = {
            showProgress(false)
            encodedImage = null
        })
    }
}
