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

package de.bsvrz.dua.daufd.stufenaesse;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.LinkedList;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickenStufeTest;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe.WFD_Stufe;
import de.bsvrz.sys.funclib.debug.Debug;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe.NI_Stufe;
import de.bsvrz.dua.daufd.stufenaesse.NaesseStufe.NS_Stufe;

/**
 * Testet den Modul NaesseStufe
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public class NaesseStufeTest implements ClientSenderInterface {
	
	/**
	 * Abtrocknungphasen Verzoegerung [AFo]
	 */
	private final long abtrocknungPhasen[] = new long[] {
		60, 60, 60, 180
	};
	/**
	 * Verbindung zum DAV
	 */
	private  ClientDavInterface DAV = null;
	/**
	 * Aspekt fuer Parametrierung
	 */
	private static final String ASP_PARAM_VORGABE = "asp.parameterVorgabe";
	/**
	 * Datenbeschreibung fuer die  Klasifizierung Daten
	 */
	private DataDescription DD_ABTROCKNUNG_PHASEN = null;
	/**
	 * Der Logger
	 */
	private Debug LOGGER = Debug.getLogger(); 
	
	private static final String TYP_UFDMS = "typ.umfeldDatenMessStelle";
	private static final String ATG_UFDMS_AP = "atg.ufdmsAbtrockungsPhasen";
	private static final String ATT_STUFE[] = new String [] { "ZeitNass4Nass3", "ZeitNass3Nass2",
		"ZeitNass2Nass1", "ZeitNass1Trocken"
	};

	/**
	 *  Tabelle aus AFo - Ermitellt aus WFD und NI stufe die NaesseStufe
	 * 
	 * Die Tabelle bildet WFDStufen an Tabellen von  NiStufen ab
	 * Jede Zeile ist eine Tabelle von NI-Stufen und NaesseStufen
	 */
	static Hashtable<WFD_Stufe, 
			Hashtable<NI_Stufe, NS_Stufe>> tabelle = new Hashtable<WFD_Stufe, Hashtable<NI_Stufe,NS_Stufe>>();
	static {
		Hashtable<NI_Stufe, NS_Stufe> zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_TROCKEN);
		tabelle.put(WFD_Stufe.WFD_STUFE0, zeile);
		
		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS1);
		tabelle.put(WFD_Stufe.WFD_STUFE1, zeile);
		

		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS2);
		tabelle.put(WFD_Stufe.WFD_STUFE2, zeile);
		
		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS3);
		tabelle.put(WFD_Stufe.WFD_STUFE3, zeile);
		
		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4); 
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_WERT_NV);
		tabelle.put(WFD_Stufe.WFD_WERT_NV, zeile);
	};

	/**
	 * Parametriert die Verzoegerung bei der Abtrocknungphasen
	 * 
	 * @param dav Verbindung zum Datenverteiler
	 * @param konfBereiche konfigurationsbereiche in denen alle Objekte parametriert werden sollen
	 */
	public void ParametriereUfds(ClientDavInterface dav, Collection<ConfigurationArea> konfBereiche) {
		try {
			
			this.DAV = dav;
			
			DD_ABTROCKNUNG_PHASEN = new DataDescription(
					DAV.getDataModel().getAttributeGroup(ATG_UFDMS_AP),
					DAV.getDataModel().getAspect(ASP_PARAM_VORGABE), (short)0);

			Collection<SystemObjectType> sotMenge = new LinkedList<SystemObjectType>();
			sotMenge.add(DAV.getDataModel().getType(TYP_UFDMS));
			
			Collection<SystemObject> ufdsObjekte = DAV.getDataModel().getObjects(konfBereiche, sotMenge, ObjectTimeSpecification.valid());
			
			if(ufdsObjekte == null) {
				LOGGER.error("Kein Objekt vom " + TYP_UFDMS + " in den KonfigurationsBeriechen :" + konfBereiche);
				System.exit(-1);
			}
			
			try {
				DAV.subscribeSender(this, ufdsObjekte, DD_ABTROCKNUNG_PHASEN, SenderRole.sender());
			} catch (Exception e) {
				LOGGER.error("Fehler bei Anmeldung für Klassifizierung der Objekte vom Typ " + TYP_UFDMS + ":" + e.getMessage());
				e.printStackTrace();
			}
			Thread.sleep(200);
			
			for(SystemObject so : ufdsObjekte ) {
				dataRequest(so, DD_ABTROCKNUNG_PHASEN, START_SENDING);
				//Thread.sleep(2);
			}
			//Thread.sleep(200);
			DAV.unsubscribeSender(this, ufdsObjekte, DD_ABTROCKNUNG_PHASEN);

		} catch (Exception e) {
			Debug.getLogger().error("Fehler bei Parametrierung der NaesseStufe Abtrocknungphasen: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		if(dataDescription.getAttributeGroup().getPid().equals(ATG_UFDMS_AP) 
				&& state == START_SENDING ) {
			
			Data datei = DAV.createData(DAV.getDataModel().getAttributeGroup(ATG_UFDMS_AP));
			
			for(int i =0; i< ATT_STUFE.length; i++) {
				datei.getTimeValue(ATT_STUFE[i]).setSeconds(abtrocknungPhasen[i]);
			}
			
			ResultData resDatei = new ResultData(object, DD_ABTROCKNUNG_PHASEN, System.currentTimeMillis(), datei);
			
			try {
				DAV.sendData(resDatei);
				System.out.println("Objekt " + object.getPid() + " parametriert " + ATG_UFDMS_AP);
			} catch (Exception e) {
				LOGGER.error("Fehler bei Sendung von Daten für Klassifizierung Niederschlaginetnsitaet des Objektes :" + object.getPid() + "\n Fehler:"+ e.getMessage());
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		return false;
	}

}
