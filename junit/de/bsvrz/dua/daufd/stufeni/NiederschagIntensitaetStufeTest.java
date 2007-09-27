/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.6 Abfrage Pufferdaten
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

package de.bsvrz.dua.daufd.stufeni;

import java.util.Collection;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dua.daufd.UfdsKlassifizierungParametrierung;
import de.bsvrz.sys.funclib.debug.Debug;


/**
 *  Testet den Modul NiederschalIntensitaetStufe
 *  
 *  @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public class NiederschagIntensitaetStufeTest  {
	
	/**
	 * NI-Stufe untere Grenzwerte [AFo]
	 */
	private final double stufeVon[] = new double[] {
		0.0, 0.2, 1.0, 4.0, 10.0 	
	};
	/**
	 * NI-Stufe obere Grenzwerte [AFo]
	 */
	private final double stufeBis[] = new double[] {
		0.3, 1.2, 5.0, 12.0, 200.0 // Max Wert vom DaK 	
	};
	
	private static final String TYP_UFDS_NI = "typ.ufdsNiederschlagsIntensität";
	private static final String ATG_UFDS_KLASS_NI = "atg.ufdsKlassifizierungNiederschlagsIntensität";
	private static final String ATT_UFDS_KLASS_NI = "KlassifizierungNiederschlagsIntensität";
	
	public void ParametriereUfds(ClientDavInterface dav, Collection<ConfigurationArea> konfBereiche) {
		try {
			UfdsKlassifizierungParametrierung param = new UfdsKlassifizierungParametrierung(
					TYP_UFDS_NI, ATG_UFDS_KLASS_NI, ATT_UFDS_KLASS_NI, stufeVon, stufeBis);
			param.ParametriereUfds(dav, konfBereiche);
		} catch (Exception e) {
			Debug.getLogger().error("Fehler bei Parametrierung der NiederschlagIntensitaet:" + e.getMessage());
			e.printStackTrace();
		}
	}

}
