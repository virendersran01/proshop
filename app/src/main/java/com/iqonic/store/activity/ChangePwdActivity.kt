package com.iqonic.store.activity

import android.os.Bundle
import com.iqonic.store.AppBaseActivity
import com.iqonic.store.R
import com.iqonic.store.fragments.BaseFragment
import com.iqonic.store.models.RequestModel
import com.iqonic.store.utils.Constants
import com.iqonic.store.utils.extensions.*
import kotlinx.android.synthetic.main.activity_change_pwd.*
import kotlinx.android.synthetic.main.toolbar.*

class ChangePwdActivity : AppBaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_pwd)
        setToolbar(toolbar)
        title=getString(R.string.lbl_change_pwd)
        edtOldPwd.transformationMethod = BaseFragment.BiggerDotTransformation
        edtNewPwd.transformationMethod = BaseFragment.BiggerDotTransformation
        edtConfirmPwd.transformationMethod = BaseFragment.BiggerDotTransformation
        btnChangePassword.onClick {
            val mPassword =
                getSharedPrefInstance().getStringValue(Constants.SharedPref.USER_PASSWORD)
            when {
                edtOldPwd.checkIsEmpty() -> {
                    edtOldPwd.showError(getString(R.string.error_field_required))
                }
                edtNewPwd.checkIsEmpty() -> {
                    edtNewPwd.showError(getString(R.string.error_field_required))
                }
                edtNewPwd.validPassword() -> {
                    edtNewPwd.showError(getString(R.string.error_pwd_digit_required))
                }
                edtConfirmPwd.checkIsEmpty() -> {
                    edtConfirmPwd.showError(getString(R.string.error_field_required))
                }
                edtConfirmPwd.validPassword() -> {
                    edtConfirmPwd.showError(getString(R.string.error_pwd_digit_required))
                }
                !edtConfirmPwd.text.toString().equals(
                    edtNewPwd.text.toString(),
                    false
                ) -> {
                    edtConfirmPwd.showError(getString(R.string.error_password_not_matches))
                }
                else -> {
                    val requestModel = RequestModel()
                    requestModel.password = edtOldPwd.text.toString()
                    requestModel.new_password = edtNewPwd.text.toString()
                    requestModel.username = getUserName()
                    showProgress(true)
                    if (isNetworkAvailable()) {
                        showProgress(true)
                        getRestApiImpl().changePwd(requestModel, onApiSuccess = {
                            showProgress(false)
                            snackBar(it.message!!)
                        }, onApiError = {
                            showProgress(false)
                            snackBar(it)
                        })
                    } else {
                        showProgress(false)
                        noInternetSnackBar()
                    }

                }
            }
        }
    }
}
