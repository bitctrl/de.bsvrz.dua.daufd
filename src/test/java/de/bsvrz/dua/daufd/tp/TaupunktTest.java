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
package de.bsvrz.dua.daufd.tp;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.DAVTest;
import de.bsvrz.dua.daufd.VerwaltungAufbereitungUFDTest;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;


/**
 * Testet den Modul Taupunkt
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
@Ignore ("Testdatenverteiler prüfen")
public class TaupunktTest extends Taupunkt {

	/**
	 * Der Index des aktuelles TestWertes im Array
	 */
	private static int testWertLuft = 0, testWertFbof = 0;
	/**
	 * Die TestWerte
	 */
	private static double taupunktLuft [] = null, taupunktFbof [] = null;
	/**
	 * Die ZeitStempel der Testwerte
	 */
	private static long zeitStempel [] = null;
	/**
	 * Intervall der Datenerzeugung;
	 */
	private final static long ZEIT_INTERVALL = 300;
	/**
	 * Die EingabeDaten
	 */
	private static DataDescription DD_SENDE_RLF_DATEN, DD_SENDE_LT_DATEN, DD_SENDE_FBOFT_DATEN;
	/**
	 * Verbindung zum dav
	 */
	private static ClientDavInterface  dav;
	/**
	 * SystemObjekte zum TestZwecken - liefern die Testdaten
	 */
	private static SystemObject rlfSensor, ltSensor, fbofSensor;
	/**
	 * Synchronizierung
	 */
	private static boolean mussWartenFbof = true, mussWartenLuft = true;
	/**
	 * Der Verwaltungsmodul
	 */
	private static VerwaltungAufbereitungUFDTest hauptModul;

	/**
	 * Berechnet dem Taupunkt fuer Luftemperatur
	 * @param relativeLuftFeuchtigkeit Feuchte
	 * @param luftTemperatur Temperatur
	 * @return Taupunkt
	 */
	public double taupunktTemperaturLuft(double relativeLuftFeuchtigkeit, double luftTemperatur) {
		return taupunktTemperatur(relativeLuftFeuchtigkeit, luftTemperatur);
	}	
	
	/**
	 * Berechnet dem Taupunkt fuer fahrbahntemperatur
	 * @param relativeLuftFeuchtigkeit Feuchte 
	 * @param fahrBahnTemperatur Temperatur
	 * @return Taupunkt
	 */
	public double taupunktTemperaturFahrbahn(double relativeLuftFeuchtigkeit, double fahrBahnTemperatur) {
		return taupunktTemperatur(relativeLuftFeuchtigkeit, fahrBahnTemperatur);
	}

	/**
	 * Berechnet dem Taupunkt 
	 * @param feuchtigkeit Feuchte
	 * @param temperatur Temperatur
	 * @return Taupunkt
	 */
	public double taupunktTemperatur(double feuchtigkeit, double temperatur) {
		double RF = feuchtigkeit;
		double T = temperatur;
		double TPT;
		
		TPT = (241.2 * Math.log(RF/100.0) + (4222.03716*T)/(241.2 + T))/
				(17.5043 - Math.log(RF/100.0) - (17.5043*T)/(241.2 + T));
		
		return TPT;
	}	
	
	/**
	 * Sendet Daten fuer Testzwecken 
	 * @param so SystemObjekt
	 * @param datenBeschreibung Datenbeschreibung
	 * @param att Name des Attributs
	 * @param wert Wert
	 * @param zeitStemepel ZeitStemepl
	 * @param implausibel True, wenn Datum Implausibel ist
	 */
	public void sendeDaten(SystemObject so, DataDescription datenBeschreibung, String att, double wert, long zeitStemepel, int implausibel) {
		
		Data data = dav.createData(datenBeschreibung.getAttributeGroup());
		data.getTimeValue("T").setMillis(ZEIT_INTERVALL);
		data.getItem(att).getScaledValue("Wert").set(wert);
	
		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);	
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(implausibel);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);
		
		ResultData result = new ResultData(so, datenBeschreibung, zeitStemepel, data);
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
		
		ResultData resultate;
		dav = verwaltung.getVerbindung();
		
		// findet Objekte die Testdaten liefern koennen
		

		for(SystemObject so :getRlfSensoren())
			if(so != null) {
				rlfSensor = so;
				break;
			}
			
		
		for(SystemObject so :getLtSensoren())
			if(so != null) {
				ltSensor = so;
				break;
			}
		
		for(SystemObject so :getFbofSensoren())
			if(so != null) {
				fbofSensor = so;
				break;
			}
		
		try {
			
			DD_SENDE_RLF_DATEN = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsRelativeLuftFeuchte"),
					dav.getDataModel().getAspect("asp.messWertErsetzung"));
			resultate = new ResultData(rlfSensor, DD_SENDE_RLF_DATEN, System.currentTimeMillis(), null);
			dav.subscribeSource(this, resultate);
			
			DD_SENDE_LT_DATEN = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsLuftTemperatur"),
					dav.getDataModel().getAspect("asp.messWertErsetzung"));
			resultate = new ResultData(ltSensor, DD_SENDE_LT_DATEN, System.currentTimeMillis(), null);
			dav.subscribeSource(this, resultate);
			
			DD_SENDE_FBOFT_DATEN = new DataDescription(dav.getDataModel().getAttributeGroup("atg.ufdsFahrBahnOberFlächenTemperatur"),
					dav.getDataModel().getAspect("asp.messWertErsetzung"));
			resultate = new ResultData(fbofSensor, DD_SENDE_FBOFT_DATEN, System.currentTimeMillis(), null);
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
	public void sendeTaupunktTemperaturLuft(LokaleDaten lDaten, long zeitStempel, boolean keineDaten)  {

		super.sendeTaupunktTemperaturLuft(lDaten, zeitStempel, keineDaten);
		if(keineDaten) return;
		if(taupunktLuft==null) return;
		double diff;
		double messwert = lDaten.taupunktLuft.getUnscaledValue("TaupunktTemperaturLuft").doubleValue();
		if(messwert>=-1000)
			messwert = lDaten.taupunktLuft.getScaledValue("TaupunktTemperaturLuft").doubleValue();
		diff = taupunktLuft[testWertLuft] - messwert;

		Assert.assertTrue("DIfferenz = " + diff + " taupunkt " + taupunktLuft[testWertLuft] + "DS " + lDaten.taupunktLuft, Math.abs(diff)<=0.05);
		Assert.assertEquals(lDaten.tpLuftZeitStemepel, TaupunktTest.zeitStempel[testWertLuft]);
		System.out.println(String.format("[ %4d ] Luft Taupunkt T OK: %15.7f == %15.7f  Differrez: %15.7f", testWertLuft, taupunktLuft[testWertLuft], messwert, diff));
		testWertLuft++;
		
		if(testWertLuft == taupunktLuft.length-1) synchronized (verwaltung) {
			mussWartenLuft = false;
			verwaltung.notify();
		}
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void sendeTaupunktTemperaturFbof(LokaleDaten lDaten, long zeitStempel, boolean keineDaten) {
		super.sendeTaupunktTemperaturFbof(lDaten, zeitStempel, keineDaten);	
		if(keineDaten) return;
		if(taupunktFbof==null) return; 
		double diff;
		double messwert = lDaten.taupunktFbof.getUnscaledValue("TaupunktTemperaturFahrBahn").doubleValue();
		if(messwert>=-1000)
			messwert = lDaten.taupunktFbof.getScaledValue("TaupunktTemperaturFahrBahn").doubleValue();
		
		diff = taupunktFbof[testWertFbof] - messwert;
		Assert.assertTrue(testWertFbof + " Differenz = " + diff + " taupunkt " + taupunktFbof[testWertFbof] + "DS " + lDaten.taupunktFbof, Math.abs(diff)<=0.05);
		Assert.assertEquals(lDaten.tpFbofZeitStemepel, TaupunktTest.zeitStempel[testWertFbof]);
		System.out.println(String.format("[ %4d ] Fbof Taupunkt T OK: %15.7f == %15.7f  Differrez: %15.7f", testWertFbof, taupunktFbof[testWertFbof], messwert, diff));
		testWertFbof++;
		
		if(testWertFbof >= taupunktFbof.length-1) synchronized (verwaltung) {
			mussWartenFbof = false;
			verwaltung.notify();
		}
	}
	/**
	 * Testet die Berechnung des Taupunktes
	 */
	@Test
	public void TestTaupunkt() {
		double T [] = new double [] { 0.1, -0.2, 0.1, 0.0, 10, 0.5, -10.1, -1.0};
		double feuchte [] = new double [] { 83, 99, 100, 70, 6, 52, 89 };
		taupunktLuft = new double[T.length * feuchte.length];
		taupunktFbof = new double[T.length * feuchte.length];
		zeitStempel = new long[taupunktLuft.length];
		final long SLEEP = 50;

		
		hauptModul = new VerwaltungAufbereitungUFDTest();
		String connArgs [] =   new String [DAVTest.CON_DATA.length] ;
		for(int i=0; i<DAVTest.CON_DATA.length; i++)
			connArgs[i] = DAVTest.CON_DATA[i];
		StandardApplicationRunner.run(hauptModul, connArgs);
		try {
			Thread.sleep(5*SLEEP);
		} catch (Exception e) { 	}
		
		long zeit = System.currentTimeMillis();
		for(int i = 0; i< T.length; i++ )
			for(int j = 0; j< feuchte.length; j++)
			{
				double d = taupunktTemperatur(feuchte[j], T[i]);
				// runden wegen Sklierung 0.1
				taupunktLuft[i*feuchte.length + j] = ((double)Math.round(d*10.0))/10.0;
				taupunktFbof[i*feuchte.length + j] = ((double)Math.round(d*10.0))/10.0;
				zeitStempel[i*feuchte.length + j] = zeit;
				zeit += ZEIT_INTERVALL;
	
				// normale daten
				if((j+i) % 5 != 0) 
					sendeDaten(ltSensor, DD_SENDE_LT_DATEN, "LuftTemperatur", T[i], zeitStempel[i*feuchte.length + j],0);
				// nicht ermittelbar
				else {
					// implausibel
					if((j+i) % 10 == 0)
						sendeDaten(ltSensor, DD_SENDE_LT_DATEN, "LuftTemperatur", T[i], zeitStempel[i*feuchte.length + j],1);
					// oder gar nichts
					else {}
					taupunktLuft[i*feuchte.length + j] = -1001; 
				}
				sendeDaten(fbofSensor, DD_SENDE_FBOFT_DATEN, "FahrBahnOberFlächenTemperatur", T[i], zeitStempel[i*feuchte.length + j],0);
				sendeDaten(rlfSensor, DD_SENDE_RLF_DATEN, "RelativeLuftFeuchte", feuchte[j], zeitStempel[i*feuchte.length + j],0);
				
				try {
					Thread.sleep(SLEEP);
				} catch (Exception e) { }
			}
		
		synchronized (verwaltung) {
			try {
				while(mussWartenLuft || mussWartenFbof) verwaltung.wait();
			} catch (Exception e) { }
		}
		hauptModul.disconnect();
		hauptModul = null;
		taupunktFbof = null;
		taupunktLuft = null;
	
	}
}
