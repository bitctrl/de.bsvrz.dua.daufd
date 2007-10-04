package de.bsvrz.dua.daufd.stufeni;

import java.util.Collection;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

public class NiederschlagIntensitaetStufe  extends AbstraktStufe {

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

//	/**
//	 *  NI Stufen, die unterscheidet werden
//	 *  
//	 * @author BitCtrl Systems GmbH, Bachraty
//	 */
//	public enum NI_Stufe {
//		NI_STUFE0,
//		NI_STUFE1,
//		NI_STUFE2,
//		NI_STUFE3,
//		NI_STUFE4,
//		NI_WERT_NV // Wert nicht verfuegbar
//	};
	
	
}
