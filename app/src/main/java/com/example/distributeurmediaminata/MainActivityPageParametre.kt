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
}