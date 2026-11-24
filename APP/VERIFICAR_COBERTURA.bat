@echo off
setlocal
cd /d "%~dp0"
call gradlew.bat clean :app:prepararCoberturaIDE --no-daemon
if errorlevel 1 (
 echo.
 echo No se ha aprobado la comprobacion. Revisa los errores anteriores.
 exit /b 1
)
echo.
type "cobertura-ide\RESULTADO_LOCAL.txt"
echo.
echo En Android Studio: Run - Manage Coverage Reports - Import.
echo Selecciona cobertura-ide\TacticalTrack.exec y desmarca los informes anteriores.
endlocal
