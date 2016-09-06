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
package de.bsvrz.dua.daufd.vew;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ReceiveOptions;
import de.bsvrz.dav.daf.main.ReceiverRole;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dua.daufd.stufen.StufeHelligkeit;
import de.bsvrz.dua.daufd.stufen.StufeWindrichtung;
import de.bsvrz.dua.daufd.stufenaesse.NaesseStufe;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe;
import de.bsvrz.dua.daufd.stufesw.SichtWeiteStufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe;
import de.bsvrz.dua.daufd.tp.Taupunkt;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.adapter.AbstraktVerwaltungsAdapter;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.SWETyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.operatingMessage.MessageSender;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Hauptklasse der SWE 4.8 Datenaufbereitung UFD
 * Bildet eine Kette von Modulen, die sie weiter steuert,
 * alle empfagene Daten gibt weiter dem ersten Modul in
 * der Kette
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class VerwaltungAufbereitungUFD
extends AbstraktVerwaltungsAdapter {

	/**
	 * Typ der MessStelle
	 */
	public final static String TYP_UFDMS = "typ.umfeldDatenMessStelle";
	/**
	 * ATG der Messdaten die Empfangen werden
	 */
	public final static String ASP_MESSWERTERSETZUNG = "asp.messWertErsetzung";
	/**
	 * Der erste Knoten in der Kette
	 */
	protected IBearbeitungsKnoten ersterKnoten = null;

	@Override
	public void initialize(ClientDavInterface dieVerbindung) throws Exception {
		MessageSender.getInstance().setApplicationLabel("Datenaufbereitung UFD");
		super.initialize(dieVerbindung);
	}
	
	@Override
	protected void initialisiere()
	throws DUAInitialisierungsException {

		Collection<SystemObject> objekte;
		Collection<SystemObjectType> systemObjektTypen = new LinkedList<SystemObjectType>();
		systemObjektTypen.add(verbindung.getDataModel().getType(TYP_UFDMS));
		objekte = verbindung.getDataModel().getObjects(this.getKonfigurationsBereiche(), systemObjektTypen, ObjectTimeSpecification.valid());
		this.objekte = objekte.toArray(new SystemObject [0]);

		if(this.objekte == null || this.objekte.length == 0)
			throw new DUAInitialisierungsException("Es wurden keine UmfeldDatenMessStellen im KB "  + this.getKonfigurationsBereiche() + " gefunden");

		IBearbeitungsKnoten knoten1, knoten2;
		AbstraktStufe stufeKnoten;
		NaesseStufe naesseKnoten;
		Taupunkt taupunkt;

		ersterKnoten = knoten2 = stufeKnoten = new NiederschlagIntensitaetStufe();
		knoten2.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);

		knoten1 = stufeKnoten = new WasserFilmDickeStufe();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);

		knoten2 = naesseKnoten = new NaesseStufe();
		knoten2.initialisiere(this);
		anmeldeEmpfaenger(naesseKnoten.getNaSensoren(), NaesseStufe.ATG_UFDS_NA, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(naesseKnoten.getFbofZustandSensoren(), NaesseStufe.ATG_UFDS_FBOFZS, ASP_MESSWERTERSETZUNG);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);

		knoten1 = stufeKnoten = new SichtWeiteStufe();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);

		knoten2 = taupunkt = new Taupunkt();
		knoten2.initialisiere(this);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);

		anmeldeEmpfaenger(taupunkt.getFbofSensoren(), Taupunkt.ATG_UFDS_FBOFT, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getLtSensoren(), Taupunkt.ATG_UFDS_LT, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getRlfSensoren(), Taupunkt.ATG_UFDS_RLF, ASP_MESSWERTERSETZUNG);

		knoten1 = stufeKnoten = new StufeHelligkeit();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);

		knoten1 = stufeKnoten = new StufeWindrichtung();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);

		knoten1.setNaechstenBearbeitungsKnoten(null);
	}

	/**
	 * Meldet sich ein Als empfaenger fuer Daten
	 * @param sensoren SystemObjekte die die Daten liefern
	 * @param attributGruppe Atributgruppe der Daten
	 * @param aspekt Aspek der Daten
	 */
	protected void anmeldeEmpfaenger(Collection<SystemObject> sensoren, String attributGruppe, String aspekt) throws DUAInitialisierungsException{

		DataDescription datenBeschreibung =  new DataDescription(verbindung.getDataModel().getAttributeGroup(attributGruppe),
																verbindung.getDataModel().getAspect(aspekt));
		verbindung.subscribeReceiver(this, sensoren, datenBeschreibung, ReceiveOptions.normal(), ReceiverRole.receiver());

	}

	public SWETyp getSWETyp() {
		return SWETyp.SWE_DATENAUFBEREITUNG_UFD;
	}

	public void update(ResultData[] results) {
		ersterKnoten.aktualisiereDaten(results);
	}

	/**
	 * Haupmethode
	 * @param args Aufrufsargumente
	 */
	public static void main(String args[]) {
		VerwaltungAufbereitungUFD verwaltung = new VerwaltungAufbereitungUFD();
		StandardApplicationRunner.run(verwaltung, args);
	}
}
