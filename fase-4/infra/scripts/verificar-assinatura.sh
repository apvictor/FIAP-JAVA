#!/usr/bin/env bash
# Verifica se a assinatura permite criar o que o projeto precisa. Não altera nada além de
# um resource group temporário (removido ao final) e um app registration de teste (removido).
# Uso: az login && ./infra/scripts/verificar-assinatura.sh [regiao]
set -uo pipefail

REGIAO="${1:-brazilsouth}"
RG="rg-teste-viabilidade-$RANDOM"
ok()   { printf '  [ok]   %s\n' "$1"; }
falha(){ printf '  [FALHA] %s\n' "$1"; }

echo "Assinatura atual:"
az account show --query '{nome:name, id:id, tenant:tenantId, estado:state}' -o table || exit 1

echo; echo "1) Providers de recursos (registra e aguarda; pode levar alguns minutos)"
for ns in Microsoft.DocumentDB Microsoft.Web Microsoft.Communication Microsoft.KeyVault \
          Microsoft.Insights Microsoft.OperationalInsights Microsoft.Storage Microsoft.Consumption; do
  az provider register -n "$ns" --wait >/dev/null 2>&1
  estado=$(az provider show -n "$ns" --query registrationState -o tsv 2>/dev/null)
  if [ "$estado" = "Registered" ]; then ok "$ns"; else falha "$ns ($estado)"; fi
done

echo; echo "2) Azure Policy com restrição de região"
az policy assignment list --query "[?contains(to_string(parameters), 'listOfAllowedLocations')].{nome:displayName, regioes:parameters.listOfAllowedLocations.value}" -o json

# Remove o resource group temporário mesmo se o script for interrompido (Ctrl+C) ou falhar.
limpar() { az group exists -n "$RG" 2>/dev/null | grep -q true && { echo "Removendo $RG..."; az group delete -n "$RG" --yes --no-wait; }; }
trap limpar EXIT

echo; echo "3) Criação de recursos em '$REGIAO' (resource group temporário $RG)"
echo "   O Cosmos DB leva de 3 a 10 minutos para criar. Não interrompa."
if az group create -n "$RG" -l "$REGIAO" -o none 2>/dev/null; then
  ok "resource group"
  az storage account check-name -n "stviab$RANDOM$RANDOM" --query nameAvailable -o tsv >/dev/null 2>&1 && ok "consulta de Storage"
  if az cosmosdb create -n "cosmos-viab-$RANDOM" -g "$RG" --capabilities EnableServerless --locations regionName="$REGIAO" -o none 2>/tmp/cosmos.err; then
    ok "Cosmos DB serverless"
  else
    falha "Cosmos DB serverless:"; head -c 600 /tmp/cosmos.err; echo
  fi
else
  falha "não foi possível criar resource group em $REGIAO (política de região?)"
fi

echo; echo "4) Permissão para criar app registration (necessária para OIDC do GitHub)"
if id=$(az ad app create --display-name "teste-viabilidade-$RANDOM" --query appId -o tsv 2>/dev/null); then
  ok "app registration permitido"; az ad app delete --id "$id"
else
  falha "sem permissão -> usar publish profile no GitHub Actions (ver PLANEJAMENTO.md)"
fi
echo; echo "Concluído. Envie esta saída para ajustar o plano."
