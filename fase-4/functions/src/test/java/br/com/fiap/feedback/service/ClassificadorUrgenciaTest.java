package br.com.fiap.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.fiap.feedback.model.Urgencia;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ClassificadorUrgenciaTest {

    private final ClassificadorUrgencia classificador = new ClassificadorUrgencia(3, 6);

    @ParameterizedTest
    @CsvSource({"0,CRITICA", "3,CRITICA", "4,MEDIA", "6,MEDIA", "7,BAIXA", "10,BAIXA"})
    void classificaPelasFaixas(int nota, Urgencia esperada) {
        assertEquals(esperada, classificador.classificar(nota));
    }

    @Test
    void rejeitaLimiaresInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new ClassificadorUrgencia(7, 6));
        assertThrows(IllegalArgumentException.class, () -> new ClassificadorUrgencia(-1, 6));
        assertThrows(IllegalArgumentException.class, () -> new ClassificadorUrgencia(3, 11));
    }
}
