// Key Vault com RBAC. Guarda a connection string do e-mail e a lista de administradores.
param location string
param vaultName string
param tags object
param communicationName string
@description('Lista de e-mails dos administradores, separados por vírgula.')
@secure()
param adminEmails string

resource vault 'Microsoft.KeyVault/vaults@2023-07-01' = {
  name: vaultName
  location: location
  tags: tags
  properties: {
    tenantId: tenant().tenantId
    sku: { family: 'A', name: 'standard' }
    enableRbacAuthorization: true
    enableSoftDelete: true
    softDeleteRetentionInDays: 7
  }
}

resource communication 'Microsoft.Communication/communicationServices@2023-04-01' existing = {
  name: communicationName
}

resource acsSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: vault
  name: 'acs-connection-string'
  properties: { value: communication.listKeys().primaryConnectionString }
}

resource adminSecret 'Microsoft.KeyVault/vaults/secrets@2023-07-01' = {
  parent: vault
  name: 'admin-emails'
  properties: { value: adminEmails }
}

output vaultName string = vault.name
output acsSecretUri string = acsSecret.properties.secretUri
output adminEmailsSecretUri string = adminSecret.properties.secretUri
