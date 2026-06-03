package com.example.recetarioboliviano.vista.actividades

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.recetarioboliviano.R
import com.example.recetarioboliviano.RecetarioApp
import com.example.recetarioboliviano.databinding.ActivitySplashBinding
import com.example.recetarioboliviano.modelo.util.Constantes
import kotlinx.coroutines.launch

/**
 * Activity Splash que muestra la pantalla de bienvenida.
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences(Constantes.PREFS_NAME, Context.MODE_PRIVATE)

        // Animaciones
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        
        binding.tvTitulo.startAnimation(slideUp)
        binding.tvSubtitulo.startAnimation(slideUp)
        binding.btnSiguiente.startAnimation(slideUp)

        // Verificar si el usuario ya está registrado
        verificarUsuarioRegistrado()

        binding.btnSiguiente.setOnClickListener {
            val usuarioRegistrado = prefs.getBoolean(Constantes.KEY_USUARIO_REGISTRADO, false)
            if (usuarioRegistrado) {
                irAMainActivity()
            } else {
                irARegistroActivity()
            }
        }
    }

    private fun verificarUsuarioRegistrado() {
        lifecycleScope.launch {
            val repository = (application as RecetarioApp).repository
            val usuario = repository.obtenerUsuarioSync()
            if (usuario != null) {
                prefs.edit().putBoolean(Constantes.KEY_USUARIO_REGISTRADO, true).apply()
                prefs.edit().putInt(Constantes.KEY_USUARIO_ID, usuario.id).apply()
            }
        }
    }

    private fun irARegistroActivity() {
        val intent = Intent(this, RegistroActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun irAMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }
}
