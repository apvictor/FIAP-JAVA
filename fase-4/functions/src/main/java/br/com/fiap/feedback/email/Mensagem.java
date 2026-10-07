package br.com.fiap.feedback.email;

/** E-mail pronto para envio (texto e HTML). */
public record Mensagem(String assunto, String texto, String html) {
}
