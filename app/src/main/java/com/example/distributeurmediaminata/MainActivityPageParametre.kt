package com.example.distributeurmediaminata

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.distributeurmediaminata.databinding.ActivityMainPageParametreBinding
import com.google.gson.Gson
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.concurrent.TimeUnit

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
        /**
         * Récupération de l'heure choisie par l'utilisateur pour planifier
         * la fermeture de l'objet
         */
        binding.btnVerrouiller.setOnClickListener {
            val lheure = binding.timePicker
            Log.d("heure", "${lheure}")
            val hour = lheure.hour
            val minute = lheure.minute
            Log.d("heure", "${hour} ${minute} " )
            // Calculer le délai jusqu'à cette heure
            val delayInMillis = calculateDelayUntil(hour, minute)
            Log.d("delayInMillis", "${delayInMillis}" )
        }
    }

    /**
     * Récupere le délai du temps entre l'action planifier et la date choisie
     *
     */
    private fun calculateDelayUntil(hour: Int, minute: Int): Long {
        val currentTime = Calendar.getInstance()
        val targetTime = Calendar.getInstance()
        // Définir l'heure cible
        targetTime.set(Calendar.HOUR_OF_DAY, hour)
        targetTime.set(Calendar.MINUTE, minute)
        targetTime.set(Calendar.SECOND, 0)
        // Si l'heure cible est déjà passée aujourd'hui, on la planifie pour demain
        if (targetTime.before(currentTime)) {
            targetTime.add(Calendar.DAY_OF_YEAR, 1)
        }
        val differenceTime = targetTime.timeInMillis - currentTime.timeInMillis
        Log.d("HDiff", "${differenceTime} ")
        // Calculer la différence en millisecondes
        return differenceTime
    }

    /**
     * Planification de la tâche se fait une seule fois alors nous utilisons once workManager
     */
    private fun planifieTache(delayInMillis: Long) {
        val monWorkRequest = OneTimeWorkRequest.Builder(PlanifierDesTaches::class.java)
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)

            // Exemple d'action : que le workmanager  quand l'heure planifier arrive  j'envoie à
            // l'objet  une dose zero pour éteindre tout les del et fermer la poste
            .build()
        //planifier la tache avec WorkManager pour une seule fois
        WorkManager.getInstance(this).enqueue(monWorkRequest)
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

    companion object {
        val handler: Any
    }

}
//Création d'un objet pour envoyer les donnés au server.
data class DonneParametrer(val quantite : Int, val dose : Int)

// Créer une classe pour envoyer les parametre de vérouillage
data class HeureVerrouiller(val heure: Int)

/**
 * Work Manager Planification d'une tache en arrière plan
 */

// Création de la classe workmanager pour planifier les tâches
class PlanifierDesTaches(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Récupérer la dose envoyée dans les données d'entrée
        val dose = inputData.getInt("dose", -1) // -1 est la valeur par défaut si la donnée est absente


        if (dose == 0) {
            // Exemple d'action à exécuter lorsque la dose est zéro
            // Par exemple, fermer la porte et éteindre les DELs

            // Créez un objet Dose si nécessaire
            val quantite = 0 // Par exemple, une quantité nulle pour cette action
            val doseObject = DonneParametrer(quantite, dose)

            // Convertir l'objet Dose en JSON
            val jsonMsg = Gson().toJson(doseObject)
            Log.d("FichierJson", jsonMsg)

            // Créer un thread pour effectuer une requête POST
            val thread = Thread {
                // Exemple de requête HTTP POST, adaptez selon votre méthode
                val reponseServer = requettePost("votre_url", jsonMsg)

                // Utiliser un Handler pour manipuler l'UI sur le thread principal
                MainActivityPageParametre.handler.post {
                    Toast.makeText(applicationContext, "Réponse du serveur: $reponseServer", Toast.LENGTH_LONG).show()
                }
            }
            thread.start()
        }

        return Result.success() // Marquer le travail comme réussi
    }
}

