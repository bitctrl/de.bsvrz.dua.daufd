/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.6 Abfrage Pufferdaten
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

package de.bsvrz.dua.daufd.stufeni;

import java.util.Collection;

import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dua.daufd.UfdsKlassifizierungParametrierung;
import de.bsvrz.dua.daufd.hysterese.HysterezeTester2;
import de.bsvrz.sys.funclib.debug.Debug;


/**
 *  Testet den Modul NiederschalIntensitaetStufe
 *  
 *  @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public class NiederschagIntensitaetStufeTest  {
	
	/**
	 * NI-Stufe untere Grenzwerte [AFo]
	 */
	private final double stufeVon[] = new double[] {
		0.0, 0.2, 1.0, 4.0, 10.0 	
	};
	/**
	 * NI-Stufe obere Grenzwerte [AFo]
	 */
	private final double stufeBis[] = new double[] {
		0.3, 1.2, 5.0, 12.0, 200.0 // Max Wert vom DaK 	
	};
	
	private static final String TYP_UFDS_NI = "typ.ufdsNiederschlagsIntensität";
	private static final String ATG_UFDS_KLASS_NI = "atg.ufdsKlassifizierungNiederschlagsIntensität";
	private static final String ATT_UFDS_KLASS_NI = "KlassifizierungNiederschlagsIntensität";
	
	/**
	 * Sendet die Parametrierung aus dem Tabellen der AFo dem DAV
	 * @param dav DAV
	 * @param konfBereiche konfigurationsbereiche, aus dennen alle Objekte parametriert werden
	 */
	public void ParametriereUfds(ClientDavInterface dav, Collection<ConfigurationArea> konfBereiche) {
		try {
			UfdsKlassifizierungParametrierung param = new UfdsKlassifizierungParametrierung(
					TYP_UFDS_NI, ATG_UFDS_KLASS_NI, ATT_UFDS_KLASS_NI, stufeVon, stufeBis);
			param.ParametriereUfds(dav, konfBereiche);
		} catch (Exception e) {
			Debug.getLogger().error("Fehler bei Parametrierung der NiederschlagIntensitaet:" + e.getMessage());
			e.printStackTrace();
		}
	}
	
	double [] vorbereiteMesswerte() {
		final int N = 31;
		final int M = 32;
		final int K = 7;
		final int L = 30;
		final int ANZAHL = N+M+K+L;
	
		double [] Messwert = new double[ANZAHL];
		
		// Zuerst steigende und dann sinkende Werte
		for(int i=0; i<N; i++) {
			Messwert[i] = 15.0 - Math.abs(i - 15.0);
		}
		
		// Divergiert vom 7.5 
		for(int i=N; i<N+M; i++) {
			int j = i - N;
			Messwert[i] = 7.5 - Math.pow(-1.0, j)*j/4.0;  
		}
		
		// Zuerst  sinkende und dann steigende Werte, mit groesserem Gradient 
		for(int i=N+M; i<N+M+K; i++) {
			int j = i - N-M;
			Messwert[i] = Math.abs(15.0-j*5);
		}
		
		// Zufaellige Werte
		for(int i=N+M+K; i<N+M+K+L; i++) {
			Messwert[i] = Math.random() * 15.0;
		}
		
		return Messwert;
	}
	
	public void gerauescheMesswerte(double [] Messwert) {
		for(int i =0; i<Messwert.length; i++) {
			Messwert[i] += Math.random()*0.4-0.2;
			if(Messwert[i]<0.0) Messwert[i] = 0.0;
		}
	}
	/**
	 * Generiert eine Reihe von Zahlen und vergleicht die geglaettet Werte mit eigenen
	 */
	@Test
	public void glaettungTest() {		
		
		final double f = 0.25;
		double [] Messwert = vorbereiteMesswerte();
		final int ANZAHL = Messwert.length;
		double [] MesswertGlatt = new double[ANZAHL];
		double [] b = new double [ANZAHL];
		
		b[0] = 0.08;
		MesswertGlatt[0] = Messwert[0];
		
		for(int i=1; i<ANZAHL; i++) {
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
		
		/*
		 * FOLGT TEST, VERGLEICHUNG MIT WERTEN VON GETESTETER KLASSE
		 * 
		 */
		
		System.out.println(Messwert);
		
		// Noch ein Test mit gerauschte Werte
		gerauescheMesswerte(Messwert);
		
		b[0] = 0.08;
		MesswertGlatt[0] = Messwert[0];
		
		for(int i=1; i<ANZAHL; i++) {
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
		
		/*
		 * FOLGT TEST, VERGLEICHUNG MIT WERTEN VON GETESTETER KLASSE
		 * 
		 */
		System.out.println(Messwert);
	}
	
	
	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 */
	@Test
	public void stufeTest() {		
		
		final double f = 0.25;
		double [] Messwert = vorbereiteMesswerte();
		final int ANZAHL = Messwert.length;
		double [] MesswertGlatt = new double[ANZAHL];
		int [] stufe = new int[ANZAHL];
		double [] b = new double [ANZAHL];
		
		b[0] = 0.08;
		MesswertGlatt[0] = Messwert[0];
		HysterezeTester2 hysTest = new HysterezeTester2();
		hysTest.init(stufeVon, stufeBis);
		stufe[0] = hysTest.hystereze(MesswertGlatt[0], -1);
		
		for(int i=1; i<ANZAHL; i++) {
			if(Messwert[i] == 0) {
				MesswertGlatt[i] = 0;
				b[i] = b[0];
			}
			else {
				b[i] = b[0] + (1.0 - f* MesswertGlatt[i-1]/Messwert[i]);
				if(b[i] < b[0] || b[i] > 1.0) b[i] = b[0];
				MesswertGlatt[i] = b[i]*Messwert[i] + (1.0 - b[i])*MesswertGlatt[i-1];
			}
			stufe[i] = hysTest.hystereze(MesswertGlatt[i], stufe[i-1]);
		}
		
		/*
		 * FOLGT TEST, VERGLEICHUNG MIT WERTEN VON GETESTETER KLASSE
		 * 
		 */
		
		System.out.println(Messwert);
		
		// Noch ein Test mit gerauschte Werte
		gerauescheMesswerte(Messwert);
		
		b[0] = 0.08;
		MesswertGlatt[0] = Messwert[0];
		stufe[0] = hysTest.hystereze(MesswertGlatt[0], -1);
		
		for(int i=1; i<ANZAHL; i++) {
			if(Messwert[i] == 0) {
				MesswertGlatt[i] = 0;
				b[i] = b[0];
			}
			else {
				b[i] = b[0] + (1.0 - f* MesswertGlatt[i-1]/Messwert[i]);
				if(b[i] < b[0] || b[i] > 1.0) b[i] = b[0];
				MesswertGlatt[i] = b[i]*Messwert[i] + (1.0 - b[i])*MesswertGlatt[i-1];
			}
			stufe[i] = hysTest.hystereze(MesswertGlatt[i], stufe[i-1]);
		}
		
		/*
		 * FOLGT TEST, VERGLEICHUNG MIT WERTEN VON GETESTETER KLASSE
		 * 
		 */
		System.out.println(Messwert);
	}

}
