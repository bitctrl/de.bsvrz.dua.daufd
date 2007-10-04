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

package de.bsvrz.dua.daufd.vew;

import java.util.Hashtable;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.hysterese.Hysterese;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

public  abstract class AbstraktStufe 
implements IBearbeitungsKnoten, ClientReceiverInterface, ClientSenderInterface {
	/**
	 * Debug-Logger
	 */
	protected static final Debug LOGGER = Debug.getLogger();
	/**
	 * Verbindung zum  Hauptmodul
	 */
	protected IVerwaltung verwaltung;
	/**
	 * der Nachste Bearbeitungsknoten
	 */
	private IBearbeitungsKnoten naechsterBearbeitungsKnoten = null;
	/**
	 * Ob man Daten ins DAV puiblizieren soll
	 */
	protected boolean publizieren = false;
	/**
	 * Datenbeschreibung fuer DS, die die Parametrierung fuer die Glaettung enthaten
	 */
	protected DataDescription DD_AGGREGATION;
	/**
	 * Datenbeschreibung fuer DS, die die Parametrierung fuer die Klassifikation enthalten
	 */
	protected DataDescription DD_KLASSIFIZIERUNG;
	/**
	 * Datenbeschreibung fuer Ausgabedatensaete
	 */
	protected DataDescription DD_QUELLE;
	
	/**
	 * Parameter und Daten, die pro Sensor gespeichert werden sollen
	 * 
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	private class SensorParameter {
		/**
		 * Untere Grenzwerte
		 */
		public double [] stufeVon = null;
		/**
		 * Obere Grenzwerte
		 */
		public double [] stufeBis = null;
		/**
		 * Koefizient fuer Glaettung
		 */
		public double b0 = 0;
		/**
		 * Koefizient fuer Glaettung
		 */
		public double fb = 0;
		/**
		 * Ermoeglicht die Hysterese zu berechenn
		 */
		public Hysterese  hysterese = null;
		/**
		 * Geglaettetes Messwert aus dem vorherigen Zyklus
		 */
		public double MesswertGlatti_1 = Double.NaN;

	};
		
	/**
	 * Attributgruppe des Objektes fuer Klassifizierung
	 */
	protected String ATG_KLASS = null;
	/**
	 * Attributgruppe des Objektes fuer Aggregation
	 */
	protected String ATG_AGGREG = null;
	/**
	 * Attributgruppe des Objektes fuer die Quelldaten
	 */
	protected String ATG_QUELLE = null;
	/**
	 * Attributgruppe der Daten zu bearbeiten
	 */ 
	protected String ATG_MESSWERTE = null;
	/**
	 * Attribut des Objektes fuer die Quelldaten
	 */
	protected String ATT_MESSWERTE = null;
	/**
	 * Attribut zur Parametrierung fuer Klassifizierung
	 */
	protected String ATT_KLASS = null;
	/**
	 * Abbildet dem SystemObjekt Sensor auf eine Sturuktur mit Parameter des Sensors
	 */
	protected Hashtable<SystemObject , SensorParameter> sensorDaten = new Hashtable<SystemObject, SensorParameter>();
	
	private final String ASP_SOLL_PARAM = "asp.parameterSoll";
	private final String ASP_KLASSIFIZIERUNG = "asp.klassifizierung";

	public void initialisiere(IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		this.verwaltung = verwaltung;	
		
		ATG_KLASS = getKlasseifizierungsAttributGruppe();
		ATG_AGGREG = getAggregationsAtrributGruppe();
		ATG_QUELLE = getStufeAttributGruppe();
		ATG_MESSWERTE = getMesswertAttributGruppe();
		ATT_MESSWERTE = getMesswertAttribut();
		ATT_KLASS = getKlasseifizierungsAttribut();
		
		
		DD_KLASSIFIZIERUNG = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_KLASS),
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_SOLL_PARAM), (short)0);
		
		DD_AGGREGATION = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_AGGREG),
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_SOLL_PARAM), (short)0);
		
		DD_QUELLE = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_QUELLE),
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_KLASSIFIZIERUNG), (short)0);
		
		
		for(SystemObject so: verwaltung.getSystemObjekte()) 
			try {			
				ResultData resultate = new ResultData(so, DD_QUELLE, System.currentTimeMillis(), null);
				verwaltung.getVerbindung().subscribeSource(this, resultate);
				sensorDaten.put(so, new SensorParameter());
		
			} catch (OneSubscriptionPerSendData e) {
				LOGGER.error("Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt" + so.getPid() + " unerfolgreich:" + e.getMessage());	
			}
		verwaltung.getVerbindung().subscribeReceiver(this, verwaltung.getSystemObjekte(), DD_AGGREGATION, ReceiveOptions.normal(), ReceiverRole.receiver());
		verwaltung.getVerbindung().subscribeReceiver(this, verwaltung.getSystemObjekte(), DD_KLASSIFIZIERUNG, ReceiveOptions.normal(), ReceiverRole.receiver());
	}	

	public void update(ResultData[] results) {
		for(ResultData resData : results) {
			DataDescription dataDescription = resData.getDataDescription();
			Data daten = resData.getData();
			if(daten == null) continue;
			SystemObject objekt = resData.getObject();
			SensorParameter param = sensorDaten.get(objekt);
			
			if(dataDescription.getAttributeGroup().getPid().equals(ATG_KLASS)) {
				Array stufen = daten.getArray(ATT_KLASS);
				
				int laenge  = stufen.getLength();
				param.stufeBis = new double[laenge];
				param.stufeVon = new double[laenge];
				
				for(int i=0; i<param.stufeBis.length; i++) {
					param.stufeVon[i] = stufen.getItem(i).getScaledValue("von").doubleValue();
					param.stufeBis[i] = stufen.getItem(i).getScaledValue("bis").doubleValue();
				}
				param.hysterese = new Hysterese();
				try {
					param.hysterese.initialisiere(param.stufeVon,param.stufeBis);
				} catch (Exception e) {
					LOGGER.error("Fehler bei Initialisierung der Hystereze Klasse:" + e.getMessage());
				}
			}
			else if(dataDescription.getAttributeGroup().getPid().equals(ATG_AGGREG)) {
				param.b0 = daten.getScaledValue("b0").doubleValue();
				param.fb = daten.getScaledValue("fb").doubleValue();
			} 
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {

		for(ResultData resData : resultate) {
			DataDescription dataDescription = resData.getDataDescription();
			Data daten = resData.getData();
			if(daten == null) continue;
			SystemObject objekt = resData.getObject();
			SensorParameter param = sensorDaten.get(objekt);
				
			if(dataDescription.getAttributeGroup().getPid().equals(ATG_MESSWERTE)) {
				int stufe = -1;
				if(daten.getItem(ATT_MESSWERTE).getItem("Wert").asUnscaledValue().longValue()>=0) {
					double messwert = daten.getItem(ATT_MESSWERTE).getItem("Wert").asScaledValue().doubleValue();
					double messwertGeglaettet = berechneMesswertGlaettung(param, messwert);
					stufe = param.hysterese.getStufe(messwertGeglaettet);
				}
				SendeStufe(resData.getObject(), stufe, resData.getDataTime());
			}
		}
		if(naechsterBearbeitungsKnoten !=  null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}

	public void SendeStufe(SystemObject objekt, int stufe, long zeitStempel) {
		Data data = verwaltung.getVerbindung().createData(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_QUELLE));
		data.getItem("Stufe").asUnscaledValue().set(stufe);
		
		ResultData resultat = new ResultData(objekt, DD_QUELLE, zeitStempel, data);
		try {
			verwaltung.getVerbindung().sendData(resultat);
		} catch (Exception e) {
			LOGGER.error("Fehler bei Sendung von daten fuer " + objekt.getPid() + " ATG " + ATG_QUELLE + " :\n" + e.getMessage());
		}
	}
	
	public double berechneMesswertGlaettung(SensorParameter param, double messwert) {
		double messwertGlatt;
		double b_i;
		
		// erstes Wert
		if(param.MesswertGlatti_1 == Double.NaN) {
			param.MesswertGlatti_1 = messwert;
			return messwert;
		}
		
		b_i = param.b0 + (1.0 - param.fb * param.MesswertGlatti_1/messwert);
		if(b_i<param.b0 || b_i>1.0) b_i = param.b0;
		
		messwertGlatt = b_i * messwert + (1.0 - b_i)*param.MesswertGlatti_1;
		
		param.MesswertGlatti_1 = messwertGlatt;
		return messwertGlatt;
	}

	
	
	public abstract String getKlasseifizierungsAttributGruppe();
	public abstract String getAggregationsAtrributGruppe();
	public abstract String getMesswertAttributGruppe();
	public abstract String getMesswertAttribut();
	public abstract String getStufeAttributGruppe();
	public abstract String getKlasseifizierungsAttribut();
	
	
	public void setNaechstenBearbeitungsKnoten(IBearbeitungsKnoten knoten) {
		this.naechsterBearbeitungsKnoten = knoten;
	}
	
	public void setPublikation(boolean publizieren) {
		this.publizieren = publizieren;
	}

	
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
	}

	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		return false;
	}
	
	public ModulTyp getModulTyp() {
		return null;
	}
}
