#!/bin/bash

# In das Verzeichnis des Skripts wechseln, damit relative Pfade funktionieren
cd `dirname $0`

# Allgemeine Einstellungen
source ../../../skripte-bash/einstellungen.sh

################################################################################
# SWE-Spezifische Parameter	(überprüfen und anpassen)                          #
################################################################################

kb="kb.UFD_Konfig_A8,kb.UFD_Konfig_B27"

################################################################################
# Folgende Parameter müssen überprüft und evtl. angepasst werden               #
################################################################################

# Parameter für den Java-Interpreter, als Standard werden die Einstellungen aus # einstellungen.sh verwendet.
#jvmArgs="-Dfile.encoding=ISO-8859-1"

# Parameter für den Datenverteiler, als Standard werden die Einstellungen aus # einstellungen.sh verwendet.
#dav1="-datenverteiler=localhost:8083 -benutzer=Tester -authentifizierung=passwd -debugFilePath=.."

################################################################################
# Ab hier muss nichts mehr angepasst werden                                    #
################################################################################

# Applikation starten
java $jvmArgs -jar ../de.bsvrz.dua.daufd-runtime.jar \
	$dav1 \
	-KonfigurationsBereichsPid=$kb \
	-debugLevelStdErrText=ERROR \
	-debugLevelFileText=INFO \
	-debugLevelFileXML=OFF \
	-debugLevelFileExcel=OFF \
	-debugLevelFileHTML=OFF \
	&
