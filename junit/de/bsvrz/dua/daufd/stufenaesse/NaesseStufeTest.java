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
import java.util.Date;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

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
import de.bsvrz.dua.daufd.VerwaltungAufbereitungUFDTest;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe.NI_Stufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe.WFD_Stufe;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Testet und Parametriert den Modul NaesseStufe
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public class NaesseStufeTest extends NaesseStufe 
implements ClientSenderInterface {
	
	/**
	 * Verbindungsdaten
	 */
	private static final String[] CON_DATA = new  String[] {
			"-datenverteiler=localhost:8083",  
			"-benutzer=Tester", 
			"-authentifizierung=c:\\passwd", 
			"-debugLevelStdErrText=WARNING", 
			"-debugLevelFileText=WARNING",
			"-KonfigurationsBereichsPid=kb.daUfdTest" }; 

	/**
	 * Abtrocknungphasen Verzoegerung [AFo]
	 */
	private static final long abtrocknungPhasen[] = new long[] {
		180, 60, 60, 60 
	};
	/**
	 * Aspekt fuer Parametrierung
	 */
	private static final String ASP_PARAM_VORGABE = "asp.parameterVorgabe";
	/**
	 * Datenbeschreibung fuer die  Klasifizierung Daten
	 */
	private static DataDescription DD_ABTROCKNUNG_PHASEN = null;
	/**
	 * Der Logger
	 */
	private static Debug LOGGER = Debug.getLogger(); 
	/**
	 * Verbindung zum dav
	 */
	private static ClientDavInterface  dav;
	/**
	 * Der Verwaltungsmodul
	 */
	private static VerwaltungAufbereitungUFDTest hauptModul;
	/**
	 * Errechnete Ausgabewerte
	 */
	private static NS_Stufe ausgabe [] = null;
	/**
	 * Errechnete zeitStempel der Ausgabewerten
	 */
	private static long ausgabeZeitStempel [] = null;
	/**
	 * Aktueller index im Ausgabewerten
	 */
	private static int ausgabeIndex = 0;
	/**
	 * Im testfaellen wird der Verzoegerungsintervall fuer
	 * Abtrocknungsphasen verkuertzt
	 */
	private final long ABTR_INTERVALL = 1;
	/**
	 * Sensore die die Testdaten liefern
	 */
	private static SystemObject fbofZustandSensor, naSensor, wfdSensor, niSensor;
	/**
	 * Datenbeschreibung der Daten die von Testsensoren geschickt werden
	 */
	private static DataDescription DD_FBOF_ZUSTAND, DD_NIE_ART;
	/**
	 * Bestimmt ob man an die bearbeitung der Daten warten soll
	 */
	private static boolean warten = true;
	/**
	 * String-Konstanten
	 */
	private static final String TYP_UFDMS = "typ.umfeldDatenMessStelle";
	private static final String ATG_UFDMS_AP = "atg.ufdmsAbtrockungsPhasen";
	private static final String ATT_STUFE[] = new String [] { 
		 "ZeitNass1Trocken", "ZeitNass4Nass3", "ZeitNass3Nass2", "ZeitNass2Nass1"
	};

	/**
	 * Zustaende 
	 */
	private final int FBOF_TROCKEN = 0;
	private final int FBOF_EIS = 66;
	private final int NART_KEIN = 0;
	private final int NART_SCHNEE = 73;
	
	/**
	 * Parametriert die Verzoegerung bei der Abtrocknungphasen
	 * 
	 * @param dav Verbindung zum Datenverteiler
	 * @param konfBereiche konfigurationsbereiche in denen alle Objekte parametriert werden sollen
	 */
	public void ParametriereUfds(ClientDavInterface dav, Collection<ConfigurationArea> konfBereiche) {
		try {
			
			NaesseStufeTest.dav = dav;
			
			DD_ABTROCKNUNG_PHASEN = new DataDescription(
					dav.getDataModel().getAttributeGroup(ATG_UFDMS_AP),
					dav.getDataModel().getAspect(ASP_PARAM_VORGABE), (short)0);

			Collection<SystemObjectType> sotMenge = new LinkedList<SystemObjectType>();
			sotMenge.add(dav.getDataModel().getType(TYP_UFDMS));
			
			Collection<SystemObject> ufdsObjekte = dav.getDataModel().getObjects(konfBereiche, sotMenge, ObjectTimeSpecification.valid());
			
			if(ufdsObjekte == null) {
				LOGGER.error("Kein Objekt vom " + TYP_UFDMS + " in den KonfigurationsBeriechen :" + konfBereiche);
				System.exit(-1);
			}
			
			try {
				dav.subscribeSender(this, ufdsObjekte, DD_ABTROCKNUNG_PHASEN, SenderRole.sender());
			} catch (Exception e) {
				LOGGER.error("Fehler bei Anmeldung für Klassifizierung der Objekte vom Typ " + TYP_UFDMS + ":" + e.getMessage());
				e.printStackTrace();
			}
			Thread.sleep(100);
			
			dav.unsubscribeSender(this, ufdsObjekte, DD_ABTROCKNUNG_PHASEN);

		} catch (Exception e) {
			Debug.getLogger().error("Fehler bei Parametrierung der NaesseStufe Abtrocknungphasen: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void dataRequest(SystemObject object,
			DataDescription dataDescription, byte state) {
		if(dataDescription.getAttributeGroup().getPid().equals(ATG_UFDMS_AP) 
				&& state == START_SENDING ) {
			
			Data datei = dav.createData(dav.getDataModel().getAttributeGroup(ATG_UFDMS_AP));
			
			for(int i =0; i< ATT_STUFE.length; i++) {
				datei.getTimeValue(ATT_STUFE[i]).setSeconds(abtrocknungPhasen[i]);
			}
			
			ResultData resDatei = new ResultData(object, DD_ABTROCKNUNG_PHASEN, System.currentTimeMillis(), datei);
			
			try {
				dav.sendData(resDatei);
				System.out.println("Objekt " + object.getPid() + " Atg: " + ATG_UFDMS_AP + " parametriert ");
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

	/**
	 * {@inheritDoc}
	 */
	@Override 
	public void initialisiere(IVerwaltung verwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(verwaltung);
		
		
		dav = verwaltung.getVerbindung();
		
		for(SystemObject so : getNaSensoren())
			if(so != null) {
				naSensor = so;
				break;
			}
		
		for(SystemObject so :getFbofZustandSensoren())
			if(so != null) {
				fbofZustandSensor = so;
				break;
			}
		
		for(SystemObject so : niSensoren)
			if(so != null) {
				niSensor = so;
				break;
			}
		for(SystemObject so : wfdSensoren)
			if(so != null) {
				wfdSensor = so;
				break;
			}
		try {
			ResultData resultate;
			DD_FBOF_ZUSTAND = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_UFDS_FBOFZS),
					dav.getDataModel().getAspect(ASP_MESSWERTERSETZUNG));
			resultate = new ResultData(fbofZustandSensor, DD_FBOF_ZUSTAND, System.currentTimeMillis(), null);
			dav.subscribeSource(this, resultate);
			
			DD_NIE_ART = new DataDescription(dav.getDataModel().getAttributeGroup(ATG_UFDS_NA),
					dav.getDataModel().getAspect(ASP_MESSWERTERSETZUNG));
			resultate = new ResultData(naSensor, DD_NIE_ART, System.currentTimeMillis(), null);
			dav.subscribeSource(this, resultate);
				
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	

	/**
	 * {@inheritDoc}
	 */
	@Override 
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
					messStelleDaten.abtrocknungsPhasen[i] = ABTR_INTERVALL; // Im Testfaellen ist keine Verzoegerung (10 ms)
				messStelleDaten.initialisiert = true;
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void publiziereNsStufe(MessStelleDaten msDaten, boolean keineDaten) {
		super.publiziereNsStufe(msDaten,keineDaten);
		if(keineDaten) return;
		// d.H. es laeuft gerade ein test von anderer Klasse die NiStufe daten benoetigt
		if(ausgabe == null) return;
		
		Assert.assertEquals("Werte nicht gleich soll:" + ausgabe[ausgabeIndex].ordinal() + " ist:" + msDaten.nsStufe.ordinal() + " index " + ausgabeIndex, ausgabe[ausgabeIndex].ordinal(), msDaten.nsStufe.ordinal());
		Assert.assertEquals(ausgabeZeitStempel[ausgabeIndex], msDaten.nsStufeZeitStempel);
		System.out.println(String.format("[ %4d ] NS Stufe OK: %-10s == %-10s", ausgabeIndex, ausgabe[ausgabeIndex],  msDaten.nsStufe));
		ausgabeIndex++;
		warten = false;
	}
	
	/**
	 * Sendet einen DS mit Wasserfilmdickestufe
	 * @param objekt Der Sensor
	 * @param stufe Die Stufe
	 * @param zeitStempel Der ZeitStempels 
	 */
	private void sendeWfdStufe(SystemObject objekt, WFD_Stufe stufe, long zeitStempel) {		
		int intStufe = WasserFilmDickeStufe.getStufe(stufe);
		hauptModul.getWfdKnotne().SendeStufe(objekt, intStufe, zeitStempel, false);
	}

	/**
	 * Sendet einen DS mit Niederschlagintensitaetstufe
	 * @param objekt Der Sensor
	 * @param stufe Die Stufe
	 * @param zeitStempel Der ZeitStempels 
	 */
	private void sendeNiStufe(SystemObject objekt, NI_Stufe stufe, long zeitStempel) {
		int intStufe = NiederschlagIntensitaetStufe.getStufe(stufe);
		hauptModul.getNiKnoten().SendeStufe(objekt, intStufe, zeitStempel, false);
	}
	
	/**
	 * Sendet einen DS mit Fahrbahnoberflaechezustand
	 * @param objekt Der Sensor
	 * @param stufe Die Stufe
	 * @param zeitStempel Der ZeitStempels 
	 */
	private  static void sendeFbofZustand(SystemObject objekt, int zustand, long zeitStempel) {
		sendeZustand(objekt,  "FahrBahnOberFlächenZustand" , DD_FBOF_ZUSTAND, zustand, zeitStempel);
	}

	/**
	 * Sendet einen DS mit Niederschlagsart
	 * @param objekt Der Sensor
	 * @param stufe Die Stufe
	 * @param zeitStempel Der ZeitStempels 
	 */
	private static void sendeNiederschlagsArt(SystemObject objekt, int zustand, long zeitStempel) {
		sendeZustand(objekt,  "NiederschlagsArt" , DD_NIE_ART, zustand, zeitStempel);
	}
	
	/**
	 *	Sendet einen allgemeinen DS mit  Zustand (int) Wert 
	 * @param objekt SystemObjekt
	 * @param attribut Attributname
	 * @param datenBeschreibung Datenbeschreibung
	 * @param wert Wert
	 * @param zeitStempel Zeitstempel
	 */
	private static void sendeZustand(SystemObject objekt, String attribut, DataDescription datenBeschreibung, int wert, long zeitStempel) {
		Data data = dav.createData(datenBeschreibung.getAttributeGroup());
		final String att = attribut;
		
		data.getTimeValue("T").setMillis(0);
		data.getItem(att).getUnscaledValue("Wert").set(wert);
	
		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);	
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);
		
		ResultData result = new ResultData(objekt, datenBeschreibung, zeitStempel, data);
		try { 
			dav.sendData(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * Konstanten, die als Abkuerzungen benutzt werden
	 */
	private final NI_Stufe NI0 = NI_Stufe.NI_STUFE0;
	private final NI_Stufe NI1 = NI_Stufe.NI_STUFE1;
	private final NI_Stufe NI2 = NI_Stufe.NI_STUFE2;
	private final NI_Stufe NI3 = NI_Stufe.NI_STUFE3;
	private final NI_Stufe NI4 = NI_Stufe.NI_STUFE4;
	private final NI_Stufe NINV = NI_Stufe.NI_WERT_NV;
	
	private final WFD_Stufe WFD0 = WFD_Stufe.WFD_STUFE0;
	private final WFD_Stufe WFD1 = WFD_Stufe.WFD_STUFE1;
	private final WFD_Stufe WFD2 = WFD_Stufe.WFD_STUFE2;
	private final WFD_Stufe WFD3 = WFD_Stufe.WFD_STUFE3;
	private final WFD_Stufe WFDNV = WFD_Stufe.WFD_WERT_NV;
	
	private final NS_Stufe NS0 = NS_Stufe.NS_TROCKEN;
	private final NS_Stufe NS1 = NS_Stufe.NS_NASS1;
	private final NS_Stufe NS2 = NS_Stufe.NS_NASS2;
	private final NS_Stufe NS3 = NS_Stufe.NS_NASS3;
	private final NS_Stufe NS4 = NS_Stufe.NS_NASS4;
	private final NS_Stufe NSNV = NS_Stufe.NS_WERT_NE;
	
	/**
	 * Testfall 1 - geht durch die ganze Tabelle
	 */
	@Test
	public void testGanzeTablle() {
		
		final int N = 50;
	
		ausgabe = new NS_Stufe[50];
		NS_Stufe tabelle [] = new NS_Stufe []  { NS0, NS0, NS1, NS2, NS2, NS0, 
								   				 NS1, NS1, NS2, NS3, NS4, NS1,
								   				 NS2, NS2, NS2, NS3, NS4, NS2,
								   				 NS2, NS2, NS3, NS3, NS4, NS3,
								   				 NS0, NS1, NS2, NS3, NS4, NSNV };
		ausgabeZeitStempel = new long[N];

		
		final NI_Stufe niStufe [] = new NI_Stufe[] { NI0, NI1, NI2, NI3, NI4, NINV };
		final WFD_Stufe wfdStufe [] = new WFD_Stufe[] { WFD0, WFD1, WFD2, WFD3, WFDNV};
		
		hauptModul = new VerwaltungAufbereitungUFDTest();
		String connArgs [] =   new String [CON_DATA.length] ;
		for(int i=0; i<CON_DATA.length; i++)
			connArgs[i] = CON_DATA[i];
		StandardApplicationRunner.run(hauptModul, connArgs);
		
		long zeitStempel = System.currentTimeMillis()- 120 * 60 * 1000;
		long delta = 5 * 60 * 1000;
		
		int k=0, m=0;
		int iNS1, iNS2;
		ausgabeIndex = 0;
		for(int i =0; i<5; i++) {

			for(int j=0; j<6; j++) {
				ausgabeZeitStempel[k] = zeitStempel;
				ausgabe[k] = tabelle[m++];
				if(k>0) {
					iNS1 = getStufe(ausgabe[k]);
					iNS2 = getStufe(ausgabe[k-1]);
					if(wfdStufe[i] == WFDNV
							&& ((iNS2 - iNS1)>1 && iNS1!=-1)) ausgabeZeitStempel[k] += ABTR_INTERVALL;
					while((iNS2 - iNS1)>1 && iNS1!=-1) {
						ausgabe[k+1] = ausgabe[k];
						ausgabe[k] = getStufe(--iNS2);
						ausgabeZeitStempel[k+1] = ausgabeZeitStempel[k] + ABTR_INTERVALL;
						k++;
					}
				}
				warten = true;
				sendeNiStufe(niSensor, niStufe[j], zeitStempel);
				sendeWfdStufe(wfdSensor, wfdStufe[i], zeitStempel);
				sendeFbofZustand(fbofZustandSensor, FBOF_TROCKEN, zeitStempel);
				sendeNiederschlagsArt(naSensor, NART_KEIN, zeitStempel);
				
				try {
					Thread.sleep(100);
				} catch (Exception e) { 	}
				k++;
				zeitStempel += delta;
			}
		}	
		try {
			while(warten) Thread.sleep(300);
		} catch (Exception e) { }
		hauptModul.disconnect();
		hauptModul = null;
		ausgabe = null;
		
	}

	/**
	 * Testfall 2 - wie Test 1 nur der Fahrbahnoberflachezustand sich aendert
	 */
	@Test
	public void testFbofZustand() {
		
		final int N = 50;
		
		ausgabe = new NS_Stufe[50];
		NS_Stufe tabelle [] = new NS_Stufe []  { NS0, NS0, NS1, NS2, NS2, NS0, 
								   				 NS1, NS1, NS2, NS3, NS4, NS1,
								   				 NS2, NS2, NS2, NS3, NS4, NS2,
								   				 NS2, NS2, NS3, NS3, NS4, NS3,
								   				 NS0, NS1, NS2, NS3, NS4, NS_Stufe.NS_WERT_NE };
		ausgabeZeitStempel = new long[N];

		
		final NI_Stufe niStufe [] = new NI_Stufe[] { NI0, NI1, NI2, NI3, NI4, NINV };
		final WFD_Stufe wfdStufe [] = new WFD_Stufe[] { WFD0, WFD1, WFD2, WFD3, WFDNV};
		
		hauptModul = new VerwaltungAufbereitungUFDTest();
		String connArgs [] =   new String [CON_DATA.length] ;
		for(int i=0; i<CON_DATA.length; i++)
			connArgs[i] = CON_DATA[i];
		StandardApplicationRunner.run(hauptModul, connArgs);
		
		long zeitStempel = System.currentTimeMillis()- 120 * 60 * 1000;
		long delta = 5 * 60 * 1000;
		
		int k=0, m=0;
		int iNS1, iNS2;
		boolean unbestimmbar = false;
		ausgabeIndex = 0;
		for(int i =0; i<5; i++) {

			for(int j=0; j<6; j++) {
				if((i+j)%5>0 && (i+j)%5<3) {
					sendeFbofZustand(fbofZustandSensor, FBOF_EIS, zeitStempel);
					unbestimmbar = true;
				}
				else {
					sendeFbofZustand(fbofZustandSensor, FBOF_TROCKEN, zeitStempel);
					unbestimmbar = false;
				}
			
				ausgabeZeitStempel[k] = zeitStempel;
				ausgabe[k] = tabelle[m++];
				
				if(unbestimmbar) ausgabe[k] = NS_Stufe.NS_WERT_NE;
				if(k>0 && !unbestimmbar) {
					iNS1 = getStufe(ausgabe[k]);
					iNS2 = getStufe(ausgabe[k-1]);
					if(wfdStufe[i] == WFDNV
							&& ((iNS2 - iNS1)>1 && iNS1!=-1)) ausgabeZeitStempel[k] += ABTR_INTERVALL;
					while((iNS2 - iNS1)>1 && iNS1!=-1) {
						ausgabe[k+1] = ausgabe[k];
						ausgabe[k] = getStufe(--iNS2);
						ausgabeZeitStempel[k+1] = ausgabeZeitStempel[k] + ABTR_INTERVALL;
						k++;
					}
				}
				warten = true;
				sendeNiStufe(niSensor, niStufe[j], zeitStempel);
				sendeWfdStufe(wfdSensor, wfdStufe[i], zeitStempel);
				sendeNiederschlagsArt(naSensor, NART_KEIN, zeitStempel);
				
				try {
					Thread.sleep(100);
				} catch (Exception e) { 	}
				k++;
				zeitStempel += delta;
			}
		}
		try {
			while(warten) Thread.sleep(300);
		} catch (Exception e) { }
		hauptModul.disconnect();
		hauptModul = null;
		ausgabe = null;
	}

	/**
	 * Testfall 3 - testet die verzoegerung bei faehlenden WFD Daten
	 */
	@Test
	public void testVerzoegerung() {
		
		ausgabe = new NS_Stufe[] { NS4, NS3, NS3, NS2, NS2, NS4, NS3, NS2, NS1, NS0 } ;
		ausgabeZeitStempel = new long[ausgabe.length];

		final NI_Stufe niStufe [] = new NI_Stufe[] { NI4, NI3, NI2, NI1, NI0 };
		
		hauptModul = new VerwaltungAufbereitungUFDTest();
		String connArgs [] =   new String [CON_DATA.length] ;
		for(int i=0; i<CON_DATA.length; i++)
			connArgs[i] = CON_DATA[i];
		StandardApplicationRunner.run(hauptModul, connArgs);
		
		long zeitStempel = System.currentTimeMillis()- 120 * 60 * 1000;
		long delta = 5 * 60 * 1000;
	
		ausgabeIndex = 0;
		
		for(int i =0; i<5; i++) {
			ausgabeZeitStempel[i] = zeitStempel;
			warten = true;	
			sendeNiStufe(niSensor, niStufe[i], zeitStempel);
			sendeWfdStufe(wfdSensor, WFD3, zeitStempel);
			sendeFbofZustand(fbofZustandSensor, FBOF_TROCKEN, zeitStempel);
			sendeNiederschlagsArt(naSensor, NART_KEIN, zeitStempel);
			
			try {
				Thread.sleep(100);
			} catch (Exception e) { 	}
			zeitStempel += delta;			
		}
		
		for(int i =5; i<10; i++) {
			ausgabeZeitStempel[i] = zeitStempel;
			if(i>5) ausgabeZeitStempel[i] += ABTR_INTERVALL;
			warten = true;	
			sendeNiStufe(niSensor, niStufe[i-5], zeitStempel);
			sendeWfdStufe(wfdSensor, WFDNV, zeitStempel);
			sendeFbofZustand(fbofZustandSensor, FBOF_TROCKEN, zeitStempel);
			sendeNiederschlagsArt(naSensor, NART_KEIN, zeitStempel);
			
			try {
				Thread.sleep(100);
			} catch (Exception e) { 	}
			zeitStempel += delta;
			
		}
	
		
		try {
			while(warten) Thread.sleep(300);
		} catch (Exception e) { }
		hauptModul.disconnect();
		hauptModul = null;
		ausgabe = null;
	}
	
	/**
	 * Testfall 4 - wie Test 1 nur die Niederschlagsart sich aendert
	 */
	@Test
	public void testNieArt() {
		
		final int N = 50;
		
		ausgabe = new NS_Stufe[50];
		NS_Stufe tabelle [] = new NS_Stufe []  { NS0, NS0, NS1, NS2, NS2, NS0, 
								   				 NS1, NS1, NS2, NS3, NS4, NS1,
								   				 NS2, NS2, NS2, NS3, NS4, NS2,
								   				 NS2, NS2, NS3, NS3, NS4, NS3,
								   				 NS0, NS1, NS2, NS3, NS4, NS_Stufe.NS_WERT_NE };
		ausgabeZeitStempel = new long[N];

		
		final NI_Stufe niStufe [] = new NI_Stufe[] { NI0, NI1, NI2, NI3, NI4, NINV };
		final WFD_Stufe wfdStufe [] = new WFD_Stufe[] { WFD0, WFD1, WFD2, WFD3, WFDNV};
		
		hauptModul = new VerwaltungAufbereitungUFDTest();
		String connArgs [] =   new String [CON_DATA.length] ;
		for(int i=0; i<CON_DATA.length; i++)
			connArgs[i] = CON_DATA[i];
		StandardApplicationRunner.run(hauptModul, connArgs);
		
		long zeitStempel = System.currentTimeMillis()- 120 * 60 * 1000;
		long delta = 5 * 60 * 1000;

		int k=0, m=0;
		int iNS1, iNS2;
		boolean unbestimmbar = false;
		ausgabeIndex = 0;
		for(int i =0; i<5; i++) {

			for(int j=0; j<6; j++) {
				if((i+j)%5>0 && (i+j)%5<3) {
					sendeNiederschlagsArt(naSensor, NART_SCHNEE, zeitStempel);
					unbestimmbar = true;
				}
				else  {
					sendeNiederschlagsArt(naSensor, NART_KEIN, zeitStempel);
					unbestimmbar = false;
				}
				
				ausgabeZeitStempel[k] = zeitStempel;
				ausgabe[k] = tabelle[m++];
				
				if(unbestimmbar) ausgabe[k] = NS_Stufe.NS_WERT_NE;
				if(k>0 && !unbestimmbar) {
					iNS1 = getStufe(ausgabe[k]);
					iNS2 = getStufe(ausgabe[k-1]);
					if(wfdStufe[i] == WFDNV
							&& ((iNS2 - iNS1)>1 && iNS1!=-1)) ausgabeZeitStempel[k] += ABTR_INTERVALL;
					while((iNS2 - iNS1)>1 && iNS1!=-1) {
						ausgabe[k+1] = ausgabe[k];
						ausgabe[k] = getStufe(--iNS2);
						ausgabeZeitStempel[k+1] = ausgabeZeitStempel[k] + ABTR_INTERVALL;
						k++;
					}
				}
				warten = true;
				sendeNiStufe(niSensor, niStufe[j], zeitStempel);
				sendeWfdStufe(wfdSensor, wfdStufe[i], zeitStempel);
				sendeFbofZustand(fbofZustandSensor, FBOF_TROCKEN, zeitStempel);
				
				try {
					Thread.sleep(100);
				} catch (Exception e) { 	}
				k++;
				zeitStempel += delta;
			}
		}
		try {
			while(warten) Thread.sleep(300);
		} catch (Exception e) { }
		hauptModul.disconnect();
		hauptModul = null;
		ausgabe = null;
	}
	
	@Override
	void infoVerzoegerung(int stufe) {
		System.out.println(" ---- Verzoegerung StufeVon: " + stufe);
	}

}
 