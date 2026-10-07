package br.com.fiap.feedback.function;

import br.com.fiap.feedback.Servicos;
import br.com.fiap.feedback.model.RelatorioSemanal;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.TimerTrigger;

/** Toda segunda-feira, 11:00 UTC (08:00 em Brasília): envia o relatório dos últimos 7 dias. */
public class GerarRelatorioSemanalFunction {

    public static final int DIAS = 7;

    @FunctionName("gerar_relatorio_semanal")
    public void run(
            @TimerTrigger(name = "timer", schedule = "0 0 11 * * 1") String timerInfo,
            ExecutionContext context) {
        RelatorioSemanal r = Servicos.envioRelatorio().enviar(DIAS);
        context.getLogger().info("Relatório semanal enviado: " + r.total() + " avaliação(ões), média " + r.media());
    }
}
