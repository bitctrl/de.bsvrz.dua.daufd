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

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub

	}

	/**
	 * Sichtweite Stufen, die unterscheidet werden
	 * 
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum SW_Stufe {
		SW_STUFE0, SW_STUFE1, SW_STUFE2, SW_STUFE3, SW_STUFE4, SW_STUFE5, SW_WERT_NV // Wert
																						// nicht
																						// verfuegbar
	};

	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	protected final static SW_Stufe mapIntStufe[] = new SW_Stufe[] {
			SW_Stufe.SW_STUFE0, SW_Stufe.SW_STUFE1, SW_Stufe.SW_STUFE2,
			SW_Stufe.SW_STUFE3, SW_Stufe.SW_STUFE4, SW_Stufe.SW_STUFE5 };

	/**
	 * Ergibt die SW Stufe fuer ein bestimmtes sensor
	 * 
	 * @param sensor
	 *            Sensoer
	 * @return SW Stufe
	 */
	public SW_Stufe getStufe(SystemObject sensor) {

		SW_Stufe stufe;
		SensorParameter sensorDaten = this.sensorDaten.get(sensor);
		if (sensorDaten.stufe < 0 || sensorDaten.stufe > mapIntStufe.length)
			stufe = SW_Stufe.SW_WERT_NV;
		else
			stufe = mapIntStufe[sensorDaten.stufe];
		return stufe;
	}
     }
