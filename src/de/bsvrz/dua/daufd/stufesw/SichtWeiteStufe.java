package de.bsvrz.dua.daufd.stufesw;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

public class SichtWeiteStufe  extends AbstraktStufe {

	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationSichtWeite";
	}

	@Override
	public String getKlasseifizierungsAttribut() {
		return "KlassifizierungSichtWeite";
	}

	@Override
	public String getKlasseifizierungsAttributGruppe() {
		return "atg.ufdsKlassifizierungSichtWeite";
	}

	@Override
	public String getMesswertAttribut() {
		return "SichtWeite";
	}

	@Override
	public String getMesswertAttributGruppe() {
		return "atg.ufdsSichtWeite";
	}

	@Override
	public String getStufeAttributGruppe() {
		return "atg.ufdsStufeSichtWeite";
	}

	@Override
	public String getSensorTyp() {
		return "typ.ufdsSichtWeite";
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub
		
	}

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
	

	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	protected final static SW_Stufe mapIntStufe [] = new SW_Stufe [] 
    { SW_Stufe.SW_STUFE0, SW_Stufe.SW_STUFE1, SW_Stufe.SW_STUFE2, SW_Stufe.SW_STUFE3, SW_Stufe.SW_STUFE4, SW_Stufe.SW_STUFE5 };

	/**
	 * Ergibt die SW Stufe fuer ein bestimmtes sensor
	 * @param sensor Sensoer
	 * @return SW Stufe
	 */
	public SW_Stufe getStufe(SystemObject sensor) {
		
		SW_Stufe stufe;
		SensorParameter sensorDaten = this.sensorDaten.get(sensor);
		if( sensorDaten.stufe < 0 || sensorDaten.stufe > mapIntStufe.length)
			stufe = SW_Stufe.SW_WERT_NV;
		else stufe = mapIntStufe[sensorDaten.stufe];
		return stufe;
	}


}
