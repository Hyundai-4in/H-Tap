package kr.co.htap.nfc.view

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.co.htap.databinding.FragmentNfcCouponWarningBinding
import kr.co.htap.databinding.FragmentNfcIdentificationFailedBinding
import kr.co.htap.helper.ViewBindingFragment
import kr.co.htap.register.LoginActivity

private const val ARG_PARAM = "errorMsg"

/**
 * 인증과정에서 문제가 발생한 경우 표시하는 프래그먼트
 * @author 이호연
 */
class NfcIdentificationWarningFragment :
    ViewBindingFragment<FragmentNfcCouponWarningBinding>() {
    // TODO: Rename and change types of parameters
    private var errorMsg: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            errorMsg = it.getString(ARG_PARAM) ?: ""
        }
    }

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): FragmentNfcCouponWarningBinding =
        FragmentNfcCouponWarningBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tvInfo.text = errorMsg
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param errorMsg Parameter 1.
         * @param loginRequired Parameter 2.
         * @return A new instance of fragment NfcIdentificationFailedFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(errorMsg: String?, loginRequired: Boolean = false) =
            NfcIdentificationWarningFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM, errorMsg)
                }
            }
    }

}