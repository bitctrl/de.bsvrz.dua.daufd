@echo off


REM ############################################################################
REM Folgende Parameter müssen überprüft und evtl. angepasst werden

REM Java-Klassenpfad
SET jar=de.bsvrz.dua.daufd-runtime.jar

REM Argumente für die Java Virtual Machine
SET jvmArgs=-showversion -Dfile.encoding=ISO-8859-1 -Xms32m -Xmx256m -cp ..\%jar%
REM Parameter für den Datenverteiler
SET benutzer=Tester
SET passwortDatei=..\..\..\skripte-dosshell\passwd
SET dav1Host=localhost
SET dav1AppPort=8083
SET kb=kb.daUfdTest


REM Applikation starten
CHCP 1252
TITLE Datenaufberietung UFD
java %jvmArgs% ^
de.bsvrz.dua.daufd.vew.VerwaltungAufbereitungUFD ^
-debugLevelStdErrText=info ^
-debugSetLoggerAndLevel=:config ^
-datenverteiler=%dav1Host%:%dav1AppPort% ^
-benutzer=%benutzer% ^
-authentifizierung=%passwortDatei% ^
-KonfigurationsBereichsPid=%kb%

REM Nach dem Beenden warten, damit Meldungen gelesen werden können
PAUSE

