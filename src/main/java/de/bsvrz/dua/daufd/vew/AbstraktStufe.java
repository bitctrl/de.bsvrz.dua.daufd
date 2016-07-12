/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Datenaufbereitung UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * Copyright 2015 by Kappich Systemberatung Aachen
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

package de.bsvrz.dua.daufd.vew;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.Data.Array;
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.hysterese.Hysterese;
import de.bsvrz.dua.daufd.hysterese.HystereseException;
import de.bsvrz.dua.daufd.stufenaesse.MessStellenDatenContainer;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Generelle Formel und Berechnungen fuer die Module
 * NiederschlagintensitaetStufe, SichtweiteStufe und WasserfilmdickeStufe
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public abstract class AbstraktStufe extends MessStellenDatenContainer implements IBearbeitungsKnoten,
		ClientReceiverInterface, ClientSenderInterface {

	private static final Debug _debug = Debug.getLogger();
	
	/**
	 * Verbindung zum Hauptmodul
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
	 * Datenbeschreibung fuer DS, die die Parametrierung fuer die Glaettung
	 * enthaten
	 */
	protected DataDescription DD_AGGREGATION;
	/**
	 * Datenbeschreibung fuer DS, die die Parametrierung fuer die Klassifikation
	 * enthalten
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
		public Hysterese hysterese = null;
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
	 * Menge der Sensoren die zu eine Messstelle gehoeren
	 */
	protected static final String MNG_SENSOREN = "UmfeldDatenSensoren";
	/**
	 * Aspekt Parameter-Soll
	 */
	protected final String ASP_SOLL_PARAM = "asp.parameterSoll";
	/**
	 * Aspekt Klassifizierung
	 */
	protected final String ASP_KLASSIFIZIERUNG = "asp.klassifizierung";
	/**
	 * Abbildet dem SystemObjekt Sensor auf eine Sturuktur mit Parameter des
	 * Sensors
	 */
	protected Hashtable<SystemObject, SensorParameter> sensorDaten = new Hashtable<SystemObject, SensorParameter>();

	/**
	 * {@inheritDoc}
	 */
	public void initialisiere(IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		this.verwaltung = verwaltung;
		DD_KLASSIFIZIERUNG = new DataDescription(verwaltung.getVerbindung()
				.getDataModel()
				.getAttributeGroup(getKlassifizierungsAttributGruppe()),
				verwaltung.getVerbindung().getDataModel()
						.getAspect(ASP_SOLL_PARAM));

		DD_AGGREGATION = new DataDescription(verwaltung.getVerbindung()
				.getDataModel()
				.getAttributeGroup(getAggregationsAtrributGruppe()), verwaltung
				.getVerbindung().getDataModel().getAspect(ASP_SOLL_PARAM));

		DD_QUELLE = new DataDescription(verwaltung.getVerbindung()
				.getDataModel().getAttributeGroup(getStufeAttributGruppe()),
				verwaltung.getVerbindung().getDataModel()
						.getAspect(ASP_KLASSIFIZIERUNG));

		if (verwaltung.getSystemObjekte() == null
				|| verwaltung.getSystemObjekte().length == 0)
			return;

		for (SystemObject so : verwaltung.getSystemObjekte()) {
			if (!(so instanceof ConfigurationObject))
				continue;
			ConfigurationObject confObjekt = (ConfigurationObject) so;
			ObjectSet sensorMenge = confObjekt.getObjectSet(MNG_SENSOREN);
			for (SystemObject sensor : sensorMenge.getElements()) {
				if (sensor.isValid()) {
					if (getSensorTyp().equals(sensor.getType().getPid())) {
						try {
							verwaltung.getVerbindung().subscribeSender(this,
									sensor, DD_QUELLE, SenderRole.source());
							sensorDaten.put(sensor, new SensorParameter());
							sensoren.add(sensor);
						} catch (OneSubscriptionPerSendData e) {
							throw new DUAInitialisierungsException(
									"Anmeldung als Quelle fuer Objekt"
											+ so.getPid() + " unerfolgreich:"
											+ e.getMessage());
						}
					}
				}
			}
		}
		verwaltung.getVerbindung().subscribeReceiver(this, sensoren,
				DD_AGGREGATION, ReceiveOptions.normal(),
				ReceiverRole.receiver());
		verwaltung.getVerbindung().subscribeReceiver(this, sensoren,
				DD_KLASSIFIZIERUNG, ReceiveOptions.normal(),
				ReceiverRole.receiver());
	}

	/**
	 * {@inheritDoc}
	 */
	public void update(ResultData[] results) {
		for (ResultData resData : results) {
			DataDescription dataDescription = resData.getDataDescription();
			Data daten = resData.getData();
			SystemObject objekt = resData.getObject();
			SensorParameter param = sensorDaten.get(objekt);

			if (dataDescription.getAttributeGroup().getPid()
					.equals(getKlassifizierungsAttributGruppe())
					&& dataDescription.getAspect().getPid()
							.equals(ASP_SOLL_PARAM)) {

				if (param == null) {
					Debug.getLogger().warning(
							"Objekt " + objekt
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (daten == null) {
					param.initialisiert = false;
					continue;
				}

				param.hysterese = leseStufen(daten, "");
				
				if (!Double.isNaN(param.b0) && !Double.isNaN(param.fb))
					param.initialisiert = true;
				if (param.letzteDaten != null && param.initialisiert)
					berechneAusgabe(objekt, param, param.letzteDatenZeitStempel);
			} else if (dataDescription.getAttributeGroup().getPid()
					.equals(getAggregationsAtrributGruppe())
					&& dataDescription.getAspect().getPid()
							.equals(ASP_SOLL_PARAM)) {

				if (param == null) {
					Debug.getLogger().warning(
							"Objekt " + objekt
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (daten == null) {
					param.initialisiert = false;
					continue;
				}
				param.b0 = daten.getScaledValue("b0").doubleValue();
				param.fb = daten.getScaledValue("fb").doubleValue();
				if (param.hysterese != null)
					param.initialisiert = true;
				if (param.letzteDaten != null && param.initialisiert)
					berechneAusgabe(objekt, param, param.letzteDatenZeitStempel);
			}
		}
	}

	protected Hysterese leseStufen(final Data daten, final String suffix) {
		try {
			Array stufen = daten.getArray(getKlassifizierungsAttribut() + suffix);

			int laenge = stufen.getLength();
			final double[] stufeBis = new double[laenge];
			final double[] stufeVon = new double[laenge];

			for(int i = 0; i < laenge; i++) {
				stufeVon[i] = stufen.getItem(i).getScaledValue("von").doubleValue();
				stufeBis[i] = stufen.getItem(i).getScaledValue("bis").doubleValue();
			}

			final Hysterese hysterese = new Hysterese();
			try {
				hysterese.initialisiere(stufeVon, stufeBis);
			}
			catch(HystereseException e) {
				throw new IllegalArgumentException("Fehler bei Initialisierung der Hysterese Klasse", e);
			}
			return hysterese;
		}
		catch(Exception e) {
			_debug.warning("Kann Klassifizierung nicht lesen, Datenmodell möglicherweise veraltet", e);
			return new Hysterese();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {

		for (ResultData resData : resultate) {
			DataDescription dataDescription = resData.getDataDescription();
			SystemObject objekt = resData.getObject();
			Data daten = resData.getData();
			SensorParameter param = sensorDaten.get(objekt);
			if (dataDescription.getAttributeGroup().getPid()
					.equals(getMesswertAttributGruppe())) {
				if (daten == null) {
					if (!param.keineDaten) {
						param.keineDaten = true;
						sendeStufe(objekt, -1, resData.getDataTime(), true);
					}
					continue;
				}
				if (param == null) {
					Debug.getLogger().warning(
							"Objekt " + objekt
									+ " in der Hashtabelle nicht gefunden");
					continue;
				}
				param.letzteDaten = daten;
				if (param.initialisiert)
					berechneAusgabe(objekt, param, resData.getDataTime());
			}
		}
		if (naechsterBearbeitungsKnoten != null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}

	/**
	 * Berechnet die Ausgabe aus dem eingekommenen DS
	 * 
	 * @param objekt
	 *            Objekt
	 * @param param
	 *            Parameter des Sensors
	 * @param zeitStempel
	 *            des letzten DS
	 */
	public void berechneAusgabe(SystemObject objekt, SensorParameter param,
			long zeitStempel) {
		int stufe;
		// hysterese ist null im fall, dass wir noch die
		// Klasifizierungsparameter nicht bekommen haben
		if (param.letzteDaten.getItem(getMesswertAttribut()).getItem("Wert")
				.asUnscaledValue().longValue() >= 0) {
			double messwert = param.letzteDaten.getItem(getMesswertAttribut())
					.getItem("Wert").asScaledValue().doubleValue();
			double messwertGeglaettet = berechneMesswertGlaettung(param,
					messwert);
			stufe = getStufe(objekt, param, messwertGeglaettet);
		}
		else {
			param.MesswertGlatti_1 = Double.NaN;
			stufe = -1;
		}
		param.stufe = stufe;
		param.keineDaten = false;
		sendeStufe(objekt, stufe, zeitStempel, false);
		param.letzteDaten = null;
	}

	protected int getStufe(final SystemObject objekt, final SensorParameter param, final double messwertGeglaettet) {
		return param.hysterese.getStufe(messwertGeglaettet);
	}

	/**
	 * Sendet einen Datensatz mit Messwert Klassifizierung
	 * 
	 * @param objekt
	 *            Sensor
	 * @param stufe
	 *            Stufe
	 * @param zeitStempel
	 *            Zeitpunkt
	 */
	public void sendeStufe(SystemObject objekt, int stufe, long zeitStempel,
			boolean keineDaten) {

		// Mann muss ein Array dem naechsten Knoten weitergeben
		ResultData[] resultate = new ResultData[1];

		if (keineDaten)
			resultate[0] = new ResultData(objekt, DD_QUELLE, zeitStempel, null);
		else {
			Data data = verwaltung.getVerbindung().createData(
					verwaltung.getVerbindung().getDataModel()
							.getAttributeGroup(getStufeAttributGruppe()));
			data.getItem("Stufe").asUnscaledValue().set(stufe); //$NON-NLS-1$
			resultate[0] = new ResultData(objekt, DD_QUELLE, zeitStempel, data);
		}

		try {
			verwaltung.getVerbindung().sendData(resultate);
		} catch (DataNotSubscribedException e) {
			Debug.getLogger()
					.error("Fehler bei Sendung von daten fuer " + objekt.getPid() + " ATG " + getStufeAttributGruppe() + " :\n" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		} catch (SendSubscriptionNotConfirmed e) {
			Debug.getLogger()
					.error("Fehler bei Sendung von daten fuer " + objekt.getPid() + " ATG " + getStufeAttributGruppe() + " :\n" + e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		if (naechsterBearbeitungsKnoten != null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}

	/**
	 * Berechnet die Glaettung nach der Formel in [AFo]
	 * 
	 * @param param
	 *            Sensorparameter (enthaelt Konstanten}
	 * @param messwert
	 *            Messwert
	 * @return Geglaetettes Messwert
	 */
	public double berechneMesswertGlaettung(SensorParameter param,
			double messwert) {
		double messwertGlatt;
		double b_i;

		// erstes Wert
		if (Double.isNaN(param.MesswertGlatti_1)) {
			param.MesswertGlatti_1 = messwert;
			return messwert;
		}
		// Messwert gleich 0
		if (Math.abs(messwert) < 0.000001) {
			param.MesswertGlatti_1 = 0.0;
			return 0.0;
		}

		b_i = param.b0 + (1.0 - param.fb * param.MesswertGlatti_1 / messwert);
		if (b_i < param.b0) {
			b_i = param.b0;
		}
		if (b_i > 1.0) {
			b_i = 1.0;
		}

		messwertGlatt = b_i * messwert + (1.0 - b_i) * param.MesswertGlatti_1;

		param.MesswertGlatti_1 = messwertGlatt;
		return messwertGlatt;
	}

	/**
	 * Erfragt die Klassifizierung ATG als Zeichenkette
	 * 
	 * @return Klassifizierung ATG
	 */
	public abstract String getKlassifizierungsAttributGruppe();

	/**
	 * Erfragt die Aggregations ATG als Zeichenkette
	 * 
	 * @return Aggregations ATG
	 */
	public abstract String getAggregationsAtrributGruppe();

	/**
	 * Erfragt die Messwert ATG als Zeichenkette
	 * 
	 * @return Messwert ATG
	 */
	public abstract String getMesswertAttributGruppe();

	/**
	 * Erfragt den Attribut fuer das Sensorwert in Messwert ATG als Zeichenkette
	 * 
	 * @return Messwert Attribut
	 */
	public abstract String getMesswertAttribut();

	/**
	 * Erfragt die Stufe ATG als Zeichenkette
	 * 
	 * @return Stufe ATG
	 */
	public abstract String getStufeAttributGruppe();

	/**
	 * Erfragt den Attribut fuer Klassifizierung in Klassifizierung ATG als
	 * Zeichenkette
	 * 
	 * @return Klasifizierung Attribut
	 */
	public abstract String getKlassifizierungsAttribut();

	/**
	 * Erfragt den Sensortyp als Zeichenkette
	 * 
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
	 * 
	 * @return Menge der Sensoren
	 */
	public Collection<SystemObject> getSensoren() {
		return this.sensoren;
	}
}
