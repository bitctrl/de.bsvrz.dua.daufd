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
import de.bsvrz.dua.daufd.KlassifizierungParametrierungTest;
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
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

public class VerwaltungAufbereitungUFD
extends AbstraktVerwaltungsAdapter {


	
	public final static String TYP_UFDMS = "typ.umfeldDatenMessStelle";
	public final static String ASP_MESSWERTERSETZUNG = "asp.messWertErsetzung";
	
	protected IBearbeitungsKnoten ersterKnoten = null;
	/**
	 * {@inheritDoc}
	 */
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
		Taupunkt taupunkt;
		
		ersterKnoten = knoten2 = stufeKnoten = new NiederschlagIntensitaetStufe();
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten2.initialisiere(this);
		
		knoten1 = stufeKnoten = new WasserFilmDickeStufe();
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten1.initialisiere(this);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);
		
		knoten2 = new NaesseStufe();
		knoten2.initialisiere(this);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);
		
		knoten1 = stufeKnoten = new SichtWeiteStufe();
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten1.initialisiere(this);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);
		
		knoten2 = taupunkt = new Taupunkt();
		knoten2.initialisiere(this);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);
		anmeldeEmpfaenger(taupunkt.getFbofSensoren(), Taupunkt.ATG_UFDS_FBOFT, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getLtSensoren(), Taupunkt.ATG_UFDS_LT, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getRlfSensoren(), Taupunkt.ATG_UFDS_RLF, ASP_MESSWERTERSETZUNG);
		
		knoten2.setNaechstenBearbeitungsKnoten(null);
	}
	
	protected void anmeldeEmpfaenger(Collection<SystemObject> sensoren, String attributGruppe, String aspekt) {
		
		DataDescription datenBeschreibung =  new DataDescription(verbindung.getDataModel().getAttributeGroup(attributGruppe), 
																verbindung.getDataModel().getAspect(aspekt));
		verbindung.subscribeReceiver(this, sensoren, datenBeschreibung, ReceiveOptions.normal(), ReceiverRole.receiver());
	}

	/**
	 * {@inheritDoc}
	 */
	public SWETyp getSWETyp() {
		return SWETyp.SWE_DATENAUFBEREITUNG_UFD;
	}

	/**
	 * {@inheritDoc}
	 */
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
