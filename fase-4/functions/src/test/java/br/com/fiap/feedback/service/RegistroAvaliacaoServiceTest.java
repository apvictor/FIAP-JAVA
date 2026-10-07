package br.com.fiap.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.fiap.feedback.FakeAvaliacaoRepository;
import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.Urgencia;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class RegistroAvaliacaoServiceTest {

    private final FakeAvaliacaoRepository repository = new FakeAvaliacaoRepository();
    private final Clock clock = Clock.fixed(Instant.parse("2026-11-02T23:30:00Z"), ZoneOffset.UTC);
    private final RegistroAvaliacaoService service =
            new RegistroAvaliacaoService(repository, new ClassificadorUrgencia(3, 6), clock);

    @Test
    void registraAvaliacaoCriticaComDiaEmUtc() {
        Avaliacao a = service.registrar("  Aula confusa  ", 2);

        assertEquals(Urgencia.CRITICA, a.urgencia());
        assertEquals("Aula confusa", a.descricao());
        assertEquals("2026-11-02", a.dia());
        assertEquals("2026-11-02T23:30:00Z", a.dataEnvio());
        assertEquals(1, repository.salvas.size());
        assertEquals(a, repository.salvas.get(0));
    }

    @Test
    void gerarIdsDistintos() {
        assertTrue(!service.registrar("a", 5).id().equals(service.registrar("b", 5).id()));
    }

    @Test
    void aceitaLimitesDaNota() {
        assertEquals(Urgencia.CRITICA, service.registrar("x", 0).urgencia());
        assertEquals(Urgencia.BAIXA, service.registrar("x", 10).urgencia());
    }

    @Test
    void rejeitaEntradasInvalidasSemPersistir() {
        assertThrows(ValidacaoException.class, () -> service.registrar(null, 5));
        assertThrows(ValidacaoException.class, () -> service.registrar("   ", 5));
        assertThrows(ValidacaoException.class, () -> service.registrar("x".repeat(1001), 5));
        assertThrows(ValidacaoException.class, () -> service.registrar("ok", null));
        assertThrows(ValidacaoException.class, () -> service.registrar("ok", -1));
        assertThrows(ValidacaoException.class, () -> service.registrar("ok", 11));
        assertTrue(repository.salvas.isEmpty());
    }

    @Test
    void aceitaDescricaoNoTamanhoMaximo() {
        service.registrar("x".repeat(1000), 5);
        assertEquals(1, repository.salvas.size());
    }
}
