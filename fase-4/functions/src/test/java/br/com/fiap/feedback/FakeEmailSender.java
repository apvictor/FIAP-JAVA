package br.com.fiap.feedback;

import br.com.fiap.feedback.email.EmailSender;
import br.com.fiap.feedback.email.Mensagem;
import java.util.ArrayList;
import java.util.List;

/** Registra os e-mails em vez de enviá-los. */
public class FakeEmailSender implements EmailSender {

    public final List<Mensagem> enviadas = new ArrayList<>();
    public List<String> ultimosDestinatarios;

    @Override
    public void enviar(List<String> destinatarios, Mensagem mensagem) {
        ultimosDestinatarios = destinatarios;
        enviadas.add(mensagem);
    }
}
