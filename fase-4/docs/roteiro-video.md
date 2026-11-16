# Roteiro do vídeo de demonstração (8 a 10 minutos)

O enunciado exige mostrar: **aplicação funcionando**, **funções serverless ativas** e **configurações do projeto**.
A tabela resume cada cena e o critério do enunciado que ela cobre; as falas completas estão em [Falas por cena](#falas-por-cena).

## Antes de gravar

- [ ] Enviar algumas avaliações em dias diferentes nos dias anteriores, para o relatório mostrar variação por dia.
- [ ] Aguardar uns 5 minutos depois de qualquer deploy ou mudança de configuração: o host reinicia e a rota responde 404 nesse intervalo.
- [ ] Abrir em abas: portal Azure (conta do SENAC, diretório SENAC.BR), GitHub (Actions), caixa de e-mail dos administradores.
- [ ] Guardar a chave de função em variável de ambiente **fora da tela** (nunca exibir a chave no vídeo):
  ```bash
  APP=<functionAppName>
  export KEY=$(az functionapp keys list -g rg-feedback -n $APP --query functionKeys.default -o tsv)
  ```
- [ ] Testar o roteiro inteiro uma vez antes, de preferência no mesmo dia.

## Cenas

| # | Tempo | O que mostrar | Critério do enunciado |
|---|---|---|---|
| 1 | 0:00–1:00 | Contexto e arquitetura: README §1–§2 e `docs/arquitetura.md`. Dizer: Azure, Functions em Java 21, Cosmos DB, Communication Services, Key Vault. Explicar por que serverless e por que 4 funções com responsabilidade única | Modelo de cloud e componentes; arquitetura |
| 2 | 1:00–3:00 | Portal: Function App com as **4 funções ativas**; Cosmos DB (container `avaliacoes`); App Settings com **referências ao Key Vault**; IAM com a Managed Identity e seus roles; HTTPS only e TLS 1.2 | Funções ativas; configurações do projeto; segurança e governança |
| 3 | 3:00–6:00 | Fluxo ao vivo (comandos abaixo): nota 9 sem e-mail; nota 1 com e-mail de urgência; payload inválido com 400 | Funcionamento; e-mail de urgência (descrição, urgência, data de envio) |
| 4 | 6:00–7:00 | Relatório: `POST /api/relatorio?dias=30` e o e-mail recebido (média, por dia, por urgência, lista) | Relatório semanal com média; timer segunda 08:00 BRT |
| 5 | 7:00–8:00 | CI/CD: GitHub Actions com o `fase-4-ci` de um PR e o `fase-4-deploy` verdes; mencionar o login OIDC sem segredos | Deploy automatizado |
| 6 | 8:00–9:00 | Monitoramento: Workbook, os e-mails Fired e Resolved do alerta já recebidos, e o alerta de falhas no portal | Aplicação monitorada; notificações de problemas |
| 7 | 9:00–9:30 | Encerramento: custos (consumo, serverless) e remoção do resource group depois da avaliação | Explicação do modelo |

## Comandos da cena 3

```bash
URL=https://$APP.azurewebsites.net/api/avaliacao

# nota alta: grava e não envia e-mail
curl -s -X POST "$URL" -H "x-functions-key: $KEY" -H "Content-Type: application/json" \
  -d '{"descricao":"Aula excelente","nota":9}'

# nota baixa: grava e aciona o e-mail de urgência (Change Feed)
curl -s -X POST "$URL" -H "x-functions-key: $KEY" -H "Content-Type: application/json" \
  -d '{"descricao":"Aula confusa e áudio ruim","nota":1}'

# payload inválido: 400
curl -s -X POST "$URL" -H "x-functions-key: $KEY" -H "Content-Type: application/json" \
  -d '{"descricao":"sem nota"}'
```

O e-mail de urgência chega em cerca de 10 a 20 segundos. Se a espera ficar longa na gravação, mostre o log no
Application Insights (`traces`, função `notificar_urgencia`: "1 e-mail(s) de urgência") e volte ao e-mail na edição.

## Falas por cena

Texto para ler ou adaptar durante a gravação. São cerca de 840 palavras, uns 6 minutos e meio de fala em ritmo
normal; com a navegação no portal, os comandos e a espera dos e-mails, o vídeo fica entre 8 e 10 minutos. Entre colchetes está o que fazer na tela enquanto fala.

### Cena 1 — Contexto e arquitetura (0:00–1:00)

[Tela: README, seções 1 e 2, depois `docs/arquitetura.md` com o diagrama de visão geral]

> Olá, eu sou o Armando Victor Pereira Santos, RM372357, e este é o meu Tech Challenge da Fase 4, feito
> individualmente. O desafio é uma plataforma de feedback de aulas: o estudante envia uma avaliação, os
> administradores recebem um aviso quando ela é crítica e, toda semana, um relatório com a média das notas.
>
> Escolhi a Microsoft Azure com um modelo serverless: as funções rodam no Azure Functions, em Java 21, no plano
> de consumo, então não há servidor para administrar e o custo acompanha o uso. Os dados ficam no Cosmos DB
> serverless, os e-mails saem pelo Azure Communication Services, os segredos ficam no Key Vault e a
> telemetria no Application Insights. Toda a infraestrutura é código, em Bicep.
>
> São quatro funções, cada uma com uma responsabilidade única: uma recebe e grava a avaliação, outra reage
> às avaliações críticas e envia o e-mail, outra gera o relatório semanal e a última dispara esse mesmo
> relatório sob demanda, para demonstração.

### Cena 2 — Portal: funções e configurações (1:00–3:00)

[Tela: resource group `rg-feedback` com a lista de recursos e as tags]

> Este é o resource group do projeto, na região Brazil South. Todos os recursos têm tags de projeto,
> ambiente e responsável, o que ajuda na governança e no controle de custos.

[Tela: Function App → Visão geral → aba Functions]

> Aqui está o Function App, em execução, com as quatro funções ativas: `receber_avaliacao`, com gatilho
> HTTP; `notificar_urgencia`, disparada pelo Change Feed do Cosmos DB; `gerar_relatorio_semanal`, com
> gatilho de tempo toda segunda às oito da manhã no horário de Brasília; e `gerar_relatorio_manual`, também HTTP.

[Tela: Configurações → Variáveis de ambiente, rolando até as que têm referência ao Key Vault]

> Nas configurações do aplicativo não há nenhuma senha em texto. A connection string do serviço de e-mail e a
> lista de administradores são referências ao Key Vault. O endpoint do Cosmos DB é só o endereço: o acesso
> é feito pela identidade gerenciada do Function App.

[Tela: Configurações → Identidade, depois IAM do Cosmos DB ou do Key Vault]

> O Function App tem uma Managed Identity, e cada permissão segue o menor privilégio: acesso de dados ao
> banco do Cosmos e leitura de segredos no Key Vault. No Cosmos DB, as chaves locais estão desabilitadas,
> então só identidades autorizadas conseguem acessar os dados.

[Tela: Function App → Configuração → Configurações gerais (HTTPS only, TLS mínimo 1.2, FTP desabilitado)]

> Em trânsito, só HTTPS, com TLS mínimo 1.2, e o FTP está desligado. E a API exige uma chave de função:
> sem ela, a resposta é 401.

### Cena 3 — Fluxo ao vivo (3:00–6:00)

[Tela: terminal, com a chave já guardada na variável `KEY` fora da gravação]

> Agora o fluxo funcionando. O enunciado cita o endpoint `POST /avaliação`; aqui ele é `/api/avaliacao`,
> porque o Azure Functions acrescenta o prefixo `/api` e eu evitei acento na URL. O corpo é o mesmo:
> descrição e nota de zero a dez.

[Executar o comando da nota 9]

> Primeiro uma nota 9. A função valida, classifica e grava: a resposta é 201, com urgência BAIXA. Notas de
> sete a dez são baixas, de quatro a seis médias, e de zero a três críticas.

[Executar o comando da nota 1]

> Agora uma nota 1. Também 201, mas com urgência CRÍTICA. Repare que a função que recebe não envia e-mail:
> ela só grava. Quem reage é a `notificar_urgencia`, acionada pelo Change Feed do Cosmos DB. Isso desacopla
> o recebimento da notificação.

[Tela: caixa de e-mail; abrir o e-mail [URGENTE]]

> Em poucos segundos chega o e-mail de urgência aos administradores, com a descrição, a urgência e a data
> de envio, que são os dados pedidos no enunciado. A avaliação de nota 9 não gerou e-mail nenhum.

[Executar o comando do payload inválido]

> Por fim, um envio sem nota: a API rejeita com 400 e explica o motivo. A validação também recusa nota fora
> da faixa, nota com casas decimais e descrição vazia ou com mais de mil caracteres.

### Cena 4 — Relatório semanal (6:00–7:00)

[Executar `POST /api/relatorio?dias=30` e depois abrir o e-mail do relatório]

> O relatório semanal roda sozinho toda segunda-feira. Os logs mostram as execuções dos dias 19 e 26 de
> outubro e 2 de novembro. Para a demonstração, uso o gatilho manual, que chama exatamente o mesmo serviço.
>
> A resposta traz o total e a média. No e-mail estão a média das notas, a quantidade de avaliações por dia,
> a quantidade por urgência e a lista de avaliações com descrição, urgência e data de envio.

### Cena 5 — CI/CD (7:00–8:00)

[Tela: GitHub → Actions, com um `fase-4-ci` de PR e um `fase-4-deploy` verdes]

> O deploy é automatizado com GitHub Actions. Em cada pull request, o `fase-4-ci` roda os testes, com
> cobertura mínima nas regras de negócio, compila o Bicep e faz um what-if na infraestrutura. Quando o PR
> entra na main, o `fase-4-deploy` aplica a infraestrutura e publica as funções.
>
> O login no Azure usa OIDC, com uma identidade gerenciada federada ao repositório: não existe senha nem
> segredo de longa duração guardado no GitHub. E a branch main é protegida, então tudo passa por pull request.

### Cena 6 — Monitoramento (8:00–9:00)

[Tela: Application Insights → Workbooks → "Feedback — saúde das funções"]

> Para monitorar, há um Workbook no Application Insights com as invocações por função, as falhas, as
> exceções, a duração média e o percentil 95, e as respostas HTTP por código.

[Tela: caixa de e-mail com os alertas Fired e Resolved de 03/11]

> E há alertas: qualquer falha ou exceção numa função dispara um e-mail aos administradores. Eu testei isso
> provocando uma falha de verdade: o alerta disparou em cerca de cinco minutos e, quando o problema foi
> corrigido, chegou o aviso de resolução.

### Cena 7 — Encerramento (9:00–9:30)

[Tela: README ou diagrama]

> Com o plano de consumo e o Cosmos DB serverless, o custo neste volume é praticamente zero. Como os créditos
> de nuvem são limitados, depois da avaliação o resource group inteiro pode ser removido com um único
> comando, e recriado a partir do Bicep. Todo o código e a documentação estão no repositório. Obrigado!

## Pontos que o avaliador vai procurar

- O enunciado cita `POST /avaliação`; a rota implementada é `POST /api/avaliacao` (explicado no README §4). Diga isso em uma frase na cena 3.
- Mostrar as **4 funções ativas** no portal, e não só o resultado.
- Mostrar a segurança: Managed Identity, Key Vault, RBAC, chaves locais do Cosmos desligadas.
- Mostrar o monitoramento funcionando, com o alerta de verdade e não só a configuração.

## Depois de gravar

- [ ] Publicar como **não listado** (YouTube) e testar o link em janela anônima.
- [ ] Colocar o link na seção 12 do README (via PR, pois a `main` é protegida).
- [ ] Só remover a infra (`az group delete -n rg-feedback`) depois que o vídeo for aprovado.
