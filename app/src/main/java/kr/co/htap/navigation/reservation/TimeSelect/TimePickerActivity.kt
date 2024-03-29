package kr.co.htap.navigation.reservation.TimeSelect

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import kr.co.htap.databinding.ActivityTimePickerBinding
import kr.co.htap.navigation.reservation.DateDTO
import java.text.SimpleDateFormat
import java.util.Locale

/**
 *
 * @author 김기훈
 *
 */
class TimePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTimePickerBinding
    private lateinit var adapter: TimeListAdapter

    private lateinit var storeName: String
    private lateinit var storeBelong: String
    private lateinit var documentId: String
    private lateinit var date: DateDTO
    private var timeData: ArrayList<TimeDTO> = arrayListOf()

    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTimePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()

        configureData()
        setUI()
    }

    private fun configureData() {
        storeName = intent.getStringExtra("name") ?: ""
        storeBelong = intent.getStringExtra("belong") ?: ""
        documentId = intent.getStringExtra("documentId") ?: ""

        date = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("date", DateDTO::class.java) ?: DateDTO(0, 0, 0)
        } else {
            intent.getSerializableExtra("date") as DateDTO
        }

        val minute = if (date.month < 10) "0" + date.month.toString() else date.month.toString()

        db
            .collection("Reservation")
            .document("record")
            .collection(documentId)
            .document(date.year.toString() + "-" + minute + "-" + date.day.toString())
            .collection("time")
            .get()
            .addOnSuccessListener { documents ->
                val map = mutableMapOf<String, Boolean>(
                    "11:30" to true, "12:00" to true,
                    "12:30" to true, "13:00" to true,
                    "13:30" to true, "14:00" to true,
                    "14:30" to true, "15:00" to true,
                    "15:30" to true, "16:00" to true,
                    "16:30" to true, "17:00" to true,
                    "17:30" to true, "18:00" to true,
                    "18:30" to true, "19:00" to true,
                    "19:30" to true, "20:00" to true
                )

                for (document in documents) {
                    map[document.id] = false
                }

                map.forEach { data ->
                    val t = data.key.split(":")
                    val hour = t[0].toInt()
                    val minute = t[1].toInt()
                    val value = if (isAfterTime(data.key)) data.value else false

                    timeData.add(TimeDTO(hour, minute, value))
                }
                binding.reservationTimeRecyclerView.adapter?.notifyDataSetChanged()
            }
            .addOnFailureListener { exception ->
                Log.d("hhh", "Error getting documents: ", exception)
            }
    }

    private fun setUI() {
        binding.title.text = storeName
        binding.belong.text = storeBelong

        binding.cancelButton.setOnClickListener {
            finish()
        }

        adapter = TimeListAdapter(timeData, documentId, date)
        binding.reservationTimeRecyclerView.adapter = adapter
        binding.reservationTimeRecyclerView.layoutManager = GridLayoutManager(this, 3)
    }

    private fun isAfterTime(time: String): Boolean {
        val currentTime = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val selectedTimeString =
            date.year.toString() + "-" + date.month.toString() + "-" + date.day.toString() + " $time" + ":00"
        val selectedTimeMillis = dateFormat.parse(selectedTimeString)?.time ?: 0

        return currentTime < selectedTimeMillis
    }
}