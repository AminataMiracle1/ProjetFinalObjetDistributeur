package com.example.distributeurmediaminata
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class HttpRepository {
    /**
     * Méthode pour envoyer une commande JSON au serveur via une requête POST
     * @param stUrl L'adresse URL du serveur
     * @param jsonMsg Le message JSON à envoyer au serveur
     * @return La réponse du serveur sous forme de chaîne de caractères
     */
    fun sendPostRequest(stUrl: String, jsonMsg: String): String? {
        try {
            val url = URL(stUrl)
            val connexion = url.openConnection() as HttpURLConnection
            connexion.requestMethod = "POST"
            connexion.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            connexion.setRequestProperty("Accept", "application/json")
            connexion.doOutput = true

            // Envoi du JSON dans la requête
            DataOutputStream(connexion.outputStream).use { os ->
                os.writeBytes(jsonMsg)
                os.flush()
            }
            // Lecture du code de réponse
            val responseCode = connexion.responseCode
            val responseMessage = connexion.responseMessage
            println("Response Code: $responseCode, Message: $responseMessage")
            // Lire le flux de réponse
            val inputStream = if (responseCode in 200..299) {
                connexion.inputStream
            } else {
                connexion.errorStream
            }
            val responseBody = inputStream.bufferedReader().use { it.readText() }
            println("Response Body: $responseBody")
            connexion.disconnect()
            return responseBody
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error: ${e.message}")
            return null
        }
    }

}