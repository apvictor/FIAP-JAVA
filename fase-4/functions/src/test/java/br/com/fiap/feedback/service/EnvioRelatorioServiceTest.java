package br.com.fiap.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import br.com.fiap.feedback.FakeAvaliacaoRepository;
import br.com.fiap.feedback.FakeEmailSender;
import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.Urgencia;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class EnvioRelatorioServiceTest {

    @Test
    void geraEEnviaORelatorioAosAdministradores() {
        FakeAvaliacaoRepository repo = new FakeAvaliacaoRepository();
        repo.salvas.add(new Avaliacao("1", "2026-11-05", "boa", 9, Urgencia.BAIXA, "2026-11-05T10:00:00Z"));
        FakeEmailSender email = new FakeEmailSender();
        Clock clock = Clock.fixed(Instant.parse("2026-11-09T11:00:00Z"), ZoneOffset.UTC);
        EnvioRelatorioService service = new EnvioRelatorioService(new RelatorioService(repo, clock), email, List.of("a@x.com", "b@x.com"));

        var relatorio = service.enviar(7);

        assertEquals(1, relatorio.total());
        assertEquals(1, email.enviadas.size());
        assertEquals(List.of("a@x.com", "b@x.com"), email.ultimosDestinatarios);
        assertTrue(email.enviadas.get(0).assunto().contains("2026-11-03 a 2026-11-09"));
    }
}
