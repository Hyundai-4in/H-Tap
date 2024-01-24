package kr.co.htap.navigation.reservation.DateSelect

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kr.co.htap.databinding.ActivityDatePickerBinding
import kr.co.htap.navigation.reservation.TimeSelect.TimePickerActivity
import java.util.Calendar

/**
 *
 * @author 김기훈
 *
 */

class DatePickerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDatePickerBinding

    private lateinit var calendar: Calendar
    private lateinit var date: DateDTO
    private lateinit var name: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDatePickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configureData()
        setUI()
    }

    private fun configureData() {
        calendar = Calendar.getInstance()
        date = DateDTO(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH))
        name = intent.getStringExtra("name") ?: ""
    }

    private fun setUI() {
        binding.title.text = name

        binding.calendarView.minDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, 10)
        binding.calendarView.maxDate = calendar.timeInMillis

        binding.calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
            date = DateDTO(year, month + 1, dayOfMonth)
        }

        binding.cancelButton.setOnClickListener {
            finish()
        }

        binding.confirmButton.setOnClickListener {
            var intent = Intent(this, TimePickerActivity::class.java)
            intent.putExtra("name", name)
            intent.putExtra("date", date)

            startActivity(intent)
        }
    }
}