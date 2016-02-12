************************************************************************************
*  Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD  *
************************************************************************************

Version: ${version}

Übersicht
=========

Die SWE Datenaufbereitung UFD dient zu Abbildung quasi-kontinuierlicher Messwerte von
Umfelddatenmessstellen auf eine parametrierbare Anzahl von Stufen mit frei parametrierbaren
Schwellwerten. Dazu werden die Messwerte zuerst durch eine exponentielle Glättung mit
wanderndem Abweichungswinkel geglättet und anschließend über eine parametrierbare
Hysteresefunktion klassifiziert. Zusätzliche werden auf Basis der Lufttemperatur, der
Fahrbahnoberflächentemperatur und der relativen Luftfeuchte die Taupunkttemperaturen
für die Luft und die Fahrbahnoberfläche ermittelt. Nach dieser Klassifizierung werden
die Daten ggf. in den Datenverteiler publiziert.


Versionsgeschichte
==================

1.3.0
- Umstellung auf Maven-Build

1.2.0
  - Stufe fuer Windrichtung ergaenzt
  - Stufe fuer Helligkeit aus Projekt Augsburg uebernommen

1.0.5

  - Berechnung der geglätteten Sichtweite abgepasst:
    Vor der Bestimmung der Nässestufe, Wasserfilmdickestufe und der Sichtweitenstufe wird jeweils ein
    geglätteter Messwert berechnet. Innerhalb dieser Berechnung wird der Glättungsfaktor b(i) bestimmt.
    Die Berechnung von b(i) unterscheidet sich aber bei der Sichtweite von der für Nässe und Wasserfilmdicke
    in der Hinsicht, dass bei der SW (Messwert) / (alten geglätteten Wert) gerechnet wird und bei den beiden
    anderen umgekehrt (alter geglätteter Wert) / (Messwert).
	Das Problem bestand darin, dass im Programm b(i) für alle Zielwerte gleich gerechnet wird, d.h. die
	geglättete SW wird dadurch falsch berechnet.

1.0.4

  - FIX: Die Hysterese kann jetzt auch mit abfallenden Intervallen bestueckt werden

1.0.3

  - FIX: Sämtliche Konstruktoren DataDescription(atg, asp, sim) ersetzt durch
         DataDescription(atg, asp)
1.0.0

  - Erste Auslieferung

1.0.1

  - Startskripte hinzu

1.1.0

  - Klassifizierung auch für Helligkeits-Sensoren hinzugefügt (um eine Steuerung der
    Helligkeit von Betriebsmitteln der FG4 zu ermöglichen)

1.1.1

  - Sämtliche Konstruktoren DataDescription(atg, asp, sim)
    ersetzt durch DataDescription(atg, asp)

Bemerkungen
===========

Diese SWE ist eine eigenständige Datenverteiler-Applikation, welche über die Klasse
de.bsvrz.dua.daufd.vew.VerwaltungAufbereitungUFD mit folgenden Parametern gestartet werden kann
(zusaetzlich zu den normalen Parametern jeder Datenverteiler-Applikation):
	-KonfigurationsBereichsPid=pid(,pid)


- Tests:

	Für die Tests wird eine Verbindung zum Datenverteiler mit einer Konfiguration mit dem
	Testkonfigurationsbereich "kb.daUfdTest" benötigt (Diese Konfiguration mit den entsprechenden
	Parametern liegt bereits komplett im Archiv test_konfig_daufd.zip vor). Weiterhin muss die
	SWE 4.8 mit diesem Konfigurationsbereich als Argument gestartet sein.

	Die Tests selbst sind als JUnit-Tests (alle unterhalb von JUnit) ausführbar. Es sind
	alle innerhalb der Prüfspezifikation verlangten Tests implementiert und so bereits durchgeführt
	worden.
	Bevor die Tests gestartet werden können, muss die Verbindung zum Datenverteiler wird über
	die statische Variable CON_DATA der Klasse de.bsvrz.dua.daufd.DAVTest hergestellt werden:

	/**
	 * Verbindungsdaten
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083",
			"-benutzer=Tester",
			"-authentifizierung=c:\\passwd",
			"-debugLevelStdErrText=CONFIG",
			"-debugLevelFileText=CONFIG",
			"-KonfigurationsBereichsPid=kb.daUfdTest" };


- Logging-Hierarchie (Wann wird welche Art von Logging-Meldung produziert?):

	ERROR:
	- DUAInitialisierungsException --> Beendigung der Applikation
	- Fehler beim An- oder Abmelden von Daten beim Datenverteiler
	- Interne unerwartete Fehler

	WARNING:
	- Fehler, die die Funktionalität grundsätzlich nicht
	  beeinträchtigen, aber zum Datenverlust führen können
	- Nicht identifizierbare Konfigurationsbereiche
	- Probleme beim Explorieren von Attributpfaden
	  (von Plausibilisierungsbeschreibungen)
	- Wenn mehrere Objekte eines Typs vorliegen, von dem
	  nur eine Instanz erwartet wird
	- Wenn Parameter nicht korrekt ausgelesen werden konnten
	  bzw. nicht interpretierbar sind
	- Wenn inkompatible Parameter übergeben wurden
	- Wenn Parameter unvollständig sind
	- Wenn ein Wert bzw. Status nicht gesetzt werden konnte

	INFO:
	- Wenn neue Parameter empfangen wurden

	CONFIG:
	- Allgemeine Ausgaben, welche die Konfiguration betreffen
	- Benutzte Konfigurationsbereiche der Applikation bzw.
	  einzelner Funktionen innerhalb der Applikation
	- Benutzte Objekte für Parametersteuerung von Applikationen
	  (z.B. die Instanz der Datenflusssteuerung, die verwendet wird)
	- An- und Abmeldungen von Daten beim Datenverteiler

	FINE:
	- Wenn Daten empfangen wurden, die nicht weiterverarbeitet
	  (plausibilisiert) werden können (weil keine Parameter vorliegen)
	- Informationen, die nur zum Debugging interessant sind


Disclaimer
==========

Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD
Copyright (C) 2007-2009 BitCtrl Systems GmbH

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to the Free Software Foundation, Inc., 51
Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.


Kontakt
=======

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
