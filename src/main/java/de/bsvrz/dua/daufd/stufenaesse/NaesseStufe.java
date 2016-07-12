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
package de.bsvrz.dua.daufd.stufenaesse;

import de.bsvrz.dav.daf.main.*;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe.NI_Stufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe.WFD_Stufe;
import de.bsvrz.dua.daufd.vew.FBZ_Klasse;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Bestimmt die NS_Stufe aus der NI_Stufe und WFD_Stufe nach 
 * der eingegebener Tabelle, bei fehlenden WFD Stufen und nachlassender 
 * Niederschlagintensitaet Verzoegert die Senkung der NS Stufe
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class NaesseStufe extends MessStellenDatenContainer implements IBearbeitungsKnoten, ClientSenderInterface, ClientReceiverInterface {

	private static final Debug _debug = Debug.getLogger();
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
	 * Nur fuer Testzwecken
	 * 
	 * @param stufe Naessestufe
	 */
	void infoVerzoegerung(int stufe) { }
	
	/**
	 *  Tabelle aus AFo - Ermittelt aus WFD und NI stufe die NaesseStufe
	 * 
	 *  Die Tabellen bildet WFDStufen an Tabellen von  NiStufen ab
	 *  Jede Zeile ist eine Abbildung von NI-Stufen auf NaesseStufen
	 *  
	 *  Es gibt je FBZ-Klasse eien Tabelle
	 */
	private static final Map<FBZ_Klasse,Map<WFD_Stufe,
			Map<NI_Stufe, NS_Stufe>>> tabellenWFDNIzumNS = new EnumMap<>(FBZ_Klasse.class);

	/**
	 * Funktion, die die gelesene Tabelle zurückgibt (für Testfälle)
	 * @return
	 */
	public static Map<FBZ_Klasse,Map<WFD_Stufe, Map<NI_Stufe, NS_Stufe>>> getTabellenWFDNIzumNS() {
		return tabellenWFDNIzumNS;
	}

	private static void initTable() {
		for(FBZ_Klasse klasse : FBZ_Klasse.values()) {
			Map<WFD_Stufe, Map<NI_Stufe, NS_Stufe>> table = new EnumMap<>(WFD_Stufe.class);
			
			Map<NI_Stufe, NS_Stufe> zeile = new EnumMap<>(NI_Stufe.class);
			zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_TROCKEN);
			zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_TROCKEN);
			zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS1);
			zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_TROCKEN);
			table.put(WFD_Stufe.WFD_STUFE0, zeile);

			zeile = new EnumMap<>(NI_Stufe.class);
			zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS1);
			zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS1);
			zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
			zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
			zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS1);
			table.put(WFD_Stufe.WFD_STUFE1, zeile);

			zeile = new EnumMap<>(NI_Stufe.class);
			zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
			zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
			zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS2);
			table.put(WFD_Stufe.WFD_STUFE2, zeile);

			zeile = new EnumMap<>(NI_Stufe.class);
			zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS3);
			zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS3);
			zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS4);
			zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_NASS3);
			table.put(WFD_Stufe.WFD_STUFE3, zeile);

			zeile = new EnumMap<>(NI_Stufe.class);
			zeile.put(NI_Stufe.NI_STUFE0, NS_Stufe.NS_TROCKEN);
			zeile.put(NI_Stufe.NI_STUFE1, NS_Stufe.NS_TROCKEN);
			zeile.put(NI_Stufe.NI_STUFE2, NS_Stufe.NS_NASS1);
			zeile.put(NI_Stufe.NI_STUFE3, NS_Stufe.NS_NASS2);
			zeile.put(NI_Stufe.NI_STUFE4, NS_Stufe.NS_NASS3);
			zeile.put(NI_Stufe.NI_WERT_NV, NS_Stufe.NS_WERT_NE);
			table.put(WFD_Stufe.WFD_WERT_NV, zeile);
			
			tabellenWFDNIzumNS.put(klasse, table);
		}
	}

	;
	
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
	public static final String ATT_STUFE[] = {
			"ZeitNass1Trocken", "ZeitNass2Nass1", "ZeitNass3Nass2", "ZeitNass4Nass3"
	};

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {

		for(ResultData resData : resultate) {

			long zeitStempel = resData.getDataTime();
			SystemObject so = resData.getObject();
			NaesseMessStelleDaten msDaten = (NaesseMessStelleDaten) messStellenDaten.get(so);

			if(msDaten == null) continue;
			
			if(aktualisiereMessstellenDaten(resData)) continue;
			synchronized(msDaten){
				if(!msDaten.keineDaten){
					publiziereNsStufe(msDaten, false);
				}
				aktualisiereNaesseStufe(msDaten, zeitStempel, msDaten.vorletzteZeitStempel);
			}
		}

		if(naechsterBearbeitungsKnoten !=  null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}

	/**
	 * Aktualisiert die NaesseStefe einer MessStelle nach den Regel aus [Afo]
	 * @param msDaten MessStelle
	 */
	public void aktualisiereNaesseStufe(NaesseMessStelleDaten msDaten, long zeitStempel, long vorletzeZeitStempel){
		
		FBZ_Klasse klasse = msDaten.getFbzKlasse();
		
		NS_Stufe neueStufe;
	
		// Ob die NS Stufe unbestimmbar ist
		msDaten.unbestimmbar = klasse == null;
		
		// Ein Datum fehlt noch
		if(!(msDaten.niStufeZeitStempel == msDaten.wfdStufeZeitStempel && 
			msDaten.fbofZustandZeitStempel == msDaten.niederschlagsArtZeitStempel &&
			msDaten.fbofZustandZeitStempel == msDaten.niStufeZeitStempel)) {
			
			// Auch im vorherigen zyklus fehlt ein DS, nsStufe wurde nicht publiziert
			if(msDaten.nsStufeZeitStempel != vorletzeZeitStempel) {
				neueStufe = msDaten.zielNsStufe;
				aktualisiereNsStufe(msDaten, neueStufe, vorletzeZeitStempel);
			}
			
			if(klasse == null){
				return;
			} 
			else if(msDaten.niStufeZeitStempel < msDaten.wfdStufeZeitStempel ) {
				msDaten.zielNsStufe = tabellenWFDNIzumNS.get(klasse).get(msDaten.wfdStufe).get(NI_Stufe.NI_WERT_NV);
			}
			else if(msDaten.niStufeZeitStempel > msDaten.wfdStufeZeitStempel ) {
				msDaten.zielNsStufe = tabellenWFDNIzumNS.get(klasse).get(WFD_Stufe.WFD_WERT_NV).get(msDaten.niStufe);
			}
			return;
		}
		
		
		if(msDaten.unbestimmbar) {
			// FBZ-Klasse ungültig
			msDaten.nsStufe = neueStufe = NS_Stufe.NS_WERT_NE;
		}
		else {
			// Alle Daten sind vorhanden
			neueStufe = tabellenWFDNIzumNS.get(klasse).get(msDaten.wfdStufe).get(msDaten.niStufe);
		}
		msDaten.zielNsStufe = neueStufe;

		aktualisiereNsStufe(msDaten, neueStufe, zeitStempel);		
	}

	private void aktualisiereNsStufe(final NaesseMessStelleDaten msDaten, NS_Stufe neueStufe, final long zeitStempel) {
		if(msDaten.minimumStufe != null && neueStufe.ordinal() >= msDaten.minimumStufe.ordinal()
				|| msDaten.abtrockungsZeitStempel < zeitStempel){
			msDaten.minimumStufe = null;
		}


		if(msDaten.wfdStufe == WFD_Stufe.WFD_WERT_NV) {
			// Prüfen ob der Wert durch die Abtrocknungsphasen angepasst werden muss
			if(msDaten.minimumStufe != null && msDaten.abtrockungsZeitStempel > zeitStempel){
				neueStufe = msDaten.minimumStufe;
			}

			if(msDaten.nsStufe != NS_Stufe.NS_WERT_NE
					&& neueStufe.ordinal() < msDaten.nsStufe.ordinal()) {
				// WFD nicht vorhanden, aber NI-Stufe, also Verzögerung beim Abklingen


				if(msDaten.minimumStufe != null 
						&& msDaten.minimumStufe.ordinal() >= msDaten.nsStufe.ordinal()) {
					// Zwischenstufen durchlaufen
					neueStufe = msDaten.nsStufe = NS_Stufe.values()[msDaten.nsStufe.ordinal() - 1];
				}

				if(msDaten.nsStufe != NS_Stufe.NS_TROCKEN) {
					long abtrocknungsDauer = msDaten.abtrocknungsPhasen[getStufe(msDaten.nsStufe) - 1];
					if(abtrocknungsDauer > 0) {
						// Diese Stufe muss die Verzögerung lang beibehalten werden
						msDaten.minimumStufe = msDaten.nsStufe;

						// Der Endzeitstempel für die Verzögerung
						msDaten.abtrockungsZeitStempel = zeitStempel + abtrocknungsDauer;

						// Vorerst den alten Wert beibehalten
						neueStufe = msDaten.nsStufe;
					}
				}
			}
		}


		msDaten.nsStufe = neueStufe;
		msDaten.nsStufeZeitStempel = zeitStempel;
		if(zeitStempel != Long.MIN_VALUE) {
			publiziereNsStufe(msDaten, false);
		}
	}

	/**
	 * Publiziert die NS stufe einer Messstelle
	 * @param msDaten MessStelle Daten
	 * @param keineDaten True, wenn ein Null Datensatz geschickt werden soll
	 */
	public void publiziereNsStufe(MessStelleDaten msDaten, boolean keineDaten) {
		
		int intStufe = getStufe(msDaten.nsStufe);
		ResultData resultat;
		msDaten.keineDaten = keineDaten;
		
		if(msDaten.nsStufeZeitStempelGesendet == msDaten.nsStufeZeitStempel) return;
		
		if(keineDaten)
			 resultat = new ResultData(msDaten.messObject, DD_NAESSE_STUFE, msDaten.nsStufeZeitStempel, null);
		else {
			Data data = verwaltung.getVerbindung().createData(
					verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_NS));
			data.getItem("NässeStufe").asUnscaledValue().set(intStufe);
			resultat = new ResultData(msDaten.messObject, DD_NAESSE_STUFE, msDaten.nsStufeZeitStempel, data);
		}
		
		msDaten.nsStufeZeitStempelGesendet = msDaten.nsStufeZeitStempel;
		
		try {
			verwaltung.getVerbindung().sendData(resultat);
		} catch (DataNotSubscribedException  e) {
			Debug.getLogger().error("Fehler bei Sendung der Daten fuer " + msDaten.messObject.getPid() + " ATG " + ATG_UFDMS_NS + " :\n" + e.getMessage());
		} catch (SendSubscriptionNotConfirmed e){
			Debug.getLogger().error("Fehler bei Sendung der Daten fuer " + msDaten.messObject.getPid() + " ATG " + ATG_UFDMS_NS + " :\n" + e.getMessage());
		}		
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

		initTable();

		for(FBZ_Klasse klasse : FBZ_Klasse.values()) {
			// erstmal alle Tabellen vorbelegen
			readTable(verwaltung, "konfigurationNS", tabellenWFDNIzumNS.get(klasse));
		}
		
		// und dann konkrete tabellen füllen:

		// erstmal alle Tabellen vorbelegen
		readTable(verwaltung, "konfigurationNSRegen", tabellenWFDNIzumNS.get(FBZ_Klasse.Regen));
		readTable(verwaltung, "konfigurationNSSchnee", tabellenWFDNIzumNS.get(FBZ_Klasse.Schnee));
		readTable(verwaltung, "konfigurationNSPlatzregen", tabellenWFDNIzumNS.get(FBZ_Klasse.Platzregen));
		readTable(verwaltung, "konfigurationNSGlaette", tabellenWFDNIzumNS.get(FBZ_Klasse.Glaette));

		DD_NAESSE_STUFE = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_NS), 
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_KLASSIFIZIERUNG));
		
		DD_ABTROCKNUNGSPHASEN = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_AP), 
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_PARAM_SOLL));
		
		if(verwaltung.getSystemObjekte() == null || verwaltung.getSystemObjekte().length == 0) return;

		initDaten(verwaltung);

		for(SystemObject systemObject : verwaltung.getSystemObjekte()) {
			ResultData resultate = new ResultData(systemObject, DD_NAESSE_STUFE, System.currentTimeMillis(), null);
			try {
				verwaltung.getVerbindung().subscribeSource(this, resultate);
			}
			catch(OneSubscriptionPerSendData e) {
				throw new DUAInitialisierungsException("Anmeldung als Quelle fuer Nässestufe fuer Objekt" + systemObject.getPid() + " unerfolgreich:" + e.getMessage());
			}
		}

		verwaltung.getVerbindung().subscribeReceiver(this, 
					verwaltung.getSystemObjekte(), DD_ABTROCKNUNGSPHASEN, ReceiveOptions.normal(), ReceiverRole.receiver());
			
			
	}

	@Override
	protected NaesseStufe.NaesseMessStelleDaten getMessStelleDaten(final SystemObject so) {
		return new NaesseMessStelleDaten(so);
	}

	protected void readTable(final IVerwaltung verwaltung, final String argument, final Map<WFD_Stufe, Map<NI_Stufe, NS_Stufe>> table) {
		String naesseStufenKonfigDatei = verwaltung.getArgument(argument);
		if(naesseStufenKonfigDatei != null) {
			Path path = Paths.get(naesseStufenKonfigDatei);
			try {
				List<String> lines = Files.readAllLines(path);
				int zeilenNummer = 0;
				for(String zeile : lines) {
					WFD_Stufe wfdStufe = WFD_Stufe.values()[zeilenNummer];
					String[] split = zeile.split("[,;]");
					int spaltenNummer = 0;
					for(String eintrag : split) {
						NI_Stufe niStufe = NI_Stufe.values()[spaltenNummer];
						String trim = eintrag.trim();
						table.get(wfdStufe).put(niStufe, stringZuNsStufe(trim));
						spaltenNummer++;
					}
					zeilenNummer++;
				}
				_debug.info("Nässestufenkonfiguration " + argument + " erfolgreich eingelesen: " + printTable(table));
			}
			catch(Exception e){
				_debug.error("Kann Nässestufenkonfiguration nicht aus Datei " + path.toAbsolutePath() + " einlesen", e);
				System.exit(-1);
			}
		}
		else {
			_debug.info("Verwende Standard-Nässestufenkonfiguration, da der Parameter -" + argument + " nicht angegeben wurde");
		}
	}

	private String printTable(final Map<WFD_Stufe, Map<NI_Stufe, NS_Stufe>> table) {
		StringBuilder stringBuilder = new StringBuilder();
		for(NI_Stufe ni_stufe : NI_Stufe.values()) {
			stringBuilder.append(String.format("%15s ", ni_stufe));
		}
		for(WFD_Stufe wfd_stufe : WFD_Stufe.values()) {
			stringBuilder.append("\n");
			stringBuilder.append(String.format("%15s ", wfd_stufe));
			for(NI_Stufe ni_stufe : NI_Stufe.values()) {
				stringBuilder.append(String.format("%15s ", table.get(wfd_stufe).get(ni_stufe)));
			}
		}		
		return stringBuilder.toString();
	}

	private NS_Stufe stringZuNsStufe(final String string) throws IOException {
		switch(string.toLowerCase()){
			case "trocken" : return NS_Stufe.NS_TROCKEN;
			case "nass1" : return NS_Stufe.NS_NASS1;
			case "nass2" : return NS_Stufe.NS_NASS2;
			case "nass3" : return NS_Stufe.NS_NASS3;
			case "nass4" : return NS_Stufe.NS_NASS4;
			case "" : return NS_Stufe.NS_WERT_NE;
		}
		throw new IOException("Ungültige Nässestufe in CSV-Datei: " + string + ". Gültige Werte: trocken, nass[1-4], <leer>");
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
			SystemObject objekt = resData.getObject();
			NaesseMessStelleDaten messStelleDaten = (NaesseMessStelleDaten) this.messStellenDaten.get(objekt);
			
			if(dataDescription.getAttributeGroup().getPid().equals(ATG_UFDMS_AP) &&
					dataDescription.getAspect().getPid().equals(ASP_PARAM_SOLL)) {
				
				if(messStelleDaten == null) {
					Debug.getLogger().warning("Objekt " + objekt + " in der Hashtabelle nicht gefunden");
					continue;
				}
				if(daten == null) {
					continue;
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


	/**
	 *  Die letzte empfangene Daten fuer jede MessStelle
	 *  nud andere parameter werden in diese Klasse gespeichert
	 *
	 *  @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	protected class NaesseMessStelleDaten extends MessStelleDaten {

		public NS_Stufe minimumStufe = null;
		
		public long abtrockungsZeitStempel = -1;

		/**
		 * Standardkonstruktor
		 * @param so Systemokjekt MessStelle
		 */
		public NaesseMessStelleDaten(SystemObject so) {
			super(so);
		}

		/**
		 * Die ZeitDauer, bis sich die Fahrbahnoberflaeche abtrocknet
		 */
		public long [] abtrocknungsPhasen = new long[ATT_STUFE.length];
	}
}
