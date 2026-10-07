package br.com.fiap.feedback.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ConfigTest {

    @Test
    void lePadroesELista() {
        Config c = new Config(Map.of("ADMIN_EMAILS", " a@x.com , b@x.com,, ")::get);

        assertEquals(List.of("a@x.com", "b@x.com"), c.emailsAdmin());
        assertEquals(3, c.urgenciaCriticaAte());
        assertEquals(6, c.urgenciaMediaAte());
    }

    @Test
    void leLimiaresConfigurados() {
        Config c = new Config(Map.of("URGENCIA_CRITICA_ATE", "2", "URGENCIA_MEDIA_ATE", "5")::get);

        assertEquals(2, c.urgenciaCriticaAte());
        assertEquals(5, c.urgenciaMediaAte());
    }

    @Test
    void falhaQuandoObrigatoriaAusente() {
        assertThrows(IllegalStateException.class, () -> new Config(Map.<String, String>of()::get).cosmosEndpoint());
    }
}
