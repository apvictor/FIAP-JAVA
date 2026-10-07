package br.com.fiap.feedback.function;

import br.com.fiap.feedback.Servicos;
import br.com.fiap.feedback.model.RelatorioSemanal;
import br.com.fiap.feedback.service.ValidacaoException;
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
 * {@code POST /api/relatorio?dias=7} — dispara o mesmo envio do relatório semanal sob demanda
 * (uso em demonstração e reenvio). Apenas um gatilho alternativo; a lógica está em {@code EnvioRelatorioService}.
 */
public class GerarRelatorioManualFunction {

    @FunctionName("gerar_relatorio_manual")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {HttpMethod.POST}, authLevel = AuthorizationLevel.FUNCTION,
                    route = "relatorio") HttpRequestMessage<Optional<String>> request,
            ExecutionContext context) {
        try {
            int dias = Integer.parseInt(request.getQueryParameters().getOrDefault("dias", String.valueOf(GerarRelatorioSemanalFunction.DIAS)));
            RelatorioSemanal r = Servicos.envioRelatorio().enviar(dias);
            return request.createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(Map.of("total", r.total(), "media", r.media(), "de", r.de(), "ate", r.ate()))
                    .build();
        } catch (NumberFormatException | ValidacaoException e) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST)
                    .header("Content-Type", "application/json")
                    .body(Map.of("erro", "Parâmetro 'dias' deve ser um inteiro entre 1 e 90."))
                    .build();
        } catch (Exception e) {
            context.getLogger().severe("Falha ao gerar relatório: " + e);
            return request.createResponseBuilder(HttpStatus.INTERNAL_SERVER_ERROR)
                    .header("Content-Type", "application/json")
                    .body(Map.of("erro", "Erro interno."))
                    .build();
        }
    }
}
