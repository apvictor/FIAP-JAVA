package br.com.fiap.feedback.email;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.RelatorioSemanal;
import br.com.fiap.feedback.model.Urgencia;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ComposicaoEmailTest {

    @Test
    void emailDeUrgenciaTrazDescricaoUrgenciaEData() {
        Avaliacao a = new Avaliacao("1", "2026-11-02", "Áudio ruim", 1, Urgencia.CRITICA, "2026-11-02T10:00:00Z");

        Mensagem m = ComposicaoEmail.urgencia(a);

        for (String conteudo : List.of(m.texto(), m.html())) {
            assertTrue(conteudo.contains("Áudio ruim"));
            assertTrue(conteudo.contains("CRITICA"));
            assertTrue(conteudo.contains("2026-11-02T10:00:00Z"));
        }
    }

    @Test
    void htmlEscapaConteudoDoEstudante() {
        Avaliacao a = new Avaliacao("1", "2026-11-02", "<script>alert(1)</script>", 1, Urgencia.CRITICA, "2026-11-02T10:00:00Z");

        Mensagem m = ComposicaoEmail.urgencia(a);

        assertFalse(m.html().contains("<script>"));
        assertTrue(m.html().contains("&lt;script&gt;"));
    }

    @Test
    void relatorioTemMediaContagensEAvaliacoes() {
        Avaliacao a = new Avaliacao("1", "2026-11-05", "Boa aula", 9, Urgencia.BAIXA, "2026-11-05T10:00:00Z");
        Map<String, Integer> porDia = new LinkedHashMap<>();
        porDia.put("2026-11-05", 1);
        Map<Urgencia, Integer> porUrgencia = new EnumMap<>(Urgencia.class);
        porUrgencia.put(Urgencia.BAIXA, 1);
        RelatorioSemanal r = new RelatorioSemanal("2026-11-03", "2026-11-09", 1, 9.0, porDia, porUrgencia, List.of(a));

        Mensagem m = ComposicaoEmail.relatorio(r);

        assertTrue(m.assunto().contains("2026-11-03 a 2026-11-09"));
        for (String conteudo : List.of(m.texto(), m.html())) {
            assertTrue(conteudo.contains("9,00"));
            assertTrue(conteudo.contains("2026-11-05"));
            assertTrue(conteudo.contains("Boa aula"));
            assertTrue(conteudo.contains("BAIXA"));
            assertTrue(conteudo.contains("CRITICA"));
        }
    }
}
