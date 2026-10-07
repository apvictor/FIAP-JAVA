package br.com.fiap.feedback.model;

import java.util.List;
import java.util.Map;

/**
 * Consolidado de um período.
 *
 * @param de            primeiro dia do período (yyyy-MM-dd)
 * @param ate           último dia do período (yyyy-MM-dd)
 * @param total         quantidade de avaliações
 * @param media         média das notas (0 quando não há avaliações)
 * @param porDia        quantidade de avaliações por dia (inclui dias sem avaliações)
 * @param porUrgencia   quantidade de avaliações por urgência
 * @param avaliacoes    avaliações do período, da mais antiga para a mais recente
 */
public record RelatorioSemanal(
        String de,
        String ate,
        int total,
        double media,
        Map<String, Integer> porDia,
        Map<Urgencia, Integer> porUrgencia,
        List<Avaliacao> avaliacoes) {
}
