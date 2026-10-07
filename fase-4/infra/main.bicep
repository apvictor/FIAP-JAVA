// Plataforma de feedback — Tech Challenge Fase 4.
// Escopo: resource group.  Uso: az deployment group create -g <rg> -f main.bicep -p main.bicepparam
targetScope = 'resourceGroup'

@description('Prefixo curto (minúsculas) usado nos nomes dos recursos.')
@minLength(3)
@maxLength(10)
param prefix string = 'feedback'

param location string = resourceGroup().location

@description('E-mails dos administradores (alertas de urgência, relatório semanal e alarmes).')
param adminEmails array

@description('Notas de 0 até este valor são CRITICA.')
param urgenciaCriticaAte int = 3

@description('Notas acima do limiar crítico e até este valor são MEDIA; acima disso, BAIXA.')
param urgenciaMediaAte int = 6

@description('Orçamento mensal em USD (0 desabilita).')
param budgetAmount int = 0

param tags object = {
  projeto: 'feedback-aulas'
  ambiente: 'demo'
  responsavel: 'tech-challenge-fase4'
}

var suffix = uniqueString(resourceGroup().id)
var name = '${prefix}-${suffix}'

module monitoring 'modules/monitoring.bicep' = {
  name: 'monitoring'
  params: { location: location, prefix: prefix, tags: tags }
}

module cosmos 'modules/cosmos.bicep' = {
  name: 'cosmos'
  params: { location: location, accountName: 'cosmos-${name}', tags: tags }
}

module communication 'modules/communication.bicep' = {
  name: 'communication'
  params: { prefix: prefix, tags: tags }
}

module keyvault 'modules/keyvault.bicep' = {
  name: 'keyvault'
  params: {
    location: location
    vaultName: 'kv-${take(prefix, 6)}-${take(suffix, 10)}'
    tags: tags
    communicationName: communication.outputs.communicationName
    adminEmails: join(adminEmails, ',')
  }
}

module functionapp 'modules/functionapp.bicep' = {
  name: 'functionapp'
  params: {
    location: location
    appName: 'func-${name}'
    storageName: 'st${take(prefix, 6)}${take(suffix, 12)}'
    tags: tags
    appInsightsConnectionString: monitoring.outputs.appInsightsConnectionString
    cosmosEndpoint: cosmos.outputs.endpoint
    cosmosDatabase: cosmos.outputs.databaseName
    cosmosContainer: cosmos.outputs.containerName
    cosmosLeasesContainer: cosmos.outputs.leasesContainerName
    senderAddress: communication.outputs.senderAddress
    acsSecretUri: keyvault.outputs.acsSecretUri
    adminEmailsSecretUri: keyvault.outputs.adminEmailsSecretUri
    urgenciaCriticaAte: urgenciaCriticaAte
    urgenciaMediaAte: urgenciaMediaAte
  }
}

module rbac 'modules/rbac.bicep' = {
  name: 'rbac'
  params: {
    principalId: functionapp.outputs.principalId
    cosmosAccountName: cosmos.outputs.accountName
    cosmosDatabaseName: cosmos.outputs.databaseName
    keyVaultName: keyvault.outputs.vaultName
  }
}

module alerts 'modules/alerts.bicep' = {
  name: 'alerts'
  params: {
    location: location
    prefix: prefix
    tags: tags
    adminEmails: adminEmails
    functionAppId: functionapp.outputs.appId
    appInsightsId: monitoring.outputs.appInsightsId
    budgetAmount: budgetAmount
  }
}

output functionAppName string = functionapp.outputs.appName
output functionAppHost string = functionapp.outputs.hostName
output cosmosAccount string = cosmos.outputs.accountName
output senderAddress string = communication.outputs.senderAddress
