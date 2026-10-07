package br.com.fiap.feedback.function;

import br.com.fiap.feedback.Servicos;
import br.com.fiap.feedback.model.Avaliacao;
import br.com.fiap.feedback.service.ValidacaoException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import java.util.Map;
import java.util.Optional;

/**
 * {@code POST /api/avaliacao} — recebe, valida e persiste uma avaliação.
 * Não envia notificações: o Change Feed do Cosmos DB aciona a função de notificação.
 */
public class ReceberAvaliacaoFunction {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @FunctionName("receber_avaliacao")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION,
                    route = "avaliacao") HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
        try {
            Avaliacao avaliacao = processar(request.getBody().orElse(null));
            context.getLogger().info("Avaliação registrada: id=" + avaliacao.id() + " urgencia=" + avaliacao.urgencia());
            return resposta(request, HttpStatus.CREATED, Map.of("id", avaliacao.id(), "urgencia", avaliacao.urgencia()));
        } catch (ValidacaoException e) {
            return resposta(request, HttpStatus.BAD_REQUEST, Map.of("erro", e.getMessage()));
        } catch (Exception e) {
            context.getLogger().severe("Falha ao registrar avaliação: " + e);
            return resposta(request, HttpStatus.INTERNAL_SERVER_ERROR, Map.of("erro", "Erro interno."));
        }
    }

    /** Interpreta o corpo JSON e delega ao serviço. Visível para testes. */
    static Avaliacao processar(String corpo) throws Exception {
        return Servicos.registroAvaliacao().registrar(descricaoDe(corpo), notaDe(corpo));
    }

    static JsonNode ler(String corpo) {
        if (corpo == null || corpo.isBlank()) {
            throw new ValidacaoException("O corpo da requisição é obrigatório.");
        }
        try {
            JsonNode raiz = MAPPER.readTree(corpo);
            if (raiz == null || !raiz.isObject()) {
                throw new ValidacaoException("O corpo deve ser um objeto JSON.");
            }
            return raiz;
        } catch (ValidacaoException e) {
            throw e;
        } catch (Exception e) {
            throw new ValidacaoException("JSON inválido.");
        }
    }

    static String descricaoDe(String corpo) {
        JsonNode no = ler(corpo).get("descricao");
        if (no != null && !no.isNull() && !no.isTextual()) {
            throw new ValidacaoException("O campo 'descricao' deve ser texto.");
        }
        return no == null || no.isNull() ? null : no.asText();
    }

    static Integer notaDe(String corpo) {
        JsonNode no = ler(corpo).get("nota");
        if (no == null || no.isNull()) {
            return null;
        }
        // Rejeita "5", 5.5 e outros tipos: só inteiros JSON.
        if (!no.isIntegralNumber() || !no.canConvertToInt()) {
            throw new ValidacaoException("O campo 'nota' deve ser um inteiro de 0 a 10.");
        }
        return no.asInt();
    }

    private static HttpResponseMessage resposta(HttpRequestMessage<?> request, HttpStatus status, Object corpo) {
        return request.createResponseBuilder(status)
                .header("Content-Type", "application/json")
                .body(corpo)
                .build();
    }
}
