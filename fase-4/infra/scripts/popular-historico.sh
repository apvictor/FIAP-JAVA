#!/usr/bin/env bash
# Grava avaliações com datas dos dias anteriores, para o relatório mostrar variação por dia.
#
# A API (POST /api/avaliacao) sempre grava com a data atual, então este script escreve direto
# no Cosmos DB pela API REST, autenticado com Entra ID (a conta não aceita chaves).
#
# Uso (Cloud Shell ou máquina com az logado):
#   ./popular-historico.sh                 # 6 dias anteriores, sem notas críticas
#   INCLUIR_CRITICAS=1 ./popular-historico.sh
#
# Notas críticas (0–3) acionam o Change Feed e enviam e-mail de urgência AGORA, com data antiga;
# por isso ficam de fora por padrão.
set -euo pipefail

RG=${RG:-rg-feedback}
DB=${DB:-feedback}
CONTAINER=${CONTAINER:-avaliacoes}
DIAS=${DIAS:-6}
INCLUIR_CRITICAS=${INCLUIR_CRITICAS:-0}

ACCOUNT=$(az cosmosdb list -g "$RG" --query "[0].name" -o tsv)
[ -n "$ACCOUNT" ] || { echo "Nenhuma conta Cosmos em $RG" >&2; exit 1; }
ENDPOINT="https://$ACCOUNT.documents.azure.com"
echo "Conta: $ACCOUNT"

# Papel de dados (Built-in Data Contributor) para o usuário logado, restrito ao database.
ME=$(az ad signed-in-user show --query id -o tsv)
if [ -z "$(az cosmosdb sql role assignment list -g "$RG" -a "$ACCOUNT" \
      --query "[?principalId=='$ME' && ends_with(roleDefinitionId, '00000000-0000-0000-0000-000000000002')].id" -o tsv)" ]; then
  echo "Concedendo Cosmos DB Built-in Data Contributor ao usuário logado..."
  az cosmosdb sql role assignment create -g "$RG" -a "$ACCOUNT" \
    --role-definition-id 00000000-0000-0000-0000-000000000002 \
    --principal-id "$ME" --scope "/dbs/$DB" -o none
  echo "Aguardando propagação (60s)..."
  sleep 60
fi

TOKEN=$(az account get-access-token --resource "$ENDPOINT" --query accessToken -o tsv)
AUTH=$(python3 -c 'import sys,urllib.parse;print(urllib.parse.quote("type=aad&ver=1.0&sig="+sys.argv[1],safe=""))' "$TOKEN")

# descricao|nota  — notas variadas para dar média e urgências diferentes.
AMOSTRAS=(
  "Aula muito boa, exemplos claros|9"
  "Conteúdo bom, mas o ritmo foi rápido|6"
  "Gostei da parte prática com Azure Functions|10"
  "Faltou tempo para tirar dúvidas|5"
  "Explicação de Cosmos DB ajudou bastante|8"
  "Material desatualizado em alguns slides|4"
  "Ótima didática do professor|9"
  "Exercícios poderiam ser mais guiados|7"
  "Laboratório travou algumas vezes|5"
  "Aula objetiva e bem organizada|8"
)
CRITICAS=(
  "Áudio ruim, não foi possível acompanhar|2"
  "Aula cancelada sem aviso|1"
)
[ "$INCLUIR_CRITICAS" = "1" ] && AMOSTRAS+=("${CRITICAS[@]}")

urgencia() { if [ "$1" -le 3 ]; then echo CRITICA; elif [ "$1" -le 6 ]; then echo MEDIA; else echo BAIXA; fi; }

i=0; total=0
for d in $(seq "$DIAS" -1 1); do
  DIA=$(date -u -d "-$d day" +%F 2>/dev/null || date -u -v-"$d"d +%F)
  QTD=$(( (d * 7) % 4 + 1 ))   # 1 a 4 por dia, para variar o gráfico
  for _ in $(seq 1 "$QTD"); do
    IFS='|' read -r DESC NOTA <<< "${AMOSTRAS[$(( i % ${#AMOSTRAS[@]} ))]}"
    i=$((i + 1))
    HORA=$(printf '%02d:%02d:%02d' $((11 + RANDOM % 10)) $((RANDOM % 60)) $((RANDOM % 60)))
    DOC=$(python3 -c 'import json,sys,uuid
d,desc,nota,urg,hora=sys.argv[1:]
print(json.dumps({"id":str(uuid.uuid4()),"dia":d,"descricao":desc,"nota":int(nota),
  "urgencia":urg,"dataEnvio":f"{d}T{hora}Z"},ensure_ascii=False))' \
      "$DIA" "$DESC" "$NOTA" "$(urgencia "$NOTA")" "$HORA")
    STATUS=$(curl -s -o /dev/null -w '%{http_code}' -X POST \
      "$ENDPOINT/dbs/$DB/colls/$CONTAINER/docs" \
      -H "Authorization: $AUTH" \
      -H "x-ms-date: $(LC_ALL=C date -u '+%a, %d %b %Y %H:%M:%S GMT')" \
      -H "x-ms-version: 2018-12-31" \
      -H "x-ms-documentdb-partitionkey: [\"$DIA\"]" \
      -H "Content-Type: application/json" \
      --data-binary "$DOC")
    echo "$DIA nota=$NOTA -> HTTP $STATUS"
    [ "$STATUS" = "201" ] || { echo "Falha (403 = papel ainda propagando; tente de novo em 1–2 min)" >&2; exit 1; }
    total=$((total + 1))
  done
done
echo "Pronto: $total avaliações em $DIAS dias. Confira com POST /api/relatorio?dias=7"
