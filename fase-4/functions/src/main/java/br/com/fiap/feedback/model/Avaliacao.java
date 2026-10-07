package br.com.fiap.feedback.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Avaliação de uma aula, como persistida no Cosmos DB.
 *
 * @param id         identificador (UUID)
 * @param dia        data de envio em UTC (yyyy-MM-dd); é a partition key
 * @param descricao  texto livre do estudante
 * @param nota       nota inteira de 0 a 10
 * @param urgencia   urgência derivada da nota
 * @param dataEnvio  instante de envio, ISO-8601 em UTC
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Avaliacao(
        String id,
        String dia,
        String descricao,
        int nota,
        Urgencia urgencia,
        String dataEnvio) {
}
