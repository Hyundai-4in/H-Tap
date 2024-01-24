package kr.co.htap.register


import android.content.Intent
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kr.co.htap.databinding.ActivityRegisterBinding

/**
 *
 * @author 송원선
 * 회원 가입
 *
 */
class RegisterActivity: AppCompatActivity(){

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        firebaseDatabase = FirebaseDatabase.getInstance()

        val view = binding.root
        setContentView(view)
        Log.d("Register", "onCreate()")

        // 전화번호 입력시 자동 하이픈(-) 처리
       binding.etPhone.addTextChangedListener(PhoneNumberFormattingTextWatcher())
        // 전화번호 13자리 모두 입력시 키보드 숨김처리
        binding.etPhone.addTextChangedListener(object :TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if(s?.length == 13)
                    hideKeyboard(binding.etPhone)
            }

        })

        // 회원 가입 버튼 리스너 등록
        binding.btnRegister.setOnClickListener {
            register()
        }
    }

    // 회원 가입
    fun register() {
        var validation = true
        val email = binding.etId.text.toString()
        val password = binding.etPassword.text.toString()
        val passwordCheck = binding.etPasswordCheck.text.toString()
        val name = binding.etName.text.toString()
        val phone = binding.etPhone.text.toString()
        val db = Firebase.firestore
        /* 입력 확인 */
        if (email.isEmpty()) {
            Toast.makeText(this, "아이디를 입력해주세요.", Toast.LENGTH_SHORT).show()
            validation = false
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
            validation = false
        }
        if (passwordCheck.isEmpty()) {
            Toast.makeText(this, "비밀번호 확인을 입력해주세요.", Toast.LENGTH_SHORT).show()
            validation = false
        }
        // 비밀번호 & 비밀번호 확인 일치 검사
        if (password != passwordCheck){
            Toast.makeText(this, "비밀번호 확인이 일치하지 않습니다.", Toast.LENGTH_SHORT).show()
            validation = false
        }
        // 이메일 패턴(유효성) 검사
        var pattern = android.util.Patterns.EMAIL_ADDRESS
        if(!pattern.matcher(email).matches()) {
            Toast.makeText(this, "이메일 형식이 올바르지 않습니다", Toast.LENGTH_SHORT).show()
            validation = false
        }
        // 전화번호 유효성 검사
        val phonePattern = """^01([0|1|6|7|8|9])-?([0-9]{4})-?([0-9]{4})$""".toRegex()
        if (!phonePattern.matches(phone)) {
            Toast.makeText(this, "전화번호 형식이 올바르지 않습니다", Toast.LENGTH_SHORT).show()
            validation = false
        }
        // 모든 검사를 통과한 경우 -> 회원 가입 진행
        if (validation == true){
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("Register", "createUserWithEmail:success")

                        // 데이터베이스에 유저 추가
                        val userid = auth.currentUser?.uid.toString()
                        val user = hashMapOf(
                            "userid" to userid,
                            "email" to email,
                            //"password" to password,
                            "name" to name,
                            "phone" to phone
                        )
                        // Firestore 에 회원 추가
                        db.collection("users")
                            .document(email)
                            .set(user)
                            .addOnSuccessListener { documentReference ->
                                Log.d("Register", "Success adding user : email = $email")
                            }
                            .addOnFailureListener{e->
                                Log.w("Register", "Error adding user", e)
                            }
                        // Firebase 사용자 displayName 설정
                        val curUser = auth.currentUser
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                        curUser?.updateProfile(profileUpdates)
                            ?.addOnCompleteListener {task->
                                if(task.isSuccessful){
                                    Log.d("Register", "Display name updated successfully -> displayName : ${curUser.displayName}")
                                }else{
                                    Log.w("Register", "Failed to set Display name")
                                }
                            }
                        // 회원가입 성공 화면으로 이동
                        val intent = Intent(this, RegisterSuccessActivity::class.java)
                        startActivity(intent)
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("Register", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Authentication failed.",
                            Toast.LENGTH_SHORT,
                        ).show()

                    }
                }

        }

    }

    // 키보드 숨기는 함수
    private fun hideKeyboard(view: View) {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }
}