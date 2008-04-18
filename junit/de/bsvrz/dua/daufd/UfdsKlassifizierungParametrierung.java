/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD
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
 * Eine generische UfdsModul Parametrierung Klasse, parametriert die 
 * Attributgruppen Klasifizierung und Aggregation, erwartet, dass
 * die Parametrierung der Parametrierung schon gemacht wurde
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
	 * Datenbeschreibung fuer die  Aggregation Daten
	 */
	private DataDescription DD_AGGREGATION = null;

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
	 * Attributgruppe des Objektes fuer Klassifizierung
	 */
	private String ATG_KLASS = null;
	/**
	 * Attributgruppe des Objektes fuer Aggregation
	 */
	private String ATG_AGGREG = null;
	/**
	 * Attribut zur Parametrierung fuer Klassifizierung
	 */
	private String ATT_KLASS = null;
	/**
	 * Untere Grenzwerte
	 */
	private double [] stufeVon = null;
	/**
	 * Obere Grenzwerte
	 */
	private double [] stufeBis = null;
	/**
	 * Koefizient fuer Glaettung
	 */
	private double b0;
	/**
	 * Koefizient fuer Glaettung
	 */
	private double fb;
	
	/**
	 * Standardkonstruktor
	 * 
	 * @param typ Typ des parametrierendes Objekts
	 * @param atgKlassifizierung Attributgruppe des parametrierendes Objekts
	 * @param attKlasifizierung Parametrierendes AttributArray
	 * @param stufeVon untere Grenzwerte
	 * @param stufeBis obere Grenzwerte
	 * @param b0 b_0 Koefizient
 	 * @param fb f_b Koefizient
	 * @throws DUAInitialisierungsException Bei fehlerhafen Eingabe der Stufen
	 */
	public UfdsKlassifizierungParametrierung(String typ, String atgKlassifizierung, String attKlasifizierung, String atgAggregation, double [] stufeVon, double [] stufeBis, double b0, double fb) throws DUAInitialisierungsException {	
		this.TYP = typ;
		this.ATG_KLASS = atgKlassifizierung;
		this.ATT_KLASS = attKlasifizierung;
		this.ATG_AGGREG = atgAggregation;
		
		if(stufeBis == null || stufeBis == null || stufeVon.length != stufeBis.length)
			throw new DUAInitialisierungsException("StufeVon oder StufeBis nicht korrekt eingegeben");
		
		this.stufeVon = stufeVon;
		this.stufeBis = stufeBis;
		this.fb = fb;
		this.b0 = b0;
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
				DAV.getDataModel().getAttributeGroup(ATG_KLASS),
				DAV.getDataModel().getAspect(ASP_PARAM_VORGABE), (short)0);
		
		DD_AGGREGATION = new DataDescription(
				DAV.getDataModel().getAttributeGroup(ATG_AGGREG),
				DAV.getDataModel().getAspect(ASP_PARAM_VORGABE), (short)0);

		Collection<SystemObjectType> sotMenge = new LinkedList<SystemObjectType>();
		sotMenge.add(DAV.getDataModel().getType(TYP));
		
		Collection<SystemObject> ufdsObjekte = DAV.getDataModel().getObjects(konfBereiche, sotMenge, ObjectTimeSpecification.valid());
		
		if(ufdsObjekte == null) {
			Debug.getLogger().error("Kein Objekt vom " + TYP + " in den KonfigurationsBeriechen :" + konfBereiche);
			System.exit(-1);
		}
		
		try {
			DAV.subscribeSender(this, ufdsObjekte, DD_KLASIFIZIERUNG, SenderRole.sender());
			// Der DAV ist zu langsam und antwortet mit "Sendeanmeldung nocht nicht bestaettigt"
			Thread.sleep(100);
			DAV.subscribeSender(this, ufdsObjekte, DD_AGGREGATION, SenderRole.sender());
			// Der DAV ist zu langsam und antwortet mit "Sendeanmeldung nocht nicht bestaettigt" 
			Thread.sleep(100);
		} catch (Exception e) {
			Debug.getLogger().error("Fehler bei Anmeldung für Klassifizierung der Objekte vom Typ " + TYP + ":" + e.getMessage());
			e.printStackTrace();
		}
		
//		Der dataRequest wird automatisch vom Datenverteiler getriggert
//		for(SystemObject so : ufdsObjekte ) {
//			dataRequest(so, DD_KLASIFIZIERUNG, START_SENDING);
//			dataRequest(so, DD_AGGREGATION, START_SENDING);
//		}
		
		DAV.unsubscribeSender(this, ufdsObjekte, DD_KLASIFIZIERUNG);
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		if(dataDescription.getAttributeGroup().getPid().equals(ATG_KLASS) 
				&& state == START_SENDING ) {
			
			Data datei = DAV.createData(DAV.getDataModel().getAttributeGroup(ATG_KLASS));
			
			Data.Array stufen = datei.getArray(ATT_KLASS);
			stufen.setLength(stufeBis.length);
			
			for(int i=0; i<stufeBis.length; i++) {
				stufen.getItem(i).getScaledValue("von").set(stufeVon[i]);
				stufen.getItem(i).getScaledValue("bis").set(stufeBis[i]);
			}
			ResultData resDatei = new ResultData(object, DD_KLASIFIZIERUNG, System.currentTimeMillis(), datei);
			
			try {
				DAV.sendData(resDatei);
				System.out.println("Objekt " + object.getPid() + " Atg: " + ATG_KLASS + " parametriert " );
			} catch (Exception e) {
				Debug.getLogger().error("Fehler bei Sendung von Daten für Klassifizierung des Objektes :" + object.getPid() + "\n Fehler:"+ e.getMessage());
				e.printStackTrace();
			}
		}
		else if(dataDescription.getAttributeGroup().getPid().equals(ATG_AGGREG) 
				&& state == START_SENDING ) {

			Data datei = DAV.createData(DAV.getDataModel().getAttributeGroup(ATG_AGGREG));
			
			datei.getScaledValue("b0").set(b0);
			datei.getScaledValue("fb").set(fb);
			
			ResultData resDatei = new ResultData(object, DD_AGGREGATION, System.currentTimeMillis(), datei);
			
			try {
				DAV.sendData(resDatei);
				System.out.println("Objekt " + object.getPid() + " Atg: " + ATG_AGGREG + " parametriert " );
			} catch (Exception e) {
				Debug.getLogger().error("Fehler bei Sendung von Daten für Aggregation des Objektes :" + object.getPid() + "\n Fehler:"+ e.getMessage());
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
