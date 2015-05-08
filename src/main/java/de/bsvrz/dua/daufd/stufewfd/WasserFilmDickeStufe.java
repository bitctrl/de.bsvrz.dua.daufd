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
package de.bsvrz.dua.daufd.stufewfd;

import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

/**
 * Berechnet die WasserFilmDickeStufe aus den Messwerten Die eigentliche
 * berechnung ins fuer mehrere Module gemeinsam in der Klasse AbstraktStufe
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class WasserFilmDickeStufe extends AbstraktStufe {

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

	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub
	}

	/**
	 * WFD Stufen, die unterscheidet werden
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum WFDStufe {
		WFD_STUFE0, WFD_STUFE1, WFD_STUFE2, WFD_STUFE3, WFD_WERT_NV // Wert
		// nicht
		// verfuegbar
	}

	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	private final static WFDStufe[] MAP_INT_STUFE = new WFDStufe[] { WFDStufe.WFD_STUFE0, WFDStufe.WFD_STUFE1,
			WFDStufe.WFD_STUFE2, WFDStufe.WFD_STUFE3 };

	/**
	 * Konvertiert die WFD_stufe aus Integer ins symbolischen Format
	 *
	 * @param stufe
	 *            Stufe Int
	 * @return WFD Stufe symbolisch
	 */
	static public WFDStufe getStufe(final int stufe) {
		WFDStufe stufeSymb;
		if ((stufe < 0) || (stufe > WasserFilmDickeStufe.MAP_INT_STUFE.length)) {
			stufeSymb = WFDStufe.WFD_WERT_NV;
		} else {
			stufeSymb = WasserFilmDickeStufe.MAP_INT_STUFE[stufe];
		}
		return stufeSymb;
	}

	/**
	 * Konvertiert die WFD_stufe aus symbolischen Format ins Integer
	 *
	 * @param stufe
	 *            WFD_Stufe symbolisch
	 * @return Stufe int
	 */
	static public int getStufe(final WFDStufe stufe) {
		int intStufe = -1;
		if (stufe != WFDStufe.WFD_WERT_NV) {
			intStufe = stufe.ordinal();
		}
		return intStufe;
	}
}
