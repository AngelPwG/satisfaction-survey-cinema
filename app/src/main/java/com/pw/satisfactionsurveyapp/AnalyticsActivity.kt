package com.pw.satisfactionsurveyapp

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pw.satisfactionsurveyapp.models.Pregunta
import com.pw.satisfactionsurveyapp.providers.SupabaseProvider
import com.pw.satisfactionsurveyapp.repositories.EncuestaRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsActivity : AppCompatActivity() {

    private lateinit var llStatsContainer: LinearLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var tvTotal: TextView
    private lateinit var btnRegresar: Button
    private lateinit var btnRefrescar: Button

    private val repository = EncuestaRepository(SupabaseProvider.client)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_analytics)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        btnRegresar = findViewById(R.id.btnRegresar)
        btnRefrescar = findViewById(R.id.btnRefrescar)
        btnRegresar.setOnClickListener {
            finish()
        }
        btnRefrescar.setOnClickListener {
            cargarDatos()
        }
        llStatsContainer = findViewById(R.id.llStatsContainer)
        progressBar = findViewById(R.id.progressBar2)
        tvTotal = findViewById(R.id.tvTotalEncuestas)

        cargarDatos()
    }

    private fun cargarDatos() {
        progressBar.visibility = View.VISIBLE
        cambiarEstadoBotones(false)
        llStatsContainer.removeAllViews()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val listaPreguntas = repository.getCuestionario()

                val listaRespuestas = repository.getAllRespuestas()

                // PASO 3: Matemáticas (Contar votos)
                // Creamos un mapa: ID_Opcion -> Cantidad de Votos
                // groupingBy cuenta cuántas veces se repite cada opcionId
                val conteoVotos = listaRespuestas.groupingBy { it.opcionId }.eachCount()

                // Calculamos cuántas encuestas hay en total (aprox)
                // Dividimos el total de respuestas entre el número de preguntas
                val totalEncuestasEstimado = if (listaPreguntas.isNotEmpty())
                    listaRespuestas.size / listaPreguntas.size
                else 0

                runOnUiThread {
                    tvTotal.text = "Total de encuestas procesadas: $totalEncuestasEstimado"

                    // PASO 4: Pintar la UI
                    for (pregunta in listaPreguntas) {
                        agregarTarjetaEstadistica(pregunta, conteoVotos)
                    }
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@AnalyticsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } finally {
                runOnUiThread {
                    progressBar.visibility = View.GONE
                    cambiarEstadoBotones(true)
                }
            }
        }
    }

    // Función para crear la tarjeta de CADA PREGUNTA
    private fun agregarTarjetaEstadistica(pregunta: Pregunta, mapaConteo: Map<Long, Int>) {

        // 1. Inflar el XML de la tarjeta (item_statistic.xml)
        val cardView = layoutInflater.inflate(R.layout.item_statistic, llStatsContainer, false)

        // 2. Llenar el título de la pregunta
        val tvPregunta = cardView.findViewById<TextView>(R.id.tvStatPregunta)
        tvPregunta.text = pregunta.texto

        // 3. Buscar el contenedor donde pondremos las barras
        val llOpciones = cardView.findViewById<LinearLayout>(R.id.llOpcionesResultados)

        // 4. Calcular el total de votos SOLO para esta pregunta (para sacar %)
        // Sumamos los votos de todas sus opciones
        var votosTotalesPregunta = 0
        pregunta.opciones.forEach { opcion ->
            votosTotalesPregunta += mapaConteo[opcion.id] ?: 0
        }

        // 5. Crear las barras por cada opción
        pregunta.opciones.forEach { opcion ->
            val votos = mapaConteo[opcion.id] ?: 0 // Si es null, es 0

            // Calculamos porcentaje (cuidado con división entre cero)
            val porcentaje = if (votosTotalesPregunta > 0) {
                (votos * 100) / votosTotalesPregunta
            } else { 0 }

            // CREAMOS LAS VISTAS DINÁMICAMENTE

            // A) Texto: "Excelente: 5 votos (20%)"
            val tvOpcion = TextView(this)
            tvOpcion.text = "${opcion.texto}: $votos ($porcentaje%)"
            tvOpcion.textSize = 14f
            tvOpcion.setPadding(0, 8, 0, 4)

            // B) Barra de Progreso (Horizontal)
            // Usamos el estilo predeterminado de Android para barras horizontales
            val pb = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal)
            pb.progressDrawable.setTint(resources.getColor(R.color.cinema_red, theme))
            pb.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                24
            )
            pb.max = 100
            pb.progress = porcentaje

            llOpciones.addView(tvOpcion)
            llOpciones.addView(pb)
        }

        llStatsContainer.addView(cardView)
    }

    private fun cambiarEstadoBotones(estado: Boolean){
        btnRegresar.isEnabled = estado
        btnRefrescar.isEnabled = estado
    }
}