package com.pw.satisfactionsurveyapp

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pw.satisfactionsurveyapp.models.Pregunta
import com.pw.satisfactionsurveyapp.models.RespuestaUsuario
import com.pw.satisfactionsurveyapp.providers.SupabaseProvider
import com.pw.satisfactionsurveyapp.repositories.EncuestaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SurveyActivity : AppCompatActivity() {

    private lateinit var llContenedor: LinearLayout
    private lateinit var btnEnviar: Button
    private lateinit var progressBar: ProgressBar

    // Repositorio y Mapas
    private val repository = EncuestaRepository(SupabaseProvider.client)
    private val respuestasUsuario = mutableMapOf<Long, Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey) // Asegúrate que sea el XML con ScrollView

        llContenedor = findViewById(R.id.llContenedorPreguntas)
        btnEnviar = findViewById(R.id.btnEnviar)
        progressBar = findViewById(R.id.progressBar)

        cargarPreguntas()

        btnEnviar.setOnClickListener {
            enviarEncuesta()
        }
    }

    private fun cargarPreguntas() {
        progressBar.visibility = View.VISIBLE
        // Limpiamos por si acaso
        llContenedor.removeAllViews()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listaPreguntas = repository.getCuestionario()

                runOnUiThread {
                    // AQUÍ ESTÁ EL TRUCO: Un ciclo simple
                    for (pregunta in listaPreguntas) {
                        agregarVistaPregunta(pregunta)
                    }
                    progressBar.visibility = View.GONE
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@SurveyActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // Esta función reemplaza al Adapter
    private fun agregarVistaPregunta(pregunta: Pregunta) {
        // 1. "Inflamos" el XML de la tarjeta (item_question.xml)
        val view = layoutInflater.inflate(R.layout.item_question, llContenedor, false)

        // 2. Buscamos los controles DENTRO de esa tarjeta
        val tvTexto = view.findViewById<TextView>(R.id.tvTextoPregunta)
        val rgOpciones = view.findViewById<RadioGroup>(R.id.rgOpciones)

        // 3. Llenamos datos
        tvTexto.text = pregunta.texto

        // 4. Creamos los RadioButtons dinámicamente
        pregunta.opciones.forEach { opcion ->
            val rb = RadioButton(this)
            rb.text = opcion.texto
            rb.id = View.generateViewId() // Importante para que funcionen los clicks

            rb.setOnClickListener {
                // Guardamos en el mapa
                respuestasUsuario[pregunta.id] = opcion.id
            }
            rgOpciones.addView(rb)
        }

        // 5. Agregamos la tarjeta completa al contenedor vertical
        llContenedor.addView(view)
    }

    private fun enviarEncuesta() {
        if (respuestasUsuario.isEmpty()) {
            Toast.makeText(this, "Contesta algo primero", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        btnEnviar.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Convertir el mapa a la lista de objetos que pide Supabase
                val listaParaEnviar = respuestasUsuario.map { (preguntaId, opcionId) ->
                    RespuestaUsuario(
                        preguntaId = preguntaId,
                        opcionId = opcionId
                    )
                }

                // 2. Enviar al repositorio
                repository.addRespuestas(listaParaEnviar)

                runOnUiThread {
                    Toast.makeText(this@SurveyActivity, "¡Encuesta enviada con éxito!", Toast.LENGTH_LONG).show()
                    finish() // Cierra la pantalla
                }

            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    btnEnviar.isEnabled = true
                    Toast.makeText(this@SurveyActivity, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}