package br.com.fiap.feedback;

import br.com.fiap.feedback.config.Config;
import br.com.fiap.feedback.email.AcsEmailSender;
import br.com.fiap.feedback.email.EmailSender;
import br.com.fiap.feedback.repository.AvaliacaoRepository;
import br.com.fiap.feedback.repository.CosmosAvaliacaoRepository;
import br.com.fiap.feedback.service.ClassificadorUrgencia;
import br.com.fiap.feedback.service.EnvioRelatorioService;
import br.com.fiap.feedback.service.NotificacaoUrgenciaService;
import br.com.fiap.feedback.service.RegistroAvaliacaoService;
import br.com.fiap.feedback.service.RelatorioService;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.Clock;

/**
 * Composição das dependências. Cada serviço é criado sob demanda e reaproveitado entre invocações
 * (os clientes do Azure são caros de criar). Uma função só inicializa o que realmente usa.
 */
public final class Servicos {

    private static volatile Config config;
    private static volatile AvaliacaoRepository repository;
    private static volatile EmailSender emailSender;

    private Servicos() {
    }

    public static RegistroAvaliacaoService registroAvaliacao() {
        Config c = config();
        return new RegistroAvaliacaoService(
                repository(), new ClassificadorUrgencia(c.urgenciaCriticaAte(), c.urgenciaMediaAte()), Clock.systemUTC());
    }

    public static NotificacaoUrgenciaService notificacaoUrgencia() {
        return new NotificacaoUrgenciaService(emailSender(), config().emailsAdmin());
    }

    public static EnvioRelatorioService envioRelatorio() {
        return new EnvioRelatorioService(
                new RelatorioService(repository(), Clock.systemUTC()), emailSender(), config().emailsAdmin());
    }

    private static Config config() {
        if (config == null) {
            config = Config.doAmbiente();
        }
        return config;
    }

    private static synchronized AvaliacaoRepository repository() {
        if (repository == null) {
            Config c = config();
            CosmosClient client = new CosmosClientBuilder()
                    .endpoint(c.cosmosEndpoint())
                    .credential(new DefaultAzureCredentialBuilder().build())
                    .buildClient();
            repository = new CosmosAvaliacaoRepository(client, c.cosmosDatabase(), c.cosmosContainer());
        }
        return repository;
    }

    private static synchronized EmailSender emailSender() {
        if (emailSender == null) {
            Config c = config();
            emailSender = new AcsEmailSender(c.acsConnectionString(), c.emailRemetente());
        }
        return emailSender;
    }
}
