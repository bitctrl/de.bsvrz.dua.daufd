/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Datenaufbereitung UFD
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

package de.bsvrz.dua.daufd.vew;

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.hysterese.Hysterese;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Eine Erweiterugn der Klasse {@link AbstraktStufe}, die die Klassifizierung nach NI-FBZ-Klasse durchführt.
 *
 * @author Kappich Systemberatung
 */
public abstract class AbstraktNiFbzStufe extends AbstraktStufe {

	@Override
	public void initialisiere(final IVerwaltung verwaltung) throws DUAInitialisierungsException {
		super.initialisiere(verwaltung);
		initDaten(verwaltung);
	}

	@Override
	protected Hysterese leseStufen(final Data daten, final String suffix) {
		Hysterese defaultHysterese = super.leseStufen(daten, "");
		Hysterese regenHysterese = super.leseStufen(daten, "Regen");
		Hysterese schneeHysterese = super.leseStufen(daten, "Schnee");
		Hysterese platzregenHysterese = super.leseStufen(daten, "Platzregen");
		Hysterese glaetteHysterese = super.leseStufen(daten, "Glätte");
		if(regenHysterese.isEmpty()) regenHysterese = defaultHysterese;
		if(schneeHysterese.isEmpty()) schneeHysterese = defaultHysterese;
		if(platzregenHysterese.isEmpty()) platzregenHysterese = defaultHysterese;
		if(glaetteHysterese.isEmpty()) glaetteHysterese = defaultHysterese;
		return new MultiHysterese(regenHysterese, schneeHysterese, platzregenHysterese, glaetteHysterese);
	}

	private class MultiHysterese extends Hysterese {
		private final Hysterese _regen;
		private final Hysterese _schnee;
		private final Hysterese _platzregen;
		private final Hysterese _glaette;

		public MultiHysterese(final Hysterese regen, final Hysterese schnee, final Hysterese platzregen, final Hysterese glaette) {
			_regen = regen;
			_schnee = schnee;
			_platzregen = platzregen;
			_glaette = glaette;
		}

		public int getStufe(final double wert, final FBZ_Klasse nsFbzKlasse) {
			if(nsFbzKlasse == null) return -1;
			switch(nsFbzKlasse){
				case Regen:
					return _regen.getStufe(wert);
				case Schnee:
					return _schnee.getStufe(wert);
				case Platzregen:
					return _platzregen.getStufe(wert);
				case Glaette:
					return _glaette.getStufe(wert);
				default:
					return -1;
			}
		}
	}

	@Override
	protected int getStufe(final SystemObject objekt, final SensorParameter param, final double messwertGeglaettet) {
		return ((MultiHysterese)param.hysterese).getStufe(messwertGeglaettet, getNsFbzKlasse(objekt));
	}

	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {
		for(ResultData resultData : resultate) {
			aktualisiereMessstellenDaten(resultData);
		}
		super.aktualisiereDaten(resultate);
	}

	private FBZ_Klasse getNsFbzKlasse(final SystemObject objekt) {
		return messStellenDaten.get(objekt).getFbzKlasse(); 
	}
}
