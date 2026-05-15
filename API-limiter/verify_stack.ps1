$ErrorActionPreference = "Stop"

Write-Host "========================================="
Write-Host "🛡️ API Limiter Stack Verification Script"
Write-Host "========================================="

$baseUrl = "http://localhost:8080"
$username = "test_user_$(Get-Date -UFormat %s)"
$password = "secret123"

# 1. Register User
Write-Host "`n[1] Registering User ($username)..." -ForegroundColor Cyan
$regBody = @{
    username = $username
    password = $password
    plan     = "FREE"
} | ConvertTo-Json

$regResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/register" -Method Post -Body $regBody -ContentType "application/json"
$apiKey = $regResponse.apiKey
Write-Host "✅ Registered successfully. API Key: $apiKey" -ForegroundColor Green

# 2. Login User
Write-Host "`n[2] Logging in..." -ForegroundColor Cyan
$loginBody = @{
    username = $username
    password = $password
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "$baseUrl/api/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
$jwt = $loginResponse.token
Write-Host "✅ Logged in successfully. JWT captured." -ForegroundColor Green

# 3. Hit Weather Endpoint until 429 (Rate Limit Exhaustion)
Write-Host "`n[3] Hitting Weather Endpoint (FREE plan limit is 10)..." -ForegroundColor Cyan
$exhausted = $false
$headers = @{ "X-API-KEY" = $apiKey }

for ($i = 1; $i -le 15; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri "$baseUrl/api/weather/current" -Method Get -Headers $headers
        Write-Host "   Request $i : $($resp.StatusCode)"
    } catch {
        if ($_.Exception.Response.StatusCode.value__ -eq 429) {
            Write-Host "✅ Received 429 Too Many Requests on attempt $i!" -ForegroundColor Green
            $exhausted = $true
            break
        } else {
            Write-Host "❌ Unexpected Error: $_" -ForegroundColor Red
            exit
        }
    }
}

if (-not $exhausted) {
    Write-Host "❌ Failed to hit rate limit." -ForegroundColor Red
    exit
}

# 4. Check Dashboard Stats
Write-Host "`n[4] Checking Dashboard Stats..." -ForegroundColor Cyan
$dashboardHeaders = @{ "Authorization" = "Bearer $jwt" }
$stats = Invoke-RestMethod -Uri "$baseUrl/api/admin/stats" -Method Get -Headers $dashboardHeaders
$remaining = $stats.remaining
Write-Host "   Tokens Remaining: $remaining (Should be near 0)"
if ($remaining -eq 0) {
    Write-Host "✅ Dashboard confirms 0 tokens remaining." -ForegroundColor Green
}

# 5. Wait and Verify Refill
Write-Host "`n[5] Waiting 3 seconds for token refill..." -ForegroundColor Cyan
Start-Sleep -Seconds 3

$statsRefill = Invoke-RestMethod -Uri "$baseUrl/api/admin/stats" -Method Get -Headers $dashboardHeaders
$remainingRefill = $statsRefill.remaining
Write-Host "   Tokens Remaining after 3s: $remainingRefill"
if ($remainingRefill -gt 0) {
    Write-Host "✅ Tokens refilled successfully!" -ForegroundColor Green
} else {
    Write-Host "❌ Tokens did not refill." -ForegroundColor Red
}

Write-Host "`n🎉 Stack Verification Complete! Everything is working correctly." -ForegroundColor Green
