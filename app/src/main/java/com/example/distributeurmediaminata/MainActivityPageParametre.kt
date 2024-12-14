package com.example.distributeurmediaminata

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.distributeurmediaminata.databinding.ActivityMainPageParametreBinding
import com.google.gson.Gson
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

private lateinit var binding: ActivityMainPageParametreBinding

class MainActivityPageParametre : AppCompatActivity() {
    val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPageParametreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRetourMain.setOnClickListener {
            // Créer un intent
            val intent= Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }


    /**
     * Méthode pour envoyer une commande json au server . à l'aide d'une requete POST
     * @param stUrl Address Url du serveur ou de notre objet connecté
     * @param jsonMsg Le message Json qui sera envoyé à l'objet connecté Password1
     */
    private fun requettePost(stUrl: String, jsonMsg: String): String? {
        try {
            // Établir la connexion à l'URL et envoyer la commande JSON dans une requête POST
            val url = URL(stUrl)
            val connexion = url.openConnection() as HttpURLConnection
            connexion.requestMethod = "POST"
            connexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connexion.setRequestProperty("Accept", "application/json")
            connexion.doOutput = true

            // Envoyer la requête JSON dans le corps de la requête
            DataOutputStream(connexion.outputStream).use { os ->
                os.writeBytes(jsonMsg)
                os.flush()
            }
            // Obtenir le code de réponse du serveur
            val responseCode = connexion.responseCode
            val responseMessage = connexion.responseMessage

            // Log des informations de réponse
            Log.d("Status", responseCode.toString())
            Log.d("MSG", responseMessage)

            // Lire le flux de réponse du serveur
            val inputStream = if (responseCode in 200..299) {
                // Si le code de réponse est dans la plage 2xx (succès), utiliser l'InputStream normal
                connexion.inputStream
            } else {
                // Sinon, utiliser l'InputStream d'erreur
                connexion.errorStream
            }
            // Lire la réponse et la convertir en chaîne de caractères
            val responseBody = inputStream.bufferedReader().use { it.readText() }
            // Afficher le corps de la réponse dans les logs
            Log.d("Response du serveur", responseBody)

            // TODO : montrer la reponse du serveur par un toast : but rendre l'application
            // Déconnecter la connexion
            connexion.disconnect()

            // Retourner la réponse du serveur
            return responseBody

        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("Erreur", e.message ?: "Erreur inconnue")
            return null
        }
    }
    private fun paramDoseEtQauntite(){
        // Gestion d'un bouton
        val btnParametrer = binding.btnParamePost
        btnParametrer.setOnClickListener {
            // Récupérer les données envoyées dans l'Intent
            val ip = intent.getStringExtra("IP")
            val port = intent.getStringExtra("PORT")
            // Le lien de connection
            val stUrl = "http://${ip}:${port}"

            // Recupere le contenus des champs
            val qauntite = binding.editQuantiteMed.text.toString().toInt()
            val dose = binding.editDoseMed.text.toString().toInt()
            //Créer un fichier Json a envoyer : Note on doit creer un data class pour faire un json
            val objetQuantDose = DonneParametrer (qauntite, dose)
            // Utiliser Gson pour convertir cet objet en chaîne JSON
            val jsonMsg = Gson().toJson(objetQuantDose)
            Log.d("FichierJson", jsonMsg)
            // Créer un nouveau thread pour exceuter la requeste POSt
            val thread = Thread{
                val reponseServer = requettePost("${stUrl}" , "${jsonMsg}")
                // un handler pour m'anipuler le layout.
                handler.post{
                    Toast.makeText(applicationContext,"Réponse du serveur: $reponseServer", Toast.LENGTH_LONG).show()
                }
            }
            // Demarer le thread
            thread.start()
        }
    }

    // Envoyer l'heure du vérouillage de l'objet :
    private fun heureVouillage(){
        // Récuper le bouton
        val btnFermer = binding.btnVerrouiller

    }

    // Gestion d'événemen pour le bouton retourner en arrière
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
//Création d'un objet pour envoyer les donnés au server.
data class DonneParametrer(val quantite : Int, val dose : Int)

// Créer une classe pour envoyer les parametre de vérouillage
data class HeureVerrouiller(val heure: Int)
