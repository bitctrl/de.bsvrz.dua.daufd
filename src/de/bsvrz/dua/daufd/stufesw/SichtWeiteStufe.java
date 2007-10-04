package de.bsvrz.dua.daufd.stufesw;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

public class SichtWeiteStufe  extends AbstraktStufe {

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
	public String getStufeAttributGruppe() {
		// TODO Auto-generated method stub
		return null;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub
		
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

//
//	/**
//	 *  Sichtweite Stufen, die unterscheidet werden
//	 *  
//	 * @author BitCtrl Systems GmbH, Bachraty
//	 */
//	public enum  SW_Stufe {
//		SW_STUFE0,
//		SW_STUFE1,
//		SW_STUFE2,
//		SW_STUFE3,
//		SW_STUFE4,
//		SW_STUFE5,
//		SW_WERT_NV // Wert nicht verfuegbar
//	};
//	


}
