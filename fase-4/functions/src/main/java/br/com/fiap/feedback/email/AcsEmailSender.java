package br.com.fiap.feedback.email;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import java.util.List;

/** Implementação sobre o Azure Communication Services Email. */
public class AcsEmailSender implements EmailSender {

    private final EmailClient client;
    private final String remetente;

    public AcsEmailSender(String connectionString, String remetente) {
        this.client = new EmailClientBuilder().connectionString(connectionString).buildClient();
        this.remetente = remetente;
    }

    @Override
    public void enviar(List<String> destinatarios, Mensagem mensagem) {
        EmailMessage email = new EmailMessage()
                .setSenderAddress(remetente)
                .setToRecipients(destinatarios.stream().map(EmailAddress::new).toList())
                .setSubject(mensagem.assunto())
                .setBodyPlainText(mensagem.texto())
                .setBodyHtml(mensagem.html());
        // Aguarda a conclusão para que falhas de envio apareçam como exceção (e como alerta).
        client.beginSend(email).waitForCompletion();
    }
}
