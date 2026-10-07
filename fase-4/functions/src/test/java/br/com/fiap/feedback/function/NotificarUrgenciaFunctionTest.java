package br.com.fiap.feedback.function;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.Urgencia;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificarUrgenciaFunctionTest {

    private static final String DOC = """
            {"id":"1","dia":"2026-11-02","descricao":"ruim","nota":1,"urgencia":"CRITICA",
             "dataEnvio":"2026-11-02T10:00:00Z","_rid":"abc","_ts":1762077600,"_etag":"x"}""";

    @Test
    void interpretaArrayIgnorandoMetadadosDoCosmos() throws Exception {
        List<Avaliacao> r = NotificarUrgenciaFunction.interpretar("[" + DOC + "," + DOC + "]");

        assertEquals(2, r.size());
        assertEquals(Urgencia.CRITICA, r.get(0).urgencia());
        assertEquals("ruim", r.get(0).descricao());
    }

    @Test
    void interpretaDocumentoUnico() throws Exception {
        assertEquals(1, NotificarUrgenciaFunction.interpretar(DOC).size());
    }
}
