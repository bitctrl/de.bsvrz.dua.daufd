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

package de.bsvrz.dua.daufd.vew;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.hysterese.Hysterese;
import de.bsvrz.dua.daufd.hysterese.HystereseException;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Generelle Formel und Berechnungen fuer die Module
 * NiederschlagintensitaetStufe, SichtweiteStufe und WasserfilmdickeStufe  
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public  abstract class AbstraktStufe 
implements IBearbeitungsKnoten, ClientReceiverInterface, ClientSenderInterface {

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
	 * Sensoren, deren Daten bearebietet werden sollen
	 */
	protected Collection<SystemObject> sensoren = new LinkedList<SystemObject>();
	
	/**
	 * Parameter und Daten, die pro Sensor gespeichert werden sollen
	 * 
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	protected class SensorParameter {
		/**
		 * Letzter Datensatz
		 */
		public Data letzteDaten = null;
		/**
		 * ZeitStempel des letzten DS
		 */
		public long letzteDatenZeitStempel = 0;
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
		public double b0 = Double.NaN;
		/**
		 * Koefizient fuer Glaettung
		 */
		public double fb = Double.NaN;
		/**
		 * Ermoeglicht die Hysterese zu berechenn
		 */
		public Hysterese  hysterese = null;
		/**
		 * Geglaettetes Messwert aus dem vorherigen Zyklus
		 */
		public double MesswertGlatti_1 = Double.NaN;
		/**
		 * Letzte berechnete Stufe
		 */
		public int stufe = -1;
		/**
		 * Ob die Parameter initialisiert sind
		 */
		boolean initialisiert = false;
		/**
		 * Ob der Letzte eingekommene DS als "keine Daten" gekennzeichnet war
		 */
		boolean keineDaten = true;
	};
	/** 
	 *  Menge der Sensoren die zu eine Messstelle gehoeren
	 */
	protected static final String MNG_SENSOREN = "UmfeldDatenSensoren";
	/**
	 *  Aspekt Parameter-Soll
	 */
	protected final String ASP_SOLL_PARAM = "asp.parameterSoll";
	/**
	 *  Aspekt Klassifizierung
	 */
	protected final String ASP_KLASSIFIZIERUNG = "asp.klassifizierung";
	/**
	 * Abbildet dem SystemObjekt Sensor auf eine Sturuktur mit Parameter des Sensors
	 */
	protected Hashtable<SystemObject , SensorParameter> sensorDaten = new Hashtable<SystemObject, SensorParameter>();

	/**
	 * {@inheritDoc}
	 */
	public void initialisiere(IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		this.verwaltung = verwaltung;		
		DD_KLASSIFIZIERUNG = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(getKlassifizierungsAttributGruppe()),
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_SOLL_PARAM), (short)0);
		
		DD_AGGREGATION = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(getAggregationsAtrributGruppe()),
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_SOLL_PARAM), (short)0);
		
		DD_QUELLE = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(getStufeAttributGruppe()),
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_KLASSIFIZIERUNG), (short)0);
		
		if(verwaltung.getSystemObjekte() == null || verwaltung.getSystemObjekte().length == 0) return;
		
		for(SystemObject so: verwaltung.getSystemObjekte())  {
			if(!(so  instanceof ConfigurationObject)) continue;
			ConfigurationObject confObjekt = (ConfigurationObject)so;
			ObjectSet sensorMenge = confObjekt.getObjectSet(MNG_SENSOREN);
			for( SystemObject sensor : sensorMenge.getElements()) {
				if(sensor.isValid()){
					if(getSensorTyp().equals(sensor.getType().getPid())) {
						try {			
							verwaltung.getVerbindung().subscribeSender(this, sensor, DD_QUELLE, SenderRole.source());
							sensorDaten.put(sensor, new SensorParameter());
							sensoren.add(sensor);
						} catch (OneSubscriptionPerSendData e) {
							throw new DUAInitialisierungsException("Anmeldung als Quelle fuer Objekt" + so.getPid() + " unerfolgreich:" + e.getMessage());
						}
					}
				}
			}
		}
		verwaltung.getVerbindung().subscribeReceiver(this, sensoren, DD_AGGREGATION, ReceiveOptions.normal(), ReceiverRole.receiver());
		verwaltung.getVerbindung().subscribeReceiver(this, sensoren, DD_KLASSIFIZIERUNG, ReceiveOptions.normal(), ReceiverRole.receiver());
	}	

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] results) {
		for(ResultData resData : results) {
			DataDescription dataDescription = resData.getDataDescription();
			Data daten = resData.getData();
			SystemObject objekt = resData.getObject();
			SensorParameter param = sensorDaten.get(objekt);
			
			if(dataDescription.getAttributeGroup().getPid().equals(getKlassifizierungsAttributGruppe()) &&
					dataDescription.getAspect().getPid().equals(ASP_SOLL_PARAM)) {
				
				if(param == null) {
					Debug.getLogger().warning("Objekt " + objekt + " in der Hashtabelle nicht gefunden");
					continue;
				}
				else if(daten == null) {
					param.initialisiert = false;
					continue;
				}
				
				Array stufen = daten.getArray(getKlassifizierungsAttribut());
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
				} catch (HystereseException e) {
					Debug.getLogger().error("Fehler bei Initialisierung der Hystereze Klasse:" + e.getMessage());
					continue;
				}
				if(!Double.isNaN(param.b0) && !Double.isNaN(param.fb))
					param.initialisiert = true;
				if(param.letzteDaten != null && param.initialisiert)
					berechneAusgabe(objekt, param, param.letzteDatenZeitStempel);
			}
			else if(dataDescription.getAttributeGroup().getPid().equals(getAggregationsAtrributGruppe()) &&
					dataDescription.getAspect().getPid().equals(ASP_SOLL_PARAM)) {
				
				if(param == null) {
					Debug.getLogger().warning("Objekt " + objekt + " in der Hashtabelle nicht gefunden");
					continue;
				}
				else if(daten == null) {
					param.initialisiert = false;
					continue;
				}
				param.b0 = daten.getScaledValue("b0").doubleValue();
				param.fb = daten.getScaledValue("fb").doubleValue();
				if(param.hysterese != null)
					param.initialisiert = true;
				if(param.letzteDaten != null && param.initialisiert)
					berechneAusgabe(objekt, param, param.letzteDatenZeitStempel);
			} 
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {

		for(ResultData resData : resultate) {
			DataDescription dataDescription = resData.getDataDescription();
			SystemObject objekt = resData.getObject();
			Data daten = resData.getData();
			SensorParameter param = sensorDaten.get(objekt);
			if	(dataDescription.getAttributeGroup().getPid().equals(getMesswertAttributGruppe())) {
				if(daten == null) {
					if(!param.keineDaten) {
						param.keineDaten = true;
						SendeStufe(objekt, -1, resData.getDataTime(), true);
					}
					continue;
				}
				if(param == null) {
					Debug.getLogger().warning("Objekt " + objekt + " in der Hashtabelle nicht gefunden");
					continue;
				}
				param.letzteDaten = daten;
				if(param.initialisiert)
					berechneAusgabe(objekt, param, resData.getDataTime());
			}
		}
		if(naechsterBearbeitungsKnoten !=  null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}
	
	/**
	 * Berechnet die Ausgabe aus dem eingekommenen DS
	 * @param objekt Objekt
	 * @param param Parameter des Sensors
	 * @param zeitStempel des letzten DS
	 */
	public void berechneAusgabe(SystemObject objekt, SensorParameter param, long zeitStempel) {
		int stufe = -1;
		// hysterese ist null im fall, dass wir noch die Klasifizierungsparameter nicht bekommen haben
		if(param.letzteDaten.getItem(getMesswertAttribut()).getItem("Wert").asUnscaledValue().longValue()>=0) {
			double messwert = param.letzteDaten.getItem(getMesswertAttribut()).getItem("Wert").asScaledValue().doubleValue();
			double messwertGeglaettet = berechneMesswertGlaettung(param, messwert);
			stufe = param.hysterese.getStufe(messwertGeglaettet);
		}
		param.stufe  = stufe;
		param.keineDaten = false;
		SendeStufe(objekt, stufe, zeitStempel, false);
		param.letzteDaten = null;
	}

	/**
	 * Sendet einen Datensatz mit Messwert Klassifizierung
	 * @param objekt Sensor
	 * @param stufe Stufe
	 * @param zeitStempel Zeitpunkt
	 */
	public void SendeStufe(SystemObject objekt, int stufe, long zeitStempel, boolean keineDaten) {

		//  Mann muss ein Array dem naechsten Knoten weitergeben
		ResultData [] resultate = new ResultData[1];
		
		if(keineDaten)
			resultate[0] = new ResultData(objekt, DD_QUELLE, zeitStempel, null);
		else {
			Data data = verwaltung.getVerbindung().createData(
					verwaltung.getVerbindung().getDataModel().getAttributeGroup(getStufeAttributGruppe()));
			data.getItem("Stufe").asUnscaledValue().set(stufe); //$NON-NLS-1$
			resultate[0] = new ResultData(objekt, DD_QUELLE, zeitStempel, data);
		}

		try {
			verwaltung.getVerbindung().sendData(resultate);
		} catch (DataNotSubscribedException  e) {
			Debug.getLogger().error("Fehler bei Sendung von daten fuer " + objekt.getPid() + " ATG " + getStufeAttributGruppe() + " :\n" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (SendSubscriptionNotConfirmed e){
			Debug.getLogger().error("Fehler bei Sendung von daten fuer " + objekt.getPid() + " ATG " + getStufeAttributGruppe() + " :\n" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		if(naechsterBearbeitungsKnoten !=  null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}
	/**
	 * Berechnet die Glaettung nach der Formel in [AFo]
	 * @param param Sensorparameter (enthaelt Konstanten}
	 * @param messwert Messwert
	 * @return Geglaetettes Messwert
	 */
	public double berechneMesswertGlaettung(SensorParameter param, double messwert) {
		double messwertGlatt;
		double b_i;
		
		// erstes Wert
		if(Double.isNaN(param.MesswertGlatti_1)) {
			param.MesswertGlatti_1 = messwert;
			return messwert;
		}
		// Messwert gleich 0
		if(Math.abs(messwert)<0.000001) {
			param.MesswertGlatti_1 = 0.0;
			return 0.0;
		}
		
		b_i = param.b0 + (1.0 - param.fb * param.MesswertGlatti_1/messwert);
		if(b_i<param.b0 || b_i>1.0) b_i = param.b0;
		
		messwertGlatt = b_i * messwert + (1.0 - b_i)*param.MesswertGlatti_1;
		
		param.MesswertGlatti_1 = messwertGlatt;
		return messwertGlatt;
	}

	/**
	 * Erfragt die Klassifizierung ATG als Zeichenkette
	 * @return Klassifizierung ATG
	 */
	public abstract String getKlassifizierungsAttributGruppe();
	/**
	 * Erfragt die Aggregations ATG als Zeichenkette
	 * @return Aggregations ATG
	 */
	public abstract String getAggregationsAtrributGruppe();
	/**
	 * Erfragt die Messwert ATG als Zeichenkette
	 * @return Messwert ATG
	 */
	public abstract String getMesswertAttributGruppe();
	/**
	 * Erfragt den Attribut fuer das Sensorwert in Messwert ATG als Zeichenkette
	 * @return Messwert Attribut
	 */
	public abstract String getMesswertAttribut();
	/**
	 * Erfragt die Stufe ATG als Zeichenkette
	 * @return Stufe ATG
	 */
	public abstract String getStufeAttributGruppe();
	/**
	 * Erfragt den Attribut fuer Klassifizierung in Klassifizierung ATG als Zeichenkette
	 * @return Klasifizierung Attribut
	 */
	public abstract String getKlassifizierungsAttribut();
	/**
	 * Erfragt den Sensortyp als Zeichenkette
	 * @return Sensortyp
	 */
	public abstract String getSensorTyp();
	
	/**
	 * {@inheritDoc}
	 */
	public void setNaechstenBearbeitungsKnoten(IBearbeitungsKnoten knoten) {
		this.naechsterBearbeitungsKnoten = knoten;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setPublikation(boolean publizieren) {
		this.publizieren = publizieren;
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return null;
	}
	
	/**
	 * erfragt die menge der bearbeiteten Sensoren
	 * @return Menge der Sensoren
	 */
	public Collection<SystemObject> getSensoren() {
		return this.sensoren;
	}
}
