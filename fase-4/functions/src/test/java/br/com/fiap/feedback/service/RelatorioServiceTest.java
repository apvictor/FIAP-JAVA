package br.com.fiap.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.fiap.feedback.FakeAvaliacaoRepository;
import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.RelatorioSemanal;
import br.com.fiap.feedback.model.Urgencia;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RelatorioServiceTest {

    private final FakeAvaliacaoRepository repository = new FakeAvaliacaoRepository();
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-09T11:00:00Z"), ZoneOffset.UTC);
    private final RelatorioService service = new RelatorioService(repository, clock);

    private static Avaliacao av(String dia, int nota, Urgencia u) {
        return new Avaliacao(dia + "-" + nota, dia, "desc " + nota, nota, u, dia + "T10:00:00Z");
    }

    @Test
    void consolidaMediaContagensEPreencheDiasVazios() {
        repository.salvas.add(av("2026-11-03", 2, Urgencia.CRITICA));
        repository.salvas.add(av("2026-11-03", 8, Urgencia.BAIXA));
        repository.salvas.add(av("2026-11-07", 5, Urgencia.MEDIA));
        repository.salvas.add(av("2026-10-01", 1, Urgencia.CRITICA)); // fora do período

        RelatorioSemanal r = service.gerarUltimosDias(7);

        assertEquals("2026-11-03", r.de());
        assertEquals("2026-11-09", r.ate());
        assertEquals(3, r.total());
        assertEquals(5.0, r.media(), 0.0001);
        assertEquals(7, r.porDia().size());
        assertEquals(2, r.porDia().get("2026-11-03"));
        assertEquals(0, r.porDia().get("2026-11-04"));
        assertEquals(1, r.porDia().get("2026-11-07"));
        assertEquals(1, r.porUrgencia().get(Urgencia.CRITICA));
        assertEquals(1, r.porUrgencia().get(Urgencia.MEDIA));
        assertEquals(1, r.porUrgencia().get(Urgencia.BAIXA));
    }

    @Test
    void semAvaliacoesMediaZero() {
        RelatorioSemanal r = service.gerarUltimosDias(7);

        assertEquals(0, r.total());
        assertEquals(0.0, r.media());
        assertEquals(0, r.porUrgencia().get(Urgencia.CRITICA));
    }

    @Test
    void rejeitaPeriodoInvalido() {
        assertThrows(ValidacaoException.class, () -> service.gerarUltimosDias(0));
        assertThrows(ValidacaoException.class, () -> service.gerarUltimosDias(91));
    }

    @Test
    void ordenaAvaliacoesPorDataDeEnvio() {
        repository.salvas.add(av("2026-11-07", 5, Urgencia.MEDIA));
        repository.salvas.add(av("2026-11-04", 9, Urgencia.BAIXA));

        RelatorioSemanal r = service.gerarUltimosDias(7);

        assertEquals("2026-11-04", r.avaliacoes().get(0).dia());
    }
}
