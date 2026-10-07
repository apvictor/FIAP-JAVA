// Action Group (e-mail dos admins), alertas de erro e orçamento opcional.
param location string
param prefix string
param tags object
param adminEmails array
param functionAppId string
param appInsightsId string
@description('Valor mensal do orçamento em USD. 0 desabilita o orçamento.')
param budgetAmount int = 0
param budgetStartDate string = utcNow('yyyy-MM-01')

resource actionGroup 'Microsoft.Insights/actionGroups@2023-01-01' = {
  name: 'ag-${prefix}-admins'
  location: 'global'
  tags: tags
  properties: {
    groupShortName: 'feedback'
    enabled: true
    emailReceivers: [for (email, i) in adminEmails: {
      name: 'admin${i}'
      emailAddress: email
      useCommonAlertSchema: true
    }]
  }
}

resource http5xx 'Microsoft.Insights/metricAlerts@2018-03-01' = {
  name: 'alert-${prefix}-http-5xx'
  location: 'global'
  tags: tags
  properties: {
    description: 'Respostas HTTP 5xx na API de avaliações.'
    severity: 1
    enabled: true
    scopes: [ functionAppId ]
    evaluationFrequency: 'PT5M'
    windowSize: 'PT5M'
    criteria: {
      'odata.type': 'Microsoft.Azure.Monitor.SingleResourceMultipleMetricCriteria'
      allOf: [
        {
          name: 'Http5xx'
          metricName: 'Http5xx'
          operator: 'GreaterThanOrEqual'
          threshold: 1
          timeAggregation: 'Total'
          criterionType: 'StaticThresholdCriterion'
        }
      ]
    }
    actions: [ { actionGroupId: actionGroup.id } ]
  }
}

resource failedExecutions 'Microsoft.Insights/scheduledQueryRules@2023-03-15-preview' = {
  name: 'alert-${prefix}-falhas-funcoes'
  location: location
  tags: tags
  properties: {
    displayName: 'Falhas em funções (${prefix})'
    description: 'Execuções de função com falha ou exceções nos últimos 5 minutos.'
    severity: 1
    enabled: true
    scopes: [ appInsightsId ]
    evaluationFrequency: 'PT5M'
    windowSize: 'PT5M'
    criteria: {
      allOf: [
        {
          query: 'union requests, exceptions | where (itemType == "request" and success == false) or itemType == "exception" | project timestamp, operation_Name'
          timeAggregation: 'Count'
          operator: 'GreaterThanOrEqual'
          threshold: 1
          failingPeriods: { numberOfEvaluationPeriods: 1, minFailingPeriodsToAlert: 1 }
        }
      ]
    }
    actions: { actionGroups: [ actionGroup.id ] }
  }
}

resource budget 'Microsoft.Consumption/budgets@2023-05-01' = if (budgetAmount > 0) {
  name: 'budget-${prefix}'
  properties: {
    category: 'Cost'
    amount: budgetAmount
    timeGrain: 'Monthly'
    timePeriod: { startDate: budgetStartDate }
    notifications: {
      aoAtingir80: {
        enabled: true
        operator: 'GreaterThanOrEqualTo'
        threshold: 80
        contactEmails: adminEmails
      }
    }
  }
}

output actionGroupId string = actionGroup.id
