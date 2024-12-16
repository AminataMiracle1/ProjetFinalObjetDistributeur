package com.example.distributeurmediaminata

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.tracing.perfetto.handshake.protocol.Response
import com.example.distributeurmediaminata.databinding.ActivityMainBinding
import com.example.distributeurmediaminata.databinding.ActivityMainPageParametreBinding
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONException
import org.json.JSONObject

private lateinit var binding: ActivityMainBinding
class MainActivity : AppCompatActivity() {

    val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Exemple pour récupérer un widget, pas besoin de faire findViewById
        val btnPageParam = binding.btnPageParam
        // Mettre à jour la page
        updatePageCHap()

        // Aller sur l'autre page :
        btnPageParam.setOnClickListener {
            // Envoyer les l'adresses IP et le port
            val ip = binding.editIPv4.text.toString()
            val port = binding.editPort.text.toString().toInt()
            if (ip.isNotEmpty() ){
                // Créer un objet Intent pour naviguer vers l'autre activité
                val intent = Intent(this, MainActivityPageParametre::class.java)
                // Ajouter des données à l'Intent
                intent.putExtra("IP", ip)
                intent.putExtra("PORT", port)
                // Démarrer l'activité
                startActivity(intent)
            }else{
                // Envoyer un toast qui lui dit de mettre les port et les IP
                Toast.makeText(applicationContext, "remplicer le champ PORT et IP", Toast.LENGTH_SHORT).show()
            }

        }
        // Voir Les médicament
        afficherMedRestant()
    }
    //TODO : Drawable l'arrière plan de l'affichage des médicaments
    /**
     * GetData La méthode qui fait les requettes Get  au serveur pour recevoir les
     * Données du l'objet. el
     */
    private fun getData(stUrl: String): String? {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(stUrl)
            .build()
        return try {
            client.newCall(request).execute().use { response: okhttp3.Response ->
                if (!response.isSuccessful) {
                    Log.e("ERREUR", "Erreur de connexion : ${response.code}")
                    null
                } else {
                    response.body?.string() // Renvoie le corps de la réponse
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ERREUR", e.toString())
            null
        }
    }
    /**
     * La gestion de la méthode get Et du boutonVoirMedicament restant
     */
    private fun afficherMedRestant() {
        val btnVoirMed = binding.btnShowMed
        try {
            btnVoirMed.setOnClickListener {
                val ip = binding.editIPv4.text.toString()
                val port = binding.editPort.text.toString().toInt()
                val stUrl = "http://${ip}:${port}"
                Log.d("SetUrl", stUrl)

                val thread = Thread {
                    val reponseServer = getData(stUrl)
                    if (reponseServer != null) {
                        Log.d("ServeurGet", reponseServer)
                        handler.post {
                            try {
                                val medicament = JSONObject(reponseServer)
                                val nomMed = medicament.getString("nomMed")
                                val restMedicament = medicament.getString("quantite")
                                Log.d("ValeurRecu", "$nomMed, $restMedicament")
                                binding.editShowNomMed.setText(nomMed)
                                binding.editShowQuantRest.setText(restMedicament)
                            } catch (e: Exception) {
                                Log.e("ERREUR", "Erreur lors du traitement de la réponse JSON : ${e.message}")
                            }
                        }
                    } else {
                        Log.e("ERREUR", "La réponse du serveur est vide ou incorrecte")
                    }
                }
                thread.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("ERREUR", e.toString())
        }
    }

    private fun updatePageCHap() {

        // Récupérer le bouton de mise à jour
        val btnMiseJour = binding.btnRafraichire
        btnMiseJour.setOnClickListener {
            // Récupérer la variable du temps (en secondes)
            val tempsUpdate = binding.editafficheMise.text.toString().toIntOrNull() ?: 0
            Log.d("Here", "la commade ic 10 ${tempsUpdate}")
            // Lorsque l'utilisateur clique sur le bouton, attendre le nombre de secondes
            val ip = "10.4.129.43"
            val port = 8080
            val setUrlDef = "http://${ip}:${port}"
            Log.d("adresseDef" , "${setUrlDef}")

            // Récupérer la IP et le Port de l'utilisateur
            val ipWidg = binding.editIPv4.text.toString()
            val portWidg = binding.editPort.text.toString()
            Log.d("adresseDef" , "${ipWidg}:${portWidg}")
            val setUrl = if (ipWidg.isNotEmpty() && portWidg.isNotEmpty()) {
               "http://${ipWidg}:${portWidg}"

            } else {
                setUrlDef
            }
            // Vérifier si le temps est valide
            if (tempsUpdate > 0) {
                Log.d("temps", "le temps ${tempsUpdate}")
                // Utilisation de Handler pour répéter l'action à intervalles réguliers
                val interval = tempsUpdate * 1000L // Convertir les secondes en millisecondes
                Log.d("temps", "le temps 2 ${interval}")
                val runnable = object : Runnable {

                    override fun run() {
                        Log.d("temps", "le temps 3 ${interval}")

                        // Effectuer la mise à jour après le délai
                        val respServer: String = getData(setUrl).toString()

                        // Vérifiez si la réponse du serveur est valide
                        if (respServer.isNullOrEmpty()) {
                            Log.e("UpdatePage", "Réponse vide ou null du serveur")
                            return // Arrêter si la réponse est invalide
                        }

                        try {
                            val medicament = JSONObject(respServer)
                            val medNom = medicament.getString("nomMed")
                            val medRestant = medicament.getString("quantite")
                            Log.d("UpdaPage", "${medNom} ${medRestant}")

                            // Mettre à jour l'affichage avec la réponse du serveur
                            binding.editShowNomMed.setText(medNom)
                            binding.editShowQuantRest.setText(medRestant)

                        } catch (e: JSONException) {
                            Log.e("UpdatePage", "Erreur lors de la conversion de la réponse en JSON: ${e.message}")
                        }

                        // Planifier la prochaine mise à jour
                        handler.postDelayed(this, interval)
                    }
                }
                // Démarrer la première mise à jour
                handler.post(runnable)
            } else {
                // Si le temps de mise à jour est nul ou invalide, effectuer la mise à jour immédiatement
                val respServer: String = getData(setUrl).toString()

                // Vérifiez si la réponse du serveur est valide
                if (respServer.isNullOrEmpty()) {
                    Log.e("UpdatePage", "Réponse vide ou null du serveur")
                    return@setOnClickListener // Arrêter si la réponse est invalide
                }

                try {
                    val medicament = JSONObject(respServer)
                    val medRestant = medicament.getString("quantite")
                    // Mettre à jour l'affichage avec la réponse du serveur (par exemple, afficher la réponse dans une TextView)
                    binding.editShowQuantRest.setText(medRestant)
                } catch (e: JSONException) {
                    Log.e("UpdatePage", "Erreur lors de la conversion de la réponse en JSON: ${e.message}")
                }
            }

        }
    }



}