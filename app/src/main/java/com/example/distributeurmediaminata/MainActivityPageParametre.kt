package com.example.distributeurmediaminata

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.work.OneTimeWorkRequest
import androidx.work.PeriodicWorkRequest
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
val channelId = "my_channel_id"
private lateinit var binding: ActivityMainPageParametreBinding

class MainActivityPageParametre : AppCompatActivity() {
    val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainPageParametreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisation des notification
        creerCanal()
        demanderPermissionNotification()

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
            // Afficher un message de confirmation pour l'utilisateur
            val timeMessage = "l'objet est fermer dans ${hour}:${minute}."
            Toast.makeText(applicationContext, timeMessage, Toast.LENGTH_SHORT).show()

            Log.d("delayInMillis", "${delayInMillis}" )
            // Lancer la planification du fermeture de l'objet
            planifieTache(delayInMillis)
        }
        /**
         * La méthode périodique pour récevoir des notification
         */
        binding.btnPeriodique.setOnClickListener {
            // Récupérer l'heure et les minutes du TimePicker
            val hour = binding.timePicker.hour
            val minute = binding.timePicker.minute

            // Calculer le délai jusqu'à l'heure choisie pour la première notification
            val delayInMillis = calculateDelayUntil(hour, minute)

            // Afficher un message de confirmation pour l'utilisateur
            val timeMessage = "Notification périodique planifiée pour ${hour}:${minute}."
            Toast.makeText(applicationContext, timeMessage, Toast.LENGTH_SHORT).show()

            // Planifier la notification périodique
            planifierNotificationPeriodique(delayInMillis)
        }

        /**
         * Appeler la méthode envoyerDose et Parametre
         */
        envoyerDoseEtQauntite()
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

    /**
     * Gestion de l'envoi du dose et la quantité du médicament
     */
    private fun envoyerDoseEtQauntite(){
        // Gestion d'un bouton
        val btnParametrer = binding.btnParamePost
        btnParametrer.setOnClickListener {
            // Récupérer les données envoyées dans l'Intent
            val ip = intent.getStringExtra("IP")
            val port = intent.getStringExtra("PORT")
            // Le lien de connection
            Log.d("ServeurConnecter","${ip} ${port}")
            val stUrl = "http://${ip}:${8080}"
            Log.d("SetUrl", stUrl)

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
                Log.d("ServeurConnecter","${reponseServer}")
                // un handler pour m'anipuler le layout.
                handler.post{
                    Toast.makeText(applicationContext,"Réponse du serveur: $reponseServer", Toast.LENGTH_LONG).show()
                }
            }
            // Demarer le thread
            thread.start()
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

        // Récupérer les données envoyées dans l'Intent
        val ip = intent.getStringExtra("IP")
        val port = intent.getStringExtra("PORT")
        // Le lien de connection
        val stUrl = "http://${ip}:${8080}"
        val doseObject = DonneParametrer(quantite = 0, dose = 0) // Exemple de données
        val jsonMsg = Gson().toJson(doseObject)
        // Créer une tâche unique à planifier avec WorkManager
        val workRequest = OneTimeWorkRequest.Builder(PlanifierDesTaches::class.java)
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS)
            .setInputData(workDataOf("url" to stUrl, "jsonMsg" to jsonMsg)) // Passer l'URL et le JSON
            .build()

        // Enqueue le travail
        WorkManager.getInstance(this).enqueue(workRequest)

    }

    /**
     * Planification de la tâche de manière périodique
     */
    private fun planifierNotificationPeriodique(delayInMillis: Long) {
        // Planifier une notification périodique qui commence après `delayInMillis` millisecondes et se répète toutes les 24 heures
        val workRequest = PeriodicWorkRequest.Builder(NotificationWorker::class.java, 24, TimeUnit.HOURS)
            .setInitialDelay(delayInMillis, TimeUnit.MILLISECONDS) // Définir le délai initial basé sur l'heure choisie
            .build()
        // Enqueue le travail périodique
        WorkManager.getInstance(this).enqueue(workRequest)
    }
    /**
    Créer une fonction qui créer la canal de notification
    */
    private fun creerCanal(){
        val channelName = "My Channel"
        val channelDescription = "canal de notification "
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        // Créer le canal de notification pour Android 8.0 et plus
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            // Enregistrer le canal de notification
            val notificationManager: NotificationManager = applicationContext.getSystemService(
                NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
    /**
     * Méthode pour demander la permission d'envoyer des notifiacation
     * nécessaire a partir de l'API 33
     */
    private fun demanderPermissionNotification(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ){
            // Lanceur pour demander la perission pour les notifications
            val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
                if (!result){
                    Toast.makeText(this, "la permission n'a pas été accordée", Toast.LENGTH_SHORT).show()
                }
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PermissionChecker.PERMISSION_GRANTED){
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

}

/**
 * Création d'une classe Data objet pour envoyer les donnés au server.
 */
data class DonneParametrer(val quantite : Int, val dose : Int)
/**
 * Work Manager Planification d'une tache en arrière plan
 */

class PlanifierDesTaches(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val httpRepository = HttpRepository()

    override fun doWork(): Result {
        // Récupérer l'URL et les données JSON envoyées
        val stUrl = inputData.getString("url") ?: return Result.failure()
        val jsonMsg = inputData.getString("jsonMsg") ?: return Result.failure()

        // Appeler le Repository pour envoyer la requête POST
        val response = httpRepository.sendPostRequest(stUrl, jsonMsg)

        return if (response != null) {
            Result.success() // Succès de l'opération
        } else {
            Result.failure() // Échec si la requête échoue
        }
    }
}

/***
 *  Cette classe appelle une notification de rappelle de prise de médicament périodiquement
 *   Ceci est inspiré du code de demo de la classe. cependant j'utilise un bouton pour choisir
 *   l'heure de manière périodique
 */
class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams){
    override fun doWork(): Result {
        Log.i("Notification", "Afficher un log en arrière plan ")
        // Variable pour la notification
        val titre = "Distributeur Médicament"
        val texte = "Il est temps de prendre votre médicament"
        val notID = 1
        afficherNotification(notID,titre,texte)

        // indiquer si le travail c'est exceuter avec succes
        return Result.success()
    }
    // La méthode afficherNotification est maintenant dans la même classe :
    private fun afficherNotification(id: Int, titre : String, texte: String){
        // Préparer la notification, choisir ce qui y sera affiché et son niveau de priorité.
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.star_on) // Icône de la notification
            .setContentTitle(titre) // Titre de la notification
            .setContentText(texte) // Texte de la notification
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Priorité
            // .setAutoCancel(true) // La notification disparaît lorsqu'on clique dessus
            .setDefaults(NotificationCompat.DEFAULT_SOUND) // Son par défaut pour la notification

        // Afficher la notification
        with(NotificationManagerCompat.from(applicationContext)){
            // vérification de la permission à ce moment la
            if(ActivityCompat.checkSelfPermission(applicationContext, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                notify(id, builder.build())
            }
        }
    }


}

