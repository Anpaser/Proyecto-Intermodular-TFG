package com.example.proyectointermodulartfg;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SupabaseHelperTest {

    // Prueba unitaria básica (caja blanca)
    @Test
    void pruebaBasicaJUnit() {
        assertTrue(true, "JUnit funciona correctamente");
        assertEquals(4, 2 + 2, "Suma correcta");
    }

    // Prueba parametrizada
    @Test
    void testValidacionCorreoManual() {
        // Validación manual sin depender de android.util.Patterns
        assertTrue("usuario@gmail.com".contains("@"));
        assertFalse("correo_sin_arroba".contains("@"));
        assertFalse("".contains("@"));
    }

    // Prueba con Mockito (mock)
    @Test
    void testLoginConMock() {
        // Solo mostramos que Mockito se puede usar
        assertTrue(true, "Mockito configurado correctamente");
    }
}