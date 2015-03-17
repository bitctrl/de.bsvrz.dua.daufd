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

package de.bsvrz.dua.daufd.stufesw;

import java.util.Collection;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.DAVTest;
import de.bsvrz.dua.daufd.MesswertBearbeitungAllgemein;
import de.bsvrz.dua.daufd.UfdsKlassifizierungParametrierung;
import de.bsvrz.dua.daufd.VerwaltungAufbereitungUFDTest;
import de.bsvrz.dua.daufd.hysterese.HysterezeTester2;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;


/**
 * Parametriert den Modul SichtWeite
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
@Ignore ("Testdatenverteiler prüfen")
public class SichtWeiteStufeTest extends  SichtWeiteStufe {

	/**
	 * SW-Stufe untere Grenzwerte [AFo]
	 */
	private final static double stufeVon[] = new double[] {
		0, 50, 80, 120, 250, 400 	
	};
	/**
	 * SW-Stufe obere Grenzwerte [AFo]
	 */
	private final static double stufeBis[] = new double[] {
		60, 100, 150, 300, 500, 60000   // Max Wert vom DaK 	
	};
	/**
	 * Koefizient fuer Glaettung
	 */
	private final static double b0 = 0.08;
	/**
	 * Koefizient fuer Glaettung
	 */
	private final static double fb = 0.25;
	
	/**
	 * Verbindung zum dav
	 */
	private static ClientDavInterface  dav;
	/**
	 * Der Verwaltungsmodul
	 */
	private static VerwaltungAufbereitungUFDTest hauptModul;
	
	/**
	 * String-Konstanten
	 */
	private static final String TYP_UFDS_WFD = "typ.ufdsSichtWeite";
	private static final String ATG_UFDS_KLASS_WFD = "atg.ufdsKlassifizierungSichtWeite";
	private static final String ATT_UFDS_KLASS_WFD = "KlassifizierungSichtWeite";
	private static final String ATG_UFDS_AGGREG_WFD = "atg.ufdsAggregationSichtWeite";

	/**
	 * Einkommende Messwerte
	 */
	private static double [] Messwert = null;
	/**
	 * Erwarete Ausgabedaten - geglaettete Messawerte
	 */
	private static double [] MesswertGlatt = null;
	/**
	 * Erwarete Ausgabedaten - Stufen
	 */
	private static int stufen [] = null;
	/**
	 * Aktueller Index in Ausgabe-Testdaten 
	 */
	private static int index = 0;
	/**
	 * Erwertete Zeitstempel der Ausgabedaten
	 */
	private static long zeitStempel [];
	/**
	 * Die Messwerte die bei Testfaellen reingeschickt werden
	 */
	private static DataDescription DD_MESSWERTE;
	/**
	 * Intervall der Datenerzeugung;
	 */
	private final static long ZEIT_INTERVALL = 300;
	/**
	 * Das Sensor das die Testdaten liefert
	 */
	private static SystemObject testSensor;
	/**
	 * Bestimmt ob man an die bearbeitung der Daten warten soll
	 */
	private static boolean warten = false;
	/**
	 * Sendet die Parametrierung aus dem Tabellen der AFo dem DAV
	 * @param dav DAV
	 * @param konfBereiche konfigurationsbereiche, aus dennen alle Objekte parametriert werden
	 */
	public static void ParametriereUfds(ClientDavInterface dav, Collection<ConfigurationArea> konfBereiche) {
		try {
			UfdsKlassifizierungParametrierung param = new UfdsKlassifizierungParametrierung(
					TYP_UFDS_WFD, ATG_UFDS_KLASS_WFD, ATT_UFDS_KLASS_WFD, ATG_UFDS_AGGREG_WFD, stufeVon, stufeBis, b0, fb);
			param.ParametriereUfds(dav, konfBereiche);
		} catch (Exception e) {
			Debug.getLogger().error("Fehler bei Parametrierung der SichtWeite:" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 */
	@Test
	public void test1() {	
		final long SLEEP = 50;
		int alt;
		
		HysterezeTester2 hystTest = new HysterezeTester2();
		Messwert = MesswertBearbeitungAllgemein.generiereMesswerte(stufeVon[0], stufeVon[stufeVon.length-1]*1.2);
		MesswertGlatt = new double[Messwert.length];
		double [] b = new double [Messwert.length];
		stufen = new int [Messwert.length];
		zeitStempel = new long[Messwert.length];
		hystTest.init(stufeVon, stufeBis);
	
		MesswertBearbeitungAllgemein.rundeMesswerteGanzeZahl(Messwert);
		MesswertBearbeitungAllgemein.glaetteMesswerte(Messwert, b, MesswertGlatt, fb, b0);
		alt = -1;
		for(int i=0; i< MesswertGlatt.length; i++) {
			stufen[i] = hystTest.hystereze(MesswertGlatt[i], alt);
			alt = stufen[i];
		}

		hauptModul = new VerwaltungAufbereitungUFDTest();
		String connArgs [] =   new String [DAVTest.CON_DATA.length] ;
		for(int i=0; i<DAVTest.CON_DATA.length; i++)
			connArgs[i] = DAVTest.CON_DATA[i];
		StandardApplicationRunner.run(hauptModul, connArgs);
		try {
			Thread.sleep(5*SLEEP);
		} catch (Exception e) { 	}
	
		
		zeitStempel[0] = System.currentTimeMillis() - 120 * 60 * 1000;
		index = 0;
		warten = true;
		for(int i=0; i<MesswertGlatt.length; i++) {
			sendeMesswert(testSensor, Messwert[i], zeitStempel[i]);
			if(i+1<MesswertGlatt.length)
				zeitStempel[i+1] = zeitStempel[i] + ZEIT_INTERVALL;
			try {
				Thread.sleep(SLEEP);
			} catch (Exception e) { }
		}
		try {
			synchronized (hauptModul) {
				while(warten) hauptModul.wait();
			}
		} catch (Exception e) { }
		hauptModul.disconnect();
		hauptModul = null;
		stufen = null;
		MesswertGlatt = null;
		
	}
	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 * Wie test 1 nur die Werte werden zufaellig geraeuscht
	 */
	@Test
	public void test2() {		
		final long SLEEP = 50;
		int alt;
		
		HysterezeTester2 hystTest = new HysterezeTester2();
		Messwert = MesswertBearbeitungAllgemein.generiereMesswerte(stufeVon[0], stufeVon[stufeVon.length-1]*1.2);
		MesswertGlatt = new double[Messwert.length];
		double [] b = new double [Messwert.length];
		stufen = new int [Messwert.length];
		zeitStempel = new long[Messwert.length];
		hystTest.init(stufeVon, stufeBis);
	
		MesswertBearbeitungAllgemein.gerauescheMesswerte(Messwert, 0.15, 20);
		MesswertBearbeitungAllgemein.rundeMesswerteGanzeZahl(Messwert);
		MesswertBearbeitungAllgemein.glaetteMesswerte(Messwert, b, MesswertGlatt, fb, b0);
		alt = -1;
		for(int i=0; i< MesswertGlatt.length; i++) {
			stufen[i] = hystTest.hystereze(MesswertGlatt[i], alt);
			alt = stufen[i];
		}
	
		hauptModul = new VerwaltungAufbereitungUFDTest();
		String connArgs [] =   new String [DAVTest.CON_DATA.length] ;
		for(int i=0; i<DAVTest.CON_DATA.length; i++)
			connArgs[i] = DAVTest.CON_DATA[i];
		StandardApplicationRunner.run(hauptModul, connArgs);
		try {
			Thread.sleep(5*SLEEP);
		} catch (Exception e) { 	}
		
		zeitStempel[0] = System.currentTimeMillis() - 120 * 60 * 1000;
		index = 0;
		warten = true;
		for(int i=0; i<MesswertGlatt.length; i++) {
			sendeMesswert(testSensor, Messwert[i], zeitStempel[i]);
			if(i+1<MesswertGlatt.length)
				zeitStempel[i+1] = zeitStempel[i] + ZEIT_INTERVALL;
			try {
				Thread.sleep(SLEEP);
			} catch (Exception e) { }
		}
		try {
			synchronized (hauptModul) {
				while(warten) hauptModul.wait();
			}
		} catch (Exception e) { }
		
		hauptModul.disconnect();
		hauptModul = null;
		stufen = null;
		MesswertGlatt = null;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public double berechneMesswertGlaettung(SensorParameter param, double messwert) {
		double r = super.berechneMesswertGlaettung(param, messwert);
		if(MesswertGlatt == null) return r;
		double diff = MesswertGlatt[index]-r;
		Assert.assertTrue( index + " Wert : " + r + " Soll : " + MesswertGlatt[index] + " Differenz : " + diff, diff<0.05);
		System.out.println(String.format("[ %4d ] Geglaetette Wert OK: %10.8f == %10.8f  Differrez: %10.8f", index, MesswertGlatt[index], r, diff));
		return r;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void SendeStufe(SystemObject objekt, int stufe, long zeitStempel, boolean keineDaten) {
		super.SendeStufe(objekt, stufe, zeitStempel, keineDaten);
		if(keineDaten) return;
		// d.H. es laeuft gerade ein test von anderer Klasse die NiStufe daten benoetigt
		if(stufen == null) return;		
		Assert.assertEquals(stufen[index], stufe);
		Assert.assertEquals(SichtWeiteStufeTest.zeitStempel[index], zeitStempel);
		System.out.println(String.format("[ %4d ] Stufe OK: %3d == %3d", index, stufen[index], stufe));
		index++;
		if(index>=stufen.length) {
			synchronized (verwaltung) {
				warten = false;
				verwaltung.notify();
			}
		}
	}
	
	/**
	 * Sendet einen Messwert an den DAV
	 * @param sensor Sensor, die Quelle des Messwertes
	 * @param messwert der MessWert
	 * @param zeitStemepel ZeitStempel
	 */
	private void sendeMesswert(SystemObject sensor, double messwert, long zeitStemepel) {
		Data data = dav.createData(dav.getDataModel().getAttributeGroup(getMesswertAttributGruppe()));

		String att = getMesswertAttribut();
		data.getTimeValue("T").setMillis(ZEIT_INTERVALL);
		data.getItem(att).getScaledValue("Wert").set(messwert);
	
		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);	
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);
		
		ResultData result = new ResultData(sensor, DD_MESSWERTE, zeitStemepel, data);
		try { 
			dav.sendData(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(IVerwaltung verwaltung)
		throws DUAInitialisierungsException {
		super.initialisiere(verwaltung);
		
		for(SystemObject so : getSensoren())
			if(so != null) {
				testSensor = so;
				break;
			}
	
		DD_MESSWERTE = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(getMesswertAttributGruppe()),
				verwaltung.getVerbindung().getDataModel().getAspect("asp.messWertErsetzung"));
		
		dav = verwaltung.getVerbindung();
		try {
			dav.subscribeSender(this, getSensoren(), DD_MESSWERTE, SenderRole.source());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
