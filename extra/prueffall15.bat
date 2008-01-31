@echo off

call ..\..\..\skripte-dosshell\einstellungen.bat

set cp=..\..\de.bsvrz.sys.funclib.bitctrl\de.bsvrz.sys.funclib.bitctrl-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.daufd-runtime.jar
set cp=%cp%;..\de.bsvrz.dua.daufd-test.jar
set cp=%cp%;..\..\junit-4.1.jar

title Pruefungen SE4 - DUA, SWE 4.8

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.8
echo #
echo #  Testet die Taupunkt Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.daufd.tp.TaupunktTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.8
echo #
echo #  Testet die WasserFilmDickeStufe Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufeTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.8
echo #
echo #  Testet die SichtWeiteStufe Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.daufd.stufesw.SichtWeiteStufeTest
pause

echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.8
echo #
echo #  Testet die NiederschlagIntensitaetStufe Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufeTest
pause
echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.8
echo #
echo #  Testet die NaesseStufe Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.daufd.stufenaesse.NaesseStufeTest
pause


echo ========================================================
echo #  Pruefungen SE4 - DUA, SWE 4.8
echo #
echo #  Testet die Hysterese Klasse
echo ========================================================
echo.

%java% -cp %cp% org.junit.runner.JUnitCore de.bsvrz.dua.daufd.hysterese.HysterezeTester2
pause


