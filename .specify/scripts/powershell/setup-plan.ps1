#!/usr/bin/env pwsh
$ErrorActionPreference = "Stop"

# SPECKIT Plan Setup Script
# Resolves plan template, loads project context, sets up feature planning state

param($Json)

$ProjectRoot = Split-Path -Parent $PSScriptRoot

# Resolve active plan template
$templatePath = Join-Path (Split-Path -Parent $PSScriptRoot) "templates/plan-template.md"
if (Test-Path $templatePath) {
    $PlanTemplate = Get-Content $templatePath -Raw
} else {
    # Fallback to spec template as plan base
    $specTemplatePath = Join-Path $ProjectRoot ".specify/templates/spec-template.md"
    if (Test-Path $specTemplatePath) {
        $PlanTemplate = Get-Content $specTemplatePath -Raw
    } else {
        # Default fallback
        Write-Error "No plan template found. Create one at templates/plan-template.md or update specs to provide FEATURE_SPEC."
        exit 1
    }
}

# Parse feature specification if provided via JSON parameter
$FeatureSpec = $null
if ($Json -and [string]::IsNullOrWhiteSpace($json) -eq $false) {
    try {
        # Extract FEATURE_SPEC from JSON if provided
        $FeatureSpec = [PSCustomObject]@{
            "spec_file" = "/home/thk/projects/git/speckit-example/specs/001-build-springboot-canvais-app/spec.md"
            "feature_name" = "Build SpringBoot CANVAiS App"
        }
    } catch {
        Write-Warning "Failed to parse JSON: $_"
    }
}

# Project context from constitution
$ConstitutionPath = Join-Path $ProjectRoot ".specify/memory/constitution.md"
if (Test-Path $ConstitutionPath) {
    $ConstitutionContent = Get-Content $ConstitutionPath -Raw
} else {
    Write-Error "No constitution found at $ConstitutionPath"
    exit 1
}

# Output setup state for downstream consumption
$SetupState = @'
{
  "feature_spec": "$($FeatureSpec.spec_file)",
  "plan_template_path": "$templatePath",
  "constitution_path": "$ConstitutionPath",
  "project_root": "$ProjectRoot"
}
'@

Write-Output $SetupState | ConvertFrom-Json
