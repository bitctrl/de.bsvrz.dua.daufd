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

package de.bsvrz.dua.daufd;

import java.util.Collection;
import java.util.LinkedList;

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
import de.bsvrz.sys.funclib.bitctrl.dua.DUAInitialisierungsException;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Eine generische UfdsModul Parametrierung Klasse, parametriert die
 * Attributgruppen Klasifizierung und Aggregation, erwartet, dass die
 * Parametrierung der Parametrierung schon gemacht wurde
 *
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class UfdsKlassifizierungParametrierung implements ClientSenderInterface {

	private static final Debug LOGGER = Debug.getLogger();
	/**
	 * Datenbeschreibung fuer die Klasifizierung Daten
	 */
	private DataDescription ddKlassifizierung = null;
	/**
	 * Datenbeschreibung fuer die Aggregation Daten
	 */
	private DataDescription ddAggregation = null;

	/**
	 * Verbindung zum DAV
	 */
	private ClientDavInterface davConnection = null;
	/**
	 * Aspekt fuer Parametrierung
	 */
	private static final String ASP_PARAM_VORGABE = "asp.parameterVorgabe";
	/**
	 * Typ des Objektes
	 */
	private String typ = null;
	/**
	 * Attributgruppe des Objektes fuer Klassifizierung
	 */
	private String atgKlassifizierung = null;
	/**
	 * Attributgruppe des Objektes fuer Aggregation
	 */
	private String atgAggregation = null;
	/**
	 * Attribut zur Parametrierung fuer Klassifizierung
	 */
	private String attKlassifizierung = null;
	/**
	 * Untere Grenzwerte
	 */
	private double[] stufeVon = null;
	/**
	 * Obere Grenzwerte
	 */
	private double[] stufeBis = null;
	/**
	 * Koefizient fuer Glaettung
	 */
	private final double b0;
	/**
	 * Koefizient fuer Glaettung
	 */
	private final double fb;

	/**
	 * Standardkonstruktor
	 *
	 * @param typ
	 *            Typ des parametrierendes Objekts
	 * @param atgKlassifizierung
	 *            Attributgruppe des parametrierendes Objekts
	 * @param attKlasifizierung
	 *            Parametrierendes AttributArray
	 * @param stufeVon
	 *            untere Grenzwerte
	 * @param stufeBis
	 *            obere Grenzwerte
	 * @param b0
	 *            b_0 Koefizient
	 * @param fb
	 *            f_b Koefizient
	 * @throws DUAInitialisierungsException
	 *             Bei fehlerhafen Eingabe der Stufen
	 */
	public UfdsKlassifizierungParametrierung(final String typ, final String atgKlassifizierung,
			final String attKlasifizierung, final String atgAggregation, final double[] stufeVon,
			final double[] stufeBis, final double b0, final double fb) throws DUAInitialisierungsException {
		this.typ = typ;
		this.atgKlassifizierung = atgKlassifizierung;
		this.attKlassifizierung = attKlasifizierung;
		this.atgAggregation = atgAggregation;

		if ((stufeVon == null) || (stufeBis == null) || (stufeVon.length != stufeBis.length)) {
			throw new DUAInitialisierungsException("StufeVon oder StufeBis nicht korrekt eingegeben");
		}

		this.stufeVon = stufeVon;
		this.stufeBis = stufeBis;
		this.fb = fb;
		this.b0 = b0;
	}

	/**
	 * Parametriert die Klassifizierung alle Objekte vom Typ TYP mit
	 * Attributsgruppe ATG Setzt die Grenzwerte der Klasifizierungsstufen
	 *
	 * @param dav
	 *            Verbindung zum Datenverteiler
	 * @param konfBereiche
	 *            konfigurationsbereiche in denen alle Objekte parametriert
	 *            werden sollen
	 */
	public void parametriereUfds(final ClientDavInterface dav, final Collection<ConfigurationArea> konfBereiche) {
		this.davConnection = dav;

		ddKlassifizierung = new DataDescription(davConnection.getDataModel().getAttributeGroup(atgKlassifizierung),
				davConnection.getDataModel().getAspect(UfdsKlassifizierungParametrierung.ASP_PARAM_VORGABE));

		ddAggregation = new DataDescription(davConnection.getDataModel().getAttributeGroup(atgAggregation),
				davConnection.getDataModel().getAspect(UfdsKlassifizierungParametrierung.ASP_PARAM_VORGABE));

		final Collection<SystemObjectType> sotMenge = new LinkedList<SystemObjectType>();
		sotMenge.add(davConnection.getDataModel().getType(typ));

		final Collection<SystemObject> ufdsObjekte = davConnection.getDataModel().getObjects(konfBereiche, sotMenge,
				ObjectTimeSpecification.valid());

		if (ufdsObjekte == null) {
			UfdsKlassifizierungParametrierung.LOGGER
			.error("Kein Objekt vom " + typ + " in den KonfigurationsBeriechen :" + konfBereiche);
			System.exit(-1);
		}

		try {
			davConnection.subscribeSender(this, ufdsObjekte, ddKlassifizierung, SenderRole.sender());
			// Der DAV ist zu langsam und antwortet mit
			// "Sendeanmeldung nocht nicht bestaettigt"
			Thread.sleep(100);
			davConnection.subscribeSender(this, ufdsObjekte, ddAggregation, SenderRole.sender());
			// Der DAV ist zu langsam und antwortet mit
			// "Sendeanmeldung nocht nicht bestaettigt"
			Thread.sleep(100);
		} catch (final Exception e) {
			UfdsKlassifizierungParametrierung.LOGGER.error(
					"Fehler bei Anmeldung für Klassifizierung der Objekte vom Typ " + typ + ":" + e.getMessage());
			e.printStackTrace();
		}

		// Der dataRequest wird automatisch vom Datenverteiler getriggert
		// for(SystemObject so : ufdsObjekte ) {
		// dataRequest(so, DD_KLASIFIZIERUNG, START_SENDING);
		// dataRequest(so, DD_AGGREGATION, START_SENDING);
		// }

		davConnection.unsubscribeSender(this, ufdsObjekte, ddKlassifizierung);

	}

	@Override
	public void dataRequest(final SystemObject object, final DataDescription dataDescription, final byte state) {
		if (dataDescription.getAttributeGroup().getPid().equals(atgKlassifizierung)
				&& (state == ClientSenderInterface.START_SENDING)) {

			final Data datei = davConnection
					.createData(davConnection.getDataModel().getAttributeGroup(atgKlassifizierung));

			final Data.Array stufen = datei.getArray(attKlassifizierung);
			stufen.setLength(stufeBis.length);

			for (int i = 0; i < stufeBis.length; i++) {
				stufen.getItem(i).getScaledValue("von").set(stufeVon[i]);
				stufen.getItem(i).getScaledValue("bis").set(stufeBis[i]);
			}
			final ResultData resDatei = new ResultData(object, ddKlassifizierung, System.currentTimeMillis(), datei);

			try {
				davConnection.sendData(resDatei);
				System.out.println("Objekt " + object.getPid() + " Atg: " + atgKlassifizierung + " parametriert ");
			} catch (final Exception e) {
				UfdsKlassifizierungParametrierung.LOGGER
				.error("Fehler bei Sendung von Daten für Klassifizierung des Objektes :" + object.getPid()
				+ "\n Fehler:" + e.getMessage());
				e.printStackTrace();
			}
		} else if (dataDescription.getAttributeGroup().getPid().equals(atgAggregation)
				&& (state == ClientSenderInterface.START_SENDING)) {

			final Data datei = davConnection.createData(davConnection.getDataModel().getAttributeGroup(atgAggregation));

			datei.getScaledValue("b0").set(b0);
			datei.getScaledValue("fb").set(fb);

			final ResultData resDatei = new ResultData(object, ddAggregation, System.currentTimeMillis(), datei);

			try {
				davConnection.sendData(resDatei);
				System.out.println("Objekt " + object.getPid() + " Atg: " + atgAggregation + " parametriert ");
			} catch (final Exception e) {
				UfdsKlassifizierungParametrierung.LOGGER
				.error("Fehler bei Sendung von Daten für Aggregation des Objektes :" + object.getPid()
				+ "\n Fehler:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public boolean isRequestSupported(final SystemObject object, final DataDescription dataDescription) {
		return false;
	}
}
