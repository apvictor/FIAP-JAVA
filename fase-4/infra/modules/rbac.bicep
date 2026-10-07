// Permissões de menor privilégio da identidade do Function App.
param principalId string
param cosmosAccountName string
param cosmosDatabaseName string
param keyVaultName string

// Cosmos DB Built-in Data Contributor, restrito ao database da aplicação.
var cosmosDataContributor = '00000000-0000-0000-0000-000000000002'
// Key Vault Secrets User (somente leitura de segredos).
var kvSecretsUser = '4633458b-17de-408a-b874-0445c86b69e6'

resource cosmos 'Microsoft.DocumentDB/databaseAccounts@2024-05-15' existing = {
  name: cosmosAccountName
}

resource cosmosAssignment 'Microsoft.DocumentDB/databaseAccounts/sqlRoleAssignments@2024-05-15' = {
  parent: cosmos
  name: guid(cosmos.id, principalId, cosmosDataContributor)
  properties: {
    roleDefinitionId: '${cosmos.id}/sqlRoleDefinitions/${cosmosDataContributor}'
    principalId: principalId
    scope: '${cosmos.id}/dbs/${cosmosDatabaseName}'
  }
}

resource vault 'Microsoft.KeyVault/vaults@2023-07-01' existing = {
  name: keyVaultName
}

resource kvAssignment 'Microsoft.Authorization/roleAssignments@2022-04-01' = {
  scope: vault
  name: guid(vault.id, principalId, kvSecretsUser)
  properties: {
    roleDefinitionId: subscriptionResourceId('Microsoft.Authorization/roleDefinitions', kvSecretsUser)
    principalId: principalId
    principalType: 'ServicePrincipal'
  }
}
