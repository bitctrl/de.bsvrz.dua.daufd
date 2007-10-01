package de.bsvrz.dua.daufd.stufesw;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

public class SichtWeiteStufe implements IBearbeitungsKnoten {

	/**
	 * Verbindung zum  Hauptmodul
	 */
	private IVerwaltung verwaltung;
	/**
	 * Die Umfeldtaten MessStellen
	 */
	private SystemObject [] umfdMessStellen;
	/**
	 * der Nachste Bearbeitungsknoten
	 */
	private IBearbeitungsKnoten naechsterBearbeitungsKnoten = null;
	
	/**
	 * Ob man Daten ins DAV puiblizieren soll
	 */
	private boolean publizieren = false;
	/**
	 *  Sichtweite Stufen, die unterscheidet werden
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum  SW_Stufe {
		SW_STUFE0,
		SW_STUFE1,
		SW_STUFE2,
		SW_STUFE3,
		SW_STUFE4,
		SW_STUFE5,
		SW_WERT_NV // Wert nicht verfuegbar
	};
	

	public void aktualisiereDaten(ResultData[] resultate) {
		// TODO Auto-generated method stub

		if(naechsterBearbeitungsKnoten !=  null)
			naechsterBearbeitungsKnoten.aktualisiereDaten(resultate);
	}

	public ModulTyp getModulTyp() {
		// TODO Auto-generated method stub
		return null;
	}

	public void initialisiere(IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		// TODO Auto-generated method stub

	}

	public void setNaechstenBearbeitungsKnoten(IBearbeitungsKnoten knoten) {
		this.naechsterBearbeitungsKnoten = knoten;
	}

	public void setPublikation(boolean publizieren) {
		this.publizieren = publizieren;
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub

	}

}
