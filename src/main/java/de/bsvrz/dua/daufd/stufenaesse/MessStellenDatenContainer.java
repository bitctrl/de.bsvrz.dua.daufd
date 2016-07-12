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

import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * TBD Dokumentation
 *
 * @author Kappich Systemberatung
 */
public abstract class MessStellenDatenContainer {
	/**
	 * Ermoeglicht die Abbildung der Sensoren und MessStellen auf die Klasse mit Lokalen Daten fuer die gegebene MessStelle
	 */
	protected Hashtable<SystemObject, MessStelleDaten> messStellenDaten = new Hashtable<SystemObject, MessStelleDaten>();
	/**
	 * Sensoren, die NiederschlagsArtDaten liefern
	 */
	protected Collection<SystemObject> naSensoren = new LinkedList<SystemObject>();
	/**
	 * Sensoren, die FahrBahnOberFlächenZustandDaten liefern
	 */
	protected Collection<SystemObject> fbofZustandSensoren = new LinkedList<SystemObject>();
	/**
	 * Sensoren, die Niederschlagsintensitaet liefern
	 */
	protected Collection<SystemObject> niSensoren = new LinkedList<SystemObject>();
	/**
	 * Sensoren, die WasserFilmDicke liefern
	 */
	protected Collection<SystemObject> wfdSensoren = new LinkedList<SystemObject>();

	protected boolean aktualisiereMessstellenDaten(final ResultData resData) {

		SystemObject so = resData.getObject();
		MessStelleDaten msDaten = messStellenDaten.get(so);
		long zeitStempel = resData.getDataTime();

		Data data = resData.getData();

		if(NaesseStufe.ATG_NI_STUFE.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
				NaesseStufe.ASP_KLASSIFIZIERUNG.equals(resData.getDataDescription().getAspect().getPid())) {
			if(msDaten == null) {
				Debug.getLogger().warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
				return true;
			}
			else if(data == null) {
				if(!msDaten.keineDaten) {
					msDaten.nsStufeZeitStempel = zeitStempel;
				}
				return true;
			}

			int stufe = data.getItem("Stufe").asUnscaledValue().intValue();
			NiederschlagIntensitaetStufe.NI_Stufe niStufe = NiederschlagIntensitaetStufe.getStufe(stufe);

			synchronized(msDaten) {
				msDaten.niStufe = niStufe;
				msDaten.vorletzteZeitStempel = msDaten.niStufeZeitStempel;
				msDaten.niStufeZeitStempel = zeitStempel;
			}
		}
		else if(NaesseStufe.ATG_WFD_STUFE.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
				NaesseStufe.ASP_KLASSIFIZIERUNG.equals(resData.getDataDescription().getAspect().getPid())) {
			if(msDaten == null) {
				Debug.getLogger().warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
				return true;
			}
			else if(data == null) {
				if(!msDaten.keineDaten) {
					msDaten.nsStufeZeitStempel = zeitStempel;
				}
				return true;
			}

			int stufe = data.getItem("Stufe").asUnscaledValue().intValue();
			WasserFilmDickeStufe.WFD_Stufe wfdStufe = WasserFilmDickeStufe.getStufe(stufe);

			synchronized(msDaten) {
				msDaten.wfdStufe = wfdStufe;
				msDaten.vorletzteZeitStempel = msDaten.wfdStufeZeitStempel;
				msDaten.wfdStufeZeitStempel = zeitStempel;
			}
		}
		else if(NaesseStufe.ATG_UFDS_NA.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
				NaesseStufe.ASP_MESSWERTERSETZUNG.equals(resData.getDataDescription().getAspect().getPid())) {

			if(msDaten == null) {
				Debug.getLogger().warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
				return true;
			}
			else if(data == null) {
				if(!msDaten.keineDaten) {
					msDaten.nsStufeZeitStempel = zeitStempel;
				}
				return true;
			}

			int implausibel = data.getItem("NiederschlagsArt").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
			int niederschlagsArt = data.getItem("NiederschlagsArt").getUnscaledValue("Wert").intValue();

			synchronized(msDaten) {
				if(implausibel == 0)
					msDaten.niederschlagsArt = niederschlagsArt;

				msDaten.vorletzteZeitStempel = msDaten.niederschlagsArtZeitStempel;
				msDaten.niederschlagsArtZeitStempel = zeitStempel;
			}
		}
		else if(NaesseStufe.ATG_UFDS_FBOFZS.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
				NaesseStufe.ASP_MESSWERTERSETZUNG.equals(resData.getDataDescription().getAspect().getPid())) {

			if(msDaten == null) {
				Debug.getLogger().warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
				return true;
			}
			else if(data == null) {
				if(!msDaten.keineDaten) {
					msDaten.nsStufeZeitStempel = zeitStempel;
				}
				return true;
			}
			int implausibel = data.getItem("FahrBahnOberFlächenZustand")
					.getItem("Status")
					.getItem("MessWertErsetzung")
					.getUnscaledValue("Implausibel")
					.intValue();
			int fbZustand = data.getItem("FahrBahnOberFlächenZustand").getUnscaledValue("Wert").intValue();

			synchronized(msDaten) {
				if(implausibel == 0)
					msDaten.fbofZustand = fbZustand;
				msDaten.vorletzteZeitStempel = msDaten.fbofZustandZeitStempel;
				msDaten.fbofZustandZeitStempel = zeitStempel;

			}
		}
		return false;
	}

	protected void initDaten(final IVerwaltung verwaltung) throws DUAInitialisierungsException {
		for(SystemObject so : verwaltung.getSystemObjekte()) {
			if(so == null) continue;
			MessStelleDaten messStelleDaten = getMessStelleDaten(so);

			ConfigurationObject confObjekt = (ConfigurationObject) so;
			ObjectSet sensorMenge = confObjekt.getObjectSet(NaesseStufe.MNG_SENSOREN);
			messStellenDaten.put(so, messStelleDaten);
			for(SystemObject sensor : sensorMenge.getElements()) {
				if(sensor.isValid()) {
					if(NaesseStufe.TYP_UFDS_NA.equals(sensor.getType().getPid())) {
						naSensoren.add(sensor);
						messStellenDaten.put(sensor, messStelleDaten);
					}
					else if(NaesseStufe.TYP_UFDS_FBOFZS.equals(sensor.getType().getPid())) {
						fbofZustandSensoren.add(sensor);
						messStellenDaten.put(sensor, messStelleDaten);
					}
					else if(NaesseStufe.TYP_UFDS_NI.equals(sensor.getType().getPid())) {
						niSensoren.add(sensor);
						messStellenDaten.put(sensor, messStelleDaten);
					}
					else if(NaesseStufe.TYP_UFDS_WFD.equals(sensor.getType().getPid())) {
						wfdSensoren.add(sensor);
						messStellenDaten.put(sensor, messStelleDaten);
					}
				}
			}
		}
	}

	protected MessStelleDaten getMessStelleDaten(final SystemObject so) {
		return new MessStelleDaten(so);
	}
}
