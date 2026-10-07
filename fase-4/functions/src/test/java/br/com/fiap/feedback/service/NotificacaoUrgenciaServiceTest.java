package br.com.fiap.feedback.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import br.com.fiap.feedback.FakeEmailSender;
import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.Urgencia;
import java.util.List;
import org.junit.jupiter.api.Test;

class NotificacaoUrgenciaServiceTest {

    private final FakeEmailSender email = new FakeEmailSender();
    private final NotificacaoUrgenciaService service = new NotificacaoUrgenciaService(email, List.of("adm@x.com"));

    private static Avaliacao av(int nota, Urgencia u) {
        return new Avaliacao("id" + nota, "2026-11-02", "desc", nota, u, "2026-11-02T10:00:00Z");
    }

    @Test
    void enviaSomenteParaCriticas() {
        int enviados = service.notificar(List.of(av(1, Urgencia.CRITICA), av(5, Urgencia.MEDIA), av(9, Urgencia.BAIXA), av(0, Urgencia.CRITICA)));

        assertEquals(2, enviados);
        assertEquals(2, email.enviadas.size());
        assertEquals(List.of("adm@x.com"), email.ultimosDestinatarios);
    }

    @Test
    void naoEnviaNadaSemCriticas() {
        assertEquals(0, service.notificar(List.of(av(9, Urgencia.BAIXA))));
        assertEquals(0, email.enviadas.size());
    }
}
