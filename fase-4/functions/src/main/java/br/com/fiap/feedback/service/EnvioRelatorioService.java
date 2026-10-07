package br.com.fiap.feedback.service;

import br.com.fiap.feedback.email.ComposicaoEmail;
import br.com.fiap.feedback.email.EmailSender;
import br.com.fiap.feedback.model.RelatorioSemanal;
import java.util.List;

/** Gera o relatório do período e o envia aos administradores. */
public class EnvioRelatorioService {

    private final RelatorioService relatorioService;
    private final EmailSender emailSender;
    private final List<String> administradores;

    public EnvioRelatorioService(RelatorioService relatorioService, EmailSender emailSender, List<String> administradores) {
        this.relatorioService = relatorioService;
        this.emailSender = emailSender;
        this.administradores = administradores;
    }

    public RelatorioSemanal enviar(int dias) {
        RelatorioSemanal relatorio = relatorioService.gerarUltimosDias(dias);
        emailSender.enviar(administradores, ComposicaoEmail.relatorio(relatorio));
        return relatorio;
    }
}
