# Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD

Version: ${version}

## Übersicht

Die SWE Datenaufbereitung UFD dient zu Abbildung quasi-kontinuierlicher Messwerte von
Umfelddatenmessstellen auf eine parametrierbare Anzahl von Stufen mit frei parametrierbaren
Schwellwerten. Dazu werden die Messwerte zuerst durch eine exponentielle Glättung mit
wanderndem Abweichungswinkel geglättet und anschließend über eine parametrierbare
Hysteresefunktion klassifiziert. Zusätzliche werden auf Basis der Lufttemperatur, der
Fahrbahnoberflächentemperatur und der relativen Luftfeuchte die Taupunkttemperaturen
für die Luft und die Fahrbahnoberfläche ermittelt. Nach dieser Klassifizierung werden
die Daten ggf. in den Datenverteiler publiziert.

## Versionsgeschichte

### 2.0.0

Release-Datum: 31.05.2016

#### Neue Abhängigkeiten

Die SWE benötigt nun das Distributionspaket de.bsvrz.sys.funclib.bitctrl.dua in
Mindestversion 1.5.0 und de.bsvrz.sys.funclib.bitctrl in Mindestversion 1.4.0.

#### Änderungen

Folgende Änderungen gegenüber vorhergehenden Versionen wurden durchgeführt:

- Bei der Glättung von Messwerten wird nun der geglättete Wert des Vorgängerintervalls
  gelöscht, wenn ein *nicht ermittelbar*er oder *fehlerhaft*er Messwert
  auftritt.
- Aus der Niederschlagsart und dem Fahrbahnoberflächenzustand wird nun je Messtelle
  für interne Berechnungen eine NS-FBZ-Klasse bestimmt, mit den möglichen
  Werten

  – Regen
  – Schnee
  – Platzregen
  – Glätte
  – Nicht bestimmbar.
		
- Die Tabellen zur Zuordnung der Niederschlagsintensitätsstufen und Wasserfilmdickenstufen
  können nun (bei aktualisierten Datenmodell) abhängig von der NSFBZ-
  Klasse parametriert werden.
- Die Tabelle zur Ermittlung der Nässestufe kann nun über einen Aufrufparameter
  vorgegeben werden (-konfigurationNS=<Dateiname>). Optional können auch
  hier je nach NS-FBZ-Klasse unterschiedliche Tabellen verwendet werden:

  – -konfigurationNSRegen=<Dateiname>
  – -konfigurationNSSchnee=<Dateiname>
  – -konfigurationNSPlatzregen=<Dateiname>
  – -konfigurationNSGlaette=<Dateiname>

  Die angegebenen Dateien müssen mit Semikolon (;) separierte CSV-/Textdateien
  sein, die 6 Spalten und 5 Zeilen enthalten (keine Spalten-/Zeilenüberschriften),
  beispielsweise:

		trocken; trocken; nass1; nass2; nass2; trocken
		nass1; nass1; nass2; nass3; nass4; nass1
		nass2; nass2; nass2; nass3; nass4; nass2
		nass2; nass2; nass3; nass3; nass4; nass3
		trocken; trocken; nass1; nass2; nass3;

  (Leere Zellen entsprechen nicht ermittelbar). Dabei entsprechen die Spalten den
  NI-Stufen 0–4 sowie “nicht verfügbar” und die Zeilen den WFD-Stufen 0–3 sowie
  “nicht verfügbar”.
- Die Berechnung der geglätteten Sichtweite wurde korrigiert.
- Die Berechnung der Abtrockungsstufen wurde überarbeitet.

### 1.5.0

- Umstellung auf Java 8 und UTF-8

### 1.4.1

- Kompatibilität zu DuA-2.0 hergestellt

### 1.4.0

- Umstellung auf Funclib-BitCtrl-Dua

### 1.3.0

- Umstellung auf Maven-Build

### 1.2.0

  - Stufe fuer Windrichtung ergaenzt
  - Stufe fuer Helligkeit aus Projekt Augsburg uebernommen

### 1.1.1

  - Sämtliche Konstruktoren DataDescription(atg, asp, sim)
    ersetzt durch DataDescription(atg, asp)

### 1.1.0

  - Klassifizierung auch für Helligkeits-Sensoren hinzugefügt (um eine Steuerung der
    Helligkeit von Betriebsmitteln der FG4 zu ermöglichen)

### 1.0.5

  - Berechnung der geglätteten Sichtweite abgepasst:
    Vor der Bestimmung der Nässestufe, Wasserfilmdickestufe und der Sichtweitenstufe wird jeweils ein
    geglätteter Messwert berechnet. Innerhalb dieser Berechnung wird der Glättungsfaktor b(i) bestimmt.
    Die Berechnung von b(i) unterscheidet sich aber bei der Sichtweite von der für Nässe und Wasserfilmdicke
    in der Hinsicht, dass bei der SW (Messwert) / (alten geglätteten Wert) gerechnet wird und bei den beiden
    anderen umgekehrt (alter geglätteter Wert) / (Messwert).
	Das Problem bestand darin, dass im Programm b(i) für alle Zielwerte gleich gerechnet wird, d.h. die
	geglättete SW wird dadurch falsch berechnet.

### 1.0.4

  - FIX: Die Hysterese kann jetzt auch mit abfallenden Intervallen bestueckt werden

### 1.0.3

  - FIX: Sämtliche Konstruktoren DataDescription(atg, asp, sim) ersetzt durch
         DataDescription(atg, asp)

### 1.0.1

  - Startskripte hinzu

### 1.0.0

  - Erste Auslieferung




## Bemerkungen

Diese SWE ist eine eigenständige Datenverteiler-Applikation, welche über die Klasse
de.bsvrz.dua.daufd.vew.VerwaltungAufbereitungUFD mit folgenden Parametern gestartet werden kann
(zusaetzlich zu den normalen Parametern jeder Datenverteiler-Applikation):
	-KonfigurationsBereichsPid=pid(,pid)


## Disclaimer

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


## Kontakt

BitCtrl Systems GmbH
Weißenfelser Straße 67
04229 Leipzig
Phone: +49 341-490670
mailto: info@bitctrl.de
