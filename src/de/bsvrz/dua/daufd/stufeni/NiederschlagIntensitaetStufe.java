package de.bsvrz.dua.daufd.stufeni;

import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.vew.AbstraktStufe;
import de.bsvrz.sys.funclib.bitctrl.dua.dfs.schnittstellen.IDatenFlussSteuerung;

public class NiederschlagIntensitaetStufe  extends AbstraktStufe {


	@Override
	public String getAggregationsAtrributGruppe() {
		return "atg.ufdsAggregationNiederschlagsIntensität";
	}

	@Override
	public String getKlasseifizierungsAttribut() {
		return "KlassifizierungNiederschlagsIntensität";
	}

	@Override
	public String getKlasseifizierungsAttributGruppe() {
		return "atg.ufdsKlassifizierungNiederschlagsIntensität";
	}

	@Override
	public String getMesswertAttribut() {
		return "NiederschlagsIntensität";
	}

	@Override
	public String getMesswertAttributGruppe() {
		return "atg.ufdsNiederschlagsIntensität";
	}

	@Override
	public String getStufeAttributGruppe() {
		return "atg.ufdsStufeNiederschlagsIntensität";
	}

	@Override
	public String getSensorTyp() {
		return "typ.ufdsNiederschlagsIntensität";
	}

	public void aktualisierePublikation(IDatenFlussSteuerung dfs) {
		// TODO Auto-generated method stub
		
	}

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
	
	/**
	 * Abbildet Integer Stufen auf Symbolische Konstanten
	 */
	protected final static NI_Stufe mapIntStufe [] = new NI_Stufe [] 
    { NI_Stufe.NI_STUFE0, NI_Stufe.NI_STUFE1, NI_Stufe.NI_STUFE2, NI_Stufe.NI_STUFE3, NI_Stufe.NI_STUFE4};

	/**
	 * Ergibt die NI Stufe fuer ein bestimmtes Sensor
	 * @param sensor Sensoer
	 * @return NI Stufe
	 */
	public NI_Stufe getStufe(SystemObject sensor) {
		
		NI_Stufe stufe;
		SensorParameter sensorDaten = this.sensorDaten.get(sensor);
		if( sensorDaten.stufe < 0 || sensorDaten.stufe > mapIntStufe.length)
			stufe = NI_Stufe.NI_WERT_NV;
		else stufe = mapIntStufe[sensorDaten.stufe];
		return stufe;
	}
	
	/**
	 * Konvertiert die NI_stufe aus Integer ins symbolischen Format
	 * @param stufe Stufe Int
	 * @return NI Stufe symbolisch
	 */
	static public NI_Stufe getStufe(int stufe) {
		NI_Stufe stufeSymb;
		if(  stufe < 0 || stufe > mapIntStufe.length)
			stufeSymb = NI_Stufe.NI_WERT_NV;
		else stufeSymb = mapIntStufe[stufe];
		return stufeSymb;
	}
	
}
