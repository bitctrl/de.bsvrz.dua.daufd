/*
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD
 * Copyright (C) 2007-2015 BitCtrl Systems GmbH
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
package de.bsvrz.dua.daufd.tp;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.DAVTest;
import de.bsvrz.dua.daufd.VerwaltungAufbereitungUFDTest;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;

/**
 * Testet den Modul Taupunkt.
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
@Ignore("Testdatenverteiler prüfen")
public class TaupunktTest extends Taupunkt {

	/** Der Index des aktuelles TestWertes im Array. */
	private static int testWertLuft, testWertFbof;

	/** Die TestWerte. */
	private static double[] taupunktLuft, taupunktFbof;

	/** Die ZeitStempel der Testwerte. */
	private static long[] zeitStempel;

	/** Intervall der Datenerzeugung. */
	private static final long ZEIT_INTERVALL = 300;

	/** Die EingabeDaten. */
	private static DataDescription ddSendeRlfDaten, ddSendeLtDaten, ddSendeFboftDaten;

	/** Verbindung zum dav. */
	private static ClientDavInterface dav;

	/** SystemObjekte zum TestZwecken - liefern die Testdaten. */
	private static SystemObject rlfSensor, ltSensor, fbofSensor;

	/** Synchronizierung. */
	private static boolean mussWartenFbof = true, mussWartenLuft = true;

	/** Der Verwaltungsmodul. */
	private static VerwaltungAufbereitungUFDTest hauptModul;

	/**
	 * Berechnet dem Taupunkt fuer Luftemperatur.
	 *
	 * @param relativeLuftFeuchtigkeit
	 *            Feuchte
	 * @param luftTemperatur
	 *            Temperatur
	 * @return Taupunkt
	 */
	public double taupunktTemperaturLuft(final double relativeLuftFeuchtigkeit, final double luftTemperatur) {
		return taupunktTemperatur(relativeLuftFeuchtigkeit, luftTemperatur);
	}

	/**
	 * Berechnet dem Taupunkt fuer fahrbahntemperatur.
	 *
	 * @param relativeLuftFeuchtigkeit
	 *            Feuchte
	 * @param fahrBahnTemperatur
	 *            Temperatur
	 * @return Taupunkt
	 */
	public double taupunktTemperaturFahrbahn(final double relativeLuftFeuchtigkeit, final double fahrBahnTemperatur) {
		return taupunktTemperatur(relativeLuftFeuchtigkeit, fahrBahnTemperatur);
	}

	/**
	 * Berechnet dem Taupunkt.
	 *
	 * @param feuchtigkeit
	 *            Feuchte
	 * @param temperatur
	 *            Temperatur
	 * @return Taupunkt
	 */
	public double taupunktTemperatur(final double feuchtigkeit, final double temperatur) {
		final double rlf = feuchtigkeit;
		final double temp = temperatur;
		double tpt;

		tpt = ((241.2 * Math.log(rlf / 100.0)) + ((4222.03716 * temp) / (241.2 + temp)))
				/ (17.5043 - Math.log(rlf / 100.0) - ((17.5043 * temp) / (241.2 + temp)));

		return tpt;
	}

	/**
	 * Sendet Daten fuer Testzwecken.
	 *
	 * @param so
	 *            SystemObjekt
	 * @param datenBeschreibung
	 *            Datenbeschreibung
	 * @param att
	 *            Name des Attributs
	 * @param wert
	 *            Wert
	 * @param zeitStemepel
	 *            ZeitStemepl
	 * @param implausibel
	 *            True, wenn Datum Implausibel ist
	 */
	public void sendeDaten(final SystemObject so, final DataDescription datenBeschreibung, final String att,
			final double wert, final long zeitStemepel, final int implausibel) {

		final Data data = TaupunktTest.dav.createData(datenBeschreibung.getAttributeGroup());
		data.getTimeValue("T").setMillis(TaupunktTest.ZEIT_INTERVALL);
		data.getItem(att).getScaledValue("Wert").set(wert);

		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel")
				.set(implausibel);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);

		final ResultData result = new ResultData(so, datenBeschreibung, zeitStemepel, data);
		try {
			TaupunktTest.dav.sendData(result);
		} catch (final Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void initialisiere(final IVerwaltung verwaltung) throws DUAInitialisierungsException {
		super.initialisiere(verwaltung);

		ResultData resultate;
		TaupunktTest.dav = verwaltung.getVerbindung();

		// findet Objekte die Testdaten liefern koennen

		for (final SystemObject so : getRlfSensoren()) {
			if (so != null) {
				TaupunktTest.rlfSensor = so;
				break;
			}
		}

		for (final SystemObject so : getLtSensoren()) {
			if (so != null) {
				TaupunktTest.ltSensor = so;
				break;
			}
		}

		for (final SystemObject so : getFbofSensoren()) {
			if (so != null) {
				TaupunktTest.fbofSensor = so;
				break;
			}
		}

		try {

			TaupunktTest.ddSendeRlfDaten = new DataDescription(
					TaupunktTest.dav.getDataModel().getAttributeGroup("atg.ufdsRelativeLuftFeuchte"),
					TaupunktTest.dav.getDataModel().getAspect("asp.messWertErsetzung"));
			resultate = new ResultData(TaupunktTest.rlfSensor, TaupunktTest.ddSendeRlfDaten, System.currentTimeMillis(),
					null);
			TaupunktTest.dav.subscribeSource(this, resultate);

			TaupunktTest.ddSendeLtDaten = new DataDescription(
					TaupunktTest.dav.getDataModel().getAttributeGroup("atg.ufdsLuftTemperatur"),
					TaupunktTest.dav.getDataModel().getAspect("asp.messWertErsetzung"));
			resultate = new ResultData(TaupunktTest.ltSensor, TaupunktTest.ddSendeLtDaten, System.currentTimeMillis(),
					null);
			TaupunktTest.dav.subscribeSource(this, resultate);

			TaupunktTest.ddSendeFboftDaten = new DataDescription(
					TaupunktTest.dav.getDataModel().getAttributeGroup("atg.ufdsFahrBahnOberFlächenTemperatur"),
					TaupunktTest.dav.getDataModel().getAspect("asp.messWertErsetzung"));
			resultate = new ResultData(TaupunktTest.fbofSensor, TaupunktTest.ddSendeFboftDaten,
					System.currentTimeMillis(), null);
			TaupunktTest.dav.subscribeSource(this, resultate);
		} catch (final Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void sendeTaupunktTemperaturLuft(final LokaleDaten lDaten, final long zeitStempel,
			final boolean keineDaten) {

		super.sendeTaupunktTemperaturLuft(lDaten, zeitStempel, keineDaten);
		if (keineDaten) {
			return;
		}
		if (TaupunktTest.taupunktLuft == null) {
			return;
		}
		double diff;
		double messwert = lDaten.taupunktLuft.getUnscaledValue("TaupunktTemperaturLuft").doubleValue();
		if (messwert >= -1000) {
			messwert = lDaten.taupunktLuft.getScaledValue("TaupunktTemperaturLuft").doubleValue();
		}
		diff = TaupunktTest.taupunktLuft[TaupunktTest.testWertLuft] - messwert;

		Assert.assertTrue("DIfferenz = " + diff + " taupunkt " + TaupunktTest.taupunktLuft[TaupunktTest.testWertLuft]
				+ "DS " + lDaten.taupunktLuft, Math.abs(diff) <= 0.05);
		Assert.assertEquals(lDaten.tpLuftZeitStemepel, TaupunktTest.zeitStempel[TaupunktTest.testWertLuft]);
		System.out.println(String.format("[ %4d ] Luft Taupunkt T OK: %15.7f == %15.7f  Differrez: %15.7f",
				TaupunktTest.testWertLuft, TaupunktTest.taupunktLuft[TaupunktTest.testWertLuft], messwert, diff));
		TaupunktTest.testWertLuft++;

		if (TaupunktTest.testWertLuft == (TaupunktTest.taupunktLuft.length - 1)) {
			synchronized (Taupunkt.verwaltung) {
				TaupunktTest.mussWartenLuft = false;
				Taupunkt.verwaltung.notify();
			}
		}
	}

	@Override
	public void sendeTaupunktTemperaturFbof(final LokaleDaten lDaten, final long zeitStempel,
			final boolean keineDaten) {
		super.sendeTaupunktTemperaturFbof(lDaten, zeitStempel, keineDaten);
		if (keineDaten) {
			return;
		}
		if (TaupunktTest.taupunktFbof == null) {
			return;
		}
		double diff;
		double messwert = lDaten.taupunktFbof.getUnscaledValue("TaupunktTemperaturFahrBahn").doubleValue();
		if (messwert >= -1000) {
			messwert = lDaten.taupunktFbof.getScaledValue("TaupunktTemperaturFahrBahn").doubleValue();
		}

		diff = TaupunktTest.taupunktFbof[TaupunktTest.testWertFbof] - messwert;
		Assert.assertTrue(
				TaupunktTest.testWertFbof + " Differenz = " + diff + " taupunkt "
						+ TaupunktTest.taupunktFbof[TaupunktTest.testWertFbof] + "DS " + lDaten.taupunktFbof,
				Math.abs(diff) <= 0.05);
		Assert.assertEquals(lDaten.tpFbofZeitStemepel, TaupunktTest.zeitStempel[TaupunktTest.testWertFbof]);
		System.out.println(String.format("[ %4d ] Fbof Taupunkt T OK: %15.7f == %15.7f  Differrez: %15.7f",
				TaupunktTest.testWertFbof, TaupunktTest.taupunktFbof[TaupunktTest.testWertFbof], messwert, diff));
		TaupunktTest.testWertFbof++;

		if (TaupunktTest.testWertFbof >= (TaupunktTest.taupunktFbof.length - 1)) {
			synchronized (Taupunkt.verwaltung) {
				TaupunktTest.mussWartenFbof = false;
				Taupunkt.verwaltung.notify();
			}
		}
	}

	/** Testet die Berechnung des Taupunktes. */
	@Test
	public void testTaupunkt() {
		final double[] values = new double[] { 0.1, -0.2, 0.1, 0.0, 10, 0.5, -10.1, -1.0 };
		final double[] feuchte = new double[] { 83, 99, 100, 70, 6, 52, 89 };
		TaupunktTest.taupunktLuft = new double[values.length * feuchte.length];
		TaupunktTest.taupunktFbof = new double[values.length * feuchte.length];
		TaupunktTest.zeitStempel = new long[TaupunktTest.taupunktLuft.length];
		final long sleepTime = 50;

		TaupunktTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(TaupunktTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		long zeit = System.currentTimeMillis();
		for (int i = 0; i < values.length; i++) {
			for (int j = 0; j < feuchte.length; j++) {
				final double d = taupunktTemperatur(feuchte[j], values[i]);
				// runden wegen Sklierung 0.1
				TaupunktTest.taupunktLuft[(i * feuchte.length) + j] = (Math.round(d * 10.0)) / 10.0;
				TaupunktTest.taupunktFbof[(i * feuchte.length) + j] = (Math.round(d * 10.0)) / 10.0;
				TaupunktTest.zeitStempel[(i * feuchte.length) + j] = zeit;
				zeit += TaupunktTest.ZEIT_INTERVALL;

				// normale daten
				if (((j + i) % 5) != 0) {
					sendeDaten(TaupunktTest.ltSensor, TaupunktTest.ddSendeLtDaten, "LuftTemperatur", values[i],
							TaupunktTest.zeitStempel[(i * feuchte.length) + j], 0);
				} else {
					// implausibel
					if (((j + i) % 10) == 0) {
						sendeDaten(TaupunktTest.ltSensor, TaupunktTest.ddSendeLtDaten, "LuftTemperatur", values[i],
								TaupunktTest.zeitStempel[(i * feuchte.length) + j], 1);
					}
					TaupunktTest.taupunktLuft[(i * feuchte.length) + j] = -1001;
				}
				sendeDaten(TaupunktTest.fbofSensor, TaupunktTest.ddSendeFboftDaten, "FahrBahnOberFlächenTemperatur",
						values[i], TaupunktTest.zeitStempel[(i * feuchte.length) + j], 0);
				sendeDaten(TaupunktTest.rlfSensor, TaupunktTest.ddSendeRlfDaten, "RelativeLuftFeuchte", feuchte[j],
						TaupunktTest.zeitStempel[(i * feuchte.length) + j], 0);

				try {
					Thread.sleep(sleepTime);
				} catch (final Exception e) {
				}
			}
		}

		synchronized (Taupunkt.verwaltung) {
			try {
				while (TaupunktTest.mussWartenLuft || TaupunktTest.mussWartenFbof) {
					Taupunkt.verwaltung.wait();
				}
			} catch (final Exception e) {
			}
		}
		TaupunktTest.hauptModul.disconnect();
		TaupunktTest.hauptModul = null;
		TaupunktTest.taupunktFbof = null;
		TaupunktTest.taupunktLuft = null;

	}
}
