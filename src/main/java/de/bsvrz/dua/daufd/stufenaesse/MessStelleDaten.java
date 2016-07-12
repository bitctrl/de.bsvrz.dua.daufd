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

package de.bsvrz.dua.daufd.stufenaesse;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe;
import de.bsvrz.dua.daufd.vew.FBZ_Klasse;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public class MessStelleDaten {
	/**
	 * Vorletzter zeitstempel
	 */
	public long vorletzteZeitStempel = Long.MIN_VALUE;
	/**
	 * SystemObjekt UmfdMessStelle
	 */
	public SystemObject messObject = null;
	/**
	 * Letzte empfangene NI_Stufe
	 */
	public NiederschlagIntensitaetStufe.NI_Stufe niStufe = NiederschlagIntensitaetStufe.NI_Stufe.NI_WERT_NV;
	/**
	 * Zeitstempel letzter empfangenen NI_Stufe
	 */
	public long niStufeZeitStempel = Long.MIN_VALUE;
	/**
	 * Letzte empfangene WFD_Stufe
	 */
	public WasserFilmDickeStufe.WFD_Stufe wfdStufe = WasserFilmDickeStufe.WFD_Stufe.WFD_WERT_NV;
	/**
	 * Zeitstempel letzter empfangenen WFD_Stufe
	 */
	public long wfdStufeZeitStempel = Long.MIN_VALUE;
	/**
	 * ZeitStempel der NaesseStufe der MessStelle
	 */
	public long nsStufeZeitStempel = Long.MIN_VALUE;
	/**
	 * NaesseStufe der MessStelle
	 */
	public NaesseStufe.NS_Stufe nsStufe = NaesseStufe.NS_Stufe.NS_WERT_NE;
	/**
	 *  NaesseStufe der MessStelle die erreicht werden soll bei verzoegerten Aenderungen
	 */
	public NaesseStufe.NS_Stufe zielNsStufe = NaesseStufe.NS_Stufe.NS_WERT_NE;
	/**
	 * Die aktuelle Niederschlagsart
	 */
	public int niederschlagsArt = -1;
	/**
	 *  der aktuelle FahrbahnOberflaecheZustand
	 */
	public int fbofZustand = -1;
	/**
	 *  ZeitStempel des letzten DS mit Niederschlagsart
	 */
	public long niederschlagsArtZeitStempel = 0;
	/**
	 *  ZeitStempel des Letzten DS mit FahrbahnOberflaecheZustand
	 */
	public long fbofZustandZeitStempel = 0;
	/**
	 *  Ob wir am letzten mal einen leeren DS bekommen haben
	 */
	public boolean keineDaten = true;
	/**
	 * Bestimmt, ob die Naessestufe unbestimmbar ist (haengt von NiederschlagsArt und
	 * FahrbahnoberflaecheZustand ab)
	 */
	public boolean unbestimmbar = false;

	/**
	 * Zeitstempel der zuletzt gesendeten NS-Stufe
	 */
	public long nsStufeZeitStempelGesendet = 0;

	public MessStelleDaten(SystemObject so) {
		messObject = so;
	}

	public FBZ_Klasse getFbzKlasse() {
		if(niederschlagsArt == 0
				|| (niederschlagsArt >= 40 && niederschlagsArt <= 42)
				|| (niederschlagsArt >= 50 && niederschlagsArt <= 59)
				|| (niederschlagsArt >= 60 && niederschlagsArt <= 69)
				|| (niederschlagsArt >= 80 && niederschlagsArt <= 84)
				|| (niederschlagsArt >= 90 && niederschlagsArt <= 99)
				){
			if(fbofZustand == 0
					|| fbofZustand == 1
					|| fbofZustand == 32){
				return FBZ_Klasse.Regen;
			}
		}	
		if((niederschlagsArt >= 70 && niederschlagsArt <= 79)
				|| (niederschlagsArt >= 85 && niederschlagsArt <= 88)
				){
			if(fbofZustand == 65){
				return FBZ_Klasse.Schnee;
			}
		}
		if(niederschlagsArt == 100) {
			return FBZ_Klasse.Platzregen;
		}
		if(fbofZustand == 64 || fbofZustand == 66 || fbofZustand == 67) return FBZ_Klasse.Glaette;
		return null;
	}
}
