package de.bsvrz.dua.daufd.stufewfd;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe.NI_Stufe;
import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

public class WasserFilmDickeStufe extends AbstraktStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationWasserFilmDicke";
	}

	@Override
	public String getKlasseifizierungsAttribut() {
		return "KlassifizierungWasserFilmDicke";
	}

	@Override
	public String getKlasseifizierungsAttributGruppe() {
		return "atg.ufdsKlassifizierungWasserFilmDicke";
	}

	@Override
	public String getMesswertAttribut() {
		return "WasserFilmDicke";
	}

	@Override
	public String getMesswertAttributGruppe() {
		return "atg.ufdsWasserFilmDicke";
	}

	@Override
	public String getStufeAttributGruppe() {
		return "atg.ufdsStufeWasserFilmDicke";
	}

	@Override
	public String getSensorTyp() {
		return "typ.ufdsWasserFilmDicke";
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub
		
	}

	/**
	 *  WFD Stufen, die unterscheidet werden
	 *  
	 * @author BitCtrl Systems GmbH, Bachraty
	 */
	public enum WFD_Stufe {
		WFD_STUFE0,
		WFD_STUFE1,
		WFD_STUFE2,
		WFD_STUFE3,
		WFD_WERT_NV // Wert nicht verfuegbar
	}
	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	protected final static WFD_Stufe mapIntStufe [] = new WFD_Stufe [] 
    { WFD_Stufe.WFD_STUFE0, WFD_Stufe.WFD_STUFE1, WFD_Stufe.WFD_STUFE2, WFD_Stufe.WFD_STUFE3 };

	/**
	 * Ergibt die WFD Stufe fuer ein bestimmtes sensor
	 * @param sensor Sensoer
	 * @return WFD Stufe
	 */
	public WFD_Stufe getStufe(SystemObject sensor) {
		
		WFD_Stufe stufe;
		SensorParameter sensorDaten = this.sensorDaten.get(sensor);
		if( sensorDaten.stufe < 0 || sensorDaten.stufe > mapIntStufe.length)
			stufe = WFD_Stufe.WFD_WERT_NV;
		else stufe = mapIntStufe[sensorDaten.stufe];
		return stufe;
	}
	
	/**
	 * Konvertiert die WFD_stufe aus Integer ins symbolischen Format
	 * @param stufe Stufe Int
	 * @return WFD Stufe symbolisch
	 */
	static public WFD_Stufe getStufe(int stufe) {
		WFD_Stufe stufeSymb;
		if(  stufe < 0 || stufe > mapIntStufe.length)
			stufeSymb = WFD_Stufe.WFD_WERT_NV;
		else stufeSymb = mapIntStufe[stufe];
		return stufeSymb;
	}

}
