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
package de.bsvrz.dua.daufd.stufesw;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

/**
 * Berechnet die Sichtweitestufe aus den Messwerten Die eigentliche berechnung
 * ins fuer mehrere Module gemeinsam in der Klasse AbstraktStufe
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class SichtWeiteStufe extends AbstraktStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationSichtWeite";
	}

	@Override
	public String getKlassifizierungsAttribut() {
		return "KlassifizierungSichtWeite";
	}

	@Override
	public String getKlassifizierungsAttributGruppe() {
		return "atg.ufdsKlassifizierungSichtWeite";
	}

	@Override
	public String getMesswertAttribut() {
		return "SichtWeite";
	}

	@Override
	public String getMesswertAttributGruppe() {
		return "atg.ufdsSichtWeite";
	}

	@Override
	public String getStufeAttributGruppe() {
		return "atg.ufdsStufeSichtWeite";
	}

	@Override
	public String getSensorTyp() {
		return "typ.ufdsSichtWeite";
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub

	}

	/**
	 * Sichtweite Stufen, die unterscheidet werden
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum SWStufe {
		SW_STUFE0, SW_STUFE1, SW_STUFE2, SW_STUFE3, SW_STUFE4, SW_STUFE5, SW_WERT_NV // Wert
		// nicht
		// verfuegbar
	};

	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	private static final SWStufe[] MAP_INT_STUFE = new SWStufe[] {
		SWStufe.SW_STUFE0, SWStufe.SW_STUFE1, SWStufe.SW_STUFE2,
		SWStufe.SW_STUFE3, SWStufe.SW_STUFE4, SWStufe.SW_STUFE5 };

	/**
	 * Ergibt die SW Stufe fuer ein bestimmtes sensor
	 *
	 * @param sensor
	 *            Sensoer
	 * @return SW Stufe
	 */
	public SWStufe getStufe(final SystemObject sensor) {

		SWStufe stufe;
		final SensorParameter sensorDaten = this.sensorDaten.get(sensor);
		if ((sensorDaten.stufe < 0)
				|| (sensorDaten.stufe > SichtWeiteStufe.MAP_INT_STUFE.length)) {
			stufe = SWStufe.SW_WERT_NV;
		} else {
			stufe = SichtWeiteStufe.MAP_INT_STUFE[sensorDaten.stufe];
		}
		return stufe;
	}

	/**
	 * Berechnet die Glaettung nach der Formel in [AFo]
	 *
	 * @param param
	 *            Sensorparameter (enthaelt Konstanten}
	 * @param messwert
	 *            Messwert
	 * @return Geglaetettes Messwert
	 */
	@Override
	public double berechneMesswertGlaettung(final SensorParameter param,
			final double messwert) {
		double messwertGlatt;
		double bI;

		// erstes Wert
		if (Double.isNaN(param.messwertGlatti1)) {
			param.messwertGlatti1 = messwert;
			return messwert;
		}
		// alter geglaetteter Messwert gleich 0
		if (Math.abs(param.messwertGlatti1) < 0.000001) {
			param.messwertGlatti1 = messwert;
		}
		if (Math.abs(messwert) < 0.000001) {
			param.messwertGlatti1 = 0.0;
			return 0.0;
		}

		bI = param.b0
				+ (1.0 - ((param.fb * messwert) / param.messwertGlatti1));
		if (bI < param.b0) {
			bI = param.b0;
		}
		if (bI > 1.0) {
			bI = 1.0;
		}

		messwertGlatt = (bI * messwert)
				+ ((1.0 - bI) * param.messwertGlatti1);

		param.messwertGlatti1 = messwertGlatt;
		return messwertGlatt;
	}

}
