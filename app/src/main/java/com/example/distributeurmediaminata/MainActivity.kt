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

        // Aller sur l'autre page :
        btnPageParam.setOnClickListener {
            // Envoyer les l'adresses IP et le port
            val ip = binding.editIPv4.text.toString()
            val port = binding.editPort.text.toString()
            if (ip.isNotEmpty() && port.isNotEmpty()){
                // Créer un objet Intent pour naviguer vers l'autre activité
                val intent = Intent(this, MainActivityPageParametre::class.java)
                // Ajouter des données à l'Intent
                intent.putExtra("IP", ip)
                intent.putExtra("PORT", port.toInt())
                // Démarrer l'activité
                startActivity(intent)
            }else{
                // Envoyer un toast qui lui dit de mettre les port et les IP
                Toast.makeText(applicationContext, "remplicer le champ PORT et IP", Toast.LENGTH_SHORT).show()
            }

        }
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
            client.newCall(request).execute().use { response : okhttp3.Response ->
                if(!response.isSuccessful){
                    Log.e("ERREUR", "Erreur de connexion : ${response.code}")
                    null
                }else{
                    response.body?.string() // Renvoie le corps de la reponse en tant que chaine
                }
            }
        }catch (e:Exception) {
            e.printStackTrace()
            Log.e("ERREUR", e.toString())
            null
        }
    }
    /**
     * La gestion de la méthode get Et du boutonVoirMedicament restant
     */
    private fun afficherMedRestant(){
        // Récuperer le widget du bouton
        val btnVoirMed = binding.btnShowMed
        try {
            btnVoirMed.setOnClickListener {
                // Récupere la IP et le Port
                val ip = binding.editIPv4.text.toString()
                val port = binding.editPort.text
                // Créer le lien de connexion
                val stUrl = "http://${ip}:${port}"
                Log.d("SetUrl", stUrl)

                // Créer un Thread pour faire les requette
                val thread = Thread {
                    // Appeller la fonction GetData pour recevoir le contenu recupere par la requette
                    val reponseServer = getData(stUrl)
                    // afficher la réponse de la requette dans un log
                    Log.d("ServeurGet", reponseServer.toString())
                    handler.post{
                        // Convertir le fichier recu en json
                        val medicament = JSONObject(reponseServer)
                        val nomMed = medicament.getString("nom")
                        val restMedicament = medicament.getString("qauntiteRest")
                        Log.d("ValeurRecu" , "${nomMed}, ${restMedicament}")
                        // Afficher les valeur recut au widget
                        binding.editShowNomMed.setText(nomMed.toString())
                        binding.editShowQuantRest.setText(restMedicament.toString())
                    }
                }
                //Lancer le thread
                thread.start()
            }
        }catch (e:Exception) {
            e.printStackTrace()
            Log.e("ERREUR", e.toString())
        }
    }

    /**
     * Mettre à jour la page avec un variable de temps voulu.
     *
     */
    private fun updatePage(){
        // Récuperer la variable du temps
        val tempsUpdate = binding.editafficheMise.text.toString().toInt()
        //Récupérer le bouton mise à jour
        val btnMiseJour = binding.btnRafraichire
        btnMiseJour.setOnClickListener {
            // Quand la personne  clique sur le bouton nous attendons le nombre de second pour faire la
            // parDefault la connexion c'est ceci mais
            val ip = "10.4.129.42"
            val port = 8080
            val setUrlDef = "http://${ip}:${port}"

            // Récupere la IP et le Port
            val ipWidg = binding.editIPv4.text.toString()
            val portWidg = binding.editPort.text
            val setUrl = "http://${ipWidg}:${portWidg}"
            val respServer : String
            if(ipWidg === ""){
                respServer = getData(setUrlDef).toString()
            }else{
                respServer = getData(setUrl).toString()
            }
        }
    }

    private fun updatePageCHap() {
        // Récupérer la variable du temps (en secondes)
        val tempsUpdate = binding.editafficheMise.text.toString().toIntOrNull() ?: 0

        // Récupérer le bouton de mise à jour
        val btnMiseJour = binding.btnRafraichire
        btnMiseJour.setOnClickListener {
            // Lorsque l'utilisateur clique sur le bouton, attendre le nombre de secondes
            val ip = "10.4.129.42"
            val port = 8080
            val setUrlDef = "http://${ip}:${port}"

            // Récupérer la IP et le Port de l'utilisateur
            val ipWidg = binding.editIPv4.text.toString()
            val portWidg = binding.editPort.text.toString()
            val setUrl = if (ipWidg.isNotEmpty() && portWidg.isNotEmpty()) {
                "http://${ipWidg}:${portWidg}"
            } else {
                setUrlDef
            }
            // Vérifier si le temps est valide
            if (tempsUpdate > 0) {
                // Utilisation de Handler pour répéter l'action à intervalles réguliers
                val interval = tempsUpdate * 1000L // Convertir les secondes en millisecondes
                val runnable = object : Runnable {
                    override fun run() {
                        // Effectuer la mise à jour après le délai
                        val respServer: String = getData(setUrl).toString()
                        val medicament = JSONObject(respServer)
                        val medRestant = medicament.getString("quantite")


                        // Mettre à jour l'affichage avec la réponse du serveur
                        binding.editShowQuantRest.setText(medRestant)

                        // Planifier la prochaine mise à jour
                        handler.postDelayed(this, interval)
                    }
                }
                // Démarrer la première mise à jour
                handler.post(runnable)
            } else {
                // Si le temps de mise à jour est nul ou invalide, effectuer la mise à jour immédiatement
                val respServer: String = getData(setUrl).toString()
                val medicament = JSONObject(respServer)
                val medRestant = medicament.getString("quantite")
                // Mettre à jour l'affichage avec la réponse du serveur (par exemple, afficher la réponse dans une TextView)
                binding.editShowQuantRest.setText(medRestant)
            }

        }
    }


}