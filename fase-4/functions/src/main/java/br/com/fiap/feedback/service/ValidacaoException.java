package br.com.fiap.feedback.service;

/** Entrada inválida; a mensagem é segura para devolver ao cliente. */
public class ValidacaoException extends RuntimeException {

    public ValidacaoException(String mensagem) {
        super(mensagem);
    }
}
