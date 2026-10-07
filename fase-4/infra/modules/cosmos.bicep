// Cosmos DB (NoSQL, serverless). Autenticação por chave desabilitada: só Entra ID / RBAC.
param location string
param accountName string
param tags object
param databaseName string = 'feedback'
param containerName string = 'avaliacoes'
param leasesContainerName string = 'leases'

resource account 'Microsoft.DocumentDB/databaseAccounts@2024-05-15' = {
  name: accountName
  location: location
  tags: tags
  kind: 'GlobalDocumentDB'
  properties: {
    databaseAccountOfferType: 'Standard'
    capabilities: [ { name: 'EnableServerless' } ]
    consistencyPolicy: { defaultConsistencyLevel: 'Session' }
    locations: [ { locationName: location, failoverPriority: 0, isZoneRedundant: false } ]
    disableLocalAuth: true
    minimalTlsVersion: 'Tls12'
  }
}

resource database 'Microsoft.DocumentDB/databaseAccounts/sqlDatabases@2024-05-15' = {
  parent: account
  name: databaseName
  properties: { resource: { id: databaseName } }
}

resource avaliacoes 'Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers@2024-05-15' = {
  parent: database
  name: containerName
  properties: {
    resource: {
      id: containerName
      partitionKey: { paths: [ '/dia' ], kind: 'Hash' }
    }
  }
}

// Com chaves desabilitadas o trigger não consegue criar o container de leases sozinho.
resource leases 'Microsoft.DocumentDB/databaseAccounts/sqlDatabases/containers@2024-05-15' = {
  parent: database
  name: leasesContainerName
  properties: {
    resource: {
      id: leasesContainerName
      partitionKey: { paths: [ '/id' ], kind: 'Hash' }
    }
  }
}

output accountName string = account.name
output endpoint string = account.properties.documentEndpoint
output databaseName string = database.name
output containerName string = avaliacoes.name
output leasesContainerName string = leases.name
