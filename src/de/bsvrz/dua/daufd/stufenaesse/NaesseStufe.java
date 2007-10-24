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
package de.bsvrz.dua.daufd.stufenaesse;

import java.util.Collection;
import java.util.Hashtable;
import java.util.LinkedList;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
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
 * Bestimmt die NS_Stufe aus der NI_Stufe und WFD_Stufe nach 
 * der eingegebener Tabelle, bei fehlenden WFD Stufen und nachlassender 
 * Niederschlagintensitaet Verzoegert die Senkung der NS Stufe
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class NaesseStufe implements IBearbeitungsKnoten, ClientSenderInterface, ClientReceiverInterface {

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
	 * Bestimmt, ob bei Bestimmbarkeit Aenderung ( z.B. die Eisbedeckte FbOf taut)
	 * die Naessestufe publiziert werden soll
	 */
	protected static final boolean beiAenderungPublizieren = false;
	
	/**
	 *  Die letzte empfangene Daten fuer jede MessStelle
	 *  nud andere parameter werden in diese Klasse gespeichert
	 * 
	 *  @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	protected class MessStelleDaten implements IObjektWeckerListener {
		/**
		 * Standardkonstruktor
		 * @param so Systemokjekt MessStelle
		 */
		public MessStelleDaten(SystemObject so) {
			messObject = so;
		}
		public SystemObject messObject = null;
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
		 * Die ZeitDauer, bis sich die Fahrbahnoberflaeche abtrocknet
		 */
		public long [] abtrocknungsPhasen = new long[ATT_STUFE.length];
		/**
		 * ZeitStempel der NaesseSteufe der MessStelle
		 */
		public long nsStufeZeitStempel = 0;
		/**
		 * NaesseSteufe der MessStelle
		 */
		public NS_Stufe nsStufe = NS_Stufe.NS_WERT_NE;
		/**
		 * NaesseSteufe der MessStelle die erreicht werden soll bei verzoegerten Aenderungen
		 */
		public NS_Stufe zielNsStufe = NS_Stufe.NS_WERT_NE;
		/**
		 * Ob die NI Stufe unbestimmbar ist
		 */
		public boolean unbestimmbar = false;
		/**
		 * ZeitStempel des Letzten DS der Unbestimmmbarkeit geaendert hat
		 */
		public long unbestimmbarZeitStempel = 0;
		/**
		 *  Ob ein aktulasierungsauftrag lauft
		 */
		public boolean aktualisierungLaeuft = false;
		/**
		 * Akutlaisiert die NaesseStufe nach den Abtrocknungsphasen,
		 * ermoeglicht verzoegerte Aktualsierung
		 */
		synchronized public void alarm() {
			if(nsStufe != zielNsStufe && unbestimmbar == false) {
				long zeitIntervall;
				int intStufe = getStufe(nsStufe);
				if(intStufe<1) {
					aktualisierungLaeuft = false;
					return;
				}
				// nur zum Test-Zwecken
				infoVerzoegerung(intStufe);
				zeitIntervall = abtrocknungsPhasen[intStufe-1];
				intStufe--;
				
				nsStufe = mapIntNSStufe[intStufe];
				nsStufeZeitStempel += zeitIntervall;
				publiziereNsStufe(messObject, nsStufe, nsStufeZeitStempel);
				
				if(nsStufe != zielNsStufe && intStufe>0) {
					zeitIntervall = abtrocknungsPhasen[intStufe-1]; 
					stufeAkutalisierer.setWecker(this, nsStufeZeitStempel + zeitIntervall);
				}
				else aktualisierungLaeuft = false;
			}
			aktualisierungLaeuft = false;
		}
	}
	/**
	 * Ermoeglicht die Abbildung der Sensoren und MessStellen auf die Klasse mit Lokalen Daten
	 * fuer die gegebene MessStelle
	 */
	protected Hashtable<SystemObject, MessStelleDaten> naesseTabelle = new Hashtable<SystemObject, MessStelleDaten>();
	/**
	 *  Naesse Stufen, die unterscheidet werden
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum  NS_Stufe implements Comparable<NS_Stufe> {
		NS_TROCKEN, // ordinal = 0
		NS_NASS1,  // ordinal = 1
		NS_NASS2,  // etc.
		NS_NASS3,
		NS_NASS4,
		NS_WERT_NE; // Wert nicht ermittelbar (-1)
		
	};
	/**
	 * Abbildet integer Werte auf Symbolische Konstanten
	 */
	protected final static NS_Stufe [] mapIntNSStufe = new NS_Stufe []  
	{ NS_Stufe.NS_TROCKEN, NS_Stufe.NS_NASS1, NS_Stufe.NS_NASS2, NS_Stufe.NS_NASS3, NS_Stufe.NS_NASS4 };
	
	/**
	 * Abbildet die NS_Stufe von Int zur symbolischen Wert
	 * @param stufe Stufe int
	 * @return Stufe enum
	 */
	public static NS_Stufe getStufe(int stufe) {
		if(stufe<0 || stufe> mapIntNSStufe.length-1)
			return NS_Stufe.NS_WERT_NE;
		return mapIntNSStufe[stufe];
	}
	
	/**
	 * Nur fuer test Zwecken
	 * @param zeitIntervall
	 */
	void infoVerzoegerung(int stufe) { }
	
	/**
	 *  Tabelle aus AFo - Ermitellt aus WFD und NI stufe die NaesseStufe
	 * 
	 *  Die Tabelle bildet WFDStufen an Tabellen von  NiStufen ab
	 *  Jede Zeile ist eine Abbildung von NI-Stufen auf NaesseStufen
	 */
	static final Hashtable<WFD_Stufe, 
			Hashtable<NI_Stufe, NS_Stufe>> tabelleWFDNIzumNS = new Hashtable<WFD_Stufe, Hashtable<NI_Stufe,NS_Stufe>>();
	static {
		Hashtable<NI_Stufe, NS_Stufe> zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_TROCKEN);
		tabelleWFDNIzumNS.put(WFD_Stufe.WFD_STUFE0, zeile);
		
		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS1);
		tabelleWFDNIzumNS.put(WFD_Stufe.WFD_STUFE1, zeile);
		

		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS2);
		tabelleWFDNIzumNS.put(WFD_Stufe.WFD_STUFE2, zeile);
		
		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS3);
		tabelleWFDNIzumNS.put(WFD_Stufe.WFD_STUFE3, zeile);
		
		zeile = new Hashtable<NI_Stufe, NS_Stufe>();
		zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_TROCKEN);
		zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS1);
		zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
		zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
		zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4); 
		zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_WERT_NE);
		tabelleWFDNIzumNS.put(WFD_Stufe.WFD_WERT_NV, zeile);
	};
	
	/** 
	 *  String-Konstanten TYPEN
	 */
	public static final String TYP_UFDS_NI = "typ.ufdsNiederschlagsIntensität";
	public static final String TYP_UFDS_WFD = "typ.ufdsWasserFilmDicke";
	public static final String TYP_UFDS_NA = "typ.ufdsNiederschlagsArt";
	public static final String TYP_UFDS_FBOFZS = "typ.ufdsFahrBahnOberFlächenZustand";
	
	/** 
	 *  String-Konstanten Attributgruppen
	 */
	public static final String ATG_UFDS_NA = "atg.ufdsNiederschlagsArt";
	public static final String ATG_UFDS_FBOFZS = "atg.ufdsFahrBahnOberFlächenZustand";
	public static final String ATG_UFDMS_NS = "atg.ufdmsNässeStufe";
	public static final String ATG_UFDMS_AP = "atg.ufdmsAbtrockungsPhasen";
	public static final String ATG_WFD_STUFE = "atg.ufdsStufeWasserFilmDicke";
	public static final String ATG_NI_STUFE = "atg.ufdsStufeNiederschlagsIntensität";
	
	/** 
	 *  String-Konstanten Aspekte
	 */
	public static final String ASP_MESSWERTERSETZUNG = "asp.messWertErsetzung";
	public static final String ASP_KLASSIFIZIERUNG = "asp.klassifizierung";
	public static final String ASP_PARAM_SOLL = "asp.parameterSoll";
	
	/** 
	 *  String-Konstanten Attribute
	 */
	public static final String MNG_SENSOREN = "UmfeldDatenSensoren";
	public static final String ATT_STUFE[] = new String [] { 
		 "ZeitNass1Trocken", "ZeitNass4Nass3", "ZeitNass3Nass2", "ZeitNass2Nass1"
	};

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {

		for(ResultData resData : resultate) {
			
			Data data = resData.getData();
			if(data == null) continue;
			SystemObject so = resData.getObject();
			MessStelleDaten msDaten = naesseTabelle.get(so);
			long zeitStempel = resData.getDataTime();
			long vorletzteZeitStempel;

			if( ATG_NI_STUFE.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_KLASSIFIZIERUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
				if(msDaten == null) {
					LOGGER.warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
					continue;
				}
				int stufe = data.getItem("Stufe").asUnscaledValue().intValue();
				NI_Stufe niStufe = NiederschlagIntensitaetStufe.getStufe(stufe);
				
				synchronized (msDaten) {
					msDaten.niStufe = niStufe;
					vorletzteZeitStempel = msDaten.niStufeZeitStempel; 
					msDaten.niStufeZeitStempel = zeitStempel;					
					aktualisiereNaesseStufe(msDaten, zeitStempel, vorletzteZeitStempel, false);
				}
			}
			else if( ATG_WFD_STUFE.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_KLASSIFIZIERUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
				if(msDaten == null) {
					LOGGER.warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
					continue;
				}
				int stufe = data.getItem("Stufe").asUnscaledValue().intValue();
				WFD_Stufe wfdStufe = WasserFilmDickeStufe.getStufe(stufe);
				
				synchronized (msDaten) {
					msDaten.wfdStufe = wfdStufe;
					vorletzteZeitStempel = msDaten.wfdStufeZeitStempel; 
					msDaten.wfdStufeZeitStempel = zeitStempel;
					aktualisiereNaesseStufe(msDaten, zeitStempel, vorletzteZeitStempel, false);
				}
			}
			else if(ATG_UFDS_NA.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_MESSWERTERSETZUNG.equals(resData.getDataDescription().getAspect().getPid())) {
		
				if(msDaten == null) {
					LOGGER.warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
					continue;
				}
				int implausibel = data.getItem("NiederschlagsArt").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
				if(implausibel == 1) continue;
				
				int niederschlagsArt = data.getItem("NiederschlagsArt").getUnscaledValue("Wert").intValue();
				// Nicht fluessig :)
				if(niederschlagsArt>69 && niederschlagsArt<80) 
					synchronized (msDaten) {
						vorletzteZeitStempel = msDaten.unbestimmbarZeitStempel; 
						msDaten.unbestimmbarZeitStempel = zeitStempel;
						boolean warUnbestimmbar = msDaten.unbestimmbar;
						msDaten.unbestimmbar = true;
						if(warUnbestimmbar == false)
							aktualisiereNaesseStufe(msDaten, zeitStempel, vorletzteZeitStempel, true);
						
					}
				else synchronized (msDaten) {
					vorletzteZeitStempel = msDaten.unbestimmbarZeitStempel;
					msDaten.unbestimmbarZeitStempel = zeitStempel;
					boolean warUnbestimmbar = msDaten.unbestimmbar;
					msDaten.unbestimmbar = false;
					if(warUnbestimmbar == true)
						aktualisiereNaesseStufe(msDaten, zeitStempel, vorletzteZeitStempel, true);
					
				}
			}
			else if(ATG_UFDS_FBOFZS.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_MESSWERTERSETZUNG.equals(resData.getDataDescription().getAspect().getPid())) {

				if(msDaten == null) {
					LOGGER.warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
					continue;
				}
				int implausibel = data.getItem("FahrBahnOberFlächenZustand").getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").intValue();
				if(implausibel == 1) continue;
				
				int fbZustand = data.getItem("FahrBahnOberFlächenZustand").getUnscaledValue("Wert").intValue();
				// Mit Eis,Schnee usw. bedeckt
				if(fbZustand>63 && fbZustand<68) 
					synchronized (msDaten) {
						vorletzteZeitStempel = msDaten.unbestimmbarZeitStempel;
						msDaten.unbestimmbarZeitStempel = zeitStempel;
						boolean warUnbestimmbar = msDaten.unbestimmbar;
						msDaten.unbestimmbar = true;
						if(warUnbestimmbar == false)
							aktualisiereNaesseStufe(msDaten, zeitStempel, vorletzteZeitStempel, true);
						
					}
				else synchronized (msDaten) {
					vorletzteZeitStempel = msDaten.unbestimmbarZeitStempel;
					msDaten.unbestimmbarZeitStempel = zeitStempel;
					boolean warUnbestimmbar = msDaten.unbestimmbar;
					msDaten.unbestimmbar = false;
					if(warUnbestimmbar == true)
						aktualisiereNaesseStufe(msDaten, zeitStempel, vorletzteZeitStempel, true);

				}
				
			}		
		}

		if(naechsterBearbeitungsKnoten !=  null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}
	
	/**
	 * Aktualisiert die NAesseStefe einer MessStelle
	 * @param messStelleDaten MessStelle
	 */
	public void aktualisiereNaesseStufe(MessStelleDaten messStelleDaten, long zeitStempel, long vorletzeZeitStemepel, boolean unbestimbarkeitAenderung){
		
		NS_Stufe neueStufe;
	
		// init
		if(messStelleDaten.nsStufeZeitStempel == 0) {
			messStelleDaten.nsStufeZeitStempel = zeitStempel-100;
		}
		
		// wenn sich die Unbestimmbarkeit geaendert hat
		if(unbestimbarkeitAenderung) {
			if(!beiAenderungPublizieren) return;
			loesche(messStelleDaten);
			
			if(messStelleDaten.unbestimmbar) 
				neueStufe = messStelleDaten.zielNsStufe = NS_Stufe.NS_WERT_NE;
			else 
				neueStufe = messStelleDaten.zielNsStufe = tabelleWFDNIzumNS.get(messStelleDaten.wfdStufe).get(messStelleDaten.niStufe);
			
			if(zeitStempel>messStelleDaten.nsStufeZeitStempel) {
				messStelleDaten.nsStufe = neueStufe;
				messStelleDaten.nsStufeZeitStempel = zeitStempel;
				publiziereNsStufe(messStelleDaten.messObject, messStelleDaten.nsStufe, messStelleDaten.nsStufeZeitStempel);
			}
			return;
		}
		
		// Ein Datum faehlt noch - entweder NIStufe oder  WFDStufe
		if(messStelleDaten.niStufeZeitStempel != messStelleDaten.wfdStufeZeitStempel ) {
			loesche(messStelleDaten);
			
			// Auch im vorherigen zyklus faehlt ein DS, nsStufe wurde nicht publiziert
			if(messStelleDaten.nsStufeZeitStempel< vorletzeZeitStemepel) {
				messStelleDaten.nsStufe = messStelleDaten.zielNsStufe;
				messStelleDaten.nsStufeZeitStempel = vorletzeZeitStemepel;
				if(messStelleDaten.unbestimmbar) messStelleDaten.nsStufe = NS_Stufe.NS_WERT_NE;
				publiziereNsStufe(messStelleDaten.messObject, messStelleDaten.nsStufe, messStelleDaten.nsStufeZeitStempel);
			}
			
			
			if(messStelleDaten.niStufeZeitStempel < messStelleDaten.wfdStufeZeitStempel ) {
				neueStufe = tabelleWFDNIzumNS.get(messStelleDaten.wfdStufe).get(NI_Stufe.NI_WERT_NV);
				messStelleDaten.nsStufeZeitStempel = messStelleDaten.wfdStufeZeitStempel;
			}
			else {
				neueStufe = tabelleWFDNIzumNS.get(WFD_Stufe.WFD_WERT_NV).get(messStelleDaten.niStufe);
				messStelleDaten.nsStufeZeitStempel = messStelleDaten.niStufeZeitStempel;
			}
			messStelleDaten.zielNsStufe = neueStufe;
			return;
		}
		
		// Beide Stufen sind vorhanden
		neueStufe = tabelleWFDNIzumNS.get(messStelleDaten.wfdStufe).get(messStelleDaten.niStufe);
		if(messStelleDaten.unbestimmbar) messStelleDaten.nsStufe = neueStufe = NS_Stufe.NS_WERT_NE;
		messStelleDaten.zielNsStufe = neueStufe;

		// Wir gehen mehr als eine Stufe nach unten 
		if(messStelleDaten.nsStufe != NS_Stufe.NS_WERT_NE && neueStufe.compareTo(messStelleDaten.nsStufe)<-1 && messStelleDaten.wfdStufe != WFD_Stufe.WFD_WERT_NV) {
			loesche(messStelleDaten);
			int intStufe = getStufe(messStelleDaten.nsStufe);
			intStufe--;
			messStelleDaten.nsStufe = mapIntNSStufe[intStufe];
			messStelleDaten.nsStufeZeitStempel = messStelleDaten.wfdStufeZeitStempel;
			publiziereNsStufe(messStelleDaten.messObject, messStelleDaten.nsStufe, messStelleDaten.nsStufeZeitStempel);
			erstelleAuktualisierungsAuftrag(messStelleDaten);
			return;
		}
		//  bei nachlassenden Niederschlag WFD nicht verfuegbar ist
		if(messStelleDaten.nsStufe != NS_Stufe.NS_WERT_NE && neueStufe.compareTo(messStelleDaten.nsStufe)<0 && messStelleDaten.wfdStufe == WFD_Stufe.WFD_WERT_NV) {
			erstelleAuktualisierungsAuftrag(messStelleDaten);
			return;
		}

		messStelleDaten.nsStufe = neueStufe;
		messStelleDaten.nsStufeZeitStempel = messStelleDaten.wfdStufeZeitStempel;
		publiziereNsStufe(messStelleDaten.messObject, neueStufe, messStelleDaten.nsStufeZeitStempel);		
	}
	
	/**
	 * Publiziert die NS stufe einer Messstelle
	 * @param objekt SystemObjekt MessStelle
	 * @param stufe NS_Stufe
	 * @param zeitStempel Zeitpunkt
	 */
	public void publiziereNsStufe(SystemObject objekt, NS_Stufe stufe, long zeitStempel) {
		
		int intStufe = getStufe(stufe);
		Data data = verwaltung.getVerbindung().createData(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_NS));
		data.getItem("NässeStufe").asUnscaledValue().set(intStufe);
		
		ResultData resultat = new ResultData(objekt, DD_NAESSE_STUFE, zeitStempel, data);
		try {
			verwaltung.getVerbindung().sendData(resultat);
		} catch (Exception e) {
			LOGGER.error("Fehler bei Sendung der Daten fuer " + objekt.getPid() + " ATG " + ATG_UFDMS_NS + " :\n" + e.getMessage());
		}
	}
	
	/**
	 * Loescht alle AktualisierungsAuftraege fuer die MessStelle
	 * @param messStelleDaten Messtelle mit Daten
	 */
	public void loesche(MessStelleDaten messStelleDaten) {
		messStelleDaten.aktualisierungLaeuft = false;
		stufeAkutalisierer.setWecker(messStelleDaten, ObjektWecker.AUS);
	}
	
	/**
	 * Fuegt ein AktualisierungsAuftraeg fuer die MessStelle ein
	 * @param messStelleDaten Messtelle mit Daten
	 */
	public void erstelleAuktualisierungsAuftrag(MessStelleDaten messStelleDaten) {
		long zeitIntervall;
		if(messStelleDaten.aktualisierungLaeuft == true) return;
		
		int intStufe = getStufe(messStelleDaten.nsStufe);
		if(intStufe<1) return;
		else zeitIntervall = messStelleDaten.abtrocknungsPhasen[intStufe-1];
		
		messStelleDaten.aktualisierungLaeuft = true;
		stufeAkutalisierer.setWecker(messStelleDaten, messStelleDaten.nsStufeZeitStempel + zeitIntervall);
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
		stufeAkutalisierer = new ObjektWecker();
		
		DD_NAESSE_STUFE = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_NS), 
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_KLASSIFIZIERUNG));
		
		DD_ABTROCKNUNGSPHASEN = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_AP), 
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_PARAM_SOLL));
		
		if(verwaltung.getSystemObjekte() == null || verwaltung.getSystemObjekte().length == 0) return;
		
		for(SystemObject so: verwaltung.getSystemObjekte()) 
			try {
				if(so==null) continue;
				MessStelleDaten messStelleDaten = new MessStelleDaten(so);
				ResultData resultate = new ResultData(so, DD_NAESSE_STUFE, System.currentTimeMillis(), null);
				verwaltung.getVerbindung().subscribeSource(this, resultate);
				
				ConfigurationObject confObjekt = (ConfigurationObject)so;
				ObjectSet sensorMenge = confObjekt.getObjectSet(MNG_SENSOREN);
				naesseTabelle.put(so, messStelleDaten);
				for( SystemObject sensor : sensorMenge.getElements()) 
					if(TYP_UFDS_NA.equals(sensor.getType().getPid())) {
						naSensoren.add(sensor);
						naesseTabelle.put(sensor, messStelleDaten);
					}
					else if(TYP_UFDS_FBOFZS.equals(sensor.getType().getPid())) {
						fbofZustandSensoren.add(sensor);
						naesseTabelle.put(sensor, messStelleDaten);
					}
					else if(TYP_UFDS_NI.equals(sensor.getType().getPid())) {
						niSensoren.add(sensor);
						naesseTabelle.put(sensor, messStelleDaten);
					}
					else if(TYP_UFDS_WFD.equals(sensor.getType().getPid())) {
						wfdSensoren.add(sensor);
						naesseTabelle.put(sensor, messStelleDaten);
					}

			} catch (OneSubscriptionPerSendData e) {
				//LOGGER.error("Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt" + so.getPid() + " unerfolgreich:" + e.getMessage());
				throw new DUAInitialisierungsException("Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt" + so.getPid() + " unerfolgreich:" + e.getMessage());
			}
		verwaltung.getVerbindung().subscribeReceiver(this, 
				verwaltung.getSystemObjekte(), DD_ABTROCKNUNGSPHASEN, ReceiveOptions.normal(), ReceiverRole.receiver());
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
	public void update(ResultData[] results) {
		for(ResultData resData : results) {
			DataDescription dataDescription = resData.getDataDescription();
			Data daten = resData.getData();
			if(daten == null) continue;
			SystemObject objekt = resData.getObject();
			MessStelleDaten messStelleDaten = this.naesseTabelle.get(objekt);
		
			if(dataDescription.getAttributeGroup().getPid().equals(ATG_UFDMS_AP) &&
					dataDescription.getAspect().getPid().equals(ASP_PARAM_SOLL)) {
				
				if(messStelleDaten == null) {
					LOGGER.warning("Objekt " + objekt + " in der Hashtabelle nicht gefunden");
					return;
				}
				// Auslesen der Parameter 
				for(int i=0; i< ATT_STUFE.length; i++)
					messStelleDaten.abtrocknungsPhasen[i] = daten.getItem(ATT_STUFE[i]).asTimeValue().getMillis();
			}
		}
	}
	
	/**
	 * Ergibt den Integer Wert einer NS_Stufe
	 * @param stufe NS_Stufe
	 * @return int Wert
	 */
	public static int getStufe(NS_Stufe stufe) {
		int intStufe = stufe.ordinal();
		if(stufe == NS_Stufe.NS_WERT_NE) intStufe = -1;
		return intStufe;
	}
	/**
	 * erfragt die menge der bearbeiteten NiederschalgsArt Sensoren
	 * @return Menge der Sensoren
	 */
	public Collection<SystemObject> getNaSensoren() {
		return this.naSensoren;
	}
	
	/**
	 * erfragt die menge der bearbeiteten FahrBahnOberFlächenZustand Sensoren
	 * @return Menge der Sensoren
	 */
	public Collection<SystemObject> getFbofZustandSensoren() {
		return this.fbofZustandSensoren;
	}
}
