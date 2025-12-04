package com.pw.satisfactionsurveyapp.repositories

import com.pw.satisfactionsurveyapp.models.*
import com.pw.satisfactionsurveyapp.providers.SupabaseProvider
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class EncuestaRepository (val supabase: SupabaseClient) {

    suspend fun getCuestionario(): List<Pregunta> = supabase.postgrest["preguntas"].select(columns = Columns.raw("*, opciones(*)")).decodeList<Pregunta>()

    suspend fun addRespuestas(respuestas: List<RespuestaUsuario>) = supabase.postgrest["respuestas"].insert(respuestas)

    suspend fun getAllRespuestas(): List<RespuestaUsuario> = supabase.postgrest["respuestas"].select().decodeList<RespuestaUsuario>()
}