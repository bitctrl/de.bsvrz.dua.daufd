package de.bsvrz.dua.daufd.stufeni;

import java.util.Collection;

import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.typen.ModulTyp;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IBearbeitungsKnoten;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

public class NiederschlagIntensitaetStufe implements IBearbeitungsKnoten {

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
	 *  NI Stufen, die unterscheidet werden
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum NI_Stufe {
		NI_STUFE0,
		NI_STUFE1,
		NI_STUFE2,
		NI_STUFE3,
		NI_STUFE4,
		NI_WERT_NV // Wert nicht verfuegbar
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
