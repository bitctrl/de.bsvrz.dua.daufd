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
package de.bsvrz.dua.daufd.tp;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.DataNotSubscribedException;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SendSubscriptionNotConfirmed;
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
 * Berechnet dem Taupunkt von Luft- bzw. Fahrbahnoberflaeche- Temperatur fuer
 * alle Messstellen, deren Liste sie vom Verwalfungsmodul bekommt
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class Taupunkt implements IBearbeitungsKnoten, ClientSenderInterface {

	/**
	 * Verbindung zum Hauptmodul
	 */
	protected static IVerwaltung verwaltung;
	/**
	 * der Nachste Bearbeitungsknoten
	 */
	private IBearbeitungsKnoten naechsterBearbeitungsKnoten = null;
	/**
	 * DatenBeschreibung des Datensatzes mit Taupunkttemperatur der Fahrbahn
	 */
	private DataDescription DD_UFDMS_TT_FB = null;
	/**
	 * DatenBeschreibung des Datensatzes mit Taupunkttemperatur der Fahrbahn
	 */
	private DataDescription DD_UFDMS_TT_L = null;
	/**
	 * LuftTemperatur Sensoren, deren Daten bearebietet werden sollen
	 */
	protected Collection<SystemObject> ltSensoren = new LinkedList<SystemObject>();
	/**
	 * FahrbahnoOberflaecheTemperatur Sensoren, deren Daten bearebietet werden
	 * sollen
	 */
	protected Collection<SystemObject> fbofSensoren = new LinkedList<SystemObject>();
	/**
	 * LuftFeuchte Sensoren, deren Daten bearebietet werden sollen
	 */
	protected Collection<SystemObject> rlfSensoren = new LinkedList<SystemObject>();

	/**
	 * String-Konstanten
	 */
	private static final String TYP_UFDS_LT = "typ.ufdsLuftTemperatur";
	private static final String TYP_UFDS_FBOFT = "typ.ufdsFahrBahnOberFlächenTemperatur";
	private static final String TYP_UFDS_RLF = "typ.ufdsRelativeLuftFeuchte";

	public static final String ATG_UFDMS_TTFB = "atg.ufdmsTaupunktTemperaturFahrBahn";
	public static final String ATG_UFDMS_TTL = "atg.ufdmsTaupunktTemperaturLuft";
	public static final String ATG_UFDS_LT = "atg.ufdsLuftTemperatur";
	public static final String ATG_UFDS_FBOFT = "atg.ufdsFahrBahnOberFlächenTemperatur";
	public static final String ATG_UFDS_RLF = "atg.ufdsRelativeLuftFeuchte";

	private static final String ASP_ANALYSE = "asp.analyse";
	private static final String ASP_MESSWERT_ERSETZUNG = "asp.messWertErsetzung";

	private static final String MNG_SENSOREN = "UmfeldDatenSensoren";

	/**
	 * Eintraege in einer Tabelle mit letzten Datensaetzen pro MessStelle
	 * 
	 * @author BitCtrl Systems GmbH, Bachraty
	 * 
	 */
	protected class LokaleDaten {
		/**
		 * Standardkonstruktor
		 * 
		 * @param messStelle
		 *            die Messstelle
		 */
		public LokaleDaten(SystemObject messStelle) {
			relativeLuftFeuchte = fbofTemperatur = luftTemperatur = null;
			rlfZeitStemepel = fboftZeitStemepel = ltZeitStemepel = 0;
			tpFbofZeitStemepel = 0;
			tpLuftZeitStemepel = 0;
			taupunktFbof = verwaltung.getVerbindung().createData(
					verwaltung.getVerbindung().getDataModel()
							.getAttributeGroup(ATG_UFDMS_TTFB));
			taupunktLuft = verwaltung.getVerbindung().createData(
					verwaltung.getVerbindung().getDataModel()
							.getAttributeGroup(ATG_UFDMS_TTL));
			this.messStelle = messStelle;
		}

		/**
		 * Die Assoziierte MessStelle
		 */
		public SystemObject messStelle;
		/**
		 * Letzter Datensatz mit relativen Luftfeuchte
		 */
		public Data relativeLuftFeuchte;
		/**
		 * Zeitstempel des letzten Datensatzes mit relativen Luftfeuchte
		 */
		public long rlfZeitStemepel;
		/**
		 * Letzter Datensatz mit Fahrbahnoberflaechetemeperatur
		 */
		public Data fbofTemperatur;
		/**
		 * Zeitstempel des letzten Datensatzes mit
		 * Fahrbahnoberflaechetemeperatur
		 */
		public long fboftZeitStemepel;
		/**
		 * Letzter Datensatz mit Lufttemeperatur
		 */
		public Data luftTemperatur;
		/**
		 * Zeitstempel des letzten Datensatzes mit Lufttemeperatur
		 */
		public long ltZeitStemepel;
		/**
		 * Erzeugende Datensatz mit Taupunkttemperatur Luft
		 */
		public Data taupunktLuft;
		/**
		 * Zeitstempel des leztes erzeugenen Datensatzes mit Taupunkttemperatur
		 * Luft
		 */
		long tpLuftZeitStemepel;
		/**
		 * Erzeugende Datensatz mit Taupunkttemperatur Fbof
		 */
		public Data taupunktFbof;
		/**
		 * Zeitstempel des leztes erzeugenen Datensatzes mit Taupunkttemperatur
		 * Fbof
		 */
		long tpFbofZeitStemepel;
		/**
		 * Wenn keine Daten vorhanden sind - die Eingabe Quelle auf "keineDaten"
		 * gestellt ist
		 */
		boolean keineFbofDaten = true;
		/**
		 * Wenn keine Daten vorhanden sind - die Eingabe Quelle auf "keineDaten"
		 * gestellt ist
		 */
		boolean keineLuftDaten = true;
		/**
		 * Bestimmt, ob der Fbof TP becrechnet wird (falls es z.B. keinen
		 * FbofTemp Sensor gbit, wird der Fbof TP nicht berechnet)
		 */
		boolean berechnetFbofTaupunkt = true;
		/**
		 * Bestimmt, ob der Luft TP becrechnet wird (falls es z.B. keinen
		 * LuftTemp Sensor gbit, wird der Luft TP nicht berechnet)
		 */
		boolean berechnetLuftTaupunkt = true;

	};

	/**
	 * HashTablelle mit letzten eingekommenen Datensaetzen Abbildet die Sensoren
	 * oder MessStellen auf die zugehoergien DatenStrukturen
	 */
	private Hashtable<SystemObject, LokaleDaten> taupunktTabelle = new Hashtable<SystemObject, LokaleDaten>();

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {
		for (ResultData resData : resultate) {

			Data data = resData.getData();
			SystemObject so = resData.getObject();
			LokaleDaten lDaten = taupunktTabelle.get(so);

			if (ATG_UFDS_LT.equals(resData.getDataDescription()
					.getAttributeGroup().getPid())
					&& ASP_MESSWERT_ERSETZUNG.equals(resData
							.getDataDescription().getAspect().getPid())) {

				if (lDaten == null) {
					Debug.getLogger().warning(
							"Objekt " + so
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (data == null) {
					if (!lDaten.keineLuftDaten && lDaten.berechnetLuftTaupunkt)
						// sende keine Daten
						sendeTaupunktTemperaturLuft(lDaten, resData
								.getDataTime(), true);
					continue;
				}
				long T = data.getTimeValue("T").getMillis();
				lDaten.luftTemperatur = data;
				lDaten.ltZeitStemepel = resData.getDataTime();
				if (lDaten.berechnetLuftTaupunkt)
					BerechneTaupunktTemperaturLuft(lDaten, resData
							.getDataTime(), T);
			} else if (ATG_UFDS_FBOFT.equals(resData.getDataDescription()
					.getAttributeGroup().getPid())
					&& ASP_MESSWERT_ERSETZUNG.equals(resData
							.getDataDescription().getAspect().getPid())) {

				if (lDaten == null) {
					Debug.getLogger().warning(
							"Objekt " + so
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (data == null) {
					if (lDaten.keineFbofDaten == false
							&& lDaten.berechnetFbofTaupunkt)
						// sende keine Daten
						sendeTaupunktTemperaturFbof(lDaten, resData
								.getDataTime(), true);
					continue;
				}
				long T = data.getTimeValue("T").getMillis();
				lDaten.fbofTemperatur = data;
				lDaten.fboftZeitStemepel = resData.getDataTime();
				if (lDaten.berechnetFbofTaupunkt)
					BerechneTaupunktTemperaturFbof(lDaten, resData
							.getDataTime(), T);

			} else if (ATG_UFDS_RLF.equals(resData.getDataDescription()
					.getAttributeGroup().getPid())
					&& ASP_MESSWERT_ERSETZUNG.equals(resData
							.getDataDescription().getAspect().getPid())) {
				if (lDaten == null) {
					Debug.getLogger().warning(
							"Objekt " + so
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (data == null) {
					if (lDaten.keineLuftDaten == false
							&& lDaten.berechnetLuftTaupunkt)
						// sende keine Daten
						sendeTaupunktTemperaturLuft(lDaten, resData
								.getDataTime(), true);
					else if (lDaten.keineFbofDaten == false
							&& lDaten.berechnetFbofTaupunkt)
						// sende keine Daten
						sendeTaupunktTemperaturFbof(lDaten, resData
								.getDataTime(), true);
					continue;
				}
				long T = data.getTimeValue("T").getMillis();
				lDaten.relativeLuftFeuchte = data;
				lDaten.rlfZeitStemepel = resData.getDataTime();

				if (lDaten.berechnetFbofTaupunkt)
					BerechneTaupunktTemperaturFbof(lDaten, resData
							.getDataTime(), T);
				if (lDaten.berechnetLuftTaupunkt)
					BerechneTaupunktTemperaturLuft(lDaten, resData
							.getDataTime(), T);
			}
		}

		if (naechsterBearbeitungsKnoten != null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}

	/**
	 * Berechnet die Taupunkttempereatur der Fahrbahnoberflaeche fuer eine
	 * Messtelle
	 * 
	 * @param lDaten
	 *            Letzte Daten (RLF und FBT)
	 * @param zeitStemepel
	 *            Zeutstempel des Itervalles, fuer dem die Daten erzeugt werden
	 *            sollen
	 * @param zeitIntervall
	 *            das Intervall
	 */
	public void BerechneTaupunktTemperaturFbof(LokaleDaten lDaten,
			long zeitStemepel, long zeitIntervall) {
		boolean nichtermittelbar = false;

		// Wenn noch ein von den DS noch nicht initialisiert ist
		if (lDaten.fboftZeitStemepel == 0 || lDaten.rlfZeitStemepel == 0) {
			lDaten.tpFbofZeitStemepel = zeitStemepel - zeitIntervall;
			if (lDaten.rlfZeitStemepel == 0)
				lDaten.rlfZeitStemepel = lDaten.tpFbofZeitStemepel;
			else
				lDaten.fboftZeitStemepel = lDaten.tpFbofZeitStemepel;
			return;
		}

		// Beide (Feuchte Temeperatur) Datensaetze noch nicht gekommen sind
		if (lDaten.fboftZeitStemepel != lDaten.rlfZeitStemepel
				&& lDaten.fbofTemperatur != null
				&& lDaten.relativeLuftFeuchte != null) {
			lDaten.taupunktFbof.getItem("TaupunktTemperaturFahrBahn")
					.asUnscaledValue().set(-1001);
			// Es faehlt mehr als ein DS von einem Typ
			while (lDaten.tpFbofZeitStemepel + zeitIntervall < zeitStemepel) {
				sendeTaupunktTemperaturFbof(lDaten, lDaten.tpFbofZeitStemepel
						+ zeitIntervall, false);
			}
			return;
		}

		if (lDaten.fbofTemperatur == null || lDaten.relativeLuftFeuchte == null)
			nichtermittelbar = true;

		// Initializierung wegen doofen Compiler
		long fbofT = 0, rlF = 0;
		// Nur wenn ermittelbar ist, lesen wir die Parameter aus
		if (!nichtermittelbar) {
			fbofT = lDaten.fbofTemperatur.getItem(
					"FahrBahnOberFlächenTemperatur").getUnscaledValue("Wert")
					.longValue();
			if (fbofT < -1000
					|| lDaten.fbofTemperatur.getItem(
							"FahrBahnOberFlächenTemperatur").getItem("Status")
							.getItem("MessWertErsetzung").getUnscaledValue(
									"Implausibel").byteValue() == 1) {
				nichtermittelbar = true;
			}
		}
		// Nur wenn ermittelbar ist, lesen wir die Parameter aus
		if (!nichtermittelbar) {
			rlF = lDaten.relativeLuftFeuchte.getItem("RelativeLuftFeuchte")
					.getUnscaledValue("Wert").longValue();
			if (rlF < 0
					|| lDaten.relativeLuftFeuchte
							.getItem("RelativeLuftFeuchte").getItem("Status")
							.getItem("MessWertErsetzung").getUnscaledValue(
									"Implausibel").byteValue() == 1) {
				nichtermittelbar = true;
			}
		}
		// Wir senden einen DS der "nicht ermittelbar" gekennzeichnet ist
		if (nichtermittelbar) {
			lDaten.taupunktFbof.getItem("TaupunktTemperaturFahrBahn")
					.asUnscaledValue().set(-1001);
			sendeTaupunktTemperaturFbof(lDaten, zeitStemepel, false);
			return;
		}
		// Berechnung und Sendung
		double relFeucht = rlF;
		double fobofTemp = 0.1 * fbofT;
		double ergebnis = Berechnetaupunkt(relFeucht, fobofTemp);
		// 
		// Der "att.ufdsTaupunktTemperatur" ist auf dem Beriech<-100,100>
		// begrentzt
		// aber das Erbegnis der Berechnungen kann teoretisch ausserhalb des
		// Intervalls sein
		// dann bekommt mann ein Exceptien bei der Sendung der Daten
		//
		if (ergebnis < -100)
			ergebnis = -100;
		else if (ergebnis > 100)
			ergebnis = 100;

		lDaten.taupunktFbof.getItem("TaupunktTemperaturFahrBahn")
				.asScaledValue().set(ergebnis);
		sendeTaupunktTemperaturFbof(lDaten, zeitStemepel, false);
	}

	/**
	 * Berechnet die Taupunkttempereatur der Luft fuer eine Messtelle
	 * 
	 * @param lDaten
	 *            Letzte Daten (RLF und LT)
	 * @param zeitStemepel
	 *            Zeutstempel des Itervalles, fuer dem die Daten erzeugt werden
	 *            sollen
	 * @param zeitIntervall
	 *            das Intervall
	 */
	public void BerechneTaupunktTemperaturLuft(LokaleDaten lDaten,
			long zeitStemepel, long zeitIntervall) {

		boolean nichtermittelbar = false;

		// Wenn noch ein von den DS noch nicht initialisiert ist
		if (lDaten.ltZeitStemepel == 0 || lDaten.rlfZeitStemepel == 0) {
			// lDaten.taupunktLuft.getItem("TaupunktTemperaturLuft").asUnscaledValue().set(-1001);
			lDaten.tpLuftZeitStemepel = zeitStemepel - zeitIntervall;
			if (lDaten.rlfZeitStemepel == 0)
				lDaten.rlfZeitStemepel = lDaten.tpFbofZeitStemepel;
			else
				lDaten.ltZeitStemepel = lDaten.tpLuftZeitStemepel;
			return;
		}

		// Beide (Feuchte Temeperatur) Datensaetze noch nicht gekommen sind
		if (lDaten.ltZeitStemepel != lDaten.rlfZeitStemepel
				&& lDaten.luftTemperatur != null
				&& lDaten.relativeLuftFeuchte != null) {
			lDaten.taupunktLuft.getItem("TaupunktTemperaturLuft")
					.asUnscaledValue().set(-1001);
			// Es faehlt mehr ale ein DS von einem Typ
			while (lDaten.tpLuftZeitStemepel + zeitIntervall < zeitStemepel) {
				sendeTaupunktTemperaturLuft(lDaten, lDaten.tpLuftZeitStemepel
						+ zeitIntervall, false);
			}
			return;
		}

		long luftT = 0, rlF = 0;
		// Nur wenn ermittelbar ist, lesen wir die Parameter aus
		if (!nichtermittelbar) {
			if (lDaten.luftTemperatur == null) {
				nichtermittelbar = true;
			} else {
				luftT = lDaten.luftTemperatur.getItem("LuftTemperatur")
						.getUnscaledValue("Wert").longValue();
				if (luftT < -1000
						|| lDaten.luftTemperatur.getItem("LuftTemperatur")
								.getItem("Status").getItem("MessWertErsetzung")
								.getUnscaledValue("Implausibel").byteValue() == 1) {

				}
			}
		}

		// Nur wenn ermittelbar ist, lesen wir die Parameter aus
		if (!nichtermittelbar) {
			if(lDaten.relativeLuftFeuchte == null){
				nichtermittelbar = true;
			}else{
				rlF = lDaten.relativeLuftFeuchte.getItem("RelativeLuftFeuchte")
						.getUnscaledValue("Wert").longValue();
				if (rlF < 0
						|| lDaten.relativeLuftFeuchte
								.getItem("RelativeLuftFeuchte").getItem("Status")
								.getItem("MessWertErsetzung").getUnscaledValue(
										"Implausibel").byteValue() == 1) {
					nichtermittelbar = true;
				}
			}
		}
		// Wir senden einen DS der "nicht ermittelbar" gekennzeichnet ist
		if (nichtermittelbar) {
			lDaten.taupunktLuft.getItem("TaupunktTemperaturLuft")
					.asUnscaledValue().set(-1001);
			sendeTaupunktTemperaturLuft(lDaten, zeitStemepel, false);
			return;
		}
		// Berechnung und Sendung
		double relFeucht = rlF;
		double luftTemp = 0.1 * luftT;
		double ergebnis = Berechnetaupunkt(relFeucht, luftTemp);
		// 
		// Der "att.ufdsTaupunktTemperatur" ist auf dem Beriech<-100,100>
		// begrentzt
		// aber das Erbegnis der Berechnungen kann teoretisch ausserhalb des
		// Intervalls sein
		// dann bekommt mann ein Exceptien bei der Sendung der Daten
		//
		if (ergebnis < -100)
			ergebnis = -100;
		else if (ergebnis > 100)
			ergebnis = 100;

		lDaten.taupunktLuft.getItem("TaupunktTemperaturLuft").asScaledValue()
				.set(ergebnis);
		sendeTaupunktTemperaturLuft(lDaten, zeitStemepel, false);
	}

	/**
	 * Sendet einen DS mit TP Temperatur der FBOF
	 * 
	 * @param lDaten
	 *            Struktur mit erzeugten DS
	 * @param zeitStempel
	 *            ZeitStempel des DS
	 * @param keineDaten
	 *            Bestimmt, ob man einen leren Datensatz senden soll
	 */
	public void sendeTaupunktTemperaturFbof(LokaleDaten lDaten,
			long zeitStempel, boolean keineDaten) {
		ResultData resDatei;
		lDaten.keineFbofDaten = keineDaten;
		lDaten.tpFbofZeitStemepel = zeitStempel;

		if (keineDaten)
			resDatei = new ResultData(lDaten.messStelle, DD_UFDMS_TT_FB,
					lDaten.tpFbofZeitStemepel, null);
		else
			resDatei = new ResultData(lDaten.messStelle, DD_UFDMS_TT_FB,
					lDaten.tpFbofZeitStemepel, lDaten.taupunktFbof);
		try {
			verwaltung.getVerbindung().sendData(resDatei);
		} catch (DataNotSubscribedException e) {
			Debug.getLogger().error(
					"Sendung von Datensatz "
							+ DD_UFDMS_TT_FB.getAttributeGroup().getPid()
							+ " fuer Objekt " + lDaten.messStelle.getPid()
							+ " unerfolgreich:\n" + e.getMessage());
		} catch (SendSubscriptionNotConfirmed e) {
			Debug.getLogger().error(
					"Sendung von Datensatz "
							+ DD_UFDMS_TT_FB.getAttributeGroup().getPid()
							+ " fuer Objekt " + lDaten.messStelle.getPid()
							+ " unerfolgreich:\n" + e.getMessage());
		}
	}

	/**
	 * Sendet einen DS mit TP Temperatur der Luft
	 * 
	 * @param lDaten
	 *            Struktur mit erzeugten DS
	 * @param zeitStempel
	 *            ZeitStempel des DS
	 * @param keineDaten
	 *            Bestimmt, ob man einen leren Datensatz senden soll
	 */
	public void sendeTaupunktTemperaturLuft(LokaleDaten lDaten,
			long zeitStempel, boolean keineDaten) {
		ResultData resDatei;
		lDaten.keineLuftDaten = keineDaten;
		lDaten.tpLuftZeitStemepel = zeitStempel;

		if (keineDaten)
			resDatei = new ResultData(lDaten.messStelle, DD_UFDMS_TT_L,
					lDaten.tpLuftZeitStemepel, null);
		else
			resDatei = new ResultData(lDaten.messStelle, DD_UFDMS_TT_L,
					lDaten.tpLuftZeitStemepel, lDaten.taupunktLuft);

		try {
			verwaltung.getVerbindung().sendData(resDatei);
		} catch (DataNotSubscribedException e) {
			Debug.getLogger().error(
					"Sendung von Datensatz "
							+ DD_UFDMS_TT_L.getAttributeGroup().getPid()
							+ " fuer Objekt " + lDaten.messStelle.getPid()
							+ " unerfolgreich:\n" + e.getMessage());
		} catch (SendSubscriptionNotConfirmed e) {
			Debug.getLogger().error(
					"Sendung von Datensatz "
							+ DD_UFDMS_TT_L.getAttributeGroup().getPid()
							+ " fuer Objekt " + lDaten.messStelle.getPid()
							+ " unerfolgreich:\n" + e.getMessage());
		}
	}

	/**
	 * Berechnet die Taupunkttemperatur aus er Feuchte und Temperatur
	 * 
	 * @param feuchte
	 *            relative Feuchte
	 * @param temperatur
	 *            Temperatur
	 * @return die Taupunkttemperatur
	 */
	public double Berechnetaupunkt(double feuchte, double temperatur) {
		double x = 241.2 * Math.log(feuchte / 100.0) + 4222.03716 * temperatur
				/ (241.2 + temperatur);
		double y = 17.5043 - Math.log(feuchte / 100.0) - 17.5043 * temperatur
				/ (241.2 + temperatur);
		return x / y;
	}

	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialisiere(IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		this.verwaltung = verwaltung;

		DD_UFDMS_TT_FB = new DataDescription(verwaltung.getVerbindung()
				.getDataModel().getAttributeGroup(ATG_UFDMS_TTFB), verwaltung
				.getVerbindung().getDataModel().getAspect(ASP_ANALYSE));

		DD_UFDMS_TT_L = new DataDescription(verwaltung.getVerbindung()
				.getDataModel().getAttributeGroup(ATG_UFDMS_TTL), verwaltung
				.getVerbindung().getDataModel().getAspect(ASP_ANALYSE));

		if (verwaltung.getSystemObjekte() == null
				|| verwaltung.getSystemObjekte().length == 0)
			return;

		for (SystemObject so : verwaltung.getSystemObjekte())
			try {
				if (!(so instanceof ConfigurationObject))
					continue;
				LokaleDaten lDaten = new LokaleDaten(so);
				taupunktTabelle.put(so, lDaten);
				ConfigurationObject confObjekt = (ConfigurationObject) so;
				ObjectSet sensorMenge = confObjekt.getObjectSet(MNG_SENSOREN);

				boolean hatRLFSensor, hatLTSensor, hatFBOFSensor;
				hatFBOFSensor = hatLTSensor = hatRLFSensor = false;
				for (SystemObject sensor : sensorMenge.getElements()) {
					if (sensor.isValid()) {
						if (TYP_UFDS_LT.equals(sensor.getType().getPid())) {
							taupunktTabelle.put(sensor, lDaten);
							ltSensoren.add(sensor);
							hatLTSensor = true;
						} else if (TYP_UFDS_FBOFT.equals(sensor.getType()
								.getPid())) {
							taupunktTabelle.put(sensor, lDaten);
							fbofSensoren.add(sensor);
							hatFBOFSensor = true;
						} else if (TYP_UFDS_RLF.equals(sensor.getType()
								.getPid())) {
							taupunktTabelle.put(sensor, lDaten);
							rlfSensoren.add(sensor);
							hatRLFSensor = true;
						}
					}
				}
				if (hatFBOFSensor && hatRLFSensor) {
					ResultData resultate = new ResultData(so, DD_UFDMS_TT_FB,
							System.currentTimeMillis(), null);
					verwaltung.getVerbindung().subscribeSource(this, resultate);
				} else
					lDaten.berechnetFbofTaupunkt = false;

				if (hatLTSensor && hatRLFSensor) {
					ResultData resultate = new ResultData(so, DD_UFDMS_TT_L,
							System.currentTimeMillis(), null);
					verwaltung.getVerbindung().subscribeSource(this, resultate);
				} else
					lDaten.berechnetLuftTaupunkt = false;

			} catch (OneSubscriptionPerSendData e) {
				throw new DUAInitialisierungsException(
						"Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt"
								+ so.getPid() + " unerfolgreich:"
								+ e.getMessage());
			}
	}

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
	}

	/**
	 * {@inheritDoc}
	 */
	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		// TODO Auto-generated method stub
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isRequestSupported(SystemObject object,
			DataDescription dataDescription) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Ergibt die Sensoren fuer RelativeLuftFeuchte
	 * 
	 * @return SensorenMenge
	 */
	public Collection<SystemObject> getRlfSensoren() {
		return this.rlfSensoren;
	}

	/**
	 * Ergibt die Sensoren fuer LuftTemperatur
	 * 
	 * @return SensorenMenge
	 */
	public Collection<SystemObject> getLtSensoren() {
		return this.ltSensoren;
	}

	/**
	 * Ergibt die Sensoren fuer FahrbahnoberflaecheTemperatur
	 * 
	 * @return SensorenMenge
	 */
	public Collection<SystemObject> getFbofSensoren() {
		return this.fbofSensoren;
	}

}
