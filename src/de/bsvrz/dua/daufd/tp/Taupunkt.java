/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
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

import java.util.Hashtable;

import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.OneSubscriptionPerSendData;
import de.bsvrz.dav.daf.main.ResultData;
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
 * Berechnet dem Taupunkt von Luft- bzw. Fahrbahnoberflaeche- Temperatur
 * fuer alle Messstellen, die er vom Verwalfungmodul bekommt
 *  
 * @author BitCtrl Systems GmbH, Bachraty
 */
public class Taupunkt implements IBearbeitungsKnoten, ClientSenderInterface {
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
	 * Ob man Daten ins DAV puiblizieren soll
	 */
	private boolean publizieren = false;
	/**
	 * DatenBeschreibung des Datensatzes mit  Taupunkttemperatur  der Fahrbahn
	 */
	private DataDescription DD_UFDMS_TT_FB = null;
	/**
	 * DatenBeschreibung des Datensatzes mit  Taupunkttemperatur  der Fahrbahn
	 */
	private DataDescription DD_UFDMS_TT_L = null;
	
	private static final String ATG_UFDMS_TTFB = "atg.ufdmsTaupunktTemperaturFahrBahn"; 
	private static final String ATG_UFDMS_TTL = "atg.ufdmsTaupunktTemperaturLuft";
	private static final String ATG_UFDS_LT = "atg.ufdsLuftTemperatur";
	private static final String ATG_UFDS_FBOFT = "atg.ufdsFahrBahnOberFlächenTemperatur";
	private static final String ATG_UFDS_RLF = "aUfdsRelativeLuftFeuchte";
	
	private static final String ASP_ANALYSE = "asp.analyse";
	private static final String ASP_MESSWERT_ERSETZUNG = "asp.messWertErsetzung";
	private static final String MNG_SENSOREN = "UmfeldDatenSensoren";
	private static final String TYP_UFDS_LT = "typ.ufdsLuftTemperatur";
	private static final String TYP_UFDS_FBOFT = "typ.ufdsFahrBahnOberFlächenTemperatur";
	
	/**
	 * Eintraege in der Lokalen Tabelle mit letzten datensaetzen pro MessStelle
	 * @author BitCtrl Systems GmbH, Bachraty
	 *
	 */
	private class LetzteDaten {
		/**
		 * Standardkonstruktor
		 */
		public LetzteDaten() {
			relativeLuftFeuchte = fbofTemperatur = luftTemperatur = null;
			rlZeitStemepel = fboftZeitStemepel = ltZeitStemepel = 0;
			tpFbofZeitStemepel = tpLuftZeitStemepel = 0;
			taupunktFbof = verwaltung.getVerbindung().createData(
					verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_TTFB));
			taupunktLuft = verwaltung.getVerbindung().createData(
					verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_TTL));
		}
		/**
		 * Letzter Datensatz mit relativen Luftfeuchte
		 */
		public Data relativeLuftFeuchte;
		/**
		 * Zeitstempel des letzten Datensatzes mit relativen Luftfeuchte
		 */
		public long rlZeitStemepel;
		/**
		 * Letzter Datensatz mit Fahrbahnoberflaechetemeperatur
		 */
		public Data fbofTemperatur;
		/**
		 * Zeitstempel des letzten Datensatzes mit Fahrbahnoberflaechetemeperatur
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
		 * Zeitstempel des leztes erzeugenen Datensatzes mit Taupunkttemperatur Luft
		 */
		long tpLuftZeitStemepel;
		/**
		 * Erzeugende Datensatz mit Taupunkttemperatur Fbof
		 */
		public Data taupunktFbof;
		/**
		 * Zeitstempel des leztes erzeugenen Datensatzes mit Taupunkttemperatur Fbof
		 */
		long tpFbofZeitStemepel;
	};
	/**
	 * HashTablelle mit letzten eingekommenen Datensaetzen
	 */
	private Hashtable<SystemObject, LetzteDaten> taupunktTabelle = new Hashtable<SystemObject, LetzteDaten>();
	/**
	 * Hashtabelle, abbildet Senzoren auf Messstellen
	 */
	private Hashtable<SystemObject, SystemObject> mapSenzorMessStelle = new Hashtable<SystemObject, SystemObject>();
	
	/**
	 * {@inheritDoc}
	 */
	public void aktualisiereDaten(ResultData[] resultate) {

		if(publizieren)
		for(ResultData resData : resultate) {
			
			Data data = resData.getData();
			if(data == null) continue;
			
			if( ATG_UFDS_LT.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_MESSWERT_ERSETZUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
				long T =  data.getTimeValue("T").getMillis();
				SystemObject messStelle = mapSenzorMessStelle.get(resData.getObject());
				LetzteDaten lDaten = taupunktTabelle.get(messStelle);
				
				BerechneTaupunktTemperaturLuft(messStelle, lDaten, resData.getDataTime() - T);
				
				lDaten.luftTemperatur = data;
				lDaten.ltZeitStemepel = resData.getDataTime();

			}
			else if( ATG_UFDS_FBOFT.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_MESSWERT_ERSETZUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
			
				long T =  data.getTimeValue("T").getMillis();
				SystemObject messStelle = mapSenzorMessStelle.get(resData.getObject());
				LetzteDaten lDaten = taupunktTabelle.get(messStelle);
				
				BerechneTaupunktTemperaturFbof(messStelle, lDaten, resData.getDataTime() - T);
				
				lDaten.fbofTemperatur = data;
				lDaten.fboftZeitStemepel = resData.getDataTime();

			}
			else if( ATG_UFDS_RLF.equals(resData.getDataDescription().getAttributeGroup().getPid()) &&
					ASP_MESSWERT_ERSETZUNG.equals(resData.getDataDescription().getAspect().getPid()))
			{
				long T =  data.getTimeValue("T").getMillis();
				SystemObject messStelle = mapSenzorMessStelle.get(resData.getObject());
				LetzteDaten lDaten = taupunktTabelle.get(messStelle);
				
				BerechneTaupunktTemperaturFbof(messStelle, lDaten, resData.getDataTime() - T);
				BerechneTaupunktTemperaturLuft(messStelle, lDaten, resData.getDataTime() - T);
				
				lDaten.relativeLuftFeuchte= data;
				lDaten.rlZeitStemepel = resData.getDataTime();
			}
		}

		if(naechsterBearbeitungsKnoten !=  null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}

	/**
	 * Berechnet die Taupunkttempereatur der Fahrbahnoberflaeche fuer eine Messtelle
	 * @param messStelle Messstelle
	 * @param lDaten Letzte Daten (RLF und FBT)
	 * @param zeitStemepel Zeutstempel des Itervalles, fuer dem die Daten erzeugt werden sollen
	 */
	public void BerechneTaupunktTemperaturFbof(SystemObject messStelle, LetzteDaten lDaten, long zeitStemepel) {
		// DS fuer leztes intervall wurde erzeugt
		if(lDaten.tpFbofZeitStemepel == zeitStemepel) return;
		boolean nichtermittelbar = false;
		
		// Nicht beide DS fuer letztes intervall vorhanden sind
		if(lDaten.fboftZeitStemepel != lDaten.rlZeitStemepel)
			nichtermittelbar = true;
		if(lDaten.fbofTemperatur == null || lDaten.relativeLuftFeuchte == null)
			nichtermittelbar = true;

		// Initializierung wegen dummen Compiler
		long fbofT = 0, rlF = 0; 
		if(!nichtermittelbar) {
			fbofT = lDaten.fbofTemperatur.getItem("FahrBahnOberFlächenTemperatur").getUnscaledValue("Wert").longValue();
			if(fbofT<1000 ||
					lDaten.fbofTemperatur.getItem("FahrBahnOberFlächenTemperatur")
						.getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").byteValue()==1) {
				nichtermittelbar = true;
			}
		}
	
		if(!nichtermittelbar) {
			rlF = lDaten.relativeLuftFeuchte.getItem("RelativeLuftFeuchte").getUnscaledValue("Wert").longValue();
			if(rlF<0 ||
					lDaten.fbofTemperatur.getItem("RelativeLuftFeuchte")
					.getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").byteValue()==1)  {
				nichtermittelbar = true;
			}
		}
		
		if(nichtermittelbar) {
			lDaten.taupunktFbof.getItem("TaupunktTemperaturFahrBahn").asUnscaledValue().set(-1001);
			lDaten.tpFbofZeitStemepel = zeitStemepel;
			sendeTaupunktTemperaturFbof(messStelle, lDaten);
			return;
		}
		
		double relFeucht = rlF;
		double fobofTemp = 0.1 * fbofT;
		double ergebnis = Berechnetaupunkt(relFeucht, fobofTemp);
		
		lDaten.taupunktFbof.getItem("TaupunktTemperaturFahrBahn").asScaledValue().set(ergebnis);
		lDaten.tpFbofZeitStemepel = zeitStemepel;
		sendeTaupunktTemperaturFbof(messStelle, lDaten);
	}
	/**
	 * Berechnet die Taupunkttempereatur der Luft fuer eine Messtelle
	 * @param messStelle Messstelle
	 * @param lDaten Letzte Daten (RLF und LT)
	 * @param zeitStemepel Zeutstempel des Itervalles, fuer dem die Daten erzeugt werden sollen
	 */
	public void BerechneTaupunktTemperaturLuft(SystemObject messStelle, LetzteDaten lDaten, long zeitStemepel) {
		// DS fuer leztes intervall wurde erzeugt
		if(lDaten.tpLuftZeitStemepel == zeitStemepel) return;
		boolean nichtermittelbar = false;
		
		// Nicht beide DS fuer letztes intervall vorhanden sind
		if(lDaten.ltZeitStemepel != lDaten.rlZeitStemepel)
			nichtermittelbar = true;
		if(lDaten.luftTemperatur == null || lDaten.relativeLuftFeuchte == null)
			nichtermittelbar = true;

		// Initializierung wegen dummen Compiler
		long luftT = 0, rlF = 0; 
		if(!nichtermittelbar) {
			luftT = lDaten.luftTemperatur.getItem("LuftTemperatur").getUnscaledValue("Wert").longValue();
			if(luftT<1000 ||
					lDaten.luftTemperatur.getItem("LuftTemperatur")
						.getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").byteValue()==1)  {
				nichtermittelbar = true;
			}
		}
	
		if(!nichtermittelbar) {
			rlF = lDaten.relativeLuftFeuchte.getItem("RelativeLuftFeuchte").getUnscaledValue("Wert").longValue();
			if(rlF<0 ||
					lDaten.fbofTemperatur.getItem("RelativeLuftFeuchte")
					.getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").byteValue()==1)  {
				nichtermittelbar = true;
			}
		}
		
		if(nichtermittelbar) {
			lDaten.taupunktFbof.getItem("TaupunktTemperaturLuft").asUnscaledValue().set(-1001);
			lDaten.tpLuftZeitStemepel = zeitStemepel;
			sendeTaupunktTemperaturLuft(messStelle, lDaten);
			return;
		}
		
		double relFeucht = rlF;
		double luftTemp = 0.1 *luftT;
		double ergebnis = Berechnetaupunkt(relFeucht, luftTemp);
		
		lDaten.taupunktLuft.getItem("TaupunktTemperaturLuft").asScaledValue().set(ergebnis);
		lDaten.tpLuftZeitStemepel = zeitStemepel;
		sendeTaupunktTemperaturLuft(messStelle, lDaten);
	}
	
	/**
	 * Sendet einen DS mit TP Temperatur der FBOF
	 * @param messStelle Messstelle
	 * @param lDaten Struktur mit erzeugten DS
	 */
	public void sendeTaupunktTemperaturFbof(SystemObject messStelle, LetzteDaten lDaten) {
		ResultData resDatei = new ResultData(messStelle, DD_UFDMS_TT_FB, lDaten.tpFbofZeitStemepel, lDaten.taupunktFbof);
		try {
			verwaltung.getVerbindung().sendData(resDatei);
		} catch (Exception e) {
			LOGGER.error("Sendung von Datensatz " + DD_UFDMS_TT_FB.getAttributeGroup().getPid() + " fuer Objekt " 
					+ messStelle.getPid() + " unerfolgreich:\n" + e.getMessage());
			
		}
	}
	/**
	 * Sendet einen DS mit TP Temperatur der Luft
	 * @param messStelle Messstelle
	 * @param lDaten Struktur mit erzeugten DS
	 */
	public void sendeTaupunktTemperaturLuft(SystemObject messStelle, LetzteDaten lDaten) {
		ResultData resDatei = new ResultData(messStelle, DD_UFDMS_TT_L, lDaten.tpLuftZeitStemepel, lDaten.taupunktLuft);
		try {
			verwaltung.getVerbindung().sendData(resDatei);
		} catch (Exception e) {
			LOGGER.error("Sendung von Datensatz " + DD_UFDMS_TT_L.getAttributeGroup().getPid() + " fuer Objekt " 
					+ messStelle.getPid() + " unerfolgreich:\n" + e.getMessage());
			
		}
	}
	
	/**
	 * Berechnet die Taupunkttemperatur aus er Feuchte und Temperatur
	 * @param feuchte relative Feuchte
	 * @param temperatur Temperatur
	 * @return Taupunkttemperatur
	 */
	public double Berechnetaupunkt(double feuchte, double temperatur) {
		double x = 241.2 * Math.log(feuchte/100.0) + 4222.03716*temperatur/(241.2 + temperatur);
		double y = 17.5043 - Math.log(feuchte/100.0) - 17.5043*temperatur/(241.2 + temperatur);
		return x/y;
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
		if(! publizieren) return;
	
		
		DD_UFDMS_TT_FB = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_TTFB), 
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_ANALYSE));
		
		DD_UFDMS_TT_L = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(ATG_UFDMS_TTL), 
				verwaltung.getVerbindung().getDataModel().getAspect(ASP_ANALYSE));
		
		for(SystemObject so: verwaltung.getSystemObjekte()) 
			try {
				if(!(so  instanceof ConfigurationObject)) continue;
				taupunktTabelle.put(so, new LetzteDaten());
				ConfigurationObject confObjekt = (ConfigurationObject)so;
				ObjectSet sensorMenge = confObjekt.getObjectSet(MNG_SENSOREN);
				for( SystemObject sensor : sensorMenge.getElements()) {
					if(TYP_UFDS_LT.equals(sensor.getType().getPid())) {
						mapSenzorMessStelle.put(sensor, so);
						
						ResultData resultate = new ResultData(so, DD_UFDMS_TT_L, System.currentTimeMillis(), null);
						verwaltung.getVerbindung().subscribeSource(this, resultate);
					}
					else if(TYP_UFDS_FBOFT.equals(sensor.getType().getPid())) {
						mapSenzorMessStelle.put(sensor, so);
						
						ResultData resultate = new ResultData(so, DD_UFDMS_TT_FB, System.currentTimeMillis(), null);
						verwaltung.getVerbindung().subscribeSource(this, resultate);
					}
				}
			} catch (OneSubscriptionPerSendData e) {
				LOGGER.error("Anmeldung als Quelle fuer Taupunkttemperatur fuer Objekt" + so.getPid() + " unerfolgreich:" + e.getMessage());	
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
		this.publizieren = publizieren;
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
}
