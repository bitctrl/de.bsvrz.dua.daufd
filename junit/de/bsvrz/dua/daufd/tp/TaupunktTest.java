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

import junit.framework.Assert;

import org.junit.Test;

import com.sun.java_cup.internal.version;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.VerwaltungAufbereitungUFDTest;
import de.bsvrz.dua.daufd.tp.Taupunkt.LetzteDaten;
import de.bsvrz.dua.daufd.vew.VerwaltungAufbereitungUFD;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;


/**
 * Testet den Modul Taupunkt
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 * 
 */
public class TaupunktTest extends Taupunkt {

	/**
	 * Verbindungsdaten
	 */
	private static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083",  
			"-benutzer=Tester", 
			"-authentifizierung=c:\\passwd", 
			"-debugLevelStdErrText=WARNING", 
			"-debugLevelFileText=WARNING",
			"-KonfigurationsBereichsPid=kb.UFD_Konfig_B27" }; 

	/**
	 * Der Reihenfolge des TestWertes im Array
	 */
	private static int testWert = 0;
	/**
	 * Die TestWerte
	 */
	private static double taupunkt [] = null;
	/**
	 * Die EingabeDaten
	 */
	private static DataDescription DD_SENDE_RLF_DATEN, DD_SENDE_LT_DATEN, DD_SENDE_FBOFT_DATEN;
	/**
	 * Verbindung zum dav
	 */
	private static ClientDavInterface  dav;
	/**
	 * SystemObjekt zum TestZwecken
	 */
	private static SystemObject rleSensor, ltSensor, fbofSensor;
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
	 * Berechnet dem Mittelwert der Werte im Array
	 * @param werte Array von double Werten
	 * @return Mittelwert
	 */
	public double mittelWert(double [] werte) {
		double sum = 0.0;
		for(int i = 0; i< werte.length; i++)
			sum += werte[i];
		
		return sum/werte.length;
	}
	/**
	 * Extrapoliert aus der Tabelle der x,y werte mit Hilfe der kleinsten Kvadrate ( Least square method)
	 * eine lineare funktion und extrapoliert dem Wert im Zeitpunkt t 
	 * @param werte Y-Werte
	 * @param zeitPunkte X-Werte
	 * @param t X-Wert zu dem man Y extrapolieren moechtet
	 * @return Y-Wert zum t
	 */
	public double trendExtrapolationKorrekt(double [] werte, double [] zeitPunkte, double t) {
		
		double wertMittel;
		double zeitMittel;
		
		wertMittel = mittelWert(werte);
		zeitMittel = mittelWert(zeitPunkte);
		
		double summe = 0.0;
		double summeKvadrat = 0.0;
		double n = werte.length;
		
		for(int i=0; i<werte.length; i++) 
			summe += werte[i] * zeitPunkte[i];
		
		summe -= n * wertMittel * zeitMittel;
		
		for(int i=0; i<zeitPunkte.length; i++) 
			summeKvadrat += zeitPunkte[i]*zeitPunkte[i];
		
		summeKvadrat -= n * zeitMittel * zeitMittel;
		
		double  a = summe / summeKvadrat;
		double b = wertMittel - a * zeitMittel;
		
		return a * t + b;
	}
	/**
	 * Extrapoliert aus der Tabelle der x,y werte mit Hilfe der kleinsten Kvadrate ( Least square method)
	 * eine lineare funktion und extrapoliert dem Wert im Zeitpunkt t
	 *  
	 * Fehlerhafte Formel aus AFo
	 *  
	 * @param werte Y-Werte
	 * @param zeitPunkte X-Werte
	 * @param t X-Wert zu dem man Y extrapolieren moechtet
	 * @return Y-Wert zum t
	 */
	public double trendExtrapolation(double [] werte, double [] zeitPunkte, double t) {
		
		double wertMittel;
		double zeitMittel;
		
		wertMittel = mittelWert(werte);
		zeitMittel = mittelWert(zeitPunkte);
		
		double summe = 0.0;
		double summeKvadrat = 0.0;
		double n = werte.length;
		
		for(int i=0; i<werte.length; i++) 
			summe += werte[i] * zeitPunkte[i] - n * wertMittel * zeitMittel;
		
		for(int i=0; i<zeitPunkte.length; i++) 
			summeKvadrat += zeitPunkte[i]*zeitPunkte[i] - n * zeitMittel * zeitMittel;
		
		double  a = summe / summeKvadrat;
		double b = wertMittel - a * zeitMittel;
		
		return a * t + b;
	}
	
	public void TestTrendExtrapolation() {
		
		double [] x = new double [] { 1, 2, 3, 4, 5 };
		double [] y = new double [] { 1, 1.5, 2, 2.5, 3 };
		
		double a [] = new double [8];
		double b [] = new double [8];
		
		for(int i = 0; i<8; i++) {
			a[i] =  trendExtrapolation(y, x, i);
			b[i] =  trendExtrapolationKorrekt(y, x, i);
		}
		
	}
	
	public void sendeDaten(SystemObject so, DataDescription datenTyp, String att, double wert) {
		
		Data data = dav.createData(datenTyp.getAttributeGroup());
		data.getTimeValue("T").setMillis(0);
		data.getItem(att).getUnscaledValue("Wert").set(wert);
	
		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);	
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);
		
		ResultData result = new ResultData(so, datenTyp, System.currentTimeMillis(), data);
		try { 
			dav.sendData(result);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Override 
	public void initialisiere(IVerwaltung verwaltung)
	throws DUAInitialisierungsException {
		super.initialisiere(verwaltung);
		
		ResultData resultate;
		dav = verwaltung.getVerbindung();
		
		for(SystemObject so :getRlfSensoren())
			if(so != null) {
				rleSensor = so;
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
			resultate = new ResultData(rleSensor, DD_SENDE_RLF_DATEN, System.currentTimeMillis(), null);
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
	
	@Override
	public void sendeTaupunktTemperaturLuft(SystemObject messStelle, LetzteDaten lDaten) {

		super.sendeTaupunktTemperaturLuft(messStelle, lDaten);		
		Assert.assertEquals(taupunkt[testWert],lDaten.taupunktLuft.getScaledValue("TaupunktTemperaturLuft"));
		synchronized (this) {
			this.notify();
		}
	}
	
	@Override
	public void sendeTaupunktTemperaturFbof(SystemObject messStelle, LetzteDaten lDaten) {		
		super.sendeTaupunktTemperaturFbof(messStelle, lDaten);		
		Assert.assertEquals(taupunkt[testWert],lDaten.taupunktFbof.getScaledValue("TaupunktTemperaturFahrBahn"));
		synchronized (this) {
			this.notify();
		}
	}
	
	@Test
	public void TestTaupunkt() {
		double T [] = new double [] { 0.1, -0.2, 0.0, 1.1 -1.0};
		double feuchte [] = new double [] { 83, 99.9, 100.0, 70.1, 6.2 };
		taupunkt = new double[T.length * feuchte.length];
		
		for(int i = 0; i< T.length; i++ )
			for(int j = 0; j< feuchte.length; j++)
			{
				taupunkt[i*feuchte.length + j] = taupunktTemperatur(feuchte[j], T[i]); 
			}

		VerwaltungAufbereitungUFD verwaltung = new VerwaltungAufbereitungUFDTest();
		StandardApplicationRunner.run(verwaltung, CON_DATA);
		
		testWert = 0;
		for(int i = 0; i< T.length; i++ )
			for(int j = 0; j< feuchte.length; j++)
			{
				sendeDaten(fbofSensor, DD_SENDE_FBOFT_DATEN, "FahrBahnOberFlächenTemperatur", T[i]);
				sendeDaten(rleSensor, DD_SENDE_RLF_DATEN, "RelativeLuftFeuchte", feuchte[j]);
				testWert = i*feuchte.length + j;
				synchronized (this) {
					try {
						this.wait();
					}catch (Exception e) { }
				}
			}
		
		
	}
}
