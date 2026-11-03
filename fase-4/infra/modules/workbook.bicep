// Workbook do Application Insights: invocações, falhas, duração e erros HTTP.
param location string
param prefix string
param tags object
param appInsightsId string

resource workbook 'Microsoft.Insights/workbooks@2023-06-01' = {
  name: guid(resourceGroup().id, 'workbook-${prefix}')
  location: location
  tags: tags
  kind: 'shared'
  properties: {
    displayName: 'Feedback — saúde das funções'
    category: 'workbook'
    sourceId: appInsightsId
    serializedData: loadTextContent('workbook.json')
  }
}

output workbookId string = workbook.id
