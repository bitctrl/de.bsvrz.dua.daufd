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
import de.bsvrz.dua.daufd.MesswertBearbeitungAllgemein;
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

	/**
	 * Generiert eine Reihe von Zahlen und vergleicht die geglaettet Werte mit eigenen
	 */
	@Test
	public void glaettungTest() {		
		
		final double f = 0.25;
		final double b0 = 0.08;
		
		double [] Messwert = MesswertBearbeitungAllgemein.generiereMesswerte(stufeVon[0], stufeVon[stufeVon.length]*1.2);
		double [] MesswertGlatt = new double[Messwert.length];
		double [] b = new double [Messwert.length];
	
		MesswertBearbeitungAllgemein.geglaetteMesswerte(Messwert, b, MesswertGlatt, f, b0);
		
		/*
		 * FOLGT TEST, VERGLEICHUNG MIT WERTEN VON GETESTETER KLASSE
		 * 
		 */
		
		System.out.println(Messwert);
		
		// Noch ein Test mit gerauschte Werte
		MesswertBearbeitungAllgemein.gerauescheMesswerte(Messwert, 0.1, 10);
		MesswertBearbeitungAllgemein.geglaetteMesswerte(Messwert, b, MesswertGlatt, f, b0);
		
		
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
		
		int alt;
		final double f = 0.25;
		final double b0 = 0.08;
		
		HysterezeTester2 hystTest = new HysterezeTester2();
		double [] Messwert = MesswertBearbeitungAllgemein.generiereMesswerte(stufeVon[0], stufeVon[stufeVon.length]*1.2);
		double [] MesswertGlatt = new double[Messwert.length];
		double [] b = new double [Messwert.length];
		int [] stufen = new int [Messwert.length];
		hystTest.init(stufeVon, stufeBis);
	
		MesswertBearbeitungAllgemein.geglaetteMesswerte(Messwert, b, MesswertGlatt, f, b0);
		alt = -1;
		for(int i=0; i< MesswertGlatt.length; i++) {
			stufen[i] = hystTest.hystereze(MesswertGlatt[i], alt);
			alt = stufen[i];
		}
		
		/*
		 * FOLGT TEST, VERGLEICHUNG MIT WERTEN VON GETESTETER KLASSE
		 * 
		 */
		
		System.out.println(Messwert);
		
		// Noch ein Test mit gerauschte Werte
		MesswertBearbeitungAllgemein.gerauescheMesswerte(Messwert, 0.1, 10);
		MesswertBearbeitungAllgemein.geglaetteMesswerte(Messwert, b, MesswertGlatt, f, b0);
		alt = -1;
		for(int i=0; i< MesswertGlatt.length; i++) {
			stufen[i] = hystTest.hystereze(MesswertGlatt[i], alt);
			alt = stufen[i];
		}
		
		/*
		 * FOLGT TEST, VERGLEICHUNG MIT WERTEN VON GETESTETER KLASSE
		 * 
		 */
		System.out.println(Messwert);
	}

}
