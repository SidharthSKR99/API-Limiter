for ($i=1; $i -le 15; $i++) { 
    try { 
        $response = Invoke-RestMethod -Uri "http://localhost:8080/api/weather/current" -Headers @{"X-API-KEY"="free_key_123"} -ErrorAction Stop
        Write-Host "Request $i: Success"
    } catch { 
        Write-Host "Request $i: Failed with $_" 
    }
    Start-Sleep -Milliseconds 100 
}
