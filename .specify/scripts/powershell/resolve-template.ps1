#!/usr/bin/env pwsh
$ErrorActionPreference = "Stop"

# SPECKIT Constitution Template Resolver
# Resolves template placeholders, project overrides, and extension layers

function Get-Template {
    param($TemplateName = "constitution-template")

    $templatePath = Join-Path (Split-Path -Parent $PSScriptRoot) "$TemplateName.md"

    if (Test-Path $templatePath) {
        return Get-Content $templatePath -Raw
    } else {
        # Core template fallback
        return @'
# SPECKIT Constitution
# Version: 1.0.0
# RATIFICATION_DATE: TODO
# LAST_AMENDED_DATE: TODO

## Principles

### Principle 1: [PRINCIPLE_NAME]
[PRINCIPLE_DESCRIPTION]

**Rationale:** [RATIONALE_TEXT]

## Governance

### Amendment Procedure
[AFFECTIONATE_AMENDMENT_PROCEDURE]

### Versioning Policy
[VERSIONING_POLICY_TEXT]

### Compliance Review
[COMPLIANCE_REVIEW_EXPECTATIONS]
'@
    }
}

function Process-Constitution($Template, $ExistingContent) {

    if ($null -ne $ExistingContent) {
        # Parse existing content and preserve non-placeholder values
        $placeholders = @()
        $existingValues = @{ }

        # Extract placeholder names
        while ($Template.Contains('[')) {
            $match = [regex]::Match($Template, '(\[.*?\])')
            if ($match.Success) {
                $placeholders += $match.Groups[1].Value
            } else {
                break
            }
        }

        # Parse existing constitution for known values
        if (-not [string]::IsNullOrWhiteSpace($ExistingContent)) {
            $existingValues['VERSION'] = (Select-String -Pattern 'Version:\s*(\d+\.\d+\.\d+)' -Input $ExistingContent).Matches.Groups[1].Value
            $existingValues['RATIFICATION_DATE'] = (Select-String -Pattern 'RATIFICATION_DATE:\s*(\d{4}-\d{2}-\d{2})' -Input $ExistingContent).Matches.Groups[1].Value
            $existingValues['LAST_AMENDED_DATE'] = (Select-String -Pattern 'LAST_AMENDED_DATE:\s*(\d{4}-\d{2}-\d{2})' -Input $ExistingContent).Matches.Groups[1].Value
            $existingValues['CONSTITUTION_VERSION'] = $existingValues['VERSION']
        }
    } else {
        $placeholders = @('CONSTITUTION_VERSION', 'RATIFICATION_DATE', 'LAST_AMENDED_DATE')
        $existingValues = @{
            'VERSION' = '1.0.0'
            'RATIFICATION_DATE' = 'TODO'
            'LAST_AMENDED_DATE' = 'TODO'
            'CONSTITUTION_VERSION' = '1.0.0'
        }
    }

    $Template | ForEach-Object { $_ -replace '^# SPECKIT Constitution$', '' } -replace '^# Version:\s*\d+\.\d+\.\d+.*$|^# RATIFICATION_DATE:\s*.*\n', '' -replace '^# LAST_AMENDED_DATE:\s*.*$', '' > $null

    return @'
# SPECKIT Constitution
# CONSTITUTION_VERSION: [CONSTITUTION_VERSION]
# RATIFICATION_DATE: [RATIFICATION_DATE]
# LAST_AMENDED_DATE: [LAST_AMENDED_DATE]

## Principles

[PRINCIPLE_1]

[PRINCIPLE_2]

[PRINCIPLE_3]

## Governance

[AFFECTIONATE_AMENDMENT_PROCEDURE]

[VERSIONING_POLICY_TEXT]

[COMPLIANCE_REVIEW_EXPECTATIONS]
'@
}

# Main execution
$TemplateContent = Get-Template -TemplateName "constitution-template"
$content = Process-Constitution $TemplateContent -None

$Output = @'
---
name: constitution
description: SPECKIT Project Governance and Principles
metadata:
  type: reference
---

''@

if ($null -ne $ExistingContent) {
    # Find version line
    if ([regex]::Match($ExistingContent, '(?m)^# CONSTITUTION_VERSION:\s*(\d+\.\d+\.\d+)').Success) {
        $oldVersion = [regex]::Match($ExistingContent, '(?m)^# CONSTITUTION_VERSION:\s*(\d+\.\d+\.\d+)').Groups[1].Value

        $newVersion = 'CONSTITUTION_VERSION' | ForEach-Object { $_ } | Select-Object -First 1
        if ([regex]::Match($TemplateContent, '^\[(?P<VERSION>\d+\.\d+\.\d+)\]\s*\(.*?\)\n\[\d{4}-\d{2}-\d{2}\]').Success) {
            $newVersion = [regex]::Match($TemplateContent, '^([0-9]+\.[0-9]+\.[0-9]+).*?(\d{4}-\d{2}-\d{2})').Groups[1].Value
        }
    }

    # Find dates
    $ratificationDate = [regex]::Match($ExistingContent, '(?m)^RATIFICATION_DATE:\s*(\d{4}-\d{2}-\d{2})').Groups[1].Value
    if ($null -eq $ratificationDate) {
        $ratificationDate = 'TODO'
    }

    $lastAmendedDate = [regex]::Match($ExistingContent, '(?m)^LAST_AMENDED_DATE:\s*(\d{4}-\d{2}-\d{2})').Groups[1].Value
    if ($null -eq $lastAmendedDate) {
        $lastAmendedDate = 'TODO'
    }

    # Write with existing values preserved
    Write-Output (Format-Constitution -OldVersion $oldVersion -LastAmendedDate $lastAmendedDate -NewContent $TemplateContent)
} else {
    # First-time initialization, use defaults
    $templateWithDefaults = $TemplateContent -replace '^\[.*?\]\s*\(.*\)$', '' | ForEach-Object { $_ } | ForEach-Object { $_ } | Select-Object -First 1
    Write-Output (Format-Constitution -OldVersion '1.0.0' -LastAmendedDate $lastAmendedDate -NewContent ($templateWithDefaults + "`n`n"))
}

function Format-Constitution($OldVersion = '1.0.0', $LastAmendedDate, $NewContent) {
    $content = [System.Text.RegularExpressions.Regex]::Matches($NewContent, '(?m)^\[(.*?)\]\s*\(.*?\)\n\[([^\]]+)\]') | ForEach-Object { $_ } | Select-Object -First 1

    return @'
# SPECKIT Constitution
# CONSTITUTION_VERSION: [CONSTITUTION_VERSION]
# RATIFICATION_DATE: [RATIFICATION_DATE]
# LAST_AMENDED_DATE: [LAST_AMENDED_DATE]

## Principles

[PRINCIPLE_1]

[PRINCIPLE_2]

[PRINCIPLE_3]

## Governance

[AFFECTIONATE_AMENDMENT_PROCEDURE]

[VERSIONING_POLICY_TEXT]

[COMPLIANCE_REVIEW_EXPECTATIONS]
'@
}
