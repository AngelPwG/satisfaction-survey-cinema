package com.pw.satisfactionsurveyapp

import android.os.Bundle
import android.util.Log
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
    private lateinit var progressBar: ProgressBar

    private val repository = EncuestaRepository(SupabaseProvider.client)
    private val storeViewModel by lazy { AppViewModelStore.provider.get(SharedViewModel::class.java) }
    private val respuestasUsuario = mutableMapOf<Long, Long>()
    private lateinit var btnRegresar: Button
    private lateinit var btnEnviar: Button
    private lateinit var btnRefrescar: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_survey)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnRegresar = findViewById<Button>(R.id.btnRegresar)
        btnRefrescar = findViewById<Button>(R.id.btnRefrescar)
        btnEnviar = findViewById<Button>(R.id.btnEnviar)

        llContenedor = findViewById(R.id.llContenedorPreguntas)

        progressBar = findViewById(R.id.progressBar)

        progressBar.visibility = View.VISIBLE
        cambiarEstadoBotones(false)
        storeViewModel.preguntas.observe(this) { listaDePreguntas ->
            if (!listaDePreguntas.isNullOrEmpty()) {
                Log.d("SurveyActivity", "Llegaron ${listaDePreguntas.size} preguntas")
                cargarPreguntas(listaDePreguntas)
            }
        }

        btnEnviar.setOnClickListener {
            enviarEncuesta()
        }
        btnRegresar.setOnClickListener {
            finish()
        }
        btnRefrescar.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            cambiarEstadoBotones(false)
            storeViewModel.loadCuestionario()
        }
    }

    private fun cargarPreguntas(listaPreguntas: List<Pregunta>) {
        llContenedor.removeAllViews()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                runOnUiThread {
                    for (pregunta in listaPreguntas) {
                        agregarVistaPregunta(pregunta)
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Log.e("SurveyActivity", "Error al cargar preguntas: ${e.message}")
                }
            } finally {
                progressBar.visibility = View.GONE
                cambiarEstadoBotones(true)
            }
        }
    }

    private fun agregarVistaPregunta(pregunta: Pregunta) {
        val view = layoutInflater.inflate(R.layout.item_question, llContenedor, false)

        val tvTexto = view.findViewById<TextView>(R.id.tvTextoPregunta)
        val rgOpciones = view.findViewById<RadioGroup>(R.id.rgOpciones)

        tvTexto.text = pregunta.texto

        pregunta.opciones.forEach { opcion ->
            val rb = RadioButton(this)
            rb.text = opcion.texto
            rb.id = View.generateViewId()

            rb.setOnClickListener {
                respuestasUsuario[pregunta.id] = opcion.id
            }
            rgOpciones.addView(rb)
        }

        llContenedor.addView(view)
    }

    private fun enviarEncuesta() {
        if (respuestasUsuario.size != llContenedor.childCount){
            Toast.makeText(this, "Contesta todas las preguntas", Toast.LENGTH_SHORT).show()
            return
        }

        progressBar.visibility = View.VISIBLE
        cambiarEstadoBotones(false)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listaParaEnviar = respuestasUsuario.map { (preguntaId, opcionId) ->
                    RespuestaUsuario(
                        preguntaId = preguntaId,
                        opcionId = opcionId
                    )
                }

                repository.addRespuestas(listaParaEnviar)

                runOnUiThread {
                    Toast.makeText(this@SurveyActivity, "¡Encuesta enviada con éxito!", Toast.LENGTH_LONG).show()
                    finish()
                }

            } catch (e: Exception) {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    cambiarEstadoBotones(true)
                    Toast.makeText(this@SurveyActivity, "Error al enviar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun cambiarEstadoBotones(estado: Boolean){
        btnEnviar.isEnabled = estado
        btnRegresar.isEnabled = estado
        btnRefrescar.isEnabled = estado
    }
}