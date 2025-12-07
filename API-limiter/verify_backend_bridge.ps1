$adminUrl = "http://localhost:8080/api/admin/stats"
$apiUrl = "http://localhost:8080/api/weather/current"
$headers = @{"X-API-KEY"="free_key_123"}

# 1. Check Initial Stats
Write-Host "--- INITIAL STATS ---"
$initialStats = Invoke-RestMethod -Uri $adminUrl
$initialStats | Format-List *

# 2. Consume Tokens Aggressively
Write-Host "--- CONSUMING TOKENS ---"
for ($i=1; $i -le 15; $i++) {
    try {
        Invoke-RestMethod -Uri $apiUrl -Headers $headers -ErrorAction Stop | Out-Null
        Write-Host "." -NoNewline
    } catch {
        Write-Host "X" -NoNewline
    }
}
Write-Host ""
Write-Host "Requests done."

# 3. Check Updated Stats Immediately
Write-Host "--- UPDATED STATS ---"
$updatedStats = Invoke-RestMethod -Uri $adminUrl
$updatedStats | Format-List *

if ([double]$updatedStats.free_plan_remaining -lt [double]$initialStats.free_plan_remaining) {
    Write-Host "SUCCESS: Tokens decreased! ($($initialStats.free_plan_remaining) -> $($updatedStats.free_plan_remaining))"
} else {
    Write-Host "FAILURE: Tokens did not decrease."
}
