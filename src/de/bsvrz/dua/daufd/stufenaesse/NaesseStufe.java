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

import java.util.Hashtable;

import com.sun.corba.se.pept.transport.ContactInfo;
import com.sun.jndi.toolkit.ctx.Continuation;

import de.bsvrz.dav.daf.main.ClientReceiverInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
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
 * Bestimmt aus NI_Stufe und WFD_Stufe die NS_Stufe
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
	private static ObjektWecker stufeAkutalisierer = new ObjektWecker(); 
	/**
	 *  Die letzte empfangene Daten fuer jede MessStelle
	 * 
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	private class MessStelleDaten implements IObjektWeckerListener {
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
		 * Akutlaisiert die NaesseStufe nach den Abtrocknungsphasen
		 */
		synchronized public void alarm() {
			if(nsStufe != zielNsStufe) {
				long zeitIntervall;
				int intStufe = mapNsStufeZumInt(nsStufe);
				if(intStufe<1) zeitIntervall = abtrocknungsPhasen[0];
				else zeitIntervall = abtrocknungsPhasen[intStufe-1];
				
				// wir gehen nach unten
				if(zielNsStufe.compareTo(nsStufe)<0)
					intStufe--;
				else intStufe = mapNsStufeZumInt(zielNsStufe);
				
				nsStufe = mapIntNSStufe[intStufe];
				nsStufeZeitStempel += zeitIntervall;
				publiziereNsStufe(messObject, nsStufe, nsStufeZeitStempel);
				
				if(nsStufe != zielNsStufe) {
					if(intStufe<1) zeitIntervall = abtrocknungsPhasen[0];
					else zeitIntervall = abtrocknungsPhasen[intStufe-1]; 
					stufeAkutalisierer.setWecker(this, nsStufeZeitStempel + zeitIntervall);
				}
			}
		}
	}
	/**
	 * Die lokale Daten werden hier pro MessStelle gespeichert
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
	 *  Tabelle aus AFo - Ermitellt aus WFD und NI stufe die NaesseStufe
	 * 
	 *  Die Tabelle bildet WFDStufen an Tabellen von  NiStufen ab
	 *  Jede Zeile ist eine Abbildung von NI-Stufen auf NaesseStufen
	 */
	static Hashtable<WFD_Stufe, 
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

	private static final String ATG_UFDMS_NS = "atg.ufdmsNässeStufe";
	private static final String ATG_UFDMS_AP = "atg.ufdmsAbtrockungsPhasen";
	private static final String ASP_KLASSIFIZIERUNG = "asp.klassifizierung";
	private static final String ATG_WFD_STUFE = "atg.ufdsStufeWasserFilmDicke";
	private static final String ATG_NI_STUFE = "atg.ufdsStufeNiederschlagsIntensität";
	private static final String ATT_STUFE[] = new String [] { 
		 "ZeitNass1Trocken", "ZeitNass4Nass3", "ZeitNass3Nass2", "ZeitNass2Nass1"
	};
	/**
	 * Aspekt fuer Parametrierung
	 */
	private static final String ASP_PARAM_SOLL = "asp.parameterSoll";

	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {

		for(ResultData resData : resultate) {
			
			Data data = resData.getData();
			if(data == null) continue;
			SystemObject so = resData.getObject();
			MessStelleDaten messStelleDaten = naesseTabelle.get(so);
			long zeitStempel = resData.getDataTime();
			
			if( ATG_NI_STUFE.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_KLASSIFIZIERUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
				
				if(messStelleDaten == null) {
					LOGGER.warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
					continue;
				}
				synchronized (messStelleDaten) {
					int stufe = data.getItem("Stufe").asUnscaledValue().intValue();
					NI_Stufe niStufe = NiederschlagIntensitaetStufe.getStufe(stufe);
					messStelleDaten.niStufe = niStufe;
					messStelleDaten.niStufeZeitStempel = zeitStempel;					
					aktualisiereNaesseStufe(messStelleDaten);
				}
			}
			else if( ATG_WFD_STUFE.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_KLASSIFIZIERUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
				if(messStelleDaten == null) {
					LOGGER.warning("Objekt " + so + " in der Hashtabelle nicht gefunden");
					continue;
				}
				synchronized (messStelleDaten) {
					int stufe = data.getItem("Stufe").asUnscaledValue().intValue();
					WFD_Stufe wfdStufe = WasserFilmDickeStufe.getStufe(stufe);
					messStelleDaten.wfdStufe = wfdStufe;
					messStelleDaten.wfdStufeZeitStempel = zeitStempel;
					aktualisiereNaesseStufe(messStelleDaten);
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
	public void aktualisiereNaesseStufe(MessStelleDaten messStelleDaten){
		
		NS_Stufe neueStufe;
	
		// Ein Datum faehlt noch - entweder NIStufe oder  WFDStufe
		if(messStelleDaten.niStufeZeitStempel != messStelleDaten.wfdStufeZeitStempel ) {
			if(messStelleDaten.niStufeZeitStempel < messStelleDaten.wfdStufeZeitStempel ) {
				neueStufe = tabelleWFDNIzumNS.get(messStelleDaten.wfdStufe).get(NI_Stufe.NI_WERT_NV);
				messStelleDaten.nsStufeZeitStempel = messStelleDaten.wfdStufeZeitStempel;
			}
			else {
				neueStufe = tabelleWFDNIzumNS.get(WFD_Stufe.WFD_WERT_NV).get(messStelleDaten.niStufe);
				messStelleDaten.nsStufeZeitStempel = messStelleDaten.niStufeZeitStempel;
			}
			// wir publizieren nicht, nur erstellen einen Aktualisiernugsauftrag fuer dem Fall, dass es keine
			// weitere DS kommen
			messStelleDaten.zielNsStufe = neueStufe;
			erstelleAuktualisierungsAuftrag(messStelleDaten);
			return;
		}
		// Beide Stufen sind vorhanden
		neueStufe = tabelleWFDNIzumNS.get(messStelleDaten.wfdStufe).get(messStelleDaten.niStufe);
		messStelleDaten.zielNsStufe = neueStufe;
		loesche(messStelleDaten);

		// Wir gehen mehr als eine Stufe nach unten oder bei nachlassenden Niederschlag WFD nicht verfuegbar ist
		if(neueStufe.compareTo(messStelleDaten.nsStufe)<-1 || 
				(neueStufe.compareTo(messStelleDaten.nsStufe)<0 && messStelleDaten.wfdStufe == WFD_Stufe.WFD_WERT_NV)) {
			int intStufe = mapNsStufeZumInt(messStelleDaten.nsStufe);
			intStufe--;
			messStelleDaten.nsStufe = mapIntNSStufe[intStufe];
			messStelleDaten.nsStufeZeitStempel = messStelleDaten.wfdStufeZeitStempel;
			publiziereNsStufe(messStelleDaten.messObject, messStelleDaten.nsStufe, messStelleDaten.nsStufeZeitStempel);
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
		
		int intStufe = mapNsStufeZumInt(stufe);
		Data data = verwaltung.getVerbindung().createData(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_NS));
		data.getItem("NässeStufe").asUnscaledValue().set(intStufe);
		
		ResultData resultat = new ResultData(objekt, DD_NAESSE_STUFE, zeitStempel, data);
		try {
			verwaltung.getVerbindung().sendData(resultat);
		} catch (Exception e) {
			LOGGER.error("Fehler bei Sendung von daten fuer " + objekt.getPid() + " ATG " + ATG_UFDMS_NS + " :\n" + e.getMessage());
		}
	}
	
	/**
	 * Loescht alle AktualisierungsAuftraege fuer die MessStelle
	 * @param messStelleDaten Messtelle mit Daten
	 */
	public void loesche(MessStelleDaten messStelleDaten) {
		stufeAkutalisierer.setWecker(messStelleDaten, ObjektWecker.AUS);
	}
	/**
	 * Fuegt ein AktualisierungsAuftraeg fuer die MessStelle ein
	 * @param messStelleDaten Messtelle mit Daten
	 */
	public void erstelleAuktualisierungsAuftrag(MessStelleDaten messStelleDaten) {
		long zeitIntervall;
//		long zeitBeginn = (messStelleDaten.niStufeZeitStempel > messStelleDaten.wfdStufeZeitStempel) ? 
//							messStelleDaten.niStufeZeitStempel : messStelleDaten.wfdStufeZeitStempel;
		int intStufe = mapNsStufeZumInt(messStelleDaten.nsStufe);
		if(intStufe<1) zeitIntervall = messStelleDaten.abtrocknungsPhasen[0];
		else zeitIntervall = messStelleDaten.abtrocknungsPhasen[intStufe-1];
		stufeAkutalisierer.setWecker(messStelleDaten, messStelleDaten.nsStufeZeitStempel + zeitIntervall);
	}

	/**
	 * {@inheritDoc}
	 */
	public ModulTyp getModulTyp() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialisiere(IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		
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
				naesseTabelle.put(so, new MessStelleDaten(so));
				ResultData resultate = new ResultData(so, DD_NAESSE_STUFE, System.currentTimeMillis(), null);
				verwaltung.getVerbindung().subscribeSource(this, resultate);
			} catch (OneSubscriptionPerSendData e) {
				LOGGER.error("Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt" + so.getPid() + " unerfolgreich:" + e.getMessage());	
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
			
			if(messStelleDaten == null) {
				LOGGER.warning("Objekt " + objekt + " in der Hashtabelle nicht gefunden");
				return;
			}
			
			if(dataDescription.getAttributeGroup().getPid().equals(ATG_UFDMS_AP)) {
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
	public static int mapNsStufeZumInt(NS_Stufe stufe) {
		int intStufe = stufe.ordinal();
		if(stufe == NS_Stufe.NS_WERT_NE) intStufe = -1;
		return intStufe;
	}

}
