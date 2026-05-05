package com.example.proyectointermodulartfg;

import io.github.jan.supabase.SupabaseClient;
import io.github.jan.supabase.SupabaseClientBuilder;
import io.github.jan.supabase.postgrest.Postgrest;
import kotlin.Unit;

public class SupabaseManager {
    private static SupabaseClient instancia;

    private static final String URL = "https://owseadckiffiwwnjgqgj.supabase.co";
    private static final String CLAVE = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im93c2VhZGNraWZmaXd3bmpncWdqIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzYzNDQ5MjUsImV4cCI6MjA5MTkyMDkyNX0.isBYVIKS4i3tUkv21SZKQpggl8nLK3m_GUsuoDgqmKE";

    private SupabaseManager() {}

    public static synchronized SupabaseClient getInstance() {
        if (instancia == null) {
            SupabaseClientBuilder builder = new SupabaseClientBuilder(URL, CLAVE);
            builder.install(Postgrest.Companion, config -> { return Unit.INSTANCE; });
            instancia = builder.build();
        }
        return instancia;
    }
}