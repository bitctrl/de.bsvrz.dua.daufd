/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
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

package de.bsvrz.dua.daufd.tp;

import java.awt.image.ConvolveOp;
import java.util.Hashtable;

import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Berechnet dem Taupunkt von Luft- bzw. Fahrbahnoberflaeche- Temperatur
 * fuer alle Messstellen, die er vom Verwalfungmodul bekommt
 *  
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class Taupunkt implements IBearbeitungsKnoten, ClientSenderInterface {
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	/**
	 * Verbindung zum  Hauptmodul
	 */
	private IVerwaltung verwaltung;
	/**
	 * der Nachste Bearbeitungsknoten
	 */
	private IBearbeitungsKnoten naechsterBearbeitungsKnoten = null;
	/**
	 * Ob man Daten ins DAV puiblizieren soll
	 */
	private boolean publizieren = false;
	/**
	 * Die Umfeldtaten MessStellen
	 */
	private SystemObject [] umfdMessStellen;
	/**
	 * DatenBeschreibung des Datensatzes mit  Taupunkttemperatur  der Fahrbahn
	 */
	private DataDescription DD_UFDMS_TT_FB = null;
	/**
	 * DatenBeschreibung des Datensatzes mit  Taupunkttemperatur  der Fahrbahn
	 */
	private DataDescription DD_UFDMS_TT_L = null;
	
	private static final String ATG_UFDMS_TTFB = "atg.ufdmsTaupunktTemperaturFahrBahn"; 
	
	private static final String ATG_UFDMS_TTL = "atg.ufdmsTaupunktTemperaturLuft";
	
	private static final String ATG_UFDS_LT = "atg.ufdsLuftTemperatur";
	
	private static final String ATG_UFDS_FBOFT = "atg.ufdsFahrBahnOberFlächenTemperatur";
	
	private static final String ASP_ANALYSE = "asp.analyse";
	private static final String ASP_MESSWERT_ERSETZUNG = "asp.messWertErsetzung";
	private static final String MNG_SENSOREN = "UmfeldDatenSensoren";
	private static final String TYP_UFDS_LT = "typ.ufdsLuftTemperatur";
	private static final String TYP_UFDS_FBOFT = "typ.ufdsFahrBahnOberFlächenTemperatur";
	
	/**
	 * Hashtabelle, abbildet Senzoren auf Messstellen
	 */
	private Hashtable<SystemObject, SystemObject> mapSenzorMessStelle = new Hashtable<SystemObject, SystemObject>();
	
	public void aktualisiereDaten(ResultData[] resultate) {
		// TODO Auto-generated method stub
		for(ResultData resData : resultate) {
			
			Data data = resData.getData();
			if(data == null) continue;
			
			if( ATG_UFDS_LT.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_MESSWERT_ERSETZUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
				long T = data.getItem("LuftTemperatur").getUnscaledValue("Wert").longValue();
				if(T<0) continue;
				double t = data.getItem("LuftTemperatur").getScaledValue("Wert").doubleValue();
				
				SystemObject messStelle = mapSenzorMessStelle.get(resData.getObject());
				
				// ....
				
			}
			else if( ATG_UFDS_FBOFT.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_MESSWERT_ERSETZUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
				long T = data.getItem("LuftTemperatur").getUnscaledValue("Wert").longValue();
				if(T<0) continue;
				double t = data.getItem("LuftTemperatur").getScaledValue("Wert").doubleValue();
				
				// ....
				
			}
		}

		if(naechsterBearbeitungsKnoten !=  null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}

	public ModulTyp getModulTyp() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initialisiere(IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		this.verwaltung = verwaltung;
		if(! publizieren) return;
	
		
		DD_UFDMS_TT_FB = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_TTFB), 
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_ANALYSE));
		
		DD_UFDMS_TT_L = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_TTL), 
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_ANALYSE));
		
		this.umfdMessStellen = verwaltung.getSystemObjekte();
		
		for(SystemObject so: umfdMessStellen) 
			try {
				if(!(so  instanceof ConfigurationObject)) continue;
				
				ConfigurationObject confObjekt = (ConfigurationObject)so;
				ObjectSet sensorMenge = confObjekt.getObjectSet(MNG_SENSOREN);
				for( SystemObject sensor : sensorMenge.getElements()) {
					if(TYP_UFDS_LT.equals(sensor.getType().getPid())) {
						mapSenzorMessStelle.put(sensor, so);
						
						ResultData resultate = new ResultData(so, DD_UFDMS_TT_L, System.currentTimeMillis(), null);
						verwaltung.getVerbindung().subscribeSource(this, resultate);
					}
					else if(TYP_UFDS_FBOFT.equals(sensor.getType().getPid())) {
						mapSenzorMessStelle.put(sensor, so);
						
						ResultData resultate = new ResultData(so, DD_UFDMS_TT_FB, System.currentTimeMillis(), null);
						verwaltung.getVerbindung().subscribeSource(this, resultate);
					}
				}
			} catch (OneSubscriptionPerSendData e) {
				LOGGER.error("Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt" + so.getPid() + " unerfolgreich:" + e.getMessage());	
			}
	}

	public void setNaechstenBearbeitungsKnoten(IBearbeitungsKnoten knoten) {
		this.naechsterBearbeitungsKnoten = knoten;
	}

	public void setPublikation(boolean publizieren) {
		this.publizieren = publizieren;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub
	}

	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// TODO Auto-generated method stub
	}

	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		// TODO Auto-generated method stub
		return false;
	}

}
