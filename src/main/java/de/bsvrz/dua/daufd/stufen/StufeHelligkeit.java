/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Datenaufbereitung UFD
 * Copyright (C) 2008 BitCtrl Systems GmbH 
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
package de.bsvrz.dua.daufd.stufen;

import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

/**
 * Ermöglicht die Klassifizierung der Helligkeit in Stufen
 * 
 * @author uhlmann
 *
 */
public class StufeHelligkeit extends AbstraktStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationHelligkeit";
	}

	@Override
	public String getKlassifizierungsAttribut() {
		return "KlassifizierungHelligkeit";
	}

	@Override
	public String getKlassifizierungsAttributGruppe() {
		return "atg.ufdsKlassifizierungHelligkeit";
	}

	@Override
	public String getMesswertAttribut() {
		return "Helligkeit";
	}

	@Override
	public String getMesswertAttributGruppe() {
		return "atg.ufdsHelligkeit";
	}

	@Override
	public String getStufeAttributGruppe() {
		return "atg.ufdsStufeHelligkeit";
	}

	@Override
	public String getSensorTyp() {
		return "typ.ufdsHelligkeit";
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// leer
	}

}
