package kr.co.htap.nfc.view

import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Intent
import android.nfc.NfcAdapter
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kr.co.htap.databinding.ActivityNfcEventBinding
import kr.co.htap.helper.ViewBindingActivity
import kr.co.htap.helper.isNotLoggedIn
import kr.co.htap.nfc.util.NFCUtil
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

/**
 * 읽은 NFC 유형이 인증(/identification])인 경우 동작하는 액티비티
 * @author 이호연
 */
class NfcIdentificationActivity : ViewBindingActivity<ActivityNfcEventBinding>() {

    private var nfcAdapter: NfcAdapter? = null
    private var pendingIntent: PendingIntent? = null
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    override fun initViewBinding(): ActivityNfcEventBinding =
        ActivityNfcEventBinding.inflate(layoutInflater)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        firebaseAuth = Firebase.auth
        firestore = Firebase.firestore

        if (firebaseAuth.isNotLoggedIn()) {
            val errorMsg = "파이어베이스 로그인이 필요합니다."
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
            onSignInRequired(errorMsg)
            return
        }

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)

        if (nfcAdapter == null) {
            Toast.makeText(this, "이 기기는 NFC를 지원하지 않습니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        supportFragmentManager.beginTransaction().apply {
            replace(binding.nfcFragment.id, NfcIdentificationFragment())
            commit()
        }

        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            parseNFC(intent)
        }
        pendingIntent = PendingIntent.getActivity(this, 0, intent, FLAG_IMMUTABLE)
    }


    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableForegroundDispatch(this, pendingIntent, null, null)
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableForegroundDispatch(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            NFCUtil.parseNFCContent(intent) { }
        }
    }

    private fun parseNFC(intent: Intent) {
        NFCUtil.parseNFCContent(intent) { eventSource ->
            Log.w(this@NfcIdentificationActivity.toString(), eventSource.toString())
            val key = "storeId"
            if (!eventSource.addtionalData.containsKey(key)) return@parseNFCContent

            findReservation(eventSource.addtionalData[key]!!)
        }
    }

    private fun getReservationCollectionPath(documentId: String, dateString: String): String {
        return String.format("/Reservation/record/${documentId}/${dateString}/time")
    }

    private fun findReservation(documentId: String) {
        val defaultTimezone = "Asia/Seoul"
        val timeZone = TimeZone.getTimeZone(defaultTimezone)
        val calendar = Calendar.getInstance(timeZone)

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)
        val time = SimpleDateFormat("HH:mm", Locale.getDefault()).format(calendar.time)

        val absoluteCollectionPath = getReservationCollectionPath(documentId, today)

        // 해당 가계의 오늘 예약을 읽어 옵니다.
        val className = this@NfcIdentificationActivity.toString()
        firestore.collection(absoluteCollectionPath).get().addOnSuccessListener {
            if (it.isEmpty) { // 해당 가계의 예약목록이 존재하지 않는 경우
                Log.e(className, "failed block case : 0")
                onFailure("예약을 찾을 수 없습니다.")
            } else { // 해당 가계의 예약목록이 존재하는 경우
                val userId = firebaseAuth.currentUser?.uid ?: ""
                Log.d(
                    className, "found collection : collectionPath $absoluteCollectionPath"
                )
                if (userId.isEmpty()) {
                    Log.e(className, "failed block case : 1 ")
                    onFailure("예약을 찾을 수 없습니다.")
                    return@addOnSuccessListener
                } else {
                    var result: TimeCompareResult? = null
                    val documents = it.documents
                    for (document in documents) {
                        if (document.getString("userid") == userId) {
                            val reservationTime = document.id
                            Log.d(className, reservationTime)
                            result = compareTime(time, reservationTime)
                            if (result == TimeCompareResult.AVAILABLE) {
                                val identificationFragment =
                                    NfcIdentificationSuccessFragment.newInstance()
                                supportFragmentManager.beginTransaction().apply {
                                    replace(binding.nfcFragment.id, identificationFragment)
                                    commit()
                                }
                                return@addOnSuccessListener
                            }
                        }
                    }
                    if (result == null) {
                        onFailure("예약을 찾을 수 없습니다.")
                    } else {
                        onWarning(result.resultMessage)
                    }
                }
            }

        }.addOnFailureListener {
            onFailure("예약을 찾을 수 없습니다.")
        }
    }


    private fun compareTime(time: String, reservationTime: String): TimeCompareResult {
        fun timeToInt(time: String): Int {
            with(time.split(":")) {
                val hour = this[0].toInt()
                val minute = this[1].toInt()
                return (hour * 60) + minute
            }
        }

        val currentTime = timeToInt(time)
        val reserve = timeToInt(reservationTime)

        if (Math.abs(currentTime - reserve) <= 30) {
            return TimeCompareResult.AVAILABLE
        }
        return if (currentTime < reserve) TimeCompareResult.TOO_EARLY else TimeCompareResult.TOO_LATE
    }

    /**
     * 시나리오 1: NFC를 태깅했지만 사용자가 로그인되어 있지 않은 경우
     * TODO: @원선님이 작성한 로그인페이지로 리다이렉션
     */
    private fun onSignInRequired(errorMsg: String) {
        val nfcIdentificationFailedFragment =
            NfcIdentificationFailedFragment.newInstance(errorMsg, true)
        supportFragmentManager.beginTransaction().apply {
            replace(binding.nfcFragment.id, nfcIdentificationFailedFragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun onWarning(warningMsg: String) {
        val nfcIdentificationFailedFragment =
            NfcIdentificationWarningFragment.newInstance(warningMsg)
        supportFragmentManager.beginTransaction().apply {
            replace(binding.nfcFragment.id, nfcIdentificationFailedFragment)
            addToBackStack(null)
            commit()
        }
    }

    private fun onFailure(errorMsg: String) {
        val nfcIdentificationFailedFragment = NfcIdentificationFailedFragment.newInstance(errorMsg)
        supportFragmentManager.beginTransaction().apply {
            replace(binding.nfcFragment.id, nfcIdentificationFailedFragment)
            addToBackStack(null)
            commit()
        }
    }

    private enum class TimeCompareResult(val resultMessage: String) {
        AVAILABLE(""), TOO_LATE("예약시간 30분을 초과했습니다."), TOO_EARLY("예약까지 30분 이상남았습니다.")
    }
}

