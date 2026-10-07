package br.com.fiap.feedback.email;

import java.util.List;

/** Envio de e-mail aos administradores. */
public interface EmailSender {

    void enviar(List<String> destinatarios, Mensagem mensagem);
}
