using '../main.bicep'

param projectName = 'website'
param environment = 'dev'
param regionCode = 'scus'
param instanceNumber = '001'

param platformResourceGroup = 'rg-mgmt-dev'
param appServicePlanName = 'asp-mgmt-dev-scus-001'
param managedIdentityName = 'umi-mgmt-dev-scus-ctl'
param appInsightsName = 'ai-mgmt-dev-scus-001'

param tags = {
  ManagedBy: 'https://github.com/Acestus/website'
  CreatedBy: 'acestus'
  Environment: 'dev'
  Project: 'acestus.com'
}
