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

package de.bsvrz.dua.daufd.stufesw;

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
 * Parametriert den Modul SichtWeite
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
@Ignore("Testdatenverteiler prüfen")
public class SichtWeiteStufeTest extends SichtWeiteStufe {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * SW-Stufe untere Grenzwerte [AFo].
	 */
	private static final double[] STUFE_VON = new double[] { 0, 50, 80, 120, 250, 400 };
	/**
	 * SW-Stufe obere Grenzwerte [AFo].
	 */
	private static final double[] STUFE_BIS = new double[] { 60, 100, 150, 300, 500, 60000 // Max
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
	 * String-Konstanten
	 */
	private static final String TYP_UFDS_WFD = "typ.ufdsSichtWeite";
	private static final String ATG_UFDS_KLASS_WFD = "atg.ufdsKlassifizierungSichtWeite";
	private static final String ATT_UFDS_KLASS_WFD = "KlassifizierungSichtWeite";
	private static final String ATG_UFDS_AGGREG_WFD = "atg.ufdsAggregationSichtWeite";

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
					SichtWeiteStufeTest.TYP_UFDS_WFD, SichtWeiteStufeTest.ATG_UFDS_KLASS_WFD,
					SichtWeiteStufeTest.ATT_UFDS_KLASS_WFD, SichtWeiteStufeTest.ATG_UFDS_AGGREG_WFD,
					SichtWeiteStufeTest.STUFE_VON, SichtWeiteStufeTest.STUFE_BIS, SichtWeiteStufeTest.B0,
					SichtWeiteStufeTest.FB);
			param.parametriereUfds(dav, konfBereiche);
		} catch (final Exception e) {
			SichtWeiteStufeTest.LOGGER.error("Fehler bei Parametrierung der SichtWeite:" + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 */
	@Test
	public void test1() {
		final long sleepTime = 50;
		int alt;

		final HysterezeTester2 hystTest = new HysterezeTester2();
		SichtWeiteStufeTest.messwert = MesswertBearbeitungAllgemein.generiereMesswerte(SichtWeiteStufeTest.STUFE_VON[0],
				SichtWeiteStufeTest.STUFE_VON[SichtWeiteStufeTest.STUFE_VON.length - 1] * 1.2);
		SichtWeiteStufeTest.messwertGlatt = new double[SichtWeiteStufeTest.messwert.length];
		final double[] b = new double[SichtWeiteStufeTest.messwert.length];
		SichtWeiteStufeTest.stufen = new int[SichtWeiteStufeTest.messwert.length];
		SichtWeiteStufeTest.zeitStempel = new long[SichtWeiteStufeTest.messwert.length];
		hystTest.init(SichtWeiteStufeTest.STUFE_VON, SichtWeiteStufeTest.STUFE_BIS);

		MesswertBearbeitungAllgemein.rundeMesswerteGanzeZahl(SichtWeiteStufeTest.messwert);
		MesswertBearbeitungAllgemein.glaetteMesswerte(SichtWeiteStufeTest.messwert, b,
				SichtWeiteStufeTest.messwertGlatt, SichtWeiteStufeTest.FB, SichtWeiteStufeTest.B0);
		alt = -1;
		for (int i = 0; i < SichtWeiteStufeTest.messwertGlatt.length; i++) {
			SichtWeiteStufeTest.stufen[i] = hystTest.hystereze(SichtWeiteStufeTest.messwertGlatt[i], alt);
			alt = SichtWeiteStufeTest.stufen[i];
		}

		SichtWeiteStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(SichtWeiteStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		SichtWeiteStufeTest.zeitStempel[0] = System.currentTimeMillis() - (120 * 60 * 1000);
		SichtWeiteStufeTest.index = 0;
		SichtWeiteStufeTest.warten = true;
		for (int i = 0; i < SichtWeiteStufeTest.messwertGlatt.length; i++) {
			sendeMesswert(SichtWeiteStufeTest.testSensor, SichtWeiteStufeTest.messwert[i],
					SichtWeiteStufeTest.zeitStempel[i]);
			if ((i + 1) < SichtWeiteStufeTest.messwertGlatt.length) {
				SichtWeiteStufeTest.zeitStempel[i + 1] = SichtWeiteStufeTest.zeitStempel[i]
						+ SichtWeiteStufeTest.ZEIT_INTERVALL;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (final Exception e) {
			}
		}
		try {
			synchronized (SichtWeiteStufeTest.hauptModul) {
				while (SichtWeiteStufeTest.warten) {
					SichtWeiteStufeTest.hauptModul.wait();
				}
			}
		} catch (final Exception e) {
		}
		SichtWeiteStufeTest.hauptModul.disconnect();
		SichtWeiteStufeTest.hauptModul = null;
		SichtWeiteStufeTest.stufen = null;
		SichtWeiteStufeTest.messwertGlatt = null;

	}

	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 * Wie test 1 nur die Werte werden zufaellig geraeuscht
	 */
	@Test
	public void test2() {
		final long sleepTime = 50;
		int alt;

		final HysterezeTester2 hystTest = new HysterezeTester2();
		SichtWeiteStufeTest.messwert = MesswertBearbeitungAllgemein.generiereMesswerte(SichtWeiteStufeTest.STUFE_VON[0],
				SichtWeiteStufeTest.STUFE_VON[SichtWeiteStufeTest.STUFE_VON.length - 1] * 1.2);
		SichtWeiteStufeTest.messwertGlatt = new double[SichtWeiteStufeTest.messwert.length];
		final double[] b = new double[SichtWeiteStufeTest.messwert.length];
		SichtWeiteStufeTest.stufen = new int[SichtWeiteStufeTest.messwert.length];
		SichtWeiteStufeTest.zeitStempel = new long[SichtWeiteStufeTest.messwert.length];
		hystTest.init(SichtWeiteStufeTest.STUFE_VON, SichtWeiteStufeTest.STUFE_BIS);

		MesswertBearbeitungAllgemein.gerauescheMesswerte(SichtWeiteStufeTest.messwert, 0.15, 20);
		MesswertBearbeitungAllgemein.rundeMesswerteGanzeZahl(SichtWeiteStufeTest.messwert);
		MesswertBearbeitungAllgemein.glaetteMesswerte(SichtWeiteStufeTest.messwert, b,
				SichtWeiteStufeTest.messwertGlatt, SichtWeiteStufeTest.FB, SichtWeiteStufeTest.B0);
		alt = -1;
		for (int i = 0; i < SichtWeiteStufeTest.messwertGlatt.length; i++) {
			SichtWeiteStufeTest.stufen[i] = hystTest.hystereze(SichtWeiteStufeTest.messwertGlatt[i], alt);
			alt = SichtWeiteStufeTest.stufen[i];
		}

		SichtWeiteStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(SichtWeiteStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		SichtWeiteStufeTest.zeitStempel[0] = System.currentTimeMillis() - (120 * 60 * 1000);
		SichtWeiteStufeTest.index = 0;
		SichtWeiteStufeTest.warten = true;
		for (int i = 0; i < SichtWeiteStufeTest.messwertGlatt.length; i++) {
			sendeMesswert(SichtWeiteStufeTest.testSensor, SichtWeiteStufeTest.messwert[i],
					SichtWeiteStufeTest.zeitStempel[i]);
			if ((i + 1) < SichtWeiteStufeTest.messwertGlatt.length) {
				SichtWeiteStufeTest.zeitStempel[i + 1] = SichtWeiteStufeTest.zeitStempel[i]
						+ SichtWeiteStufeTest.ZEIT_INTERVALL;
			}
			try {
				Thread.sleep(sleepTime);
			} catch (final Exception e) {
			}
		}
		try {
			synchronized (SichtWeiteStufeTest.hauptModul) {
				while (SichtWeiteStufeTest.warten) {
					SichtWeiteStufeTest.hauptModul.wait();
				}
			}
		} catch (final Exception e) {
		}

		SichtWeiteStufeTest.hauptModul.disconnect();
		SichtWeiteStufeTest.hauptModul = null;
		SichtWeiteStufeTest.stufen = null;
		SichtWeiteStufeTest.messwertGlatt = null;
	}

	@Override
	public double berechneMesswertGlaettung(final SensorParameter param, final double messwert) {
		final double r = super.berechneMesswertGlaettung(param, messwert);
		if (SichtWeiteStufeTest.messwertGlatt == null) {
			return r;
		}
		final double diff = SichtWeiteStufeTest.messwertGlatt[SichtWeiteStufeTest.index] - r;
		Assert.assertTrue(
				SichtWeiteStufeTest.index + " Wert : " + r + " Soll : "
						+ SichtWeiteStufeTest.messwertGlatt[SichtWeiteStufeTest.index] + " Differenz : " + diff,
				diff < 0.05);
		System.out.println(String.format("[ %4d ] Geglaetette Wert OK: %10.8f == %10.8f  Differrez: %10.8f",
				SichtWeiteStufeTest.index, SichtWeiteStufeTest.messwertGlatt[SichtWeiteStufeTest.index], r, diff));
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
		if (SichtWeiteStufeTest.stufen == null) {
			return;
		}
		Assert.assertEquals(SichtWeiteStufeTest.stufen[SichtWeiteStufeTest.index], stufe);
		Assert.assertEquals(SichtWeiteStufeTest.zeitStempel[SichtWeiteStufeTest.index], zeitStempel);
		System.out.println(String.format("[ %4d ] Stufe OK: %3d == %3d", SichtWeiteStufeTest.index,
				SichtWeiteStufeTest.stufen[SichtWeiteStufeTest.index], stufe));
		SichtWeiteStufeTest.index++;
		if (SichtWeiteStufeTest.index >= SichtWeiteStufeTest.stufen.length) {
			synchronized (verwaltung) {
				SichtWeiteStufeTest.warten = false;
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
		final Data data = SichtWeiteStufeTest.dav
				.createData(SichtWeiteStufeTest.dav.getDataModel().getAttributeGroup(getMesswertAttributGruppe()));

		final String att = getMesswertAttribut();
		data.getTimeValue("T").setMillis(SichtWeiteStufeTest.ZEIT_INTERVALL);
		data.getItem(att).getScaledValue("Wert").set(messwert);

		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);

		final ResultData result = new ResultData(sensor, SichtWeiteStufeTest.ddMessWerte, zeitStemepel, data);
		try {
			SichtWeiteStufeTest.dav.sendData(result);
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
				SichtWeiteStufeTest.testSensor = so;
				break;
			}
		}

		SichtWeiteStufeTest.ddMessWerte = new DataDescription(
				verwaltung.getVerbindung().getDataModel().getAttributeGroup(getMesswertAttributGruppe()),
				verwaltung.getVerbindung().getDataModel().getAspect("asp.messWertErsetzung"));

		SichtWeiteStufeTest.dav = verwaltung.getVerbindung();
		try {
			SichtWeiteStufeTest.dav.subscribeSender(this, getSensoren(), SichtWeiteStufeTest.ddMessWerte,
					SenderRole.source());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
