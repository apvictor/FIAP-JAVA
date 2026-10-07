package br.com.fiap.feedback.repository;

import br.com.fiap.feedback.model.Avaliacao;
import java.time.LocalDate;
import java.util.List;

/** Persistência de avaliações. */
public interface AvaliacaoRepository {

    void salvar(Avaliacao avaliacao);

    /** Avaliações cujo {@code dia} está entre {@code de} e {@code ate}, inclusive. */
    List<Avaliacao> listarPorPeriodo(LocalDate de, LocalDate ate);
}
