package br.com.fiap.feedback.service;

import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.model.RelatorioSemanal;
import br.com.fiap.feedback.model.Urgencia;
import br.com.fiap.feedback.repository.AvaliacaoRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Consolida as avaliações de um período. Não envia e-mails. */
public class RelatorioService {

    private final AvaliacaoRepository repository;
    private final Clock clock;

    public RelatorioService(AvaliacaoRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    /** Gera o relatório dos últimos {@code dias} dias, incluindo hoje (UTC). */
    public RelatorioSemanal gerarUltimosDias(int dias) {
        if (dias < 1 || dias > 90) {
            throw new ValidacaoException("O período deve estar entre 1 e 90 dias.");
        }
        LocalDate ate = clock.instant().atZone(ZoneOffset.UTC).toLocalDate();
        LocalDate de = ate.minusDays(dias - 1L);
        return consolidar(de, ate, repository.listarPorPeriodo(de, ate));
    }

    static RelatorioSemanal consolidar(LocalDate de, LocalDate ate, List<Avaliacao> avaliacoes) {
        List<Avaliacao> ordenadas = avaliacoes.stream()
                .sorted(Comparator.comparing(Avaliacao::dataEnvio))
                .toList();

        Map<String, Integer> porDia = new LinkedHashMap<>();
        for (LocalDate d = de; !d.isAfter(ate); d = d.plusDays(1)) {
            porDia.put(d.toString(), 0);
        }
        Map<Urgencia, Integer> porUrgencia = new EnumMap<>(Urgencia.class);
        for (Urgencia u : Urgencia.values()) {
            porUrgencia.put(u, 0);
        }

        for (Avaliacao a : ordenadas) {
            porDia.merge(a.dia(), 1, Integer::sum);
            porUrgencia.merge(a.urgencia(), 1, Integer::sum);
        }

        double media = ordenadas.stream().mapToInt(Avaliacao::nota).average().orElse(0);
        return new RelatorioSemanal(de.toString(), ate.toString(), ordenadas.size(), media, porDia, porUrgencia, ordenadas);
    }
}
