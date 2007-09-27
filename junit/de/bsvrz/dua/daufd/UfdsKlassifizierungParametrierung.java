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

package de.bsvrz.dua.daufd;

import java.util.Collection;
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
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Eine genesrische UFdsModul Parametrierung Klasse
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class UfdsKlassifizierungParametrierung implements ClientSenderInterface {

	/**
	 * Datenbeschreibung fuer die  Klasifizierung Daten
	 */
	private DataDescription DD_KLASIFIZIERUNG = null;
	/**
	 * Der Logger
	 */
	private Debug LOGGER = Debug.getLogger(); 
	/**
	 * Verbindung zum DAV
	 */
	private  ClientDavInterface DAV = null;
	/**
	 * Aspekt fuer Parametrierung
	 */
	private static final String ASP_PARAM_VORGABE = "asp.parameterVorgabe";
	/**
	 * Typ des Objektes
	 */
	private String TYP = null;
	/**
	 * Attributgruppe des Objektes
	 */
	private String ATG = null;
	/**
	 * Attribut zur Parametrierung
	 */
	private String ATT = null;
	/**
	 * Untere Grenzwerte
	 */
	private double [] stufeVon = null;
	/**
	 * Obere Grenzwerte
	 */
	private double [] stufeBis = null;
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param typ Typ des parametrierendes Objekts
	 * @param atg Attributgruppe des parametrierendes Objekts
	 * @param att Parametrierendes AttributArray
	 * @param stufeVon untere Grenzwerte
	 * @param stufeBis obere Grenzwerte
	 * @throws DUAInitialisierungsException Bei fehlerhafen Eingabe der Stufen
	 */
	public UfdsKlassifizierungParametrierung(String typ, String atg, String att, double [] stufeVon, double [] stufeBis) throws DUAInitialisierungsException {	
		this.TYP = typ;
		this.ATG = atg;
		this.ATT = att;
		
		if(stufeBis == null || stufeBis == null || stufeVon.length != stufeBis.length)
			throw new DUAInitialisierungsException("StufeVon oder StufeBis nicht korrekt eingegeben");
		
		this.stufeVon = stufeVon;
		this.stufeBis = stufeBis;
	}
	
	/**
	 * Parametriert die Klassifizierung alle Objekte vom Typ TYP mit Attributsgruppe ATG
	 * Setzt die Grenzwerte der Klasifizierungsstufen
	 * 
	 * @param dav Verbindung zum Datenverteiler
	 * @param konfBereiche konfigurationsbereiche in denen alle Objekte parametriert werden sollen
	 */
	public void ParametriereUfds(ClientDavInterface dav, Collection<ConfigurationArea> konfBereiche) {
		this.DAV = dav;
		
		DD_KLASIFIZIERUNG = new DataDescription(
				DAV.getDataModel().getAttributeGroup(ATG),
				DAV.getDataModel().getAspect(ASP_PARAM_VORGABE), (short)0);

		Collection<SystemObjectType> sotMenge = new LinkedList<SystemObjectType>();
		sotMenge.add(DAV.getDataModel().getType(TYP));
		
		Collection<SystemObject> ufdsObjekte = DAV.getDataModel().getObjects(konfBereiche, sotMenge, ObjectTimeSpecification.valid());
		
		if(ufdsObjekte == null) {
			LOGGER.error("Kein Objekt vom " + TYP + " in den KonfigurationsBeriechen :" + konfBereiche);
			System.exit(-1);
		}
		
		try {
			DAV.subscribeSender(this, ufdsObjekte, DD_KLASIFIZIERUNG, SenderRole.sender());
			// Der DAV ist zu langsam und antwortet mit "Sendeanmeldung nocht nicht bestaettigt" 
			Thread.sleep(800);
		} catch (Exception e) {
			LOGGER.error("Fehler bei Anmeldung für Klassifizierung der Objekte vom Typ " + TYP + ":" + e.getMessage());
			e.printStackTrace();
		}
		
		for(SystemObject so : ufdsObjekte ) {
			dataRequest(so, DD_KLASIFIZIERUNG, START_SENDING);
		}
		
		DAV.unsubscribeSender(this, ufdsObjekte, DD_KLASIFIZIERUNG);
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		if(dataDescription.getAttributeGroup().getPid().equals(ATG) 
				&& state == START_SENDING ) {
			
			Data datei = DAV.createData(DAV.getDataModel().getAttributeGroup(ATG));
			
			Data.Array stufen = datei.getArray(ATT);
			stufen.setLength(stufeBis.length);
			
			for(int i=0; i<stufeBis.length; i++) {
				stufen.getItem(i).getScaledValue("von").set(stufeVon[i]);
				stufen.getItem(i).getScaledValue("bis").set(stufeBis[i]);
			}
			ResultData resDatei = new ResultData(object, DD_KLASIFIZIERUNG, System.currentTimeMillis(), datei);
			
			try {
				DAV.sendData(resDatei);
				System.out.println("Objekt " + object.getPid() + " parametriert " + ATG);
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
