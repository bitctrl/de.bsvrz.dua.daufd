/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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
 * Allgemenie Klasse die Eingabewerte generiert und mit allgemeinen Formel glaettet, wird
 * zum Testfaellen benutzt
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class MesswertBearbeitungAllgemein {
	
	/**
	 * Generiert ein Array von Messwerten fuer Testfaelle
	 * 
	 * @param min min Messwert
	 * @param max max Messwert
	 * @return Messwerte
	 */
	public static double [] generiereMesswerte(double min, double max) {
		
		final int N = 31;
		final int M = 32;
		final int K = 7;
		final int L = 30;
		final int ANZAHL = N+M+K+L;
		final double intervall = max - min;
		double schritt;
	
		double [] Messwert = new double[ANZAHL];
	
		// Zuerst steigende und dann sinkende Werte
		schritt = intervall / N;
		for(int i=0; i<N; i++) {
			Messwert[i] = max - Math.abs(2*i*schritt - intervall);
		}
		
		// Divergiert vo der Mitte des INtervalles 
		schritt = intervall / M;
		for(int i=N; i<N+M; i++) {
			int j = i - N;
			Messwert[i] = intervall/2 - Math.pow(-1.0, j)*j*schritt/2;  
		}
		
		// Zuerst  sinkende und dann steigende Werte, mit groesserem Gradient
		schritt = intervall / K;
		for(int i=N+M; i<N+M+K; i++) {
			int j = i - N-M;
			Messwert[i] = min + Math.abs(intervall-j*schritt*2);
		}
		
		// Zufaellige Werte
		for(int i=N+M+K; i<N+M+K+L; i++) {
			Messwert[i] = min + Math.random() * intervall;
		}
		
		return Messwert;
	}
	
	/**
	 * Generiert Geraeusch und setzt zufaellige Werte als  0
	 *  
	 * @param Messwert Original Array
	 * @param intervall Intervall des Gerausches
	 * @param anzahlNullWerte Anzahl der Messwerte, die als 0 gesetzt werden
	 */
	public static void gerauescheMesswerte(double [] Messwert, double intervall, int anzahlNullWerte ) {
		for(int i =0; i<Messwert.length; i++) {
			Messwert[i] += Math.random()*intervall-intervall/2;			
		}
		for(int i=0; i<anzahlNullWerte; i++) {
			if(Messwert[i]< 0) Messwert[i] = 0;
		}
		for(int i=0; i<anzahlNullWerte; i++) {
			int j = (int)(Math.random()*Messwert.length);
			Messwert[j] = 0;
		}
	}
	
	/**
	 * Glaettet die Messwerte
	 * 
	 * @param Messwert Messwerte
	 * @param b Koefizient b
	 * @param MesswertGlatt Ausgabe geglaettete Messwerte
	 * @param f Koefizient f
	 * @param b0 Koefizient b[0]
	 */
	public static void glaetteMesswerte(double [] Messwert, double [] b, double [] MesswertGlatt, double f, double b0) {
		
		b[0] = b0;
		MesswertGlatt[0] = Messwert[0];
		
		for(int i=1; i<Messwert.length; i++) {
			if(Messwert[i] == 0) {
				MesswertGlatt[i] = 0;
				b[i] = b[0];
			}
			else {
				b[i] = b[0] + (1.0 - f* MesswertGlatt[i-1]/Messwert[i]);
				if(b[i] < b[0] || b[i] > 1.0) b[i] = b[0];
				MesswertGlatt[i] = b[i]*Messwert[i] + (1.0 - b[i])*MesswertGlatt[i-1];
			}
		}
	}
	/**
	 * Rundet die Messwerte auf eine Stelle nach dem Komma, 
	 * weil die Skalierung ist 0.1, und deswegen in Datensaetzen
	 * genauere Werte nicht enthalten werden koennen   
	 * 
	 * @param Messwert
	 */
	public static void rundeMesswerte(double [] Messwert) {
		double d;
		for(int i=0; i<Messwert.length; i++) {
			d = Math.round(Messwert[i] * 10);
			Messwert[i] = d/10.0;
		}
	}
	/**
	 * Rundet die Messwerte auf Ganze Zahl 
	 * @param Messwert
	 */
	public static void rundeMesswerteGanzeZahl(double [] Messwert) {
		for(int i=0; i<Messwert.length; i++) {
			Messwert[i] = Math.round(Messwert[i]);			
		}
	}
}
