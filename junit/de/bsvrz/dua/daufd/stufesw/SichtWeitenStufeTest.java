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

package de.bsvrz.dua.daufd.stufesw;

import java.util.Collection;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dua.daufd.UfdsKlassifizierungParametrierung;
import de.bsvrz.sys.funclib.debug.Debug;


/**
 * Testet den Modul SichtWeite
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public class SichtWeitenStufeTest  {
	
	/**
	 * SW-Stufe untere Grenzwerte [AFo]
	 */
	private final double stufeVon[] = new double[] {
		0, 50, 80, 120, 250, 400 	
	};
	/**
	 * SW-Stufe obere Grenzwerte [AFo]
	 */
	private final double stufeBis[] = new double[] {
		60, 100, 150, 300, 500, 60000   // Max Wert vom DaK 	
	};
	
	private static final String TYP_UFDS_WFD = "typ.ufdsSichtWeite";
	private static final String ATG_UFDS_KLASS_WFD = "atg.ufdsKlassifizierungSichtWeite";
	private static final String ATT_UFDS_KLASS_WFD = "KlassifizierungSichtWeite";
	
	public void ParametriereUfds(ClientDavInterface dav, Collection<ConfigurationArea> konfBereiche) {
		try {
			UfdsKlassifizierungParametrierung param = new UfdsKlassifizierungParametrierung(
					TYP_UFDS_WFD, ATG_UFDS_KLASS_WFD, ATT_UFDS_KLASS_WFD, stufeVon, stufeBis);
			param.ParametriereUfds(dav, konfBereiche);
		} catch (Exception e) {
			Debug.getLogger().error("Fehler bei Parametrierung der SichtWeite:" + e.getMessage());
			e.printStackTrace();
		}
	}

}
