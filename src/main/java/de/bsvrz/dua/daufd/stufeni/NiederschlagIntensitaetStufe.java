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
package de.bsvrz.dua.daufd.stufeni;

import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

/**
 * Berechnet die Niederschlagintensitaetstufe aus den Messwerten Die eigentliche
 * berechnung ins fuer mehrere Module gemeinsam in der Klasse AbstraktStufe
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class NiederschlagIntensitaetStufe extends AbstraktStufe {

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

	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub

	}

	/**
	 * NI Stufen, die unterscheidet werden
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum NIStufe {
		NI_STUFE0, NI_STUFE1, NI_STUFE2, NI_STUFE3, NI_STUFE4, NI_WERT_NV // Wert
		// nicht
		// verfuegbar
	};

	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	private static final NIStufe[] MAP_INT_STUFE = new NIStufe[] { NIStufe.NI_STUFE0, NIStufe.NI_STUFE1,
			NIStufe.NI_STUFE2, NIStufe.NI_STUFE3, NIStufe.NI_STUFE4 };

	/**
	 * Konvertiert die NI_stufe aus Integer ins symbolische Format
	 *
	 * @param stufe
	 *            Stufe int
	 * @return NI_Stufe symbolisch
	 */
	static public NIStufe getStufe(final int stufe) {
		NIStufe stufeSymb;
		if ((stufe < 0) || (stufe > NiederschlagIntensitaetStufe.MAP_INT_STUFE.length)) {
			stufeSymb = NIStufe.NI_WERT_NV;
		} else {
			stufeSymb = NiederschlagIntensitaetStufe.MAP_INT_STUFE[stufe];
		}
		return stufeSymb;
	}

	/**
	 * Konvertiert die NI_stufe aus symbolischen Format ins Integer
	 *
	 * @param stufe
	 *            NI_Stufe symbolisch
	 * @return Stufe int
	 */
	static public int getStufe(final NIStufe stufe) {
		int intStufe = -1;
		if (stufe != NIStufe.NI_WERT_NV) {
			intStufe = stufe.ordinal();
		}
		return intStufe;
	}

}
