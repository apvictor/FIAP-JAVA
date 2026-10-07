package br.com.fiap.feedback.function;

import br.com.fiap.feedback.Servicos;
import br.com.fiap.feedback.model.Avaliacao;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.CosmosDBTrigger;
import com.microsoft.azure.functions.annotation.FunctionName;
import java.util.ArrayList;
import java.util.List;

/**
 * Change Feed do Cosmos DB — envia e-mail aos administradores para avaliações críticas.
 * A entrega do Change Feed é "pelo menos uma vez": em reprocessamento um e-mail pode se repetir.
 */
public class NotificarUrgenciaFunction {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @FunctionName("notificar_urgencia")
    public void run(
            @CosmosDBTrigger(name = "documentos",
                    databaseName = "%COSMOS_DATABASE%",
                    containerName = "%COSMOS_CONTAINER%",
                    leaseContainerName = "%COSMOS_LEASES_CONTAINER%",
                    connection = "COSMOS_CONNECTION",
                    createLeaseContainerIfNotExists = false) String documentos,
            ExecutionContext context) throws Exception {
        List<Avaliacao> avaliacoes = interpretar(documentos);
        int enviados = Servicos.notificacaoUrgencia().notificar(avaliacoes);
        context.getLogger().info("Change Feed: " + avaliacoes.size() + " documento(s), " + enviados + " e-mail(s) de urgência.");
    }

    /** Aceita um array de documentos ou um documento único. Visível para testes. */
    static List<Avaliacao> interpretar(String json) throws Exception {
        JsonNode raiz = MAPPER.readTree(json);
        List<Avaliacao> resultado = new ArrayList<>();
        if (raiz.isArray()) {
            for (JsonNode no : raiz) {
                resultado.add(MAPPER.treeToValue(no, Avaliacao.class));
            }
        } else if (raiz.isObject()) {
            resultado.add(MAPPER.treeToValue(raiz, Avaliacao.class));
        }
        return resultado;
    }
}
