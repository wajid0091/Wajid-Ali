package com.example.data

import android.content.Context
import android.net.Uri
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONObject
import java.io.IOException

object ImageUploader {

    private val client = OkHttpClient()

    fun uploadToImgBB(
        context: Context,
        uri: Uri,
        apiKey: String = "db801e55f83a34710dc37d103f1048a8",
        onProgress: (Boolean) -> Unit = {},
        onResult: (String?) -> Unit
    ) {
        onProgress(true)
        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(uri)
        val bytes = inputStream?.use { it.readBytes() }
        if (bytes == null) {
            onProgress(false)
            onResult(null)
            return
        }

        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "image",
                "upload.jpg",
                RequestBody.create("image/jpeg".toMediaTypeOrNull(), bytes)
            )
            .build()

        val request = Request.Builder()
            .url("https://api.imgbb.com/1/upload?key=$apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onProgress(false)
                onResult(null)
            }

            override fun onResponse(call: Call, response: Response) {
                onProgress(false)
                val bodyStr = response.body?.string()
                if (response.isSuccessful && bodyStr != null) {
                    try {
                        val json = JSONObject(bodyStr)
                        if (json.has("data")) {
                            val data = json.getJSONObject("data")
                            if (data.has("url")) {
                                val url = data.getString("url")
                                onResult(url)
                                return
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                onResult(null)
            }
        })
    }
}
