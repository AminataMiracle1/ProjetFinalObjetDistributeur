package com.example.distributeurmediaminata

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.distributeurmediaminata.databinding.ActivityMainBinding
import com.example.distributeurmediaminata.databinding.ActivityMainPageParametreBinding

private lateinit var binding: ActivityMainPageParametreBinding

class MainActivityPageParametre : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPageParametreBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }
    /**
     * EditText dateTimeEditText = findViewById(R.id.dateTimeEditText);
     *
     * dateTimeEditText.setOnClickListener(v -> {
     *     Calendar calendar = Calendar.getInstance();
     *     int year = calendar.get(Calendar.YEAR);
     *     int month = calendar.get(Calendar.MONTH);
     *     int day = calendar.get(Calendar.DAY_OF_MONTH);
     *
     *     DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, year1, month1, dayOfMonth) -> {
     *         calendar.set(year1, month1, dayOfMonth);
     *
     *         // Show time picker after selecting date
     *         new TimePickerDialog(this, (timePicker, hourOfDay, minute) -> {
     *             calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
     *             calendar.set(Calendar.MINUTE, minute);
     *             dateTimeEditText.setText(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(calendar.getTime()));
     *         }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
     *     }, year, month, day);
     *
     *     datePickerDialog.show();
     * });

     */
}