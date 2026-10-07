package br.com.fiap.feedback.repository;

import br.com.fiap.feedback.model.Avaliacao;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Persistência no Cosmos DB (container particionado por {@code /dia}). */
public class CosmosAvaliacaoRepository implements AvaliacaoRepository {

    private final CosmosContainer container;

    public CosmosAvaliacaoRepository(CosmosClient client, String database, String containerName) {
        this.container = client.getDatabase(database).getContainer(containerName);
    }

    @Override
    public void salvar(Avaliacao a) {
        Map<String, Object> doc = new LinkedHashMap<>();
        doc.put("id", a.id());
        doc.put("dia", a.dia());
        doc.put("descricao", a.descricao());
        doc.put("nota", a.nota());
        doc.put("urgencia", a.urgencia().name());
        doc.put("dataEnvio", a.dataEnvio());
        container.createItem(doc, new PartitionKey(a.dia()), null);
    }

    @Override
    public List<Avaliacao> listarPorPeriodo(LocalDate de, LocalDate ate) {
        SqlQuerySpec query = new SqlQuerySpec(
                "SELECT * FROM c WHERE c.dia >= @de AND c.dia <= @ate",
                new SqlParameter("@de", de.toString()),
                new SqlParameter("@ate", ate.toString()));
        return container.queryItems(query, new CosmosQueryRequestOptions(), Avaliacao.class).stream().toList();
    }
}
