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

package de.bsvrz.dua.daufd;

import java.util.Collection;
import java.util.LinkedList;

import junit.framework.Assert;

import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dua.daufd.stufenaesse.*;
import de.bsvrz.dua.daufd.stufesw.*;
import de.bsvrz.dua.daufd.stufewfd.*;
import de.bsvrz.dua.daufd.tp.*;
import de.bsvrz.dua.daufd.vew.*;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufeTest;

/**
 * Beim Testfaellen steuert die Verwaltung statt normalen
 * Klassen die vererbten versionen deren Namen mit -Test 
 * enden
 *  
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class VerwaltungAufbereitungUFDTest extends VerwaltungAufbereitungUFD {
	
	private NiederschlagIntensitaetStufeTest niKnoten= null;
	private WasserFilmDickeStufeTest wfdKnoten = null;
	@Override
	protected void initialisiere()
	throws DUAInitialisierungsException {

		Collection<SystemObject> objekte;
		Collection<SystemObjectType> systemObjektTypen = new LinkedList<SystemObjectType>(); 
		systemObjektTypen.add(verbindung.getDataModel().getType(TYP_UFDMS));
		objekte = verbindung.getDataModel().getObjects(this.getKonfigurationsBereiche(), systemObjektTypen, ObjectTimeSpecification.valid());
		this.objekte = objekte.toArray(new SystemObject [0]);
		
		Assert.assertNotNull(this.objekte);
		
		IBearbeitungsKnoten knoten1, knoten2;
		AbstraktStufe stufeKnoten;
		NaesseStufeTest naesseKnoten;
		Taupunkt taupunkt;
		
		ersterKnoten = knoten2 = stufeKnoten = niKnoten = new NiederschlagIntensitaetStufeTest();
		knoten2.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		
		knoten1 = stufeKnoten = wfdKnoten = new WasserFilmDickeStufeTest();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);
		
		knoten2 = naesseKnoten =  new NaesseStufeTest();
		knoten2.initialisiere(this);
		anmeldeEmpfaenger(naesseKnoten.getNaSensoren(), NaesseStufe.ATG_UFDS_NA, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(naesseKnoten.getFbofZustandSensoren(), NaesseStufe.ATG_UFDS_FBOFZS, ASP_MESSWERTERSETZUNG);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);
		
		knoten1 = stufeKnoten = new SichtWeiteStufeTest();
		knoten1.initialisiere(this);
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);
		
		knoten2 = taupunkt = new TaupunktTest();
		knoten2.initialisiere(this);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);
		anmeldeEmpfaenger(taupunkt.getFbofSensoren(), Taupunkt.ATG_UFDS_FBOFT, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getLtSensoren(), Taupunkt.ATG_UFDS_LT, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getRlfSensoren(), Taupunkt.ATG_UFDS_RLF, ASP_MESSWERTERSETZUNG);
		
		knoten2.setNaechstenBearbeitungsKnoten(null);
	}
	/**
	 * Leifert den NiederschlagintensitaetsKnoten
	 * (wird in NaesseStufeTest benutzt)
	 * @return NI-Knoten
	 */
	public NiederschlagIntensitaetStufeTest getNiKnoten() {
		return niKnoten;
	}
	/**
	 * Leifert den WasserflmdickeKnoten
	 * (wird in NaesseStufeTest benutzt)
	 * @return NI-Knoten
	 */
	public WasserFilmDickeStufeTest getWfdKnotne() {
		return wfdKnoten;
	}
	/**
	 * Trennt die Verbindung
	 */
	public void disconnect() {
		verbindung.disconnect(false, "");
	}
}
