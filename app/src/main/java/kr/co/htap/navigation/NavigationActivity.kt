package kr.co.htap.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kr.co.htap.R
import kr.co.htap.databinding.ActivityNavigationBinding
import kr.co.htap.helper.isNotLoggedIn
import kr.co.htap.helper.requestLogin
import kr.co.htap.navigation.reservation.ReservationFragment

/**
 *
 * @author 김기훈
 *
 */

class NavigationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigationBinding
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = Firebase.auth
        firebaseAuth.signOut()
        if (firebaseAuth.isNotLoggedIn()) {
            requestLogin({ result ->
                Toast.makeText(this, "로그인에 성공했습니다.", Toast.LENGTH_SHORT).show()
            }, { result ->
                Toast.makeText(this, "로그인이 취소되었습니다.", Toast.LENGTH_SHORT).show()
            })
        }
        binding.navigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.mainFragment -> setFragment("home", MainFragment())
                R.id.reservationFragment -> setFragment("reservation", ReservationFragment())
                R.id.myPageFragment -> setFragment("myPage", MyPageFragment())

            }
            true
        }
        // 온보딩 액티비티 시작
        startActivity(Intent(this, OnboardingActivity::class.java))
    }


    private fun setFragment(tag: String, fragment: Fragment) {
        val manager: FragmentManager = supportFragmentManager
        val fragTransaction = manager.beginTransaction()

        if (manager.findFragmentByTag(tag) == null) {
            fragTransaction.add(binding.mainFrameLayout.id, fragment, tag)
        }

        val home = manager.findFragmentByTag("home")
        val reservation = manager.findFragmentByTag("reservation")
        val myPage = manager.findFragmentByTag("myPage")

        if (home != null) {
            fragTransaction.hide(home)
        }

        if (reservation != null) {
            fragTransaction.hide(reservation)
        }

        if (myPage != null) {
            fragTransaction.hide(myPage)
        }

        if (tag == "home") {
            if (home != null) {
                fragTransaction.show(home)
            }
        } else if (tag == "reservation") {
            if (reservation != null) {
                fragTransaction.show(reservation)
            }
        } else if (tag == "myPage") {
            if (myPage != null) {
                fragTransaction.show(myPage)
            }
        }
        fragTransaction.commitAllowingStateLoss()
    }
}