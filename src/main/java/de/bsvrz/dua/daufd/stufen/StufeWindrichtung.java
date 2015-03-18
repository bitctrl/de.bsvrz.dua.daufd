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
package de.bsvrz.dua.daufd.stufen;

import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

/**
 * Ermöglicht die Klassifizierung der Windrichtung in Stufen
 *
 * @author peuker
 *
 */
public class StufeWindrichtung extends AbstraktStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationWindRichtung";
	}

	@Override
	public String getKlassifizierungsAttribut() {
		return "KlassifizierungWindRichtung";
	}

	@Override
	public String getKlassifizierungsAttributGruppe() {
		return "atg.ufdsKlassifizierungWindRichtung";
	}

	@Override
	public String getMesswertAttribut() {
		return "WindRichtung";
	}

	@Override
	public String getMesswertAttributGruppe() {
		return "atg.ufdsWindRichtung";
	}

	@Override
	public String getStufeAttributGruppe() {
		return "atg.ufdsStufeWindRichtung";
	}

	@Override
	public String getSensorTyp() {
		return "typ.ufdsWindRichtung";
	}

	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
		// leer
	}

}
