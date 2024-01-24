package kr.co.htap.register

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kr.co.htap.R
import kr.co.htap.databinding.ActivityLoginBinding
import kr.co.htap.navigation.NavigationActivity
import kr.co.htap.helper.isNotLoggedIn
import kr.co.htap.onboarding.OnboardingActivity


/**
 *
 * @author 송원선
 * 로그인
 *
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivityLoginBinding
    private lateinit var registserView: TextView
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firestore:FirebaseFirestore
    private var GOOGLE_LOGIN_CODE = 9001

    private lateinit var googleLoginLauncher: ActivityResultLauncher<Intent>
    private lateinit var navigationIntent: Intent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        val view = binding.root
        val loginButton: Button = binding.btnLogin
        val googleLoginButton = binding.ivGoogleLogin
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        setContentView(view)
        navigationIntent = Intent(this, NavigationActivity::class.java)
        navigationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        // [회원가입] 클릭 -> 회원가입 액티비티로 이동
        registserView = binding.tvRegister
        registserView.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        // 로그인 버튼 클릭 (일반로그인)
        loginButton.setOnClickListener {
            customLogin()
        }

        // [아이디 찾기] 클릭
        binding.tvFindId.setOnClickListener {
            val intent = Intent(this, FindUserIdActivity::class.java)
            startActivity(intent)
        }
        // [비밀번호 찾기] 클릭
        binding.tvFindPassword.setOnClickListener {
            val intent = Intent(this, FindUserPasswordActivity::class.java)
            startActivity(intent)
        }
        googleLoginLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == -1) {
                    Log.d("GoogleLogin", result.data.toString())
                    val data = result.data
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    getGoogleInfo(task)
                }
            }

        // 구글 로그인
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        // 구글 로그인 버튼 클릭
        googleLoginButton.setOnClickListener {
            googleLogin()
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)

        if (isFirstRun()) {
            startActivity(Intent(this, OnboardingActivity::class.java))
            return
        }

        if (Firebase.auth.isNotLoggedIn() == false) {
            startActivity(Intent(this, NavigationActivity::class.java))
        }
    }

    // 김기훈
    private fun isFirstRun(): Boolean {
        val pref = getSharedPreferences("hTap", 0)
        return pref.getBoolean("isFirstRun", true)
    }

    // 일반로그인
    private fun customLogin() {
        val email: String = binding.etId.text.toString().trim()
        val password: String = binding.etPassword.text.toString().trim()
        if (email == null)
            Toast.makeText(this@LoginActivity, "이메일을 입력해주세요", Toast.LENGTH_SHORT).show()
        else if (password == null)
            Toast.makeText(this@LoginActivity, "비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show()
        else {
            Log.d("Login", "Login Try -> email : $email")
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this@LoginActivity, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) { // 로그인 성공
                        Log.d("Login", "Logined success: $email")
                        onSignInSuccess()
                    } else { //로그인 실패
                        Log.w("Login", "Login failed : ", task.exception)
                        Toast.makeText(
                            this@LoginActivity,
                            "로그인에 실패했습니다. 이메일과 비밀번호를 확인해주세요",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                })
        }
    }

    /**
     * 로그인에 성공한 경우 원래 액티비티로 이동하는 콜백
     * @author hoyeon
     */
    private fun onSignInSuccess() {
        // wonson's code
        val intent = Intent(this@LoginActivity, NavigationActivity::class.java)
        setResult(Activity.RESULT_OK)
        startActivity(intent)
    }

    /**
     * 로그인을 취소한 경우 원래 액티비티로 이동하는 콜백
     * @author hoyeon
     */
    private fun onSignInCanceled() {
        setResult(Activity.RESULT_CANCELED)
        Toast.makeText(this, "로그인에 실패했습니다.", Toast.LENGTH_SHORT).show()
    }

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            onSignInCanceled()
        }
    }

    /**
     * 구글 로그인
     * @author 송원선
     */
    private fun googleLogin() {
        Log.d("GoogleLogin", "googleLogin()")
        val signInIntent = googleSignInClient!!.signInIntent
//        googleLoginLauncher.launch(signInIntent)
        startActivityForResult(signInIntent, RC_SIGN_IN)
        // -> onActivityResult 콜백 호출
    }

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String, data:Intent?) {
        Log.d("GoogleLogin", "firebasAuthWithGoogle(idToken : $idToken ) ")
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        var user = auth.currentUser
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = task.getResult(ApiException::class.java)!!
        Log.d("GoogleLogin", "firebaseAuthWithGoogle() | account email : ${account.email}")
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                Log.d("GoogleLogin", "signInWithCredentail | task : $task")
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("GoogleLogin", "signInWithCredential:success")
                    checkIfUserExistsInFirestore(account)
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("GoogleLogin", "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
    // [END auth_with_google]
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("GoogleLogin", "onActivityResult() 호출 | requestCode : $requestCode, resultCode : $resultCode, data : $data")
        auth = FirebaseAuth.getInstance()
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                Log.d("GoogleLogin", "onActivityResult() | auth.currentuser = ${auth.currentUser}")
                val account = task.getResult(ApiException::class.java)!!
                Log.d("GoogleLogin", "onActivityResult() | account = $account")
                Log.d("GoogleLogin", "onActivityResult() | account.id :" + account.id)
                firebaseAuthWithGoogle(account.idToken!!, data)

                //startActivity(navigationIntent)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w("GoogleLogin", "Google sign in failed", e)
            }
        }
    }
    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            val displayName = user.displayName
            val email = user.email
            val uid = user.uid

            Log.d("GoogleLogin", "UpdateUI - Display Name: $displayName")
            Log.d("GoogleLogin", "UpdateUI - Email: $email")
            Log.d("GoogleLogin", "UpdateUI - UID: $uid")

            onSignInSuccess()
        }
    }
    fun getGoogleInfo(completedTask: Task<GoogleSignInAccount>) {
        try {
            val TAG = "구글 로그인 결과"
            val account = completedTask.getResult(ApiException::class.java)
            Log.d(TAG, account.id!!)
            Log.d(TAG, account.familyName!!)
            Log.d(TAG, account.givenName!!)
            Log.d(TAG, account.email!!)
        } catch (e: ApiException) {
            Log.w("GoogleLogin", "signInResult:failed code=" + e.statusCode)
        }
    }

    // 구글 로그인한 사용자가 FireStore 에 등록 되어있는지 체크 메소드
    private fun checkIfUserExistsInFirestore(account: GoogleSignInAccount) {
        Log.d("GoogleLogin", "checkIfUserExistsInFirestore($account)")
        if (account != null) {
            val userId = account.id
            val usersCollection = firestore.collection("users")
            val userEamil = account.email
            // Check if the user already exists in Firestore
            usersCollection.whereEqualTo("email", userEamil).get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 조회된 문서의 개수를 확인
                        val documentCount = task.result?.size() ?: 0

                        if (documentCount > 0) {
                            // 이미 등록된 사용자인 경우
                            Log.d("GoogleLogin", "checkIfUserExistsInFirestore: 이미 등록된 사용자")
                        } else {
                            // 등록되지 않은 사용자인 경우
                            addUserToFirestore(account)
                            Log.d("GoogleLogin", "checkIfUserExistsInFirestore: 사용자 등록 완료")
                        }
                    } else {
                        // task 실패
                        Log.e("GoogleLogin", "Error checking user in Firestore", task.exception)
                    }
                }
                .addOnFailureListener { e->
                    Log.w("GoogleLogin", "Failed to check existence of user : $e")
                }
        }
    }
    // 구글 로그인 유저 FireStore 에 추가
    private fun addUserToFirestore(account: GoogleSignInAccount) {
        val userId = account.id
        val email = account.email
        val name = account.displayName

        // Create user data
        val userData = hashMapOf(
            "userid" to userId,
            "email" to email,
            "name" to name,
        )

        // Add user data to Firestore
        firestore.collection("users").document(email!!)
            .set(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("GoogleLogin", "User added to Firestore successfully")
                    startActivity(navigationIntent)
                } else {
                    Log.e("GoogleLogin", "Error adding user to Firestore", task.exception)
                    updateUI(null)
                }
            }
    }
companion object {
    private const val RC_SIGN_IN = 9001
}
}