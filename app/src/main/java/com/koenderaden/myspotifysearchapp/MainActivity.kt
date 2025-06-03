package com.koenderaden.myspotifysearchapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import okhttp3.*
import java.io.IOException
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.koenderaden.myspotifysearchapp.ui.theme.MySpotifySearchAppTheme

class MainActivity : ComponentActivity() {

    private val clientId = "912663c672f54d7d80afbe856378f46a"
    private val redirectUri = "spotifydemo://callback"
    private val requestCode = 1337

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val request = AuthorizationRequest.Builder(
            clientId,
            AuthorizationResponse.Type.TOKEN,
            redirectUri
        )
            .setScopes(arrayOf("streaming", "user-read-email"))
            .build()

        AuthorizationClient.openLoginActivity(this, requestCode, request)

        setContent {
            MySpotifySearchAppTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)

        if (requestCode == this.requestCode) {
            val response = AuthorizationClient.getResponse(resultCode, intent)

            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val accessToken = response.accessToken
                    Log.d("SPOTIFY", "✅ Login success: $accessToken")

                    val client = OkHttpClient()
                    val request = Request.Builder()
                        .url("https://api.spotify.com/v1/search?q=drake&type=track")
                        .addHeader("Authorization", "Bearer $accessToken")
                        .build()

                    client.newCall(request).enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            Log.e("SPOTIFY", "❌ API call failed: ${e.message}")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            if (response.isSuccessful) {
                                val body = response.body?.string()
                                Log.d("SPOTIFY", "✅ API response:\n$body")
                            } else {
                                Log.e("SPOTIFY", "⚠️ API error: ${response.code}")
                            }
                        }
                    })
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e("SPOTIFY", "❌ Login error: ${response.error}")
                }

                else -> {
                    Log.e("SPOTIFY", "❓ Unexpected response: ${response.type}")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier)
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MySpotifySearchAppTheme {
        Greeting("Android")
    }
}
