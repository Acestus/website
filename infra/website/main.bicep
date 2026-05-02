targetScope = 'resourceGroup'

// ---------------------------------------------------------------------------
// Parameters
// ---------------------------------------------------------------------------

@minLength(2)
@maxLength(8)
@description('The project name used in resource naming conventions (e.g., website).')
param projectName string

@allowed([
  'dev'
  'stg'
  'prd'
])
@description('The deployment environment.')
param environment string

@minLength(3)
@maxLength(8)
@description('The Azure region code used in resource naming conventions (e.g., usw2).')
param regionCode string

@minLength(3)
@maxLength(3)
@description('The instance number used in resource naming conventions (e.g., 001).')
param instanceNumber string

@description('Tags to apply to all resources.')
param tags object

@description('The Azure region where resources will be deployed.')
param location string = resourceGroup().location

@description('Resource group containing shared platform resources (ASP, UMI, App Insights).')
param platformResourceGroup string

@description('Name of the existing App Service Plan in the platform resource group.')
param appServicePlanName string

@description('Name of the existing user-assigned managed identity for control-plane operations.')
param managedIdentityName string

@description('Name of the existing Application Insights instance in the platform resource group.')
param appInsightsName string

// ---------------------------------------------------------------------------
// Variables
// ---------------------------------------------------------------------------

var functionAppName = 'func-${projectName}-${environment}-${regionCode}-${instanceNumber}'
var storageAccountName = 'st${projectName}${environment}${regionCode}${instanceNumber}'

// ---------------------------------------------------------------------------
// References to existing platform resources in rg-mgmt-dev
// ---------------------------------------------------------------------------

resource existingAsp 'Microsoft.Web/serverFarms@2024-04-01' existing = {
  name: appServicePlanName
  scope: resourceGroup(platformResourceGroup)
}

resource existingUmi 'Microsoft.ManagedIdentity/userAssignedIdentities@2023-01-31' existing = {
  name: managedIdentityName
  scope: resourceGroup(platformResourceGroup)
}

resource existingAppInsights 'Microsoft.Insights/components@2020-02-02' existing = {
  name: appInsightsName
  scope: resourceGroup(platformResourceGroup)
}

// ---------------------------------------------------------------------------
// Storage Account (AVM)
// Holds Function App deployment artifacts.
// RBAC: Storage Blob Data Contributor → UMI (storage-scoped)
// ---------------------------------------------------------------------------

module storageAccount 'br/public:avm/res/storage/storage-account:0.32.0' = {
  name: 'deploy-${storageAccountName}'
  params: {
    name: storageAccountName
    location: location
    tags: tags
    skuName: 'Standard_LRS'
    kind: 'StorageV2'
    supportsHttpsTrafficOnly: true
    minimumTlsVersion: 'TLS1_2'
    allowBlobPublicAccess: false
    networkAcls: {
      defaultAction: 'Allow'
      bypass: 'AzureServices'
    }
    roleAssignments: [
      {
        roleDefinitionIdOrName: 'Storage Blob Data Owner'
        principalId: existingUmi.properties.principalId
        principalType: 'ServicePrincipal'
      }
      {
        roleDefinitionIdOrName: 'Storage Queue Data Contributor'
        principalId: existingUmi.properties.principalId
        principalType: 'ServicePrincipal'
      }
      {
        roleDefinitionIdOrName: 'Storage Account Contributor'
        principalId: existingUmi.properties.principalId
        principalType: 'ServicePrincipal'
      }
    ]
    blobServices: {
      containers: [
        {
          name: 'app-package-${functionAppName}'
          publicAccess: 'None'
        }
      ]
    }
  }
}

// ---------------------------------------------------------------------------
// Function App — Flex Consumption (native resource)
// AVM web/site does not yet expose functionAppConfig for Flex Consumption.
// Identity: UMI for deployment/storage access.
// ---------------------------------------------------------------------------

resource functionApp 'Microsoft.Web/sites@2024-04-01' = {
  name: functionAppName
  location: location
  tags: tags
  kind: 'functionapp,linux'
  identity: {
    type: 'UserAssigned'
    userAssignedIdentities: {
      '${existingUmi.id}': {}
    }
  }
  properties: {
    serverFarmId: existingAsp.id
    httpsOnly: true
    siteConfig: {
      appSettings: [
        { name: 'AzureWebJobsStorage__accountName', value: storageAccountName }
        { name: 'AzureWebJobsStorage__credential', value: 'managedidentity' }
        { name: 'AzureWebJobsStorage__clientId', value: existingUmi.properties.clientId }
          { name: 'FUNCTIONS_EXTENSION_VERSION', value: '~4' }
          { name: 'APPINSIGHTS_INSTRUMENTATIONKEY', value: existingAppInsights.properties.InstrumentationKey }
          { name: 'APPLICATIONINSIGHTS_CONNECTION_STRING', value: existingAppInsights.properties.ConnectionString }
          { name: 'AZURE_CLIENT_ID', value: existingUmi.properties.clientId }
      ]
    }
      functionAppConfig: {
      deployment: {
        storage: {
          type: 'blobContainer'
          value: '${storageAccount.outputs.primaryBlobEndpoint}app-package-${functionAppName}'
          authentication: {
            type: 'UserAssignedIdentity'
            userAssignedIdentityResourceId: existingUmi.id
          }
        }
      }
      scaleAndConcurrency: {
        maximumInstanceCount: 2
        instanceMemoryMB: 2048
      }
        runtime: {
          name: 'java'
          version: '21'
        }
      }
  }
}

// ---------------------------------------------------------------------------
// Outputs
// ---------------------------------------------------------------------------

@description('The name of the Function App.')
output functionAppName string = functionApp.name

@description('The default hostname of the Function App.')
output functionAppHostName string = functionApp.properties.defaultHostName

@description('The name of the storage account.')
output storageAccountName string = storageAccount.outputs.name
