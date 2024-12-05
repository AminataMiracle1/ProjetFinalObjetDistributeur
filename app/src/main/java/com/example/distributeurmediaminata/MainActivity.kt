package com.example.distributeurmediaminata

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.distributeurmediaminata.databinding.ActivityMainBinding
import com.example.distributeurmediaminata.databinding.ActivityMainPageParametreBinding

private lateinit var binding: ActivityMainBinding
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Exemple pour récupérer un widget, pas besoin de faire findViewById
        val btnPageParam = binding.btnPageParam

        // Aller sur l'autre page :
        btnPageParam.setOnClickListener {
            // Créer un objet Intent pour naviguer vers l'autre activité
            val intent = Intent(this, MainActivityPageParametre::class.java)
            startActivity(intent)
        }

    }
}