// Azure Communication Services Email com domínio gerenciado pelo Azure.
param prefix string
param tags object
@description('Região dos dados (deve ser a mesma no Email Service e no Communication Service).')
param dataLocation string = 'Brazil'

resource emailService 'Microsoft.Communication/emailServices@2023-04-01' = {
  name: 'email-${prefix}'
  location: 'global'
  tags: tags
  properties: { dataLocation: dataLocation }
}

resource domain 'Microsoft.Communication/emailServices/domains@2023-04-01' = {
  parent: emailService
  name: 'AzureManagedDomain'
  location: 'global'
  tags: tags
  properties: { domainManagement: 'AzureManaged' }
}

resource communication 'Microsoft.Communication/communicationServices@2023-04-01' = {
  name: 'acs-${prefix}'
  location: 'global'
  tags: tags
  properties: {
    dataLocation: dataLocation
    linkedDomains: [ domain.id ]
  }
}

output communicationName string = communication.name
output senderAddress string = 'DoNotReply@${domain.properties.mailFromSenderDomain}'
