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
package de.bsvrz.dua.daufd.stufesw;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

public class SichtWeiteStufe  extends AbstraktStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationSichtWeite";
	}

	@Override
	public String getKlasseifizierungsAttribut() {
		return "KlassifizierungSichtWeite";
	}

	@Override
	public String getKlasseifizierungsAttributGruppe() {
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
	 *  Sichtweite Stufen, die unterscheidet werden
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum  SW_Stufe {
		SW_STUFE0,
		SW_STUFE1,
		SW_STUFE2,
		SW_STUFE3,
		SW_STUFE4,
		SW_STUFE5,
		SW_WERT_NV // Wert nicht verfuegbar
	};
	

	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	protected final static SW_Stufe mapIntStufe [] = new SW_Stufe [] 
    { SW_Stufe.SW_STUFE0, SW_Stufe.SW_STUFE1, SW_Stufe.SW_STUFE2, SW_Stufe.SW_STUFE3, SW_Stufe.SW_STUFE4, SW_Stufe.SW_STUFE5 };

	/**
	 * Ergibt die SW Stufe fuer ein bestimmtes sensor
	 * @param sensor Sensoer
	 * @return SW Stufe
	 */
	public SW_Stufe getStufe(SystemObject sensor) {
		
		SW_Stufe stufe;
		SensorParameter sensorDaten = this.sensorDaten.get(sensor);
		if( sensorDaten.stufe < 0 || sensorDaten.stufe > mapIntStufe.length)
			stufe = SW_Stufe.SW_WERT_NV;
		else stufe = mapIntStufe[sensorDaten.stufe];
		return stufe;
	}


}
