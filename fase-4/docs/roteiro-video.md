# Roteiro do vídeo de demonstração (8 a 10 minutos)

O enunciado exige mostrar: **aplicação funcionando**, **funções serverless ativas** e **configurações do projeto**.
Cada cena abaixo indica o que mostrar, o que dizer e o critério do enunciado que ela cobre.

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

## Pontos que o avaliador vai procurar

- O enunciado cita `POST /avaliação`; a rota implementada é `POST /api/avaliacao` (explicado no README §4). Diga isso em uma frase na cena 3.
- Mostrar as **4 funções ativas** no portal, e não só o resultado.
- Mostrar a segurança: Managed Identity, Key Vault, RBAC, chaves locais do Cosmos desligadas.
- Mostrar o monitoramento funcionando, com o alerta de verdade e não só a configuração.

## Depois de gravar

- [ ] Publicar como **não listado** (YouTube) e testar o link em janela anônima.
- [ ] Colocar o link na seção 12 do README (via PR, pois a `main` é protegida).
- [ ] Só remover a infra (`az group delete -n rg-feedback`) depois que o vídeo for aprovado.
