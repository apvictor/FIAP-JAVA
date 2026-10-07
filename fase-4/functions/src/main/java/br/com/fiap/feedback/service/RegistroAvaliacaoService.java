package br.com.fiap.feedback.service;

import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.Urgencia;
import br.com.fiap.feedback.repository.AvaliacaoRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/** Valida, classifica e persiste uma avaliação. Não envia notificações. */
public class RegistroAvaliacaoService {

    public static final int TAMANHO_MAXIMO_DESCRICAO = 1000;

    private final AvaliacaoRepository repository;
    private final ClassificadorUrgencia classificador;
    private final Clock clock;

    public RegistroAvaliacaoService(AvaliacaoRepository repository, ClassificadorUrgencia classificador, Clock clock) {
        this.repository = repository;
        this.classificador = classificador;
        this.clock = clock;
    }

    public Avaliacao registrar(String descricao, Integer nota) {
        if (descricao == null || descricao.isBlank()) {
            throw new ValidacaoException("O campo 'descricao' é obrigatório.");
        }
        if (descricao.length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new ValidacaoException("O campo 'descricao' deve ter no máximo " + TAMANHO_MAXIMO_DESCRICAO + " caracteres.");
        }
        if (nota == null || nota < 0 || nota > 10) {
            throw new ValidacaoException("O campo 'nota' deve ser um inteiro de 0 a 10.");
        }

        Instant agora = clock.instant();
        Urgencia urgencia = classificador.classificar(nota);
        Avaliacao avaliacao = new Avaliacao(
                UUID.randomUUID().toString(),
                agora.atZone(ZoneOffset.UTC).toLocalDate().toString(),
                descricao.strip(),
                nota,
                urgencia,
                agora.toString());
        repository.salvar(avaliacao);
        return avaliacao;
    }
}
