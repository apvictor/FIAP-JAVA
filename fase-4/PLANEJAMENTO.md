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
> Adiantado: o código das Fases 1 (Bicep) e 3 (funções Java) já está escrito e validado localmente; falta implantar e testar no Azure.

- [x] Decidir cloud: **Azure**
- [x] Assinatura: **Azure for Students**, US$ 100, expira em 07/10/2027, tenant **SENAC.BR** (conta `armando.vpsantos@sp.senac.br`)
- [x] Verificação da assinatura (1ª rodada): regiões permitidas por policy = `brazilsouth`, `italynorth`, `belgiumcentral`, `chilecentral`, `francecentral` (**usar `brazilsouth`**); criação de app registration **permitida** (OIDC viável); providers não registrados
- [x] Providers registrados e verificação concluída (2ª rodada): Cosmos DB serverless criado com sucesso em `brazilsouth`. **Não verificado ainda:** Function App Linux Consumption e Communication Services Email (só aparecem no primeiro deploy)
- [x] Equipe: projeto **individual** (Armando Victor Pereira Santos); todos os papéis ficam com uma pessoa
- [x] Repositório no GitHub criado e `.gitignore` configurado (`*.bicepparam`, `local.settings.json`, `.env`, `target/`)
- [x] `main` protegida por ruleset `proteger-main` (PR obrigatório com 0 aprovações, sem force push, sem exclusão)
- [x] Repositório público (verificado pela API do GitHub)
- [x] Sem colaboradores (projeto individual)
- [x] Orçamento `orcamento-fase4` criado (US$ 25/mês, 01/10 a 30/12/2026). Conferir escopo = assinatura e alertas de 50% e 80%
- [x] Criar estrutura de pastas e primeiro commit

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
- [x] Criar `functions/pom.xml` (azure-functions-java-library, azure-cosmos, azure-communication-email, azure-identity) e `host.json`
- [x] Núcleo: `Urgencia`/`Avaliacao`, serviço de classificação, repositório Cosmos, serviço de e-mail (com testes)
- [x] `receber_avaliacao` (HTTP): validação, gravação, 201/400
- [x] `notificar_urgencia` (Cosmos DB trigger): ignora não críticas, envia e-mail, idempotente
- [x] `gerar_relatorio_semanal` (Timer `0 0 11 * * 1` UTC = segunda 08:00 BRT): consulta 7 dias, média, contagem por dia e por urgência, e-mail
- [x] Execução manual do relatório (`gerar_relatorio_manual`) para demonstração
- [x] Testes unitários (JUnit 5, fakes em memória) com cobertura mínima de 80% nas regras de negócio (JaCoCo)
- [ ] Teste local com `mvn azure-functions:run` + conta de dev (depende da infra implantada)

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

## Papéis

Projeto individual: Infra/Segurança, Backend, DevOps/QA e Documentação/Vídeo ficam com uma pessoa.
Para caber no prazo, a ordem de prioridade é: (1) infra + funções funcionando, (2) deploy automatizado,
(3) monitoramento e alerta real, (4) documentação, (5) vídeo. Itens opcionais só se sobrar tempo.

## Riscos e mitigação

| Risco | Mitigação |
|---|---|
| Assinatura de estudante com região/SKU restrito (Cosmos, Functions) | Testar criação dos recursos na Fase 0/1; ter região alternativa |
| App registration (OIDC) no tenant do Senac | Verificado: permitido. Se mudar, plano B: `publish-profile` do Function App como secret no GitHub |
| Regiões limitadas por policy (5 regiões) e possível falta de capacidade do Cosmos/Functions em `brazilsouth` | Alternativas permitidas: `chilecentral`, `francecentral`, `italynorth`, `belgiumcentral` |
| Carga de trabalho concentrada em uma pessoa | Seguir a ordem de prioridade; congelar funcionalidades em 10/11; adiantar o que for possível antes dos marcos |
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
