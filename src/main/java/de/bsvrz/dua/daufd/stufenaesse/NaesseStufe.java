/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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
import de.bsvrz.dav.daf.main.config.ConfigurationObject;
import de.bsvrz.dav.daf.main.config.ObjectSet;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe.NI_Stufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe.WFD_Stufe;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.ObjektWecker;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IObjektWeckerListener;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Bestimmt die NS_Stufe aus der NI_Stufe und WFD_Stufe nach der eingegebener
 * Tabelle, bei fehlenden WFD Stufen und nachlassender Niederschlagintensitaet
 * Verzoegert die Senkung der NS Stufe
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class NaesseStufe implements IBearbeitungsKnoten, ClientSenderInterface,
		ClientReceiverInterface {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Verbindung zum Hauptmodul
	 */
	private IVerwaltung verwaltung;
	/**
	 * der Nachste Bearbeitungsknoten
	 */
	private IBearbeitungsKnoten naechsterBearbeitungsKnoten = null;
	/**
	 * Die Ausgabe Datensaetze
	 */
	private DataDescription DD_NAESSE_STUFE;
	/**
	 * Die parameter Datensaetze
	 */
	private DataDescription DD_ABTROCKNUNGSPHASEN;
	/**
	 * Aktulaisiert die Stufen nachfolgend der Abtrocknungsphasen
	 */
	private static ObjektWecker stufeAkutalisierer = null;
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
	/**
	 * Bestimmt, ob bei Bestimmbarkeit Aenderung ( z.B. die Eisbedeckte FbOf
	 * taut) die Naessestufe publiziert werden soll
	 */
	protected static final boolean beiAenderungPublizieren = false;

	/**
	 * Die letzte empfangene Daten fuer jede MessStelle nud andere parameter
	 * werden in diese Klasse gespeichert
	 *
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	protected class MessStelleDaten implements IObjektWeckerListener {
		/**
		 * Standardkonstruktor
		 * 
		 * @param so
		 *            Systemokjekt MessStelle
		 */
		public MessStelleDaten(final SystemObject so) {
			messObject = so;
		}

		/**
		 * SystemObjekt UmfdMessStelle
		 */
		public SystemObject messObject = null;
		/**
		 * Die ZeitDauer, bis sich die Fahrbahnoberflaeche abtrocknet
		 */
		public long[] abtrocknungsPhasen = new long[NaesseStufe.ATT_STUFE.length];
		/**
		 * Letzte empfangene NI_Stufe
		 */
		public NI_Stufe niStufe = NI_Stufe.NI_WERT_NV;
		/**
		 * Zeitstempel letzter empfangenen NI_Stufe
		 */
		public long niStufeZeitStempel = 0;
		/**
		 * Letzte empfangene WFD_Stufe
		 */
		public WFD_Stufe wfdStufe = WFD_Stufe.WFD_WERT_NV;
		/**
		 * Zeitstempel letzter empfangenen WFD_Stufe
		 */
		public long wfdStufeZeitStempel = 0;
		/**
		 * Ob die Abtrocknungsphasen initialisiert sind
		 */
		public boolean initialisiert = false;
		/**
		 * ZeitStempel der NaesseSteufe der MessStelle
		 */
		public long nsStufeZeitStempel = 0;
		/**
		 * NaesseSteufe der MessStelle
		 */
		public NS_Stufe nsStufe = NS_Stufe.NS_WERT_NE;
		/**
		 * NaesseSteufe der MessStelle die erreicht werden soll bei verzoegerten
		 * Aenderungen
		 */
		public NS_Stufe zielNsStufe = NS_Stufe.NS_WERT_NE;
		/**
		 * Die aktuelle Niederschlagsart
		 */
		public int niederschlagsArt = 0;
		/**
		 * der aktuelle FahrbahnOberflaecheZustand
		 */
		public int fbofZustand = 0;
		/**
		 * ZeitStempel des letzten DS mit Niederschlagsart
		 */
		public long niederschlagsArtZeitStemepel = 0;
		/**
		 * ZeitStempel des Letzten DS mit FahrbahnOberflaecheZustand
		 */
		public long fbofZustandZeitStempel = 0;
		/**
		 * Ob ein aktulasierungsauftrag lauft
		 */
		public boolean aktualisierungLaeuft = false;
		/**
		 * Ob wir am letzten mal einen leeren DS bekommen haben
		 */
		public boolean keineDaten = true;
		/**
		 * Bestimmt, ob die Naessestufe unbestimmbar ist (haengt von
		 * NiederschlagsArt und FahrbahnoberflaecheZustand ab)
		 */
		public boolean unbestimmbar = false;

		/**
		 * Akutlaisiert die NaesseStufe nach den Abtrocknungsphasen, ermoeglicht
		 * verzoegerte Aktualsierung
		 */
		@Override
		public void alarm() {
			synchronized (this) {
				if ((nsStufe != zielNsStufe) && (unbestimmbar == false)) {
					long zeitIntervall;
					int intStufe = NaesseStufe.getStufe(nsStufe);
					if (intStufe < 1) {
						aktualisierungLaeuft = false;
						return;
					}
					// nur zum Test-Zwecken
					infoVerzoegerung(intStufe);
					zeitIntervall = abtrocknungsPhasen[intStufe - 1];
					intStufe--;

					nsStufe = NaesseStufe.mapIntNSStufe[intStufe];
					nsStufeZeitStempel += zeitIntervall;
					publiziereNsStufe(this, false);

					if ((nsStufe != zielNsStufe) && (intStufe > 0)) {
						zeitIntervall = abtrocknungsPhasen[intStufe - 1];
						NaesseStufe.stufeAkutalisierer.setWecker(this,
								nsStufeZeitStempel + zeitIntervall);
					} else {
						aktualisierungLaeuft = false;
					}
				}
				aktualisierungLaeuft = false;
			}
		}
	}

	/**
	 * Ermoeglicht die Abbildung der Sensoren und MessStellen auf die Klasse mit
	 * Lokalen Daten fuer die gegebene MessStelle
	 */
	protected Hashtable<SystemObject, MessStelleDaten> naesseTabelle = new Hashtable<SystemObject, MessStelleDaten>();

	/**
	 * Naesse Stufen, die unterscheidet werden
	 * 
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum NS_Stufe implements Comparable<NS_Stufe> {
		NS_TROCKEN, // ordinal = 0
		NS_NASS1, // ordinal = 1
		NS_NASS2, // etc.
		NS_NASS3, NS_NASS4, NS_WERT_NE; // Wert nicht ermittelbar (-1)

	};

	/**
	 * Abbildet integer Werte auf Symbolische Konstanten
	 */
	protected final static NS_Stufe[] mapIntNSStufe = new NS_Stufe[] {
			NS_Stufe.NS_TROCKEN, NS_Stufe.NS_NASS1, NS_Stufe.NS_NASS2,
			NS_Stufe.NS_NASS3, NS_Stufe.NS_NASS4 };

	/**
	 * Abbildet die NS_Stufe von Int zur symbolischen Wert
	 * 
	 * @param stufe
	 *            Stufe int
	 * @return Stufe enum
	 */
	public static NS_Stufe getStufe(final int stufe) {
		if ((stufe < 0) || (stufe > (NaesseStufe.mapIntNSStufe.length - 1))) {
			return NS_Stufe.NS_WERT_NE;
		}
		return NaesseStufe.mapIntNSStufe[stufe];
	}

	/**
	 * Nur fuer Testzwecken
	 *
	 * @param stufe
	 *            Naessestufe
	 */
	void infoVerzoegerung(final int stufe) {
	}

	/**
	 * Tabelle aus AFo - Ermitellt aus WFD und NI stufe die NaesseStufe
	 *
	 * Die Tabelle bildet WFDStufen an Tabellen von NiStufen ab Jede Zeile ist
	 * eine Abbildung von NI-Stufen auf NaesseStufen
	 */
	static final Hashtable<WFD_Stufe, Hashtable<NI_Stufe, NS_Stufe>> tabelleWFDNIzumNS = new Hashtable<WFD_Stufe, Hashtable<NI_Stufe, NS_Stufe>>();
	static {
		Hashtable<NI_Stufe, NS_Stufe> zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_TROCKEN);
		NaesseStufe.tabelleWFDNIzumNS.put(WFD_Stufe.WFD_STUFE0, zeile);

		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS1);
		NaesseStufe.tabelleWFDNIzumNS.put(WFD_Stufe.WFD_STUFE1, zeile);

		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS2);
		NaesseStufe.tabelleWFDNIzumNS.put(WFD_Stufe.WFD_STUFE2, zeile);

		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS3);
		NaesseStufe.tabelleWFDNIzumNS.put(WFD_Stufe.WFD_STUFE3, zeile);

		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_WERT_NE);
		NaesseStufe.tabelleWFDNIzumNS.put(WFD_Stufe.WFD_WERT_NV, zeile);
	};

	/**
	 * String-Konstanten TYPEN
	 */
	public static final String TYP_UFDS_NI = "typ.ufdsNiederschlagsIntensität";
	public static final String TYP_UFDS_WFD = "typ.ufdsWasserFilmDicke";
	public static final String TYP_UFDS_NA = "typ.ufdsNiederschlagsArt";
	public static final String TYP_UFDS_FBOFZS = "typ.ufdsFahrBahnOberFlächenZustand";

	/**
	 * String-Konstanten Attributgruppen
	 */
	public static final String ATG_UFDS_NA = "atg.ufdsNiederschlagsArt";
	public static final String ATG_UFDS_FBOFZS = "atg.ufdsFahrBahnOberFlächenZustand";
	public static final String ATG_UFDMS_NS = "atg.ufdmsNässeStufe";
	public static final String ATG_UFDMS_AP = "atg.ufdmsAbtrockungsPhasen";
	public static final String ATG_WFD_STUFE = "atg.ufdsStufeWasserFilmDicke";
	public static final String ATG_NI_STUFE = "atg.ufdsStufeNiederschlagsIntensität";

	/**
	 * String-Konstanten Aspekte
	 */
	public static final String ASP_MESSWERTERSETZUNG = "asp.messWertErsetzung";
	public static final String ASP_KLASSIFIZIERUNG = "asp.klassifizierung";
	public static final String ASP_PARAM_SOLL = "asp.parameterSoll";

	/**
	 * String-Konstanten Attribute
	 */
	public static final String MNG_SENSOREN = "UmfeldDatenSensoren";
	public static final String[] ATT_STUFE = new String[] { "ZeitNass1Trocken",
			"ZeitNass4Nass3", "ZeitNass3Nass2", "ZeitNass2Nass1" };

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aktualisiereDaten(final ResultData[] resultate) {

		for (final ResultData resData : resultate) {

			final Data data = resData.getData();
			final SystemObject so = resData.getObject();
			final MessStelleDaten msDaten = naesseTabelle.get(so);
			final long zeitStempel = resData.getDataTime();
			long vorletzteZeitStempel;

			if (NaesseStufe.ATG_NI_STUFE.equals(resData.getDataDescription()
					.getAttributeGroup().getPid())
					&& NaesseStufe.ASP_KLASSIFIZIERUNG.equals(resData
							.getDataDescription().getAspect().getPid())) {
				if (msDaten == null) {
					LOGGER.warning(
							"Objekt " + so
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (!msDaten.initialisiert) {
					continue;
				} else if (data == null) {
					if (msDaten.keineDaten == false) {
						msDaten.nsStufeZeitStempel = zeitStempel;
						publiziereNsStufe(msDaten, true);
					}
					continue;
				}

				final int stufe = data.getItem("Stufe").asUnscaledValue()
						.intValue();
				final NI_Stufe niStufe = NiederschlagIntensitaetStufe
						.getStufe(stufe);

				synchronized (msDaten) {
					msDaten.niStufe = niStufe;
					vorletzteZeitStempel = msDaten.niStufeZeitStempel;
					msDaten.niStufeZeitStempel = zeitStempel;
					aktualisiereNaesseStufe(msDaten, zeitStempel,
							vorletzteZeitStempel);
				}
			} else if (NaesseStufe.ATG_WFD_STUFE.equals(resData
					.getDataDescription().getAttributeGroup().getPid())
					&& NaesseStufe.ASP_KLASSIFIZIERUNG.equals(resData
							.getDataDescription().getAspect().getPid())) {
				if (msDaten == null) {
					LOGGER.warning(
							"Objekt " + so
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (!msDaten.initialisiert) {
					continue;
				} else if (data == null) {
					if (msDaten.keineDaten == false) {
						msDaten.nsStufeZeitStempel = zeitStempel;
						publiziereNsStufe(msDaten, true);
					}
					continue;
				}

				final int stufe = data.getItem("Stufe").asUnscaledValue()
						.intValue();
				final WFD_Stufe wfdStufe = WasserFilmDickeStufe.getStufe(stufe);

				synchronized (msDaten) {
					msDaten.wfdStufe = wfdStufe;
					vorletzteZeitStempel = msDaten.wfdStufeZeitStempel;
					msDaten.wfdStufeZeitStempel = zeitStempel;
					aktualisiereNaesseStufe(msDaten, zeitStempel,
							vorletzteZeitStempel);
				}
			} else if (NaesseStufe.ATG_UFDS_NA.equals(resData
					.getDataDescription().getAttributeGroup().getPid())
					&& NaesseStufe.ASP_MESSWERTERSETZUNG.equals(resData
							.getDataDescription().getAspect().getPid())) {

				if (msDaten == null) {
					LOGGER.warning(
							"Objekt " + so
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (!msDaten.initialisiert) {
					continue;
				} else if (data == null) {
					if (msDaten.keineDaten == false) {
						msDaten.nsStufeZeitStempel = zeitStempel;
						publiziereNsStufe(msDaten, true);
					}
					continue;
				}

				final int implausibel = data.getItem("NiederschlagsArt")
						.getItem("Status").getItem("MessWertErsetzung")
						.getUnscaledValue("Implausibel").intValue();
				final int niederschlagsArt = data.getItem("NiederschlagsArt")
						.getUnscaledValue("Wert").intValue();

				synchronized (msDaten) {
					if (implausibel == 0) {
						msDaten.niederschlagsArt = niederschlagsArt;
					}

					vorletzteZeitStempel = msDaten.niederschlagsArtZeitStemepel;
					msDaten.niederschlagsArtZeitStemepel = zeitStempel;
					aktualisiereNaesseStufe(msDaten, zeitStempel,
							vorletzteZeitStempel);
				}
			} else if (NaesseStufe.ATG_UFDS_FBOFZS.equals(resData
					.getDataDescription().getAttributeGroup().getPid())
					&& NaesseStufe.ASP_MESSWERTERSETZUNG.equals(resData
							.getDataDescription().getAspect().getPid())) {

				if (msDaten == null) {
					LOGGER.warning(
							"Objekt " + so
									+ " in der Hashtabelle nicht gefunden");
					continue;
				} else if (!msDaten.initialisiert) {
					continue;
				} else if (data == null) {
					if (msDaten.keineDaten == false) {
						msDaten.nsStufeZeitStempel = zeitStempel;
						publiziereNsStufe(msDaten, true);
					}
					continue;
				}
				final int implausibel = data
						.getItem("FahrBahnOberFlächenZustand")
						.getItem("Status").getItem("MessWertErsetzung")
						.getUnscaledValue("Implausibel").intValue();
				final int fbZustand = data
						.getItem("FahrBahnOberFlächenZustand")
						.getUnscaledValue("Wert").intValue();

				synchronized (msDaten) {
					if (implausibel == 0) {
						msDaten.fbofZustand = fbZustand;
					}
					vorletzteZeitStempel = msDaten.fbofZustandZeitStempel;
					msDaten.fbofZustandZeitStempel = zeitStempel;
					aktualisiereNaesseStufe(msDaten, zeitStempel,
							vorletzteZeitStempel);

				}
			}
		}

		if (naechsterBearbeitungsKnoten != null) {
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
		}
	}

	/**
	 * Aktualisiert die NaesseStefe einer MessStelle nach den Regel aus [Afo]
	 * 
	 * @param msDaten
	 *            MessStelle
	 */
	public void aktualisiereNaesseStufe(final MessStelleDaten msDaten,
			final long zeitStempel, final long vorletzeZeitStemepel) {

		NS_Stufe neueStufe;

		// init
		if (msDaten.nsStufeZeitStempel == 0) {
			msDaten.nsStufeZeitStempel = zeitStempel - 10;
		}
		// Ob die NS Stufe unbestimmbar ist
		if (((msDaten.niederschlagsArt > 69) && (msDaten.niederschlagsArt < 80))
				|| ((msDaten.fbofZustand > 63) && (msDaten.fbofZustand < 68))) {
			msDaten.unbestimmbar = true;
		} else {
			msDaten.unbestimmbar = false;
		}

		// Ein Datum faehlt noch
		if (!((msDaten.niStufeZeitStempel == msDaten.wfdStufeZeitStempel)
				&& (msDaten.fbofZustandZeitStempel == msDaten.niederschlagsArtZeitStemepel) && (msDaten.fbofZustandZeitStempel == msDaten.niStufeZeitStempel))) {
			loesche(msDaten);

			// Auch im vorherigen zyklus faehlt ein DS, nsStufe wurde nicht
			// publiziert
			if (msDaten.nsStufeZeitStempel < vorletzeZeitStemepel) {
				msDaten.nsStufe = msDaten.zielNsStufe;
				msDaten.nsStufeZeitStempel = vorletzeZeitStemepel;
				if (msDaten.unbestimmbar) {
					msDaten.nsStufe = NS_Stufe.NS_WERT_NE;
				}
				publiziereNsStufe(msDaten, false);
			}

			if (msDaten.niStufeZeitStempel < msDaten.wfdStufeZeitStempel) {
				msDaten.zielNsStufe = NaesseStufe.tabelleWFDNIzumNS.get(
						msDaten.wfdStufe).get(NI_Stufe.NI_WERT_NV);
			} else if (msDaten.niStufeZeitStempel > msDaten.wfdStufeZeitStempel) {
				msDaten.zielNsStufe = NaesseStufe.tabelleWFDNIzumNS.get(
						WFD_Stufe.WFD_WERT_NV).get(msDaten.niStufe);
			}
			return;
		}

		// Alle Daten sind vorhanden
		neueStufe = NaesseStufe.tabelleWFDNIzumNS.get(msDaten.wfdStufe).get(
				msDaten.niStufe);
		if (msDaten.unbestimmbar) {
			msDaten.nsStufe = neueStufe = NS_Stufe.NS_WERT_NE;
		}
		msDaten.zielNsStufe = neueStufe;

		// Wir gehen mehr als eine Stufe nach unten
		if ((msDaten.nsStufe != NS_Stufe.NS_WERT_NE)
				&& (neueStufe.compareTo(msDaten.nsStufe) < -1)
				&& (msDaten.wfdStufe != WFD_Stufe.WFD_WERT_NV)) {
			loesche(msDaten);
			int intStufe = NaesseStufe.getStufe(msDaten.nsStufe);
			intStufe--;
			msDaten.nsStufe = NaesseStufe.mapIntNSStufe[intStufe];
			msDaten.nsStufeZeitStempel = msDaten.wfdStufeZeitStempel;
			publiziereNsStufe(msDaten, false);
			erstelleAuktualisierungsAuftrag(msDaten);
			return;
		}
		// bei nachlassenden Niederschlag wenn WFD nicht verfuegbar ist
		if ((msDaten.nsStufe != NS_Stufe.NS_WERT_NE)
				&& (neueStufe.compareTo(msDaten.nsStufe) < 0)
				&& (msDaten.wfdStufe == WFD_Stufe.WFD_WERT_NV)) {
			// die Aktualisierungsmethode addiert nur den Zeitintervall der
			// Verzoegerung
			// hier wird die Basiszeit gestellt.
			msDaten.nsStufeZeitStempel = msDaten.wfdStufeZeitStempel;
			erstelleAuktualisierungsAuftrag(msDaten);
			return;
		}

		msDaten.nsStufe = neueStufe;
		msDaten.nsStufeZeitStempel = msDaten.wfdStufeZeitStempel;
		publiziereNsStufe(msDaten, false);
	}

	/**
	 * Publiziert die NS stufe einer Messstelle
	 * 
	 * @param msDaten
	 *            MessStelle Daten
	 * @param keineDaten
	 *            True, wenn ein Null Datensatz geschickt werden soll
	 */
	public void publiziereNsStufe(final MessStelleDaten msDaten,
			final boolean keineDaten) {

		final int intStufe = NaesseStufe.getStufe(msDaten.nsStufe);
		ResultData resultat;
		msDaten.keineDaten = keineDaten;

		if (keineDaten) {
			resultat = new ResultData(msDaten.messObject, DD_NAESSE_STUFE,
					msDaten.nsStufeZeitStempel, null);
		} else {
			final Data data = verwaltung.getVerbindung().createData(
					verwaltung.getVerbindung().getDataModel()
							.getAttributeGroup(NaesseStufe.ATG_UFDMS_NS));
			data.getItem("NässeStufe").asUnscaledValue().set(intStufe);
			resultat = new ResultData(msDaten.messObject, DD_NAESSE_STUFE,
					msDaten.nsStufeZeitStempel, data);
		}

		try {
			verwaltung.getVerbindung().sendData(resultat);
		} catch (final DataNotSubscribedException e) {
			LOGGER.error(
					"Fehler bei Sendung der Daten fuer "
							+ msDaten.messObject.getPid() + " ATG "
							+ NaesseStufe.ATG_UFDMS_NS + " :\n"
							+ e.getMessage());
		} catch (final SendSubscriptionNotConfirmed e) {
			LOGGER.error(
					"Fehler bei Sendung der Daten fuer "
							+ msDaten.messObject.getPid() + " ATG "
							+ NaesseStufe.ATG_UFDMS_NS + " :\n"
							+ e.getMessage());
		}
	}

	/**
	 * Loescht alle AktualisierungsAuftraege fuer die MessStelle
	 * 
	 * @param messStelleDaten
	 *            Messtelle mit Daten
	 */
	public void loesche(final MessStelleDaten messStelleDaten) {
		messStelleDaten.aktualisierungLaeuft = false;
		NaesseStufe.stufeAkutalisierer.setWecker(messStelleDaten,
				ObjektWecker.AUS);
	}

	/**
	 * Fuegt ein AktualisierungsAuftraeg fuer die MessStelle ein
	 * 
	 * @param messStelleDaten
	 *            Messtelle mit Daten
	 */
	public void erstelleAuktualisierungsAuftrag(
			final MessStelleDaten messStelleDaten) {
		long zeitIntervall;
		if (messStelleDaten.aktualisierungLaeuft == true) {
			return;
		}

		final int intStufe = NaesseStufe.getStufe(messStelleDaten.nsStufe);
		if (intStufe < 1) {
			return;
		} else {
			zeitIntervall = messStelleDaten.abtrocknungsPhasen[intStufe - 1];
		}

		messStelleDaten.aktualisierungLaeuft = true;
		NaesseStufe.stufeAkutalisierer.setWecker(messStelleDaten,
				messStelleDaten.nsStufeZeitStempel + zeitIntervall);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ModulTyp getModulTyp() {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(final IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		this.verwaltung = verwaltung;
		NaesseStufe.stufeAkutalisierer = new ObjektWecker();

		DD_NAESSE_STUFE = new DataDescription(verwaltung.getVerbindung()
				.getDataModel().getAttributeGroup(NaesseStufe.ATG_UFDMS_NS),
				verwaltung.getVerbindung().getDataModel()
						.getAspect(NaesseStufe.ASP_KLASSIFIZIERUNG));

		DD_ABTROCKNUNGSPHASEN = new DataDescription(verwaltung.getVerbindung()
				.getDataModel().getAttributeGroup(NaesseStufe.ATG_UFDMS_AP),
				verwaltung.getVerbindung().getDataModel()
						.getAspect(NaesseStufe.ASP_PARAM_SOLL));

		if ((verwaltung.getSystemObjekte() == null)
				|| (verwaltung.getSystemObjekte().length == 0)) {
			return;
		}

		for (final SystemObject so : verwaltung.getSystemObjekte()) {
			try {
				if (so == null) {
					continue;
				}
				final MessStelleDaten messStelleDaten = new MessStelleDaten(so);
				final ResultData resultate = new ResultData(so,
						DD_NAESSE_STUFE, System.currentTimeMillis(), null);
				verwaltung.getVerbindung().subscribeSource(this, resultate);

				final ConfigurationObject confObjekt = (ConfigurationObject) so;
				final ObjectSet sensorMenge = confObjekt
						.getObjectSet(NaesseStufe.MNG_SENSOREN);
				naesseTabelle.put(so, messStelleDaten);
				for (final SystemObject sensor : sensorMenge.getElements()) {
					if (sensor.isValid()) {
						if (NaesseStufe.TYP_UFDS_NA.equals(sensor.getType()
								.getPid())) {
							naSensoren.add(sensor);
							naesseTabelle.put(sensor, messStelleDaten);
						} else if (NaesseStufe.TYP_UFDS_FBOFZS.equals(sensor
								.getType().getPid())) {
							fbofZustandSensoren.add(sensor);
							naesseTabelle.put(sensor, messStelleDaten);
						} else if (NaesseStufe.TYP_UFDS_NI.equals(sensor
								.getType().getPid())) {
							niSensoren.add(sensor);
							naesseTabelle.put(sensor, messStelleDaten);
						} else if (NaesseStufe.TYP_UFDS_WFD.equals(sensor
								.getType().getPid())) {
							wfdSensoren.add(sensor);
							naesseTabelle.put(sensor, messStelleDaten);
						}
					}
				}
			} catch (final OneSubscriptionPerSendData e) {
				// Debug.getLogger().error("Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt"
				// + so.getPid() + " unerfolgreich:" + e.getMessage());
				throw new DUAInitialisierungsException(
						"Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt"
								+ so.getPid() + " unerfolgreich:"
								+ e.getMessage());
			}
		}

		verwaltung.getVerbindung().subscribeReceiver(this,
				verwaltung.getSystemObjekte(), DD_ABTROCKNUNGSPHASEN,
				ReceiveOptions.normal(), ReceiverRole.receiver());

	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setNaechstenBearbeitungsKnoten(final IBearbeitungsKnoten knoten) {
		this.naechsterBearbeitungsKnoten = knoten;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setPublikation(final boolean publizieren) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void aktualisierePublikation(final IDatenFlussSteuerung dfs) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dataRequest(final SystemObject object,
			final DataDescription dataDescription, final byte state) {
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isRequestSupported(final SystemObject object,
			final DataDescription dataDescription) {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void update(final ResultData[] results) {
		for (final ResultData resData : results) {
			final DataDescription dataDescription = resData
					.getDataDescription();
			final Data daten = resData.getData();
			final SystemObject objekt = resData.getObject();
			final MessStelleDaten messStelleDaten = this.naesseTabelle
					.get(objekt);

			if (dataDescription.getAttributeGroup().getPid()
					.equals(NaesseStufe.ATG_UFDMS_AP)
					&& dataDescription.getAspect().getPid()
							.equals(NaesseStufe.ASP_PARAM_SOLL)) {

				if (messStelleDaten == null) {
					LOGGER.warning(
							"Objekt " + objekt
									+ " in der Hashtabelle nicht gefunden");
					continue;
				}
				if (daten == null) {
					messStelleDaten.initialisiert = false;
					continue;
				}
				// Auslesen der Parameter
				for (int i = 0; i < NaesseStufe.ATT_STUFE.length; i++) {
					messStelleDaten.abtrocknungsPhasen[i] = daten
							.getItem(NaesseStufe.ATT_STUFE[i]).asTimeValue()
							.getMillis();
				}
				messStelleDaten.initialisiert = true;
			}
		}
	}

	/**
	 * Ergibt den Integer Wert einer NS_Stufe
	 * 
	 * @param stufe
	 *            NS_Stufe
	 * @return int Wert
	 */
	public static int getStufe(final NS_Stufe stufe) {
		int intStufe = stufe.ordinal();
		if (stufe == NS_Stufe.NS_WERT_NE) {
			intStufe = -1;
		}
		return intStufe;
	}

	/**
	 * erfragt die menge der bearbeiteten NiederschalgsArt Sensoren
	 * 
	 * @return Menge der Sensoren
	 */
	public Collection<SystemObject> getNaSensoren() {
		return this.naSensoren;
	}

	/**
	 * erfragt die menge der bearbeiteten FahrBahnOberFlächenZustand Sensoren
	 * 
	 * @return Menge der Sensoren
	 */
	public Collection<SystemObject> getFbofZustandSensoren() {
		return this.fbofZustandSensoren;
	}
}
