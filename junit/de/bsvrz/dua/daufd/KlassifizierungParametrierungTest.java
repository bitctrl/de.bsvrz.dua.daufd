/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.6 Abfrage Pufferdaten
 * Copyright (C) 2007 BitCtrl Systems GmbH 
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
import de.bsvrz.dav.daf.main.config.ConfigurationArea;
import de.bsvrz.dua.daufd.stufenaesse.NaesseStufeTest;
import de.bsvrz.dua.daufd.stufeni.NiederschagIntensitaetStufeTest;
import de.bsvrz.dua.daufd.stufesw.SichtWeitenStufeTest;
import de.bsvrz.dua.daufd.stufewfd.WasserFilmDickenStufeTest;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;
import de.bsvrz.sys.funclib.debug.Debug;

/**
 * Ermoeglicht alle Ufds*Stufe Module zu Parametrieren
 * 
 * @author BitCtrl Systems GmbH, Bachraty
 *
 */
public class KlassifizierungParametrierungTest implements StandardApplication {
	
	/**
	 * Verbindungsdaten
	 */
	private static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083",  
			"-benutzer=Tester", 
			"-authentifizierung=c:\\passwd", 
			"-debugLevelStdErrText=WARNING", 
			"-debugLevelFileText=WARNING",
			"-kb=kb.UFD_Konfig_B27" }; 
	
	/**
	 * Der Logger
	 */
	private Debug LOGGER = Debug.getLogger(); 
	/**
	 * Verbindung zum DAV
	 */
	private  ClientDavInterface DAV = null;
	/**
	 * Die KOnfigurationsbereiche in dem die Objekte Parametriert werden
	 */
	private Collection<ConfigurationArea> konfBereiche = null;
	/**
	 * Die KOnfigurationsbereiche in dem die Objekte Parametriert werden
	 */
	private String [] strKonfBereiche = null;
	
	/**
	 * Haupmethode
	 * @param args Aufrufsargumente
	 */
	public static void main(String args[]) {
		
		KlassifizierungParametrierungTest test = new KlassifizierungParametrierungTest();
		StandardApplicationRunner.run(test, CON_DATA);
		test.disconnect();
		
	}

	/**
	 * {@inheritDoc}
	 */
	public void initialize(ClientDavInterface connection) throws Exception {
		this.DAV = connection;
		konfBereiche = new LinkedList<ConfigurationArea>();
		
		for(String kb : strKonfBereiche) {
			ConfigurationArea ca = connection.getDataModel().getConfigurationArea(kb);
			if(ca!=null)
				konfBereiche.add(ca);
		}
		
		NiederschagIntensitaetStufeTest test1 = new NiederschagIntensitaetStufeTest();		
		test1.ParametriereUfds(DAV, konfBereiche);
	//	 Der DAV ist zu langsam, er schafft nicht so  viele telegramme zu bearbeiten
		Thread.sleep(800);
		WasserFilmDickenStufeTest test2 = new WasserFilmDickenStufeTest();
		test2.ParametriereUfds(DAV, konfBereiche);
		Thread.sleep(800);
		SichtWeitenStufeTest test3 = new SichtWeitenStufeTest();
		test3.ParametriereUfds(DAV, konfBereiche);
		Thread.sleep(800);
		NaesseStufeTest test4 = new NaesseStufeTest();
		test4.ParametriereUfds(DAV, konfBereiche);
	}

	/**
	 * {@inheritDoc}
	 */
	public void parseArguments(ArgumentList argumentList) throws Exception {
		strKonfBereiche = argumentList.fetchArgument("-kb").asString().split(",");
	}
	
	/**
	 * Abmeldung am Ende
	 */
	public void disconnect() {
		DAV.disconnect(false, "");
	}
}
