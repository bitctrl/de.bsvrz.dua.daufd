package de.bsvrz.dua.daufd.stufewfd;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

public class WasserFilmDickeStufe extends AbstraktStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKlasseifizierungsAttribut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getKlasseifizierungsAttributGruppe() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMesswertAttribut() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getMesswertAttributGruppe() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStufeAttributGruppe() {
		// TODO Auto-generated method stub
		return null;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub
		
	}

//	
//	/**
//	 *  WFD Stufen, die unterscheidet werden
//	 *  
//	 * @author BitCtrl Systems GmbH, Bachraty
//	 */
//	public enum WFD_Stufe {
//		WFD_STUFE0,
//		WFD_STUFE1,
//		WFD_STUFE2,
//		WFD_STUFE3,
//		WFD_WERT_NV // Wert nicht verfuegbar
//	}

	
}
