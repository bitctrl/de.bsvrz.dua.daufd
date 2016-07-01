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
package de.bsvrz.dua.daufd.vew;

import java.util.Collection;
import java.util.LinkedList;

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

/**
 * Hauptklasse der SWE 4.8 Datenaufbereitung UFD Bildet eine Kette von Modulen,
 * die sie weiter steuert, alle empfagene Daten gibt weiter dem ersten Modul in
 * der Kette
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class VerwaltungAufbereitungUFD extends AbstraktVerwaltungsAdapter {

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
	protected void initialisiere() throws DUAInitialisierungsException {

		Collection<SystemObject> objekte;
		final Collection<SystemObjectType> systemObjektTypen = new LinkedList<SystemObjectType>();
		systemObjektTypen.add(getVerbindung().getDataModel().getType(VerwaltungAufbereitungUFD.TYP_UFDMS));
		objekte = getVerbindung().getDataModel().getObjects(this.getKonfigurationsBereiche(), systemObjektTypen,
				ObjectTimeSpecification.valid());

		if (objekte.isEmpty()) {
			throw new DUAInitialisierungsException(
					"Es wurden keine UmfeldDatenMessStellen im KB " + this.getKonfigurationsBereiche() + " gefunden");
		}

		setSystemObjekte(objekte);

		IBearbeitungsKnoten knoten1, knoten2;
		AbstraktStufe stufeKnoten;
		NaesseStufe naesseKnoten;
		Taupunkt taupunkt;

		ersterKnoten = knoten2 = stufeKnoten = new NiederschlagIntensitaetStufe();
		knoten2.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(),
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);

		knoten1 = stufeKnoten = new WasserFilmDickeStufe();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(),
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);

		knoten2 = naesseKnoten = new NaesseStufe();
		knoten2.initialisiere(this);
		anmeldeEmpfaenger(naesseKnoten.getNaSensoren(), NaesseStufe.ATG_UFDS_NA,
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(naesseKnoten.getFbofZustandSensoren(), NaesseStufe.ATG_UFDS_FBOFZS,
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);

		knoten1 = stufeKnoten = new SichtWeiteStufe();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(),
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);

		knoten2 = taupunkt = new Taupunkt();
		knoten2.initialisiere(this);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);

		anmeldeEmpfaenger(taupunkt.getFbofSensoren(), Taupunkt.ATG_UFDS_FBOFT,
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getLtSensoren(), Taupunkt.ATG_UFDS_LT,
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getRlfSensoren(), Taupunkt.ATG_UFDS_RLF,
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);

		knoten1 = stufeKnoten = new StufeHelligkeit();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(),
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);

		knoten1 = stufeKnoten = new StufeWindrichtung();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(),
				VerwaltungAufbereitungUFD.ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);

		knoten1.setNaechstenBearbeitungsKnoten(null);
	}

	/**
	 * Meldet sich ein Als empfaenger fuer Daten
	 *
	 * @param sensoren
	 *            SystemObjekte die die Daten liefern
	 * @param attributGruppe
	 *            Atributgruppe der Daten
	 * @param aspekt
	 *            Aspek der Daten
	 */
	protected void anmeldeEmpfaenger(final Collection<SystemObject> sensoren, final String attributGruppe,
			final String aspekt) throws DUAInitialisierungsException {

		final DataDescription datenBeschreibung = new DataDescription(
				getVerbindung().getDataModel().getAttributeGroup(attributGruppe),
				getVerbindung().getDataModel().getAspect(aspekt));
		getVerbindung().subscribeReceiver(this, sensoren, datenBeschreibung, ReceiveOptions.normal(),
				ReceiverRole.receiver());

	}

	@Override
	public SWETyp getSWETyp() {
		return SWETyp.SWE_DATENAUFBEREITUNG_UFD;
	}

	@Override
	public void update(final ResultData[] results) {
		ersterKnoten.aktualisiereDaten(results);
	}

	/**
	 * Haupmethode
	 *
	 * @param args
	 *            Aufrufsargumente
	 */
	public static void main(final String[] args) {
		final VerwaltungAufbereitungUFD verwaltung = new VerwaltungAufbereitungUFD();
		StandardApplicationRunner.run(verwaltung, args);
	}
}
