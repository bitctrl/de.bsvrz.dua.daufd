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

package de.bsvrz.dua.daufd.stufenaesse;

import java.util.Collection;
import java.util.LinkedList;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.dav.daf.main.ClientSenderInterface;
import de.bsvrz.dav.daf.main.Data;
import de.bsvrz.dav.daf.main.DataDescription;
import de.bsvrz.dav.daf.main.ResultData;
import de.bsvrz.dav.daf.main.SenderRole;
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dav.daf.main.config.ObjectTimeSpecification;
import de.bsvrz.dav.daf.main.config.SystemObject;
import de.bsvrz.dav.daf.main.config.SystemObjectType;
import de.bsvrz.dua.daufd.DAVTest;
import de.bsvrz.dua.daufd.VerwaltungAufbereitungUFDTest;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe;
import de.bsvrz.dua.daufd.stufeni.NiederschlagIntensitaetStufe.NIStufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickeStufe.WFDStufe;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.bitctrl.dua.schnittstellen.IVerwaltung;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Testet und Parametriert den Modul NaesseStufe
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
@Ignore("Testdatenverteiler prüfen")
public class NaesseStufeTest extends NaesseStufe implements ClientSenderInterface {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Abtrocknungphasen Verzoegerung [AFo]
	 */
	private static final long[] ABTROCKNUNG_PHASEN = new long[] { 180, 60, 60, 60 };
	/**
	 * Aspekt fuer Parametrierung
	 */
	private static final String ASP_PARAM_VORGABE = "asp.parameterVorgabe";
	/**
	 * Datenbeschreibung fuer die Klasifizierung Daten
	 */
	private static DataDescription ddAbtrocknungPhasen = null;

	/**
	 * Verbindung zum dav
	 */
	private static ClientDavInterface dav;
	/**
	 * Der Verwaltungsmodul
	 */
	private static VerwaltungAufbereitungUFDTest hauptModul;
	/**
	 * Errechnete Ausgabewerte
	 */
	private static NSStufe[] ausgabe = null;
	/**
	 * Errechnete zeitStempel der Ausgabewerten
	 */
	private static long[] ausgabeZeitStempel = null;
	/**
	 * Aktueller index im Ausgabewerten
	 */
	private static int ausgabeIndex = 0;
	/**
	 * Im testfaellen wird der Verzoegerungsintervall fuer Abtrocknungsphasen
	 * verkuertzt
	 */
	private static final long ABTR_INTERVALL = 1;
	/**
	 * Sensore die die Testdaten liefern
	 */
	private static SystemObject fbofZustandSensor, naSensor, wfdSensor, niSensor;
	/**
	 * Datenbeschreibung der Daten die von Testsensoren geschickt werden
	 */
	private static DataDescription ddFbofZustand, ddNieArt;
	/**
	 * Bestimmt ob man an die bearbeitung der Daten warten soll
	 */
	private static boolean warten = true;
	/**
	 * String-Konstanten
	 */
	private static final String TYP_UFDMS = "typ.umfeldDatenMessStelle";
	private static final String ATG_UFDMS_AP = "atg.ufdmsAbtrockungsPhasen";
	private static final String[] ATT_STUFE = new String[] { "ZeitNass1Trocken", "ZeitNass4Nass3", "ZeitNass3Nass2",
			"ZeitNass2Nass1" };

	/**
	 * Zustaende.
	 */
	private static final int FBOF_TROCKEN = 0;
	private static final int FBOF_EIS = 66;
	private static final int NART_KEIN = 0;
	private static final int NART_SCHNEE = 73;

	/**
	 * Parametriert die Verzoegerung bei der Abtrocknungphasen.
	 *
	 * @param dav
	 *            Verbindung zum Datenverteiler
	 * @param konfBereiche
	 *            konfigurationsbereiche in denen alle Objekte parametriert
	 *            werden sollen
	 */
	public static void parametriereUfds(final ClientDavInterface dav,
			final Collection<ConfigurationArea> konfBereiche) {
		try {

			NaesseStufeTest.dav = dav;
			final NaesseStufeTest param = new NaesseStufeTest();

			NaesseStufeTest.ddAbtrocknungPhasen = new DataDescription(
					dav.getDataModel().getAttributeGroup(NaesseStufeTest.ATG_UFDMS_AP),
					dav.getDataModel().getAspect(NaesseStufeTest.ASP_PARAM_VORGABE));

			final Collection<SystemObjectType> sotMenge = new LinkedList<SystemObjectType>();
			sotMenge.add(dav.getDataModel().getType(NaesseStufeTest.TYP_UFDMS));

			final Collection<SystemObject> ufdsObjekte = dav.getDataModel().getObjects(konfBereiche, sotMenge,
					ObjectTimeSpecification.valid());

			if (ufdsObjekte == null) {
				NaesseStufeTest.LOGGER.error("Kein Objekt vom " + NaesseStufeTest.TYP_UFDMS
						+ " in den KonfigurationsBeriechen :" + konfBereiche);
				System.exit(-1);
			}

			try {
				dav.subscribeSender(param, ufdsObjekte, NaesseStufeTest.ddAbtrocknungPhasen, SenderRole.sender());
			} catch (final Exception e) {
				NaesseStufeTest.LOGGER.error("Fehler bei Anmeldung für Klassifizierung der Objekte vom Typ "
						+ NaesseStufeTest.TYP_UFDMS + ":" + e.getMessage());
				e.printStackTrace();
			}
			Thread.sleep(100);

			dav.unsubscribeSender(param, ufdsObjekte, NaesseStufeTest.ddAbtrocknungPhasen);

		} catch (final Exception e) {
			NaesseStufeTest.LOGGER
			.error("Fehler bei Parametrierung der NaesseStufe Abtrocknungphasen: " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void dataRequest(final SystemObject object, final DataDescription dataDescription, final byte state) {
		if (dataDescription.getAttributeGroup().getPid().equals(NaesseStufeTest.ATG_UFDMS_AP)
				&& (state == ClientSenderInterface.START_SENDING)) {

			final Data datei = NaesseStufeTest.dav
					.createData(NaesseStufeTest.dav.getDataModel().getAttributeGroup(NaesseStufeTest.ATG_UFDMS_AP));

			for (int i = 0; i < NaesseStufeTest.ATT_STUFE.length; i++) {
				datei.getTimeValue(NaesseStufeTest.ATT_STUFE[i]).setSeconds(NaesseStufeTest.ABTROCKNUNG_PHASEN[i]);
			}

			final ResultData resDatei = new ResultData(object, NaesseStufeTest.ddAbtrocknungPhasen,
					System.currentTimeMillis(), datei);

			try {
				NaesseStufeTest.dav.sendData(resDatei);
				System.out.println(
						"Objekt " + object.getPid() + " Atg: " + NaesseStufeTest.ATG_UFDMS_AP + " parametriert ");
			} catch (final Exception e) {
				NaesseStufeTest.LOGGER
						.error("Fehler bei Sendung von Daten für Klassifizierung Niederschlaginetnsitaet des Objektes :"
								+ object.getPid() + "\n Fehler:" + e.getMessage());
				e.printStackTrace();
			}
		}

	}

	@Override
	public boolean isRequestSupported(final SystemObject object, final DataDescription dataDescription) {
		return false;
	}

	@Override
	public void initialisiere(final IVerwaltung verwaltung) throws DUAInitialisierungsException {
		super.initialisiere(verwaltung);

		NaesseStufeTest.dav = verwaltung.getVerbindung();

		for (final SystemObject so : getNaSensoren()) {
			if (so != null) {
				NaesseStufeTest.naSensor = so;
				break;
			}
		}

		for (final SystemObject so : getFbofZustandSensoren()) {
			if (so != null) {
				NaesseStufeTest.fbofZustandSensor = so;
				break;
			}
		}

		for (final SystemObject so : niSensoren) {
			if (so != null) {
				NaesseStufeTest.niSensor = so;
				break;
			}
		}
		for (final SystemObject so : wfdSensoren) {
			if (so != null) {
				NaesseStufeTest.wfdSensor = so;
				break;
			}
		}
		try {
			ResultData resultate;
			NaesseStufeTest.ddFbofZustand = new DataDescription(
					NaesseStufeTest.dav.getDataModel().getAttributeGroup(NaesseStufe.ATG_UFDS_FBOFZS),
					NaesseStufeTest.dav.getDataModel().getAspect(NaesseStufe.ASP_MESSWERTERSETZUNG));
			resultate = new ResultData(NaesseStufeTest.fbofZustandSensor, NaesseStufeTest.ddFbofZustand,
					System.currentTimeMillis(), null);
			NaesseStufeTest.dav.subscribeSource(this, resultate);

			NaesseStufeTest.ddNieArt = new DataDescription(
					NaesseStufeTest.dav.getDataModel().getAttributeGroup(NaesseStufe.ATG_UFDS_NA),
					NaesseStufeTest.dav.getDataModel().getAspect(NaesseStufe.ASP_MESSWERTERSETZUNG));
			resultate = new ResultData(NaesseStufeTest.naSensor, NaesseStufeTest.ddNieArt, System.currentTimeMillis(),
					null);
			NaesseStufeTest.dav.subscribeSource(this, resultate);

		} catch (final Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public void update(final ResultData[] results) {
		for (final ResultData resData : results) {
			final DataDescription dataDescription = resData.getDataDescription();
			final Data daten = resData.getData();
			if (daten == null) {
				continue;
			}
			final SystemObject objekt = resData.getObject();
			final MessStelleDaten messStelleDaten = this.naesseTabelle.get(objekt);

			if (messStelleDaten == null) {
				NaesseStufeTest.LOGGER.warning("Objekt " + objekt + " in der Hashtabelle nicht gefunden");
				return;
			}

			if (dataDescription.getAttributeGroup().getPid().equals(NaesseStufeTest.ATG_UFDMS_AP)) {
				for (int i = 0; i < NaesseStufeTest.ATT_STUFE.length; i++) {
					messStelleDaten.abtrocknungsPhasen[i] = NaesseStufeTest.ABTR_INTERVALL; // Im
					// Testfaellen
					// ist
					// keine
					// Verzoegerung
					// (10
					// ms)
				}
				messStelleDaten.initialisiert = true;
			}
		}
	}

	@Override
	public void publiziereNsStufe(final MessStelleDaten msDaten, final boolean keineDaten) {
		super.publiziereNsStufe(msDaten, keineDaten);
		if (keineDaten) {
			return;
		}
		// d.H. es laeuft gerade ein test von anderer Klasse die NiStufe daten
		// benoetigt
		if (NaesseStufeTest.ausgabe == null) {
			return;
		}

		Assert.assertEquals(
				"Werte nicht gleich soll:" + NaesseStufeTest.ausgabe[NaesseStufeTest.ausgabeIndex].ordinal() + " ist:"
						+ msDaten.nsStufe.ordinal() + " index " + NaesseStufeTest.ausgabeIndex,
				NaesseStufeTest.ausgabe[NaesseStufeTest.ausgabeIndex].ordinal(), msDaten.nsStufe.ordinal());
		Assert.assertEquals(NaesseStufeTest.ausgabeZeitStempel[NaesseStufeTest.ausgabeIndex],
				msDaten.nsStufeZeitStempel);
		System.out.println(String.format("[ %4d ] NS Stufe OK: %-10s == %-10s", NaesseStufeTest.ausgabeIndex,
				NaesseStufeTest.ausgabe[NaesseStufeTest.ausgabeIndex], msDaten.nsStufe));
		NaesseStufeTest.ausgabeIndex++;
		NaesseStufeTest.warten = false;
	}

	/**
	 * Sendet einen DS mit Wasserfilmdickestufe
	 *
	 * @param objekt
	 *            Der Sensor
	 * @param stufe
	 *            Die Stufe
	 * @param zeitStempel
	 *            Der ZeitStempels
	 */
	private void sendeWfdStufe(final SystemObject objekt, final WFDStufe stufe, final long zeitStempel) {
		final int intStufe = WasserFilmDickeStufe.getStufe(stufe);
		NaesseStufeTest.hauptModul.getWfdKnotne().sendeStufe(objekt, intStufe, zeitStempel, false);
	}

	/**
	 * Sendet einen DS mit Niederschlagintensitaetstufe
	 *
	 * @param objekt
	 *            Der Sensor
	 * @param stufe
	 *            Die Stufe
	 * @param zeitStempel
	 *            Der ZeitStempels
	 */
	private void sendeNiStufe(final SystemObject objekt, final NIStufe stufe, final long zeitStempel) {
		final int intStufe = NiederschlagIntensitaetStufe.getStufe(stufe);
		NaesseStufeTest.hauptModul.getNiKnoten().sendeStufe(objekt, intStufe, zeitStempel, false);
	}

	/**
	 * Sendet einen DS mit Fahrbahnoberflaechezustand
	 *
	 * @param objekt
	 *            Der Sensor
	 * @param stufe
	 *            Die Stufe
	 * @param zeitStempel
	 *            Der ZeitStempels
	 */
	private static void sendeFbofZustand(final SystemObject objekt, final int stufe, final long zeitStempel) {
		NaesseStufeTest.sendeZustand(objekt, "FahrBahnOberFlächenZustand", NaesseStufeTest.ddFbofZustand, stufe,
				zeitStempel);
	}

	/**
	 * Sendet einen DS mit Niederschlagsart
	 *
	 * @param objekt
	 *            Der Sensor
	 * @param stufe
	 *            Die Stufe
	 * @param zeitStempel
	 *            Der ZeitStempels
	 */
	private static void sendeNiederschlagsArt(final SystemObject objekt, final int stufe, final long zeitStempel) {
		NaesseStufeTest.sendeZustand(objekt, "NiederschlagsArt", NaesseStufeTest.ddNieArt, stufe, zeitStempel);
	}

	/**
	 * Sendet einen allgemeinen DS mit Zustand (int) Wert
	 *
	 * @param objekt
	 *            SystemObjekt
	 * @param attribut
	 *            Attributname
	 * @param datenBeschreibung
	 *            Datenbeschreibung
	 * @param wert
	 *            Wert
	 * @param zeitStempel
	 *            Zeitstempel
	 */
	private static void sendeZustand(final SystemObject objekt, final String attribut,
			final DataDescription datenBeschreibung, final int wert, final long zeitStempel) {
		final Data data = NaesseStufeTest.dav.createData(datenBeschreibung.getAttributeGroup());
		final String att = attribut;

		data.getTimeValue("T").setMillis(0);
		data.getItem(att).getUnscaledValue("Wert").set(wert);

		data.getItem(att).getItem("Status").getItem("Erfassung").getUnscaledValue("NichtErfasst").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMax").set(0);
		data.getItem(att).getItem("Status").getItem("PlFormal").getUnscaledValue("WertMin").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Implausibel").set(0);
		data.getItem(att).getItem("Status").getItem("MessWertErsetzung").getUnscaledValue("Interpoliert").set(0);
		data.getItem(att).getItem("Güte").getUnscaledValue("Index").set(1000);
		data.getItem(att).getItem("Güte").getUnscaledValue("Verfahren").set(0);

		final ResultData result = new ResultData(objekt, datenBeschreibung, zeitStempel, data);
		try {
			NaesseStufeTest.dav.sendData(result);
		} catch (final Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Konstanten, die als Abkuerzungen benutzt werden
	 */
	private static final NIStufe NI0 = NIStufe.NI_STUFE0;
	private static final NIStufe NI1 = NIStufe.NI_STUFE1;
	private static final NIStufe NI2 = NIStufe.NI_STUFE2;
	private static final NIStufe NI3 = NIStufe.NI_STUFE3;
	private static final NIStufe NI4 = NIStufe.NI_STUFE4;
	private static final NIStufe NINV = NIStufe.NI_WERT_NV;

	private static final WFDStufe WFD0 = WFDStufe.WFD_STUFE0;
	private static final WFDStufe WFD1 = WFDStufe.WFD_STUFE1;
	private static final WFDStufe WFD2 = WFDStufe.WFD_STUFE2;
	private static final WFDStufe WFD3 = WFDStufe.WFD_STUFE3;
	private static final WFDStufe WFDNV = WFDStufe.WFD_WERT_NV;

	private static final NSStufe NS0 = NSStufe.NS_TROCKEN;
	private static final NSStufe NS1 = NSStufe.NS_NASS1;
	private static final NSStufe NS2 = NSStufe.NS_NASS2;
	private static final NSStufe NS3 = NSStufe.NS_NASS3;
	private static final NSStufe NS4 = NSStufe.NS_NASS4;
	private static final NSStufe NSNV = NSStufe.NS_WERT_NE;

	/**
	 * Testfall 1 - geht durch die ganze Tabelle
	 */
	@Test
	public void testGanzeTablle() {
		final long sleepTime = 100;
		final int n = 50;

		NaesseStufeTest.ausgabe = new NSStufe[50];
		final NSStufe[] tabelle = new NSStufe[] { NaesseStufeTest.NS0, NaesseStufeTest.NS0, NaesseStufeTest.NS1,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS0, NaesseStufeTest.NS1, NaesseStufeTest.NS1,
				NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS4, NaesseStufeTest.NS1, NaesseStufeTest.NS2,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS4, NaesseStufeTest.NS2,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS3, NaesseStufeTest.NS4,
				NaesseStufeTest.NS3, NaesseStufeTest.NS0, NaesseStufeTest.NS1, NaesseStufeTest.NS2, NaesseStufeTest.NS3,
				NaesseStufeTest.NS4, NaesseStufeTest.NSNV };
		NaesseStufeTest.ausgabeZeitStempel = new long[n];

		final NIStufe[] niStufe = new NIStufe[] { NaesseStufeTest.NI0, NaesseStufeTest.NI1, NaesseStufeTest.NI2,
				NaesseStufeTest.NI3, NaesseStufeTest.NI4, NaesseStufeTest.NINV };
		final WFDStufe[] wfdStufe = new WFDStufe[] { NaesseStufeTest.WFD0, NaesseStufeTest.WFD1, NaesseStufeTest.WFD2,
				NaesseStufeTest.WFD3, NaesseStufeTest.WFDNV };

		NaesseStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(NaesseStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		long zeitStempel = System.currentTimeMillis() - (120 * 60 * 1000);
		final long delta = 5 * 60 * 1000;

		int k = 0, m = 0;
		int iNS1, iNS2;
		NaesseStufeTest.ausgabeIndex = 0;
		for (int i = 0; i < 5; i++) {

			for (int j = 0; j < 6; j++) {
				NaesseStufeTest.ausgabeZeitStempel[k] = zeitStempel;
				NaesseStufeTest.ausgabe[k] = tabelle[m++];
				if (k > 0) {
					iNS1 = NaesseStufe.getStufe(NaesseStufeTest.ausgabe[k]);
					iNS2 = NaesseStufe.getStufe(NaesseStufeTest.ausgabe[k - 1]);
					if ((wfdStufe[i] == NaesseStufeTest.WFDNV) && (((iNS2 - iNS1) > 1) && (iNS1 != -1))) {
						NaesseStufeTest.ausgabeZeitStempel[k] += NaesseStufeTest.ABTR_INTERVALL;
					}
					while (((iNS2 - iNS1) > 1) && (iNS1 != -1)) {
						NaesseStufeTest.ausgabe[k + 1] = NaesseStufeTest.ausgabe[k];
						NaesseStufeTest.ausgabe[k] = NaesseStufe.getStufe(--iNS2);
						NaesseStufeTest.ausgabeZeitStempel[k + 1] = NaesseStufeTest.ausgabeZeitStempel[k]
								+ NaesseStufeTest.ABTR_INTERVALL;
						k++;
					}
				}
				NaesseStufeTest.warten = true;
				sendeNiStufe(NaesseStufeTest.niSensor, niStufe[j], zeitStempel);
				sendeWfdStufe(NaesseStufeTest.wfdSensor, wfdStufe[i], zeitStempel);
				NaesseStufeTest.sendeFbofZustand(NaesseStufeTest.fbofZustandSensor, NaesseStufeTest.FBOF_TROCKEN,
						zeitStempel);
				NaesseStufeTest.sendeNiederschlagsArt(NaesseStufeTest.naSensor, NaesseStufeTest.NART_KEIN, zeitStempel);

				try {
					Thread.sleep(sleepTime);
				} catch (final Exception e) {
				}
				k++;
				zeitStempel += delta;
			}
		}
		try {
			while (NaesseStufeTest.warten) {
				Thread.sleep(sleepTime);
			}
		} catch (final Exception e) {
		}
		NaesseStufeTest.hauptModul.disconnect();
		NaesseStufeTest.hauptModul = null;
		NaesseStufeTest.ausgabe = null;

	}

	/**
	 * Testfall 2 - wie Test 1 nur der Fahrbahnoberflachezustand sich aendert
	 */
	@Test
	public void testFbofZustand() {
		final long sleepTime = 100;
		final int n = 50;

		NaesseStufeTest.ausgabe = new NSStufe[50];
		final NSStufe[] tabelle = new NSStufe[] { NaesseStufeTest.NS0, NaesseStufeTest.NS0, NaesseStufeTest.NS1,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS0, NaesseStufeTest.NS1, NaesseStufeTest.NS1,
				NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS4, NaesseStufeTest.NS1, NaesseStufeTest.NS2,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS4, NaesseStufeTest.NS2,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS3, NaesseStufeTest.NS4,
				NaesseStufeTest.NS3, NaesseStufeTest.NS0, NaesseStufeTest.NS1, NaesseStufeTest.NS2, NaesseStufeTest.NS3,
				NaesseStufeTest.NS4, NSStufe.NS_WERT_NE };
		NaesseStufeTest.ausgabeZeitStempel = new long[n];

		final NIStufe[] niStufe = new NIStufe[] { NaesseStufeTest.NI0, NaesseStufeTest.NI1, NaesseStufeTest.NI2,
				NaesseStufeTest.NI3, NaesseStufeTest.NI4, NaesseStufeTest.NINV };
		final WFDStufe[] wfdStufe = new WFDStufe[] { NaesseStufeTest.WFD0, NaesseStufeTest.WFD1, NaesseStufeTest.WFD2,
				NaesseStufeTest.WFD3, NaesseStufeTest.WFDNV };

		NaesseStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(NaesseStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		long zeitStempel = System.currentTimeMillis() - (120 * 60 * 1000);
		final long delta = 5 * 60 * 1000;

		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		int k = 0, m = 0;
		int iNS1, iNS2;
		boolean unbestimmbar = false;
		NaesseStufeTest.ausgabeIndex = 0;
		for (int i = 0; i < 5; i++) {

			for (int j = 0; j < 6; j++) {
				if ((((i + j) % 5) > 0) && (((i + j) % 5) < 3)) {
					NaesseStufeTest.sendeFbofZustand(NaesseStufeTest.fbofZustandSensor, NaesseStufeTest.FBOF_EIS,
							zeitStempel);
					unbestimmbar = true;
				} else {
					NaesseStufeTest.sendeFbofZustand(NaesseStufeTest.fbofZustandSensor, NaesseStufeTest.FBOF_TROCKEN,
							zeitStempel);
					unbestimmbar = false;
				}

				NaesseStufeTest.ausgabeZeitStempel[k] = zeitStempel;
				NaesseStufeTest.ausgabe[k] = tabelle[m++];

				if (unbestimmbar) {
					NaesseStufeTest.ausgabe[k] = NSStufe.NS_WERT_NE;
				}
				if ((k > 0) && !unbestimmbar) {
					iNS1 = NaesseStufe.getStufe(NaesseStufeTest.ausgabe[k]);
					iNS2 = NaesseStufe.getStufe(NaesseStufeTest.ausgabe[k - 1]);
					if ((wfdStufe[i] == NaesseStufeTest.WFDNV) && (((iNS2 - iNS1) > 1) && (iNS1 != -1))) {
						NaesseStufeTest.ausgabeZeitStempel[k] += NaesseStufeTest.ABTR_INTERVALL;
					}
					while (((iNS2 - iNS1) > 1) && (iNS1 != -1)) {
						NaesseStufeTest.ausgabe[k + 1] = NaesseStufeTest.ausgabe[k];
						NaesseStufeTest.ausgabe[k] = NaesseStufe.getStufe(--iNS2);
						NaesseStufeTest.ausgabeZeitStempel[k + 1] = NaesseStufeTest.ausgabeZeitStempel[k]
								+ NaesseStufeTest.ABTR_INTERVALL;
						k++;
					}
				}
				NaesseStufeTest.warten = true;
				sendeNiStufe(NaesseStufeTest.niSensor, niStufe[j], zeitStempel);
				sendeWfdStufe(NaesseStufeTest.wfdSensor, wfdStufe[i], zeitStempel);
				NaesseStufeTest.sendeNiederschlagsArt(NaesseStufeTest.naSensor, NaesseStufeTest.NART_KEIN, zeitStempel);

				try {
					Thread.sleep(sleepTime);
				} catch (final Exception e) {
				}
				k++;
				zeitStempel += delta;
			}
		}
		try {
			while (NaesseStufeTest.warten) {
				Thread.sleep(5 * sleepTime);
			}
		} catch (final Exception e) {
		}
		NaesseStufeTest.hauptModul.disconnect();
		NaesseStufeTest.hauptModul = null;
		NaesseStufeTest.ausgabe = null;
	}

	/**
	 * Testfall 3 - testet die verzoegerung bei faehlenden WFD Daten
	 */
	@Test
	public void testVerzoegerung() {
		final long sleepTime = 100;
		NaesseStufeTest.ausgabe = new NSStufe[] { NaesseStufeTest.NS4, NaesseStufeTest.NS3, NaesseStufeTest.NS3,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS4, NaesseStufeTest.NS3, NaesseStufeTest.NS2,
				NaesseStufeTest.NS1, NaesseStufeTest.NS0 };
		NaesseStufeTest.ausgabeZeitStempel = new long[NaesseStufeTest.ausgabe.length];

		final NIStufe[] niStufe = new NIStufe[] { NaesseStufeTest.NI4, NaesseStufeTest.NI3, NaesseStufeTest.NI2,
				NaesseStufeTest.NI1, NaesseStufeTest.NI0 };

		NaesseStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(NaesseStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		long zeitStempel = System.currentTimeMillis() - (120 * 60 * 1000);
		final long delta = 5 * 60 * 1000;

		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		NaesseStufeTest.ausgabeIndex = 0;

		for (int i = 0; i < 5; i++) {
			NaesseStufeTest.ausgabeZeitStempel[i] = zeitStempel;
			NaesseStufeTest.warten = true;
			sendeNiStufe(NaesseStufeTest.niSensor, niStufe[i], zeitStempel);
			sendeWfdStufe(NaesseStufeTest.wfdSensor, NaesseStufeTest.WFD3, zeitStempel);
			NaesseStufeTest.sendeFbofZustand(NaesseStufeTest.fbofZustandSensor, NaesseStufeTest.FBOF_TROCKEN,
					zeitStempel);
			NaesseStufeTest.sendeNiederschlagsArt(NaesseStufeTest.naSensor, NaesseStufeTest.NART_KEIN, zeitStempel);

			try {
				Thread.sleep(sleepTime);
			} catch (final Exception e) {
			}
			zeitStempel += delta;
		}

		for (int i = 5; i < 10; i++) {
			NaesseStufeTest.ausgabeZeitStempel[i] = zeitStempel;
			if (i > 5) {
				NaesseStufeTest.ausgabeZeitStempel[i] += NaesseStufeTest.ABTR_INTERVALL;
			}
			NaesseStufeTest.warten = true;
			sendeNiStufe(NaesseStufeTest.niSensor, niStufe[i - 5], zeitStempel);
			sendeWfdStufe(NaesseStufeTest.wfdSensor, NaesseStufeTest.WFDNV, zeitStempel);
			NaesseStufeTest.sendeFbofZustand(NaesseStufeTest.fbofZustandSensor, NaesseStufeTest.FBOF_TROCKEN,
					zeitStempel);
			NaesseStufeTest.sendeNiederschlagsArt(NaesseStufeTest.naSensor, NaesseStufeTest.NART_KEIN, zeitStempel);

			try {
				Thread.sleep(sleepTime);
			} catch (final Exception e) {
			}
			zeitStempel += delta;

		}

		try {
			while (NaesseStufeTest.warten) {
				Thread.sleep(5 * sleepTime);
			}
		} catch (final Exception e) {
		}
		NaesseStufeTest.hauptModul.disconnect();
		NaesseStufeTest.hauptModul = null;
		NaesseStufeTest.ausgabe = null;
	}

	/**
	 * Testfall 4 - wie Test 1 nur die Niederschlagsart sich aendert
	 */
	@Test
	public void testNieArt() {
		final long sleepTime = 100;
		final int n = 50;

		NaesseStufeTest.ausgabe = new NSStufe[50];
		final NSStufe[] tabelle = new NSStufe[] { NaesseStufeTest.NS0, NaesseStufeTest.NS0, NaesseStufeTest.NS1,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS0, NaesseStufeTest.NS1, NaesseStufeTest.NS1,
				NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS4, NaesseStufeTest.NS1, NaesseStufeTest.NS2,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS4, NaesseStufeTest.NS2,
				NaesseStufeTest.NS2, NaesseStufeTest.NS2, NaesseStufeTest.NS3, NaesseStufeTest.NS3, NaesseStufeTest.NS4,
				NaesseStufeTest.NS3, NaesseStufeTest.NS0, NaesseStufeTest.NS1, NaesseStufeTest.NS2, NaesseStufeTest.NS3,
				NaesseStufeTest.NS4, NSStufe.NS_WERT_NE };
		NaesseStufeTest.ausgabeZeitStempel = new long[n];

		final NIStufe[] niStufe = new NIStufe[] { NaesseStufeTest.NI0, NaesseStufeTest.NI1, NaesseStufeTest.NI2,
				NaesseStufeTest.NI3, NaesseStufeTest.NI4, NaesseStufeTest.NINV };
		final WFDStufe[] wfdStufe = new WFDStufe[] { NaesseStufeTest.WFD0, NaesseStufeTest.WFD1, NaesseStufeTest.WFD2,
				NaesseStufeTest.WFD3, NaesseStufeTest.WFDNV };

		NaesseStufeTest.hauptModul = new VerwaltungAufbereitungUFDTest();
		final String[] connArgs = new String[DAVTest.CON_DATA.length];
		for (int i = 0; i < DAVTest.CON_DATA.length; i++) {
			connArgs[i] = DAVTest.CON_DATA[i];
		}
		StandardApplicationRunner.run(NaesseStufeTest.hauptModul, connArgs);
		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		long zeitStempel = System.currentTimeMillis() - (120 * 60 * 1000);
		final long delta = 5 * 60 * 1000;

		try {
			Thread.sleep(5 * sleepTime);
		} catch (final Exception e) {
		}

		int k = 0, m = 0;
		int iNS1, iNS2;
		boolean unbestimmbar = false;
		NaesseStufeTest.ausgabeIndex = 0;
		for (int i = 0; i < 5; i++) {

			for (int j = 0; j < 6; j++) {
				if ((((i + j) % 5) > 0) && (((i + j) % 5) < 3)) {
					NaesseStufeTest.sendeNiederschlagsArt(NaesseStufeTest.naSensor, NaesseStufeTest.NART_SCHNEE,
							zeitStempel);
					unbestimmbar = true;
				} else {
					NaesseStufeTest.sendeNiederschlagsArt(NaesseStufeTest.naSensor, NaesseStufeTest.NART_KEIN,
							zeitStempel);
					unbestimmbar = false;
				}

				NaesseStufeTest.ausgabeZeitStempel[k] = zeitStempel;
				NaesseStufeTest.ausgabe[k] = tabelle[m++];

				if (unbestimmbar) {
					NaesseStufeTest.ausgabe[k] = NSStufe.NS_WERT_NE;
				}
				if ((k > 0) && !unbestimmbar) {
					iNS1 = NaesseStufe.getStufe(NaesseStufeTest.ausgabe[k]);
					iNS2 = NaesseStufe.getStufe(NaesseStufeTest.ausgabe[k - 1]);
					if ((wfdStufe[i] == NaesseStufeTest.WFDNV) && (((iNS2 - iNS1) > 1) && (iNS1 != -1))) {
						NaesseStufeTest.ausgabeZeitStempel[k] += NaesseStufeTest.ABTR_INTERVALL;
					}
					while (((iNS2 - iNS1) > 1) && (iNS1 != -1)) {
						NaesseStufeTest.ausgabe[k + 1] = NaesseStufeTest.ausgabe[k];
						NaesseStufeTest.ausgabe[k] = NaesseStufe.getStufe(--iNS2);
						NaesseStufeTest.ausgabeZeitStempel[k + 1] = NaesseStufeTest.ausgabeZeitStempel[k]
								+ NaesseStufeTest.ABTR_INTERVALL;
						k++;
					}
				}
				NaesseStufeTest.warten = true;
				sendeNiStufe(NaesseStufeTest.niSensor, niStufe[j], zeitStempel);
				sendeWfdStufe(NaesseStufeTest.wfdSensor, wfdStufe[i], zeitStempel);
				NaesseStufeTest.sendeFbofZustand(NaesseStufeTest.fbofZustandSensor, NaesseStufeTest.FBOF_TROCKEN,
						zeitStempel);

				try {
					Thread.sleep(sleepTime);
				} catch (final Exception e) {
				}
				k++;
				zeitStempel += delta;
			}
		}
		try {
			while (NaesseStufeTest.warten) {
				Thread.sleep(5 * sleepTime);
			}
		} catch (final Exception e) {
		}
		NaesseStufeTest.hauptModul.disconnect();
		NaesseStufeTest.hauptModul = null;
		NaesseStufeTest.ausgabe = null;
	}

	@Override
	void infoVerzoegerung(final int stufe) {
		System.out.println(" ---- Verzoegerung StufeVon: " + stufe);
	}

}
