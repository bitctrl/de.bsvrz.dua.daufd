/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
 * Contact Information:<br>
 * BitCtrl Systems GmbH<br>
 * Weißenfelser Straße 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.daufd;

/**
 * Allgemenie Klasse die Eingabewerte generiert und mit allgemeinen Formel
 * glaettet, wird zum Testfaellen benutzt
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class MesswertBearbeitungAllgemein {

	/**
	 * Generiert ein Array von Messwerten fuer Testfaelle
	 *
	 * @param min
	 *            min Messwert
	 * @param max
	 *            max Messwert
	 * @return Messwerte
	 */
	public static double[] generiereMesswerte(final double min, final double max) {

		final int n = 31;
		final int m = 32;
		final int k = 7;
		final int l = 30;
		final int anzahl = n + m + k + l;
		final double intervall = max - min;
		double schritt;

		final double[] messwert = new double[anzahl];

		// Zuerst steigende und dann sinkende Werte
		schritt = intervall / n;
		for (int i = 0; i < n; i++) {
			messwert[i] = max - Math.abs((2 * i * schritt) - intervall);
		}

		// Divergiert vo der Mitte des INtervalles
		schritt = intervall / m;
		for (int i = n; i < (n + m); i++) {
			final int j = i - n;
			messwert[i] = (intervall / 2)
					- ((Math.pow(-1.0, j) * j * schritt) / 2);
		}

		// Zuerst sinkende und dann steigende Werte, mit groesserem Gradient
		schritt = intervall / k;
		for (int i = n + m; i < (n + m + k); i++) {
			final int j = i - n - m;
			messwert[i] = min + Math.abs(intervall - (j * schritt * 2));
		}

		// Zufaellige Werte
		for (int i = n + m + k; i < (n + m + k + l); i++) {
			messwert[i] = min + (Math.random() * intervall);
		}

		return messwert;
	}

	/**
	 * Generiert Geraeusch und setzt zufaellige Werte als 0
	 * 
	 * @param messwert
	 *            Original Array
	 * @param intervall
	 *            Intervall des Gerausches
	 * @param anzahlNullWerte
	 *            Anzahl der Messwerte, die als 0 gesetzt werden
	 */
	public static void gerauescheMesswerte(final double[] messwert,
			final double intervall, final int anzahlNullWerte) {
		for (int i = 0; i < messwert.length; i++) {
			messwert[i] += (Math.random() * intervall) - (intervall / 2);
		}
		for (int i = 0; i < anzahlNullWerte; i++) {
			if (messwert[i] < 0) {
				messwert[i] = 0;
			}
		}
		for (int i = 0; i < anzahlNullWerte; i++) {
			final int j = (int) (Math.random() * messwert.length);
			messwert[j] = 0;
		}
	}

	/**
	 * Glaettet die Messwerte
	 *
	 * @param messwert
	 *            Messwerte
	 * @param b
	 *            Koefizient b
	 * @param messwertGlatt
	 *            Ausgabe geglaettete Messwerte
	 * @param f
	 *            Koefizient f
	 * @param b0
	 *            Koefizient b[0]
	 */
	public static void glaetteMesswerte(final double[] messwert,
			final double[] b, final double[] messwertGlatt, final double f,
			final double b0) {

		b[0] = b0;
		messwertGlatt[0] = messwert[0];

		for (int i = 1; i < messwert.length; i++) {
			if (messwert[i] == 0) {
				messwertGlatt[i] = 0;
				b[i] = b[0];
			} else {
				b[i] = b[0]
						+ (1.0 - ((f * messwertGlatt[i - 1]) / messwert[i]));
				if ((b[i] < b[0]) || (b[i] > 1.0)) {
					b[i] = b[0];
				}
				messwertGlatt[i] = (b[i] * messwert[i])
						+ ((1.0 - b[i]) * messwertGlatt[i - 1]);
			}
		}
	}

	/**
	 * Rundet die Messwerte auf eine Stelle nach dem Komma, weil die Skalierung
	 * ist 0.1, und deswegen in Datensaetzen genauere Werte nicht enthalten
	 * werden koennen.
	 *
	 * @param messwert
	 */
	public static void rundeMesswerte(final double[] messwert) {
		double d;
		for (int i = 0; i < messwert.length; i++) {
			d = Math.round(messwert[i] * 10);
			messwert[i] = d / 10.0;
		}
	}

	/**
	 * Rundet die Messwerte auf Ganze Zahl.
	 * 
	 * @param messwert
	 */
	public static void rundeMesswerteGanzeZahl(final double[] messwert) {
		for (int i = 0; i < messwert.length; i++) {
			messwert[i] = Math.round(messwert[i]);
		}
	}
}
