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

package de.bsvrz.dua.daufd.stufewfd;

import java.util.Collection;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dua.daufd.DAVTest;
import de.bsvrz.dua.daufd.MesswertBearbeitungAllgemein;
import de.bsvrz.dua.daufd.UfdsKlassifizierungParametrierung;
import de.bsvrz.dua.daufd.VerwaltungAufbereitungUFDTest;
import de.bsvrz.dua.daufd.hysterese.HysterezeTester2;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Testet und parametriert den Modul WasserFilmDickenStufe
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
@Ignore("Testdatenverteiler prüfen")
public class WasserFilmDickeStufeTest extends WasserFilmDickeStufe {

	private static final Debug LOGGER = Debug.getLogger();
	/*
	 * ################ WARNUNG #################
	 *
	 * Werte im Afo sind mit genuaigkeit 0.01 mm wobei die Skalierung ist 0.1 mm
	 */
	/**
	 * WFD-Stufe untere Grenzwerte [AFo]
	 */
	private static double[] stufeVon = new double[] { 0.0, 0.20, 0.27, 1.60 };
	/**
	 * WFD-Stufe obere Grenzwerte [AFo]
	 */
	private static double[] stufeBis = new double[] { 0.21, 0.28, 1.70, 200.0 // Max
		// Wert
		// vom
		// DaK
	};
	/**
	 * Koefizient fuer Glaettung.
	 */
	private static final double B0 = 0.08;
	/**
	 * Koefizient fuer Glaettung.
	 */
	private static final double FB = 0.25;

	/**
	 * Verbindung zum dav.
	 */
	private static ClientDavInterface dav;
	/**
	 * Der Verwaltungsmodul.
	 */
	private static VerwaltungAufbereitungUFDTest hauptModul;

	/**
	 * String-KOnstanten.
	 */
	private static final String TYP_UFDS_WFD = "typ.ufdsWasserFilmDicke";
	private static final String ATG_UFDS_KLASS_WFD = "atg.ufdsKlassifizierungWasserFilmDicke";
	private static final String ATT_UFDS_KLASS_WFD = "KlassifizierungWasserFilmDicke";
	private static final String ATG_UFDS_AGGREG_WFD = "atg.ufdsAggregationWasserFilmDicke";

	/**
	 * Einkommende Messwerte
	 */
	private static double[] messwert = null;
	/**
	 * Erwarete Ausgabedaten - geglaettete Messawerte
	 */
	private static double[] messwertGlatt = null;
	/**
	 * Erwarete Ausgabedaten - Stufen
	 */
	private static int[] stufen = null;
	/**
	 * Aktueller Index in Ausgabe-Testdaten
	 */
	private static int index = 0;
	/**
	 * Erwertete Zeitstempel der Ausgabedaten
	 */
	private static long[] zeitStempel;
	/**
	 * Die Messwerte die bei Testfaellen reingeschickt werden
	 */
	private static DataDescription ddMessWerte;
	/**
	 * Intervall der Datenerzeugung;
	 */
	private final static long ZEIT_INTERVALL = 300;
	/**
	 * Das Sensor das die Testdaten liefert
	 */
	private static SystemObject testSensor;
	/**
	 * Bestimmt ob man an die bearbeitung der Daten warten soll
	 */
	private static boolean warten = false;

	/**
	 * Sendet die Parametrierung aus dem Tabellen der AFo dem DAV
	 *
	 * @param dav
	 *            DAV
	 * @param konfBereiche
	 *            konfigurationsbereiche, aus dennen alle Objekte parametriert
	 *            werden
	 */
	public static void parametriereUfds(final ClientDavInterface dav,
			final Collection<ConfigurationArea> konfBereiche) {
		try {
			final UfdsKlassifizierungParametrierung param = new UfdsKlassifizierungParametrierung(
					WasserFilmDickeStufeTest.TYP_UFDS_WFD, WasserFilmDickeStufeTest.ATG_UFDS_KLASS_WFD,
					WasserFilmDickeStufeTest.ATT_UFDS_KLASS_WFD, WasserFilmDickeStufeTest.ATG_UFDS_AGGREG_WFD,
					WasserFilmDickeStufeTest.stufeVon, WasserFilmDickeStufeTest.stufeBis, WasserFilmDickeStufeTest.B0,
					WasserFilmDickeStufeTest.FB);
			param.parametriereUfds(dav, konfBereiche);
		} catch (final Exception e) {
			WasserFilmDickeStufeTest.LOGGER.error("Fehler bei Parametrierung der WasserFilmDicke:" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 */
	@Test
	public void test1() {
		final long sleepTime = 50;
		/*
		 * ################ WARNUNG #################
		 *
		 * Werte im Afo sind mit genuaigkeit 0.01 mm wobei die Skalierung ist
		 * 0.1 mm
		 *
		 * deswegen das Abrunden
		 */
		for (int i = 0; i < WasserFilmDickeStufeTest.stufeBis.length; i++) {
			WasserFilmDickeStufeTest.stufeBis[i] = (Math.round(WasserFilmDickeStufeTest.stufeBis[i] * 10)) / 10.0;
			WasserFilmDickeStufeTest.stufeVon[i] = (Math.round(WasserFilmDickeStufeTest.stufeVon[i] * 10)) / 10.0;
		}
		int alt;

		final HysterezeTester2 hystTest = new HysterezeTester2();
		WasserFilmDickeStufeTest.messwert = MesswertBearbeitungAllgemein.generiereMesswerte(
				WasserFilmDickeStufeTest.stufeVon[0],
				WasserFilmDickeStufeTest.stufeVon[WasserFilmDickeStufeTest.stufeVon.length - 1] * 1.2);
		WasserFilmDickeStufeTest.messwertGlatt = new double[WasserFilmDickeStufeTest.messwert.length];
		final double[] b = new double[WasserFilmDickeStufeTest.messwert.length];
		WasserFilmDickeStufeTest.stufen = new int[WasserFilmDickeStufeTest.messwert.length];
		WasserFilmDickeStufeTest.zeitStempel = new long[WasserFilmDickeStufeTest.messwert.length];
		hystTest.init(WasserFilmDickeStufeTest.stufeVon, WasserFilmDickeStufeTest.stufeBis);

		MesswertBearbeitungAllgemein.rundeMesswerte(WasserFilmDickeStufeTest.messwert);
		MesswertBearbeitungAllgemein.glaetteMesswerte(WasserFilmDickeStufeTest.messwert, b,
				WasserFilmDickeStufeTest.messwertGlatt, WasserFilmDickeStufeTest.FB, WasserFilmDickeStufeTest.B0);
		alt = -1;
		for (int i = 0; i < WasserFilmDickeStufeTest.messwertGlatt.length; i++) {
			WasserFilmDickeStufeTest.stufen[i] = hystTest.hystereze(WasserFilmDickeStufeTest.messwertGlatt[i], alt);
			alt = WasserFilmDickeStufeTest.stufen[i];
		}

		WasserFilmDickeStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(WasserFilmDickeStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		WasserFilmDickeStufeTest.zeitStempel[0] = System.currentTimeMillis() - (120 * 60 * 1000);
		WasserFilmDickeStufeTest.index = 0;
		WasserFilmDickeStufeTest.warten = true;
		for (int i = 0; i < WasserFilmDickeStufeTest.messwertGlatt.length; i++) {
			sendeMesswert(WasserFilmDickeStufeTest.testSensor, WasserFilmDickeStufeTest.messwert[i],
					WasserFilmDickeStufeTest.zeitStempel[i]);
			if ((i + 1) < WasserFilmDickeStufeTest.messwertGlatt.length) {
				WasserFilmDickeStufeTest.zeitStempel[i + 1] = WasserFilmDickeStufeTest.zeitStempel[i]
						+ WasserFilmDickeStufeTest.ZEIT_INTERVALL;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (final Exception e) {
			}
		}

		try {
			synchronized (WasserFilmDickeStufeTest.hauptModul) {
				while (WasserFilmDickeStufeTest.warten) {
					WasserFilmDickeStufeTest.hauptModul.wait();
				}
			}
		} catch (final Exception e) {
		}

		WasserFilmDickeStufeTest.hauptModul.disconnect();
		WasserFilmDickeStufeTest.hauptModul = null;
		WasserFilmDickeStufeTest.stufen = null;
		WasserFilmDickeStufeTest.messwertGlatt = null;
	}

	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 * Wie test 1 nur die Werte werden zufaellig geraeuscht
	 */
	@Test
	public void test2() {
		final long sleepTime = 50;
		/*
		 * ################ WARNUNG #################
		 *
		 * Werte im Afo sind mit genuaigkeit 0.01 mm wobei die Skalierung ist
		 * 0.1 mm
		 *
		 * deswegen das Abrunden
		 */
		for (int i = 0; i < WasserFilmDickeStufeTest.stufeBis.length; i++) {
			WasserFilmDickeStufeTest.stufeBis[i] = (Math.round(WasserFilmDickeStufeTest.stufeBis[i] * 10)) / 10.0;
			WasserFilmDickeStufeTest.stufeVon[i] = (Math.round(WasserFilmDickeStufeTest.stufeVon[i] * 10)) / 10.0;
		}
		int alt;

		final HysterezeTester2 hystTest = new HysterezeTester2();
		WasserFilmDickeStufeTest.messwert = MesswertBearbeitungAllgemein.generiereMesswerte(
				WasserFilmDickeStufeTest.stufeVon[0],
				WasserFilmDickeStufeTest.stufeVon[WasserFilmDickeStufeTest.stufeVon.length - 1] * 1.2);
		WasserFilmDickeStufeTest.messwertGlatt = new double[WasserFilmDickeStufeTest.messwert.length];
		final double[] b = new double[WasserFilmDickeStufeTest.messwert.length];
		WasserFilmDickeStufeTest.stufen = new int[WasserFilmDickeStufeTest.messwert.length];
		WasserFilmDickeStufeTest.zeitStempel = new long[WasserFilmDickeStufeTest.messwert.length];
		hystTest.init(WasserFilmDickeStufeTest.stufeVon, WasserFilmDickeStufeTest.stufeBis);

		MesswertBearbeitungAllgemein.gerauescheMesswerte(WasserFilmDickeStufeTest.messwert, 0.15, 20);
		MesswertBearbeitungAllgemein.rundeMesswerte(WasserFilmDickeStufeTest.messwert);
		MesswertBearbeitungAllgemein.glaetteMesswerte(WasserFilmDickeStufeTest.messwert, b,
				WasserFilmDickeStufeTest.messwertGlatt, WasserFilmDickeStufeTest.FB, WasserFilmDickeStufeTest.B0);
		alt = -1;
		for (int i = 0; i < WasserFilmDickeStufeTest.messwertGlatt.length; i++) {
			WasserFilmDickeStufeTest.stufen[i] = hystTest.hystereze(WasserFilmDickeStufeTest.messwertGlatt[i], alt);
			alt = WasserFilmDickeStufeTest.stufen[i];
		}

		WasserFilmDickeStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(WasserFilmDickeStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		WasserFilmDickeStufeTest.zeitStempel[0] = System.currentTimeMillis() - (120 * 60 * 1000);
		WasserFilmDickeStufeTest.index = 0;
		WasserFilmDickeStufeTest.warten = true;
		for (int i = 0; i < WasserFilmDickeStufeTest.messwertGlatt.length; i++) {
			sendeMesswert(WasserFilmDickeStufeTest.testSensor, WasserFilmDickeStufeTest.messwert[i],
					WasserFilmDickeStufeTest.zeitStempel[i]);
			if ((i + 1) < WasserFilmDickeStufeTest.messwertGlatt.length) {
				WasserFilmDickeStufeTest.zeitStempel[i + 1] = WasserFilmDickeStufeTest.zeitStempel[i]
						+ WasserFilmDickeStufeTest.ZEIT_INTERVALL;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (final Exception e) {
			}
		}
		try {
			synchronized (WasserFilmDickeStufeTest.hauptModul) {
				while (WasserFilmDickeStufeTest.warten) {
					WasserFilmDickeStufeTest.hauptModul.wait();
				}
			}
		} catch (final Exception e) {
		}

		WasserFilmDickeStufeTest.hauptModul.disconnect();
		WasserFilmDickeStufeTest.hauptModul = null;
		WasserFilmDickeStufeTest.stufen = null;
		WasserFilmDickeStufeTest.messwertGlatt = null;
	}

	@Override
	public double berechneMesswertGlaettung(final SensorParameter param, final double messwert) {
		final double r = super.berechneMesswertGlaettung(param, messwert);
		if (WasserFilmDickeStufeTest.messwertGlatt == null) {
			return r;
		}
		final double diff = WasserFilmDickeStufeTest.messwertGlatt[WasserFilmDickeStufeTest.index] - r;
		Assert.assertTrue(WasserFilmDickeStufeTest.index + " Wert : " + r + " Soll : "
				+ WasserFilmDickeStufeTest.messwertGlatt[WasserFilmDickeStufeTest.index] + " Differenz : " + diff,
				diff < 0.001);
		System.out.println(String.format("[ %4d ] Geglaetette Wert OK: %10.8f == %10.8f  Differrez: %10.8f",
				WasserFilmDickeStufeTest.index, WasserFilmDickeStufeTest.messwertGlatt[WasserFilmDickeStufeTest.index],
				r, diff));
		return r;
	}

	@Override
	public void sendeStufe(final SystemObject objekt, final int stufe, final long zeitStempel,
			final boolean keineDaten) {
		super.sendeStufe(objekt, stufe, zeitStempel, keineDaten);
		if (keineDaten) {
			return;
		}
		// d.H. es laeuft gerade ein test von anderer Klasse die NiStufe daten
		// benoetigt
		if (WasserFilmDickeStufeTest.stufen == null) {
			return;
		}
		Assert.assertEquals(WasserFilmDickeStufeTest.stufen[WasserFilmDickeStufeTest.index], stufe);
		Assert.assertEquals(WasserFilmDickeStufeTest.zeitStempel[WasserFilmDickeStufeTest.index], zeitStempel);
		System.out.println(String.format("[ %4d ] Stufe OK: %3d == %3d", WasserFilmDickeStufeTest.index,
				WasserFilmDickeStufeTest.stufen[WasserFilmDickeStufeTest.index], stufe));
		WasserFilmDickeStufeTest.index++;
		if (WasserFilmDickeStufeTest.index >= WasserFilmDickeStufeTest.stufen.length) {
			synchronized (verwaltung) {
				WasserFilmDickeStufeTest.warten = false;
				verwaltung.notify();
			}
		}
	}

	/**
	 * Sendet einen Messwert an den DAV
	 *
	 * @param sensor
	 *            Sensor, die Quelle des Messwertes
	 * @param messwert
	 *            der MessWert
	 * @param zeitStemepel
	 *            ZeitStempel
	 */
	private void sendeMesswert(final SystemObject sensor, final double messwert, final long zeitStemepel) {
		final Data data = WasserFilmDickeStufeTest.dav
				.createData(WasserFilmDickeStufeTest.dav.getDataModel().getAttributeGroup(getMesswertAttributGruppe()));

		final String att = getMesswertAttribut();
		data.getTimeValue("T").setMillis(WasserFilmDickeStufeTest.ZEIT_INTERVALL);
		data.getItem(att).getScaledValue("Wert").set(messwert);

		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);

		final ResultData result = new ResultData(sensor, WasserFilmDickeStufeTest.ddMessWerte, zeitStemepel, data);
		try {
			WasserFilmDickeStufeTest.dav.sendData(result);
		} catch (final Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void initialisiere(final IVerwaltung verwaltung) throws DUAInitialisierungsException {
		super.initialisiere(verwaltung);

		for (final SystemObject so : getSensoren()) {
			if (so != null) {
				WasserFilmDickeStufeTest.testSensor = so;
				break;
			}
		}

		WasserFilmDickeStufeTest.ddMessWerte = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(getMesswertAttributGruppe()),
				verwaltung.getVerbindung().getDataModel().getAspect("asp.messWertErsetzung"));

		WasserFilmDickeStufeTest.dav = verwaltung.getVerbindung();
		try {
			WasserFilmDickeStufeTest.dav.subscribeSender(this, getSensoren(), WasserFilmDickeStufeTest.ddMessWerte,
					SenderRole.source());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
