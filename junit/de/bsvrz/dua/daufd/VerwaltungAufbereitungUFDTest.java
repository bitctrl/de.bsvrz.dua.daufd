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

public class VerwaltungAufbereitungUFDTest extends VerwaltungAufbereitungUFD {
	
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
		Taupunkt taupunkt;
		
		ersterKnoten = knoten2 = stufeKnoten = new NiederschlagIntensitaetStufeTest();
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten2.initialisiere(this);
		
		knoten1 = stufeKnoten = new WasserFilmDickeStufeTest();
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten1.initialisiere(this);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);
		
		knoten2 = new NaesseStufeTest();
		knoten2.initialisiere(this);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);
		
		knoten1 = stufeKnoten = new SichtWeiteStufeTest();
		anmeldeEmpfaenger(stufeKnoten.getSensoren(), stufeKnoten.getMesswertAttributGruppe(), ASP_MESSWERTERSETZUNG);
		knoten1.initialisiere(this);
		knoten2.setNaechstenBearbeitungsKnoten(knoten1);
		
		knoten2 = taupunkt = new TaupunktTest();
		knoten2.initialisiere(this);
		knoten1.setNaechstenBearbeitungsKnoten(knoten2);
		anmeldeEmpfaenger(taupunkt.getFbofSensoren(), Taupunkt.ATG_UFDS_FBOFT, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getLtSensoren(), Taupunkt.ATG_UFDS_LT, ASP_MESSWERTERSETZUNG);
		anmeldeEmpfaenger(taupunkt.getRlfSensoren(), Taupunkt.ATG_UFDS_RLF, ASP_MESSWERTERSETZUNG);
		
		knoten2.setNaechstenBearbeitungsKnoten(null);
	}
	
}
