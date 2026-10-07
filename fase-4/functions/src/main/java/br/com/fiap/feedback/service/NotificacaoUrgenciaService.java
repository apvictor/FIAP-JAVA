package br.com.fiap.feedback.service;

import br.com.fiap.feedback.email.ComposicaoEmail;
import br.com.fiap.feedback.email.EmailSender;
import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.Urgencia;
import java.util.List;

/** Envia um e-mail aos administradores para cada avaliação crítica. */
public class NotificacaoUrgenciaService {

    private final EmailSender emailSender;
    private final List<String> administradores;

    public NotificacaoUrgenciaService(EmailSender emailSender, List<String> administradores) {
        this.emailSender = emailSender;
        this.administradores = administradores;
    }

    /** @return quantidade de e-mails enviados */
    public int notificar(List<Avaliacao> avaliacoes) {
        int enviados = 0;
        for (Avaliacao a : avaliacoes) {
            if (a.urgencia() == Urgencia.CRITICA) {
                emailSender.enviar(administradores, ComposicaoEmail.urgencia(a));
                enviados++;
            }
        }
        return enviados;
    }
}
