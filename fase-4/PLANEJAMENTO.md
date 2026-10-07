# Planejamento de Entrega — Tech Challenge Fase 4

**Prazo final: 17/11/2026** (janela de entrega: 09/09 a 17/11/2026). Meta interna de entrega: **16/11**, com folga de 24 h.

**Stack decidida:** Azure · Azure Functions (Java 21 + Maven) · Cosmos DB serverless · Communication Services Email · Application Insights · Bicep · GitHub Actions.

## Calendário (a partir de 06/10/2026)

| Período | Fases | Marco |
|---|---|---|
| 06/10 – 11/10 | 0 | Repositório, assinatura Azure, orçamento e decisões fechadas |
| 12/10 – 25/10 | 1, 2, 3 | Infra base, segurança e as 3 funções funcionando |
| 26/10 – 01/11 | 4 | Pipeline de deploy automatizado comprovado |
| 02/11 – 06/11 | 5 | Monitoramento e alerta real testado |
| 07/11 – 10/11 | 6 | Testes E2E e documentação completa |
| 11/11 – 13/11 | 7 | Vídeo gravado e publicado |
| 14/11 – 16/11 | 8 | Revisão final e submissão (buffer até 17/11) |

Congelamento de funcionalidades: **10/11**. Depois disso, só correções e documentação.

## Mapa requisito → entrega

| Requisito do enunciado | Como será atendido | Fase |
|---|---|---|
| Cloud configurada, segurança e governança | Managed Identity, RBAC, Key Vault, tags, chave de função, TLS | 1–2 |
| Componentes de suporte (bancos etc.) | Cosmos DB, Communication Services, Key Vault, Action Group | 1 |
| Deploy automatizado | GitHub Actions + Bicep + functions-action | 4 |
| Aplicação monitorada | Application Insights, workbook, alertas | 5 |
| Notificação aos admins (críticos) | Função `notificar_urgencia` + e-mail; alertas via Action Group | 3, 5 |
| Relatório semanal com média | Função `gerar_relatorio_semanal` (Timer trigger) | 3 |
| ≥ 2 funções, responsabilidade única | 3 funções distintas | 3 |
| Repositório aberto | GitHub público | 0 |
| Vídeo de demonstração | Roteiro e gravação | 7 |

## Fases e tarefas

### Fase 0 — Fundação (06/10 – 11/10)
- [x] Decidir cloud: **Azure**
- [ ] Confirmar assinatura (Azure for Students ou outra) e saldo; ver regiões/SKUs permitidos
- [ ] Definir equipe, papéis e canal de comunicação
- [ ] Criar repositório **público** no GitHub; `main` protegida; PRs obrigatórios; `.gitignore` (`*.bicepparam`, `local.settings.json`, `.env`, `target/`)
- [ ] Criar orçamento (Cost Management) com alerta
- [ ] Criar estrutura de pastas e primeiro commit

### Fase 1 — Infraestrutura base (Bicep)
- [ ] Resource group `rg-feedback` com tags
- [ ] Cosmos DB (serverless), database + container `avaliacoes` (partition key `/dia`)
- [ ] Communication Services + Email Service + domínio gerenciado Azure; anotar o remetente
- [ ] Function App (Linux, Consumption/Flex, Java 21) + Storage Account
- [ ] Key Vault
- [ ] Log Analytics + Application Insights
- [ ] Parâmetros: e-mails admin, limiares de urgência, região

### Fase 2 — Segurança e governança
- [ ] Managed Identity do Function App com roles mínimas (Cosmos data, Key Vault, envio de e-mail)
- [ ] Desabilitar chaves locais do Cosmos DB; HTTPS only, TLS 1.2, FTP off
- [ ] Segredos no Key Vault com referências nas App Settings
- [ ] App registration/federated credential para GitHub OIDC (**risco:** assinaturas de estudante podem bloquear; plano B: service principal com secret no GitHub)
- [ ] Revisão: nenhum segredo no repositório

### Fase 3 — Funções serverless
- [ ] Criar `functions/pom.xml` (azure-functions-java-library, azure-cosmos, azure-communication-email, azure-identity) e `host.json`
- [ ] Núcleo: `Urgencia`/`Avaliacao`, serviço de classificação, repositório Cosmos, serviço de e-mail (com testes)
- [ ] `receber_avaliacao` (HTTP): validação, gravação, 201/400
- [ ] `notificar_urgencia` (Cosmos DB trigger): ignora não críticas, envia e-mail, idempotente
- [ ] `gerar_relatorio_semanal` (Timer `0 0 11 * * 1` UTC = segunda 08:00 BRT): consulta 7 dias, média, contagem por dia e por urgência, e-mail
- [ ] Permitir execução manual do relatório (HTTP admin ou `func` manual) para demonstração
- [ ] Testes unitários (JUnit 5, Mockito) com cobertura mínima de 80% nas regras de negócio (JaCoCo)
- [ ] Teste local com `mvn azure-functions:run` + conta de dev

### Fase 4 — CI/CD
- [ ] `fase-4-ci.yml` (em `.github/workflows/` na raiz do repo; `paths: fase-4/**`): `mvn verify`, `az bicep build`, `what-if`
- [ ] `fase-4-deploy.yml` (`working-directory: fase-4`): deploy da infra + `Azure/functions-action`
- [ ] Provar o fluxo: alterar uma função → push → deploy automático

### Fase 5 — Monitoramento e alertas
- [ ] Workbook/dashboard (invocações, falhas, duração, 4xx/5xx)
- [ ] Alertas (já escritos em `alerts.bicep`): falhas de função, 5xx, orçamento → Action Group por e-mail
- [ ] Simular falha e comprovar o e-mail de alerta

### Fase 6 — Testes ponta a ponta e documentação
- [ ] E2E: enviar avaliações com notas 1, 5, 9; só a crítica gera e-mail
- [ ] Relatório com dados de vários dias; validar números
- [ ] Preencher README (seções 10, 12, 13), diagrama em `docs/` e prints
- [ ] Revisão cruzada de código e documentação

### Fase 7 — Vídeo de demonstração (~8–10 min)
1. **Contexto e arquitetura** (1 min): modelo de cloud e componentes
2. **Portal Azure** (2 min): Function App com as 3 funções ativas, Cosmos DB, Key Vault, Managed Identity/RBAC
3. **Fluxo ao vivo** (3 min): nota alta (sem e-mail), nota baixa (e-mail de urgência), payload inválido (400)
4. **Relatório semanal** (1 min): execução manual + e-mail
5. **CI/CD** (1 min): commit → pipeline → função atualizada
6. **Monitoramento** (1 min): Application Insights + alerta disparado
7. **Encerramento**: custos e remoção do resource group

- [ ] Gravar com dados preparados; publicar (YouTube não listado) e colocar o link no README
- [ ] Só remover a infra após o vídeo aprovado

### Fase 8 — Entrega
- [ ] Repositório público verificado em janela anônima
- [ ] README completo, link do vídeo funcionando
- [ ] Submeter na plataforma antes do prazo

## Divisão sugerida (ajustar ao tamanho do grupo)

| Papel | Escopo |
|---|---|
| Infra/Segurança | Fases 1, 2, 5 |
| Backend | Fase 3 |
| DevOps/QA | Fases 4, 6 |
| Documentação/Vídeo | Fases 6, 7, 8 |

## Riscos e mitigação

| Risco | Mitigação |
|---|---|
| Assinatura de estudante com região/SKU restrito (Cosmos, Functions) | Testar criação dos recursos na Fase 0/1; ter região alternativa |
| Sem permissão para criar app registration (OIDC) | Plano B: service principal com secret |
| Limites do domínio gerenciado de e-mail (taxa, spam) | Poucos e-mails; avisar admins para checar spam; domínio próprio só se necessário |
| Cosmos trigger acorda a função em toda avaliação | Aceitável no volume; ignora não críticas no código |
| Estouro de créditos | Orçamento com alerta; remover o resource group após o vídeo |
| Relatório semanal difícil de demonstrar | Execução manual e parâmetro de janela em dias |
| Segredos em repo público | `.gitignore`, Key Vault, OIDC, revisão antes do 1º push |

## Definição de pronto

- Todos os requisitos do mapa atendidos e demonstrados no vídeo
- Três funções com responsabilidade única, testadas e documentadas
- Deploy automatizado comprovado
- Alerta real recebido por e-mail
- README cobre arquitetura, deploy, monitoramento, funções e segurança
