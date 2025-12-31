
$baseUrl = "http://localhost:8080/api/v1"
$username = "testuser" + (Get-Date -Format "yyyyMMddHHmmss")
$password = "Password123"
$email = "$username@example.com"

# 1. Register
Write-Host "Registering user $username..."
$registerBody = @{
    username = $username
    password = $password
    email = $email
} | ConvertTo-Json

try {
    $registerRes = Invoke-RestMethod -Uri "$baseUrl/auth/register" -Method Post -Body $registerBody -ContentType "application/json"
    Write-Host "Register Response: $($registerRes | ConvertTo-Json)"
} catch {
    Write-Host "Register Failed: $_"
    exit
}

# 2. Login
Write-Host "Logging in..."
$loginBody = @{
    username = $username
    password = $password
} | ConvertTo-Json

try {
    $loginRes = Invoke-RestMethod -Uri "$baseUrl/auth/login" -Method Post -Body $loginBody -ContentType "application/json"
    Write-Host "Login Response: $($loginRes | ConvertTo-Json)"
    $token = $loginRes.data.accessToken
} catch {
    Write-Host "Login Failed: $_"
    exit
}

# 3. Calculate 1RM (CalculationController)
Write-Host "Calculating 1RM (with record)..."
$headers = @{
    Authorization = "Bearer $token"
}

try {
    $ormRes = Invoke-RestMethod -Uri "$baseUrl/calculate/1rm?weight=100.5&reps=10&model=Brzycki" -Method Get -Headers $headers
    Write-Host "1RM Response: $($ormRes | ConvertTo-Json)"
} catch {
    Write-Host "1RM Calculation Failed: $_"
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $errorBody = $reader.ReadToEnd()
        Write-Host "Error Body: $errorBody"
    }
}

# 4. Get History (CalculationController)
Write-Host "Getting History...";
try {
    $historyRes = Invoke-RestMethod -Uri "$baseUrl/calculate/history" -Method Get -Headers $headers
    Write-Host "History Response: $($historyRes | ConvertTo-Json -Depth 5)"
} catch {
    Write-Host "Get History Failed: $_"
}

# 5. Calculate Volume
Write-Host "Calculating Volume...";
try {
    $volRes = Invoke-RestMethod -Uri "$baseUrl/calculate/volume?weight=80&sets=5&reps=10" -Method Get -Headers $headers
    Write-Host "Volume Response: $($volRes | ConvertTo-Json)"
} catch {
    Write-Host "Volume Calculation Failed: $_"
}

# 6. Calculate Calories
Write-Host "Calculating Calories...";
try {
    $calRes = Invoke-RestMethod -Uri "$baseUrl/calculate/calories?duration=60&intensity=7.0" -Method Get -Headers $headers
    Write-Host "Calories Response: $($calRes | ConvertTo-Json)"
} catch {
    Write-Host "Calories Calculation Failed: $_"
}
