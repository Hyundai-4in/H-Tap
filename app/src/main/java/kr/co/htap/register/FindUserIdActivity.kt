package kr.co.htap.register

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kr.co.htap.databinding.ActivityFindUserIdBinding
import kr.co.htap.navigation.NavigationActivity
/**
 *
 * @author 송원선
 * 아이디 찾기
 *
 */
class FindUserIdActivity : AppCompatActivity() {
    private lateinit var binding:ActivityFindUserIdBinding
    private lateinit var findBtn:Button
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFindUserIdBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        var view = binding.root
        setContentView(view)
        findBtn = binding.btnFindId

        findBtn.setOnClickListener {
            findUser()
        }

    }

    private fun findUser(){
        val name = binding.etName.text.toString()
        val phone = binding.etPhone.text.toString()

        if(name.isEmpty()){
            Toast.makeText(this, "이름을 입력해주세요", Toast.LENGTH_SHORT).show()
        }
        if(phone.isEmpty()){
            Toast.makeText(this, "전화번호를 입력해주세요", Toast.LENGTH_SHORT).show()
        }

        if(phone.isNotEmpty() && name.isNotEmpty()){
            val query: Query = firestore.collection("users")
                .whereEqualTo("name", name)
                .whereEqualTo("phone", phone)
            query.get()
                .addOnSuccessListener { documents->
                    if(!documents.isEmpty) {
                        // 사용자가 존재하는 경우
                        for(document in documents){
                            val userData = document.data
                            val userEmail = userData["email"].toString()
                            binding.tvResult.text = "사용자 이메일 : $userEmail"

                            // invsibile 버튼 사용자에게 보여주기 (홈으로, 로그인)
                            binding.btnHiddenHome.visibility = View.VISIBLE
                            binding.btnHiddenLogin.visibility = View.VISIBLE
                            binding.btnHiddenHome.setOnClickListener {
                                val intent = Intent(this, NavigationActivity::class.java)
                                startActivity(intent)
                            }
                            binding.btnHiddenLogin.setOnClickListener {
                                val intent = Intent(this, LoginActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }else{ // 사용자가 존재하지 않는 경우
                        Toast.makeText(this, "해당하는 사용자 정보가 없습니다.", Toast.LENGTH_LONG).show()
                        // TextView 초기화
                        binding.tvResult.text = ""
                    }
                }
                .addOnFailureListener {exception ->
                    // 조회 중 에러 발생
                    Toast.makeText(this, "사용자 조회 중 에러가 발생했습니다.", Toast.LENGTH_SHORT).show()
                    Log.e("FindUserIdActivity", "Error getting documents: ", exception)

                    // TextView 초기화
                    binding.tvResult.text = ""
                }

        }


    }
}