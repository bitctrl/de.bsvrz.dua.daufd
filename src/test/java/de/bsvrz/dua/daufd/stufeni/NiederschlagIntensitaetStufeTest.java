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

package de.bsvrz.dua.daufd.stufeni;

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
 * Testet und paramteriert den Modul NiederschalIntensitaetStufe
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
@Ignore("Testdatenverteiler prüfen")
public class NiederschlagIntensitaetStufeTest extends
		NiederschlagIntensitaetStufe {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * NI-Stufe untere Grenzwerte [AFo]
	 */
	private final static double[] stufeVon = new double[] { 0.0, 0.2, 1.0, 4.0,
			10.0 };
	/**
	 * NI-Stufe obere Grenzwerte [AFo]
	 */
	private final static double[] stufeBis = new double[] { 0.3, 1.2, 5.0,
			12.0, 200.0 // Max Wert vom DaK
	};
	/**
	 * Koefizient fuer Glaettung
	 */
	private final static double b0 = 0.08;
	/**
	 * Koefizient fuer Glaettung
	 */
	private final static double fb = 0.25;

	/**
	 * Verbindung zum dav
	 */
	private static ClientDavInterface dav;
	/**
	 * Der Verwaltungsmodul
	 */
	private static VerwaltungAufbereitungUFDTest hauptModul;

	/**
	 * String-KOnstanten
	 */
	private static final String TYP_UFDS_NI = "typ.ufdsNiederschlagsIntensität";
	private static final String ATG_UFDS_KLASS_NI = "atg.ufdsKlassifizierungNiederschlagsIntensität";
	private static final String ATT_UFDS_KLASS_NI = "KlassifizierungNiederschlagsIntensität";
	private static final String ATG_UFDS_AGGREG_NI = "atg.ufdsAggregationNiederschlagsIntensität";

	/**
	 * Einkommende Messwerte
	 */
	private static double[] Messwert = null;
	/**
	 * Erwarete Ausgabedaten - geglaettete Messawerte
	 */
	private static double[] MesswertGlatt = null;
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
	private static DataDescription DD_MESSWERTE;
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
	public static void ParametriereUfds(final ClientDavInterface dav,
			final Collection<ConfigurationArea> konfBereiche) {
		try {
			final UfdsKlassifizierungParametrierung param = new UfdsKlassifizierungParametrierung(
					NiederschlagIntensitaetStufeTest.TYP_UFDS_NI,
					NiederschlagIntensitaetStufeTest.ATG_UFDS_KLASS_NI,
					NiederschlagIntensitaetStufeTest.ATT_UFDS_KLASS_NI,
					NiederschlagIntensitaetStufeTest.ATG_UFDS_AGGREG_NI,
					NiederschlagIntensitaetStufeTest.stufeVon,
					NiederschlagIntensitaetStufeTest.stufeBis,
					NiederschlagIntensitaetStufeTest.b0,
					NiederschlagIntensitaetStufeTest.fb);
			param.ParametriereUfds(dav, konfBereiche);
		} catch (final Exception e) {
			LOGGER.error(
					"Fehler bei Parametrierung der NiederschlagIntensitaet:"
							+ e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 */
	@Test
	public void test1() {
		final long SLEEP = 50;
		int alt;

		final HysterezeTester2 hystTest = new HysterezeTester2();
		NiederschlagIntensitaetStufeTest.Messwert = MesswertBearbeitungAllgemein
				.generiereMesswerte(
						NiederschlagIntensitaetStufeTest.stufeVon[0],
						NiederschlagIntensitaetStufeTest.stufeVon[NiederschlagIntensitaetStufeTest.stufeVon.length - 1] * 1.2);
		NiederschlagIntensitaetStufeTest.MesswertGlatt = new double[NiederschlagIntensitaetStufeTest.Messwert.length];
		final double[] b = new double[NiederschlagIntensitaetStufeTest.Messwert.length];
		NiederschlagIntensitaetStufeTest.stufen = new int[NiederschlagIntensitaetStufeTest.Messwert.length];
		NiederschlagIntensitaetStufeTest.zeitStempel = new long[NiederschlagIntensitaetStufeTest.Messwert.length];
		hystTest.init(NiederschlagIntensitaetStufeTest.stufeVon,
				NiederschlagIntensitaetStufeTest.stufeBis);

		MesswertBearbeitungAllgemein
				.rundeMesswerte(NiederschlagIntensitaetStufeTest.Messwert);
		MesswertBearbeitungAllgemein.glaetteMesswerte(
				NiederschlagIntensitaetStufeTest.Messwert, b,
				NiederschlagIntensitaetStufeTest.MesswertGlatt,
				NiederschlagIntensitaetStufeTest.fb,
				NiederschlagIntensitaetStufeTest.b0);
		alt = -1;
		for (int i = 0; i < NiederschlagIntensitaetStufeTest.MesswertGlatt.length; i++) {
			NiederschlagIntensitaetStufeTest.stufen[i] = hystTest.hystereze(
					NiederschlagIntensitaetStufeTest.MesswertGlatt[i], alt);
			alt = NiederschlagIntensitaetStufeTest.stufen[i];
		}

		NiederschlagIntensitaetStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(
				NiederschlagIntensitaetStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * SLEEP);
		} catch (final Exception e) {
		}

		NiederschlagIntensitaetStufeTest.zeitStempel[0] = System
				.currentTimeMillis() - (120 * 60 * 1000);
		NiederschlagIntensitaetStufeTest.index = 0;
		NiederschlagIntensitaetStufeTest.warten = true;
		for (int i = 0; i < NiederschlagIntensitaetStufeTest.MesswertGlatt.length; i++) {
			sendeMesswert(NiederschlagIntensitaetStufeTest.testSensor,
					NiederschlagIntensitaetStufeTest.Messwert[i],
					NiederschlagIntensitaetStufeTest.zeitStempel[i]);
			if ((i + 1) < NiederschlagIntensitaetStufeTest.MesswertGlatt.length) {
				NiederschlagIntensitaetStufeTest.zeitStempel[i + 1] = NiederschlagIntensitaetStufeTest.zeitStempel[i]
						+ NiederschlagIntensitaetStufeTest.ZEIT_INTERVALL;
			}
			try {
				Thread.sleep(SLEEP);
			} catch (final Exception e) {
			}
		}
		try {
			synchronized (NiederschlagIntensitaetStufeTest.hauptModul) {
				while (NiederschlagIntensitaetStufeTest.warten) {
					NiederschlagIntensitaetStufeTest.hauptModul.wait();
				}
			}
		} catch (final Exception e) {
		}
		NiederschlagIntensitaetStufeTest.hauptModul.disconnect();
		NiederschlagIntensitaetStufeTest.hauptModul = null;
		NiederschlagIntensitaetStufeTest.stufen = null;
		NiederschlagIntensitaetStufeTest.MesswertGlatt = null;
	}

	/**
	 * Generiert eine Reihe von Zahlen und vergleicht mit der getesteten Klasse
	 * Wie test 1 nur die Werte werden zufaellig geraeuscht
	 */
	@Test
	public void test2() {
		final long SLEEP = 50;
		int alt;

		final HysterezeTester2 hystTest = new HysterezeTester2();
		NiederschlagIntensitaetStufeTest.Messwert = MesswertBearbeitungAllgemein
				.generiereMesswerte(
						NiederschlagIntensitaetStufeTest.stufeVon[0],
						NiederschlagIntensitaetStufeTest.stufeVon[NiederschlagIntensitaetStufeTest.stufeVon.length - 1] * 1.2);
		NiederschlagIntensitaetStufeTest.MesswertGlatt = new double[NiederschlagIntensitaetStufeTest.Messwert.length];
		final double[] b = new double[NiederschlagIntensitaetStufeTest.Messwert.length];
		NiederschlagIntensitaetStufeTest.stufen = new int[NiederschlagIntensitaetStufeTest.Messwert.length];
		NiederschlagIntensitaetStufeTest.zeitStempel = new long[NiederschlagIntensitaetStufeTest.Messwert.length];
		hystTest.init(NiederschlagIntensitaetStufeTest.stufeVon,
				NiederschlagIntensitaetStufeTest.stufeBis);

		MesswertBearbeitungAllgemein.gerauescheMesswerte(
				NiederschlagIntensitaetStufeTest.Messwert, 0.15, 20);
		MesswertBearbeitungAllgemein
				.rundeMesswerte(NiederschlagIntensitaetStufeTest.Messwert);
		MesswertBearbeitungAllgemein.glaetteMesswerte(
				NiederschlagIntensitaetStufeTest.Messwert, b,
				NiederschlagIntensitaetStufeTest.MesswertGlatt,
				NiederschlagIntensitaetStufeTest.fb,
				NiederschlagIntensitaetStufeTest.b0);
		alt = -1;
		for (int i = 0; i < NiederschlagIntensitaetStufeTest.MesswertGlatt.length; i++) {
			NiederschlagIntensitaetStufeTest.stufen[i] = hystTest.hystereze(
					NiederschlagIntensitaetStufeTest.MesswertGlatt[i], alt);
			alt = NiederschlagIntensitaetStufeTest.stufen[i];
		}

		NiederschlagIntensitaetStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(
				NiederschlagIntensitaetStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * SLEEP);
		} catch (final Exception e) {
		}

		NiederschlagIntensitaetStufeTest.zeitStempel[0] = System
				.currentTimeMillis() - (120 * 60 * 1000);
		NiederschlagIntensitaetStufeTest.index = 0;
		NiederschlagIntensitaetStufeTest.warten = true;
		for (int i = 0; i < NiederschlagIntensitaetStufeTest.MesswertGlatt.length; i++) {
			sendeMesswert(NiederschlagIntensitaetStufeTest.testSensor,
					NiederschlagIntensitaetStufeTest.Messwert[i],
					NiederschlagIntensitaetStufeTest.zeitStempel[i]);
			if ((i + 1) < NiederschlagIntensitaetStufeTest.MesswertGlatt.length) {
				NiederschlagIntensitaetStufeTest.zeitStempel[i + 1] = NiederschlagIntensitaetStufeTest.zeitStempel[i]
						+ NiederschlagIntensitaetStufeTest.ZEIT_INTERVALL;
			}
			try {
				Thread.sleep(SLEEP);
			} catch (final Exception e) {
			}
		}
		try {
			synchronized (NiederschlagIntensitaetStufeTest.hauptModul) {
				while (NiederschlagIntensitaetStufeTest.warten) {
					NiederschlagIntensitaetStufeTest.hauptModul.wait();
				}
			}
		} catch (final Exception e) {
		}
		NiederschlagIntensitaetStufeTest.hauptModul.disconnect();
		NiederschlagIntensitaetStufeTest.hauptModul = null;
		NiederschlagIntensitaetStufeTest.stufen = null;
		NiederschlagIntensitaetStufeTest.MesswertGlatt = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public double berechneMesswertGlaettung(final SensorParameter param,
			final double messwert) {
		final double r = super.berechneMesswertGlaettung(param, messwert);
		if (NiederschlagIntensitaetStufeTest.MesswertGlatt == null) {
			return r;
		}
		final double diff = NiederschlagIntensitaetStufeTest.MesswertGlatt[NiederschlagIntensitaetStufeTest.index]
				- r;
		Assert.assertTrue(
				NiederschlagIntensitaetStufeTest.index
						+ " Wert : "
						+ r
						+ " Soll : "
						+ NiederschlagIntensitaetStufeTest.MesswertGlatt[NiederschlagIntensitaetStufeTest.index]
						+ " Differenz : " + diff, diff < 0.001);
		System.out
				.println(String
						.format("[ %4d ] Geglaetette Wert OK: %10.8f == %10.8f  Differrez: %10.8f",
								NiederschlagIntensitaetStufeTest.index,
								NiederschlagIntensitaetStufeTest.MesswertGlatt[NiederschlagIntensitaetStufeTest.index],
								r, diff));
		return r;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void SendeStufe(final SystemObject objekt, final int stufe,
			final long zeitStempel, final boolean keineDaten) {
		super.SendeStufe(objekt, stufe, zeitStempel, keineDaten);
		if (keineDaten) {
			return;
		}
		// d.H. es laeuft gerade ein test von anderer Klasse die NiStufe daten
		// benoetigt
		if (NiederschlagIntensitaetStufeTest.stufen == null) {
			return;
		}
		Assert.assertEquals(
				NiederschlagIntensitaetStufeTest.stufen[NiederschlagIntensitaetStufeTest.index],
				stufe);
		Assert.assertEquals(
				NiederschlagIntensitaetStufeTest.zeitStempel[NiederschlagIntensitaetStufeTest.index],
				zeitStempel);
		System.out
				.println(String
						.format("[ %4d ] Stufe OK: %3d == %3d",
								NiederschlagIntensitaetStufeTest.index,
								NiederschlagIntensitaetStufeTest.stufen[NiederschlagIntensitaetStufeTest.index],
								stufe));
		NiederschlagIntensitaetStufeTest.index++;
		if (NiederschlagIntensitaetStufeTest.index >= NiederschlagIntensitaetStufeTest.stufen.length) {
			synchronized (verwaltung) {
				NiederschlagIntensitaetStufeTest.warten = false;
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
	private void sendeMesswert(final SystemObject sensor,
			final double messwert, final long zeitStemepel) {
		final Data data = NiederschlagIntensitaetStufeTest.dav
				.createData(NiederschlagIntensitaetStufeTest.dav.getDataModel()
						.getAttributeGroup(getMesswertAttributGruppe()));

		final String att = getMesswertAttribut();
		data.getTimeValue("T").setMillis(
				NiederschlagIntensitaetStufeTest.ZEIT_INTERVALL);
		data.getItem(att).getScaledValue("Wert").set(messwert);

		data.getItem(att).getItem("Status").getItem("Erfassung")
				.getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal")
				.getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal")
				.getUnscaledValue("WertMin").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung")
				.getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung")
				.getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);

		final ResultData result = new ResultData(sensor,
				NiederschlagIntensitaetStufeTest.DD_MESSWERTE, zeitStemepel,
				data);
		try {
			NiederschlagIntensitaetStufeTest.dav.sendData(result);
		} catch (final Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void initialisiere(final IVerwaltung verwaltung)
			throws DUAInitialisierungsException {
		super.initialisiere(verwaltung);

		for (final SystemObject so : getSensoren()) {
			if (so != null) {
				NiederschlagIntensitaetStufeTest.testSensor = so;
				break;
			}
		}

		NiederschlagIntensitaetStufeTest.DD_MESSWERTE = new DataDescription(
				verwaltung.getVerbindung().getDataModel()
						.getAttributeGroup(getMesswertAttributGruppe()),
				verwaltung.getVerbindung().getDataModel()
						.getAspect("asp.messWertErsetzung"));

		NiederschlagIntensitaetStufeTest.dav = verwaltung.getVerbindung();
		try {
			NiederschlagIntensitaetStufeTest.dav.subscribeSender(this,
					getSensoren(),
					NiederschlagIntensitaetStufeTest.DD_MESSWERTE,
					SenderRole.source());
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

}
