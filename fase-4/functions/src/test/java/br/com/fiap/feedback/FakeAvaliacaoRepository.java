package br.com.fiap.feedback;

import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.repository.AvaliacaoRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Repositório em memória para testes. */
public class FakeAvaliacaoRepository implements AvaliacaoRepository {

    public final List<Avaliacao> salvas = new ArrayList<>();

    @Override
    public void salvar(Avaliacao avaliacao) {
        salvas.add(avaliacao);
    }

    @Override
    public List<Avaliacao> listarPorPeriodo(LocalDate de, LocalDate ate) {
        return salvas.stream()
                .filter(a -> a.dia().compareTo(de.toString()) >= 0 && a.dia().compareTo(ate.toString()) <= 0)
                .toList();
    }
}
