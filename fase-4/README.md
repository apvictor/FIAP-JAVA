# Plataforma de Feedback de Aulas — Tech Challenge Fase 4 (FIAP Pós Tech)

Plataforma serverless no Azure onde estudantes avaliam aulas e administradores recebem
alertas de feedbacks críticos e um relatório semanal com médias e estatísticas.

> Prazo de entrega: 17/11/2026.
> Status: planejamento. Veja [PLANEJAMENTO.md](PLANEJAMENTO.md) para o plano de entrega.
> Itens marcados com `TODO` serão preenchidos durante a implementação.

## 1. Modelo de cloud escolhido

| Decisão | Escolha | Justificativa |
|---|---|---|
| Provedor | **Microsoft Azure** | Alinhado ao conteúdo da fase (Azure Functions, Application Insights) e ao crédito disponível |
| Modelo de serviço | **FaaS (Azure Functions) + serviços gerenciados (PaaS)** | Sem servidores para operar; paga-se por uso |
| Plano de execução | **Consumption (ou Flex Consumption)** | Escala a zero; custo próximo de zero neste volume |
| Runtime | **Java 21** + Maven, Azure Functions Java | Linguagem da pós-graduação; suporte oficial no Functions |
| IaC | **Bicep** | Nativo do Azure; dispensa backend de state |
| CI/CD | **GitHub Actions** (login OIDC no Azure) | Deploy automatizado sem segredos de longa duração |

## 2. Arquitetura

```
 Estudante
    │ POST /api/avaliacao  (x-functions-key)
    ▼
┌─────────────────────────────┐
│ λ receber_avaliacao         │  HTTP trigger
│ valida, classifica, grava   │
└──────────────┬──────────────┘
               ▼
┌─────────────────────────────┐
│ Cosmos DB (NoSQL, serverless)│  container `avaliacoes`, partition key /dia
│ Change Feed                  │
└───────┬─────────────┬───────┘
 Change Feed          │ Query últimos 7 dias
        ▼             ▼
┌────────────────┐  ┌──────────────────────────┐
│ λ notificar_   │  │ λ gerar_relatorio_semanal│  Timer trigger
│ urgencia       │  │ (segunda 08:00 BRT)      │
│ Cosmos trigger │  └────────────┬─────────────┘
└───────┬────────┘               │
        ▼                        ▼
 Azure Communication Services (Email) ──▶ e-mail aos administradores

 Application Insights + Log Analytics ──▶ Alertas (Azure Monitor) ──▶ Action Group ──▶ e-mail dos admins
 Key Vault + Managed Identity ──▶ acesso sem senhas entre os serviços
```

### Componentes e responsabilidade única

| Componente | Responsabilidade (única) |
|---|---|
| `receber_avaliacao` | Validar payload, classificar urgência, persistir. **Não** envia e-mail. |
| `notificar_urgencia` | Reagir ao Change Feed e enviar e-mail de urgência para avaliações críticas. |
| `gerar_relatorio_semanal` | Agregar a semana e enviar o relatório. |
| Cosmos DB | Persistência; o Change Feed desacopla recebimento de notificação. |
| Communication Services Email | Envio de e-mails. |
| Application Insights / Azure Monitor | Telemetria, dashboard e alertas operacionais. |
| Key Vault | Segredos e configurações sensíveis (ex.: lista de e-mails dos admins). |

As três funções ficam em um único Function App (um deploy, uma Managed Identity). A separação de
responsabilidades está no código e nos gatilhos; cada função é uma classe independente em `functions/src/main/java/.../function/`.

## 3. Regras de negócio

- Urgência derivada da nota: **0–3 = CRITICA**, **4–6 = MEDIA**, **7–10 = BAIXA** (limiares configuráveis).
- Apenas `CRITICA` dispara e-mail imediato.

## 4. API

### `POST /api/avaliacao`

Header: `x-functions-key: <chave>`

```json
{ "descricao": "A aula foi confusa e o áudio estava ruim", "nota": 2 }
```

| Status | Significado |
|---|---|
| `201` | Avaliação registrada: `{ "id": "...", "urgencia": "CRITICA" }` |
| `400` | `descricao` vazia/ausente (máx. 1000 caracteres) ou `nota` não inteira fora de 0–10 |
| `401` | Chave ausente/inválida |

### Modelo de dados (Cosmos DB, container `avaliacoes`)

| Atributo | Tipo | Observação |
|---|---|---|
| `id` | string | UUID |
| `dia` | string | `YYYY-MM-DD` — **partition key** |
| `descricao` | string | |
| `nota` | number | 0–10 |
| `urgencia` | string | `CRITICA` / `MEDIA` / `BAIXA` |
| `dataEnvio` | string | ISO-8601 UTC |

O relatório consulta os 7 dias anteriores por `dia`.

## 5. Conteúdo dos e-mails

**Urgência:** descrição, urgência, data de envio.

**Relatório semanal:** média das notas; lista de avaliações (descrição, urgência, data de envio);
quantidade de avaliações por dia; quantidade de avaliações por urgência.

## 6. Segurança e governança

- **Managed Identity** no Function App; acesso ao Cosmos DB via RBAC de dados (chaves locais desabilitadas) e ao Key Vault via RBAC.
- **Menor privilégio**: roles `Cosmos DB Built-in Data Contributor` restrita ao container, `Key Vault Secrets User`, e permissão de envio no Communication Services.
- **Sem segredos no CI**: GitHub Actions usa federação OIDC; segredos da aplicação ficam no Key Vault (referências nas App Settings).
- **Em trânsito**: HTTPS obrigatório, TLS mínimo 1.2, FTP desabilitado.
- **Em repouso**: criptografia padrão do Azure no Cosmos DB e no Storage.
- **Proteção da API**: chave de função (`authLevel: function`), validação rigorosa do payload, limite de tamanho do corpo.
- **Dados pessoais**: a API não coleta identificação do aluno; logs não gravam a `descricao` completa.
- **Governança**: tudo em um resource group com tags obrigatórias (`projeto`, `ambiente`, `responsavel`), retenção de logs de 30 dias, orçamento com alerta de custo, Activity Log ativo.

## 7. Monitoramento

- **Application Insights** (workspace-based) com logs estruturados e rastreamento distribuído.
- **Dashboard/Workbook**: invocações, falhas, duração, taxa de sucesso por função, requisições 4xx/5xx.
- **Alertas** (Azure Monitor → Action Group → e-mail dos admins), definidos em `infra/modules/alerts.bicep`:
  - HTTP 5xx ≥ 1 em 5 min (métrica do Function App)
  - Execução com falha ou exceção em qualquer função, em 5 min (consulta no Application Insights)
  - Orçamento mensal com aviso aos 80% (opcional, parâmetro `budgetAmount`)
  - Limitação: alertas de log aceitam janela de no máximo 2 dias, então "relatório semanal não executou" não é alertável; uma falha na execução dele cai no alerta de falhas.
- **Health**: endpoint de saúde opcional e teste de disponibilidade.

## 8. Estrutura do repositório

Este repositório reúne as fases do curso; este projeto fica em `fase-4/`.
Os workflows do GitHub Actions ficam na raiz do repositório (`.github/workflows/`, único local que o
GitHub reconhece) e usam `working-directory: fase-4` e filtro de caminho `fase-4/**`.

```
fase-4/
├── README.md
├── PLANEJAMENTO.md
├── functions/                   # projeto Maven (Azure Functions, Java 21)
│   ├── pom.xml
│   ├── host.json
│   └── src/
│       ├── main/java/br/com/fiap/feedback/
│       │   ├── function/        # ReceberAvaliacao, NotificarUrgencia, GerarRelatorioSemanal
│       │   ├── service/         # classificação de urgência, relatório, e-mail
│       │   ├── repository/      # acesso ao Cosmos DB
│       │   └── model/           # Avaliacao, Urgencia
│       └── test/java/br/com/fiap/feedback/   # JUnit 5
├── infra/                       # Bicep
│   ├── main.bicep
│   ├── main.bicepparam.example
│   └── modules/ (functionapp, cosmos, communication, keyvault, monitoring)
└── docs/                        # diagramas, prints, roteiro do vídeo

.github/workflows/               # na raiz do repositório
├── fase-4-ci.yml                # mvn verify + bicep build/what-if
└── fase-4-deploy.yml            # infra + publicação das funções
```

## 9. Deploy

Os comandos abaixo partem de `fase-4/`.

Pré-requisitos: assinatura Azure, Azure CLI, Azure Functions Core Tools v4, JDK 21 e Maven 3.9+.

```bash
az login
az group create -n rg-feedback -l brazilsouth --tags projeto=feedback
cp infra/main.bicepparam.example infra/main.bicepparam      # editar e-mails dos admins
az deployment group create -g rg-feedback -f infra/main.bicep -p infra/main.bicepparam
cd functions && mvn clean package azure-functions:deploy   # appName vem do output da infra
```

**Deploy automatizado:** push na `main` → `fase-4-ci.yml` (lint, testes, `bicep build`, `what-if`) →
`fase-4-deploy.yml` (implanta a infra e publica as funções com `Azure/functions-action`).
Variáveis do repositório: `AZURE_CLIENT_ID`, `AZURE_TENANT_ID`, `AZURE_SUBSCRIPTION_ID`.

### Testar

```bash
curl -X POST "https://<app>.azurewebsites.net/api/avaliacao" \
  -H "x-functions-key: $FUNCTION_KEY" -H "Content-Type: application/json" \
  -d '{"descricao":"Aula confusa","nota":2}'
```

Testes locais: `cd functions && mvn test`; execução local com `mvn clean package azure-functions:run`.

## 10. Documentação das funções

`TODO` — preencher por função: gatilho, entrada/saída, App Settings, permissões, timeout.

## 11. Custos

Consumption + Cosmos DB serverless + Communication Services têm custo desprezível neste volume.
Como os créditos são limitados, a demonstração é gravada em vídeo e o resource group pode ser
removido com `az group delete -n rg-feedback`.

## 12. Demonstração

Link do vídeo: `TODO`

## 13. Equipe

`TODO` — nomes e RMs.
