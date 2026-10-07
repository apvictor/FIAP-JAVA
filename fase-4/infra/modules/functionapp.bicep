// Function App (Linux, Consumption, Java 21) com Managed Identity.
param location string
param appName string
param storageName string
param tags object
param appInsightsConnectionString string
param cosmosEndpoint string
param cosmosDatabase string
param cosmosContainer string
param cosmosLeasesContainer string
param senderAddress string
param acsSecretUri string
param adminEmailsSecretUri string
param urgenciaCriticaAte int = 3
param urgenciaMediaAte int = 6

resource storage 'Microsoft.Storage/storageAccounts@2023-05-01' = {
  name: storageName
  location: location
  tags: tags
  sku: { name: 'Standard_LRS' }
  kind: 'StorageV2'
  properties: {
    minimumTlsVersion: 'TLS1_2'
    allowBlobPublicAccess: false
    supportsHttpsTrafficOnly: true
  }
}

resource plan 'Microsoft.Web/serverfarms@2023-12-01' = {
  name: 'plan-${appName}'
  location: location
  tags: tags
  kind: 'linux'
  sku: { name: 'Y1', tier: 'Dynamic' }
  properties: { reserved: true }
}

var storageConnection = 'DefaultEndpointsProtocol=https;AccountName=${storage.name};EndpointSuffix=${environment().suffixes.storage};AccountKey=${storage.listKeys().keys[0].value}'

resource app 'Microsoft.Web/sites@2023-12-01' = {
  name: appName
  location: location
  tags: tags
  kind: 'functionapp,linux'
  identity: { type: 'SystemAssigned' }
  properties: {
    serverFarmId: plan.id
    httpsOnly: true
    siteConfig: {
      linuxFxVersion: 'JAVA|21'
      minTlsVersion: '1.2'
      ftpsState: 'Disabled'
      http20Enabled: true
      appSettings: [
        { name: 'AzureWebJobsStorage', value: storageConnection }
        { name: 'FUNCTIONS_EXTENSION_VERSION', value: '~4' }
        { name: 'FUNCTIONS_WORKER_RUNTIME', value: 'java' }
        { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: appInsightsConnectionString }
        // Conexão do Cosmos por identidade (sem chave): <nome>__accountEndpoint
        { name: 'COSMOS_CONNECTION__accountEndpoint', value: cosmosEndpoint }
        { name: 'COSMOS_ENDPOINT', value: cosmosEndpoint }
        { name: 'COSMOS_DATABASE', value: cosmosDatabase }
        { name: 'COSMOS_CONTAINER', value: cosmosContainer }
        { name: 'COSMOS_LEASES_CONTAINER', value: cosmosLeasesContainer }
        { name: 'EMAIL_SENDER', value: senderAddress }
        { name: 'ACS_CONNECTION_STRING', value: '@Microsoft.KeyVault(SecretUri=${acsSecretUri})' }
        { name: 'ADMIN_EMAILS', value: '@Microsoft.KeyVault(SecretUri=${adminEmailsSecretUri})' }
        { name: 'URGENCIA_CRITICA_ATE', value: string(urgenciaCriticaAte) }
        { name: 'URGENCIA_MEDIA_ATE', value: string(urgenciaMediaAte) }
      ]
    }
  }
}

output appName string = app.name
output appId string = app.id
output principalId string = app.identity.principalId
output hostName string = app.properties.defaultHostName
