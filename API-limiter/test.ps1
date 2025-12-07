$url = "http://localhost:8080/api/weather/current"
$headers = @{"X-API-KEY"="free_key_123"}

for ($i=1; $i -le 15; $i++) {
    try {
        $response = Invoke-RestMethod -Uri $url -Headers $headers -ErrorAction Stop
        Write-Output "Request $i : OK"
    } catch {
        Write-Output "Request $i : BLOCKED"
    }
    Start-Sleep -Milliseconds 100
}
