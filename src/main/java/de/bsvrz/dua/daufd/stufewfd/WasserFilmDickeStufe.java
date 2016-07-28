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
package de.bsvrz.dua.daufd.stufewfd;

import de.bsvrz.dua.daufd.vew.AbstraktNiFbzStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

/**
 * Berechnet die WasserFilmDickeStufe aus den Messwerten
 * Die eigentliche berechnung ins fuer mehrere Module gemeinsam
 * in der Klasse AbstraktStufe 
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public class WasserFilmDickeStufe extends AbstraktNiFbzStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationWasserFilmDicke";
	}

	@Override
	public String getKlassifizierungsAttribut() {
		return "KlassifizierungWasserFilmDicke";
	}

	@Override
	public String getKlassifizierungsAttributGruppe() {
		return "atg.ufdsKlassifizierungWasserFilmDicke";
	}

	@Override
	public String getMesswertAttribut() {
		return "WasserFilmDicke";
	}

	@Override
	public String getMesswertAttributGruppe() {
		return "atg.ufdsWasserFilmDicke";
	}

	@Override
	public String getStufeAttributGruppe() {
		return "atg.ufdsStufeWasserFilmDicke";
	}

	@Override
	public String getSensorTyp() {
		return "typ.ufdsWasserFilmDicke";
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
	}

	/**
	 *  WFD Stufen, die unterscheidet werden
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum WFD_Stufe {
		WFD_STUFE0,
		WFD_STUFE1,
		WFD_STUFE2,
		WFD_STUFE3,
		WFD_WERT_NV // Wert nicht verfuegbar
	}
	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	protected final static WFD_Stufe mapIntStufe [] = new WFD_Stufe [] 
    { WFD_Stufe.WFD_STUFE0, WFD_Stufe.WFD_STUFE1, WFD_Stufe.WFD_STUFE2, WFD_Stufe.WFD_STUFE3 };

	
	/**
	 * Konvertiert die WFD_stufe aus Integer ins symbolischen Format
	 * @param stufe Stufe Int
	 * @return WFD Stufe symbolisch
	 */
	static public WFD_Stufe getStufe(int stufe) {
		WFD_Stufe stufeSymb;
		if(  stufe < 0 || stufe > mapIntStufe.length)
			stufeSymb = WFD_Stufe.WFD_WERT_NV;
		else stufeSymb = mapIntStufe[stufe];
		return stufeSymb;
	}
	

	/**
	 * Konvertiert die WFD_stufe aus  symbolischen Format ins Integer
	 * @param stufe WFD_Stufe symbolisch
	 * @return Stufe int
	 */
	static public int getStufe(WFD_Stufe stufe) {
		int intStufe = -1;
		if(stufe != WFD_Stufe.WFD_WERT_NV) {
			intStufe = stufe.ordinal();
		}
		return intStufe;
	}
}
