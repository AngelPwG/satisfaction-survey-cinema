package com.pw.satisfactionsurveyapp.providers

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest

object SupabaseProvider {
    val client: SupabaseClient by lazy{
        createSupabaseClient(
            supabaseUrl = "https://tgbnxykawqutzsbsxail.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRnYm54eWthd3F1dHpzYnN4YWlsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjI3NjcxOTgsImV4cCI6MjA3ODM0MzE5OH0.7Fc9OhVOl70WFwZzk-TudAjc0DwEi8oqs1ojvXpnrh8"
        ) {
            install(Postgrest.Companion)
        }
    }
}