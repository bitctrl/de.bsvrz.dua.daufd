/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Datenaufbereitung UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * Copyright 2015 by Kappich Systemberatung Aachen
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.daufd.
 * 
 * de.bsvrz.dua.daufd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.daufd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.daufd.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */
package de.bsvrz.dua.daufd.stufeni;

import de.bsvrz.dua.daufd.vew.AbstraktNiFbzStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

/**
 * Berechnet die Niederschlagintensitaetstufe aus den Messwerten
 * Die eigentliche Berechnung ist für mehrere Module gemeinsam
 * in der Klasse AbstraktStufe 
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public class NiederschlagIntensitaetStufe extends AbstraktNiFbzStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationNiederschlagsIntensität";
	}

	@Override
	public String getKlassifizierungsAttribut() {
		return "KlassifizierungNiederschlagsIntensität";
	}

	@Override
	public String getKlassifizierungsAttributGruppe() {
		return "atg.ufdsKlassifizierungNiederschlagsIntensität";
	}

	@Override
	public String getMesswertAttribut() {
		return "NiederschlagsIntensität";
	}

	@Override
	public String getMesswertAttributGruppe() {
		return "atg.ufdsNiederschlagsIntensität";
	}

	@Override
	public String getStufeAttributGruppe() {
		return "atg.ufdsStufeNiederschlagsIntensität";
	}

	@Override
	public String getSensorTyp() {
		return "typ.ufdsNiederschlagsIntensität";
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
	}

	/**
	 *  NI Stufen, die unterscheidet werden
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum NI_Stufe {
		NI_STUFE0,
		NI_STUFE1,
		NI_STUFE2,
		NI_STUFE3,
		NI_STUFE4,
		NI_WERT_NV // Wert nicht verfuegbar
	};
	
	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	protected final static NI_Stufe mapIntStufe [] = new NI_Stufe [] 
    { NI_Stufe.NI_STUFE0, NI_Stufe.NI_STUFE1, NI_Stufe.NI_STUFE2, NI_Stufe.NI_STUFE3, NI_Stufe.NI_STUFE4 };

	
	/**
	 * Konvertiert die NI_stufe aus Integer ins symbolische Format
	 * @param stufe Stufe int
	 * @return NI_Stufe symbolisch
	 */
	static public NI_Stufe getStufe(int stufe) {
		NI_Stufe stufeSymb;
		if(  stufe < 0 || stufe > mapIntStufe.length)
			stufeSymb = NI_Stufe.NI_WERT_NV;
		else stufeSymb = mapIntStufe[stufe];
		return stufeSymb;
	}
	
	/**
	 * Konvertiert die NI_stufe aus  symbolischen Format ins Integer
	 * @param stufe NI_Stufe symbolisch
	 * @return Stufe int
	 */
	static public int getStufe(NI_Stufe stufe) {
		int intStufe = -1;
		if(stufe != NI_Stufe.NI_WERT_NV) {
			intStufe = stufe.ordinal();
		}
		return intStufe;
	}
	
}
