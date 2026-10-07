package br.com.fiap.feedback.function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import br.com.fiap.feedback.service.ValidacaoException;
import org.junit.jupiter.api.Test;

class ReceberAvaliacaoFunctionTest {

    @Test
    void leCamposValidos() {
        String json = "{\"descricao\":\"Boa\",\"nota\":7}";
        assertEquals("Boa", ReceberAvaliacaoFunction.descricaoDe(json));
        assertEquals(7, ReceberAvaliacaoFunction.notaDe(json));
    }

    @Test
    void camposAusentesViramNulo() {
        assertNull(ReceberAvaliacaoFunction.descricaoDe("{}"));
        assertNull(ReceberAvaliacaoFunction.notaDe("{}"));
        assertNull(ReceberAvaliacaoFunction.notaDe("{\"nota\":null}"));
    }

    @Test
    void rejeitaNotaQueNaoEInteiro() {
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.notaDe("{\"nota\":\"5\"}"));
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.notaDe("{\"nota\":5.5}"));
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.notaDe("{\"nota\":99999999999}"));
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.notaDe("{\"nota\":true}"));
    }

    @Test
    void rejeitaDescricaoQueNaoETexto() {
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.descricaoDe("{\"descricao\":123}"));
    }

    @Test
    void rejeitaCorpoInvalido() {
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.ler(null));
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.ler("  "));
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.ler("não é json"));
        assertThrows(ValidacaoException.class, () -> ReceberAvaliacaoFunction.ler("[1,2]"));
    }
}
