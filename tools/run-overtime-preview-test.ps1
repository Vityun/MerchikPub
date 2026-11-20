param([string]$JdkHome)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$studioJdk = Join-Path $env:ProgramFiles 'Android/Android Studio/jbr'
if (-not $JdkHome) {
    $JdkHome = if (Test-Path -LiteralPath (Join-Path $studioJdk 'bin/javac.exe')) {
        $studioJdk
    } else {
        $env:JAVA_HOME
    }
}
if (-not $JdkHome) {
    throw 'Set -JdkHome to a JDK 17 or newer.'
}

$gradleHome = if ($env:GRADLE_USER_HOME) { $env:GRADLE_USER_HOME } else { Join-Path $HOME '.gradle' }
$cache = Join-Path $gradleHome 'caches/modules-2/files-2.1'

function Find-CachedJar($group, $artifact, $version) {
    $dir = Join-Path $cache "$group/$artifact/$version"
    $jar = Get-ChildItem -LiteralPath $dir -Filter "$artifact-$version.jar" -File -Recurse -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if (-not $jar) {
        throw "Missing cached dependency: ${group}:${artifact}:${version} in $cache"
    }
    $jar.FullName
}

$deps = @(
    (Find-CachedJar 'com.google.code.gson' 'gson' '2.10.1')
    (Find-CachedJar 'junit' 'junit' '4.13.2')
    (Find-CachedJar 'org.hamcrest' 'hamcrest-core' '1.3')
) -join ';'
$out = Join-Path $root 'app/build/overtime-preview-test/classes'
New-Item -ItemType Directory -Path $out -Force | Out-Null
$packagePath = 'ua/com/merchik/merchik/Options'
$policy = Join-Path $root "app/src/main/java/$packagePath/OvertimeOfferPolicy.java"
$test = Join-Path $root "app/src/test/java/$packagePath/OvertimeRulePreviewTest.java"

& (Join-Path $JdkHome 'bin/javac.exe') --release 17 -proc:none -encoding UTF-8 -cp $deps -d $out $policy $test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

& (Join-Path $JdkHome 'bin/java.exe') -cp "$out;$deps" org.junit.runner.JUnitCore `
    ua.com.merchik.merchik.Options.OvertimeRulePreviewTest
exit $LASTEXITCODE
