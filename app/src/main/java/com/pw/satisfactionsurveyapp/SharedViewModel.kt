package com.pw.satisfactionsurveyapp

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pw.satisfactionsurveyapp.models.Pregunta
import com.pw.satisfactionsurveyapp.providers.SupabaseProvider
import com.pw.satisfactionsurveyapp.repositories.EncuestaRepository
import kotlinx.coroutines.launch

class SharedViewModel: ViewModel() {
    private val encuestaRepository = EncuestaRepository(SupabaseProvider.client)
    private val _preguntas = MutableLiveData<List<Pregunta>>()
    val preguntas: LiveData<List<Pregunta>> get() = _preguntas

    init{
        loadCuestionario()
    }

    fun loadCuestionario(){
        viewModelScope.launch {
            try {
                _preguntas.value = encuestaRepository.getCuestionario()
            } catch (e: Exception) {
                Log.e("SharedViewModel", "Error al cargar preguntas", e)
            }
        }
    }
}