/**
 * Segment 4 Datenübernahme und Aufbereitung (DUA), SWE 4.3 Pl-Prüfung logisch UFD
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

import java.util.Random;

import de.bsvrz.dav.daf.main.ClientDavInterface;
import de.bsvrz.sys.funclib.application.StandardApplication;
import de.bsvrz.sys.funclib.application.StandardApplicationRunner;
import de.bsvrz.sys.funclib.bitctrl.app.Pause;
import de.bsvrz.sys.funclib.commandLineArgs.ArgumentList;

/**
 * Stellt eine Datenverteiler-Verbindung
 * zur Verfügung.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 * 
 */
public class DAVTest {

	/**
	 * Verbindungsdaten
	 */
	public static final String[] CON_DATA = new String[] {
			"-datenverteiler=localhost:8083",  
			"-benutzer=Tester", 
			"-authentifizierung=..\\..\\..\\skripte-dosshell\\passwd", 
			"-debugLevelStdErrText=CONFIG", 
			"-debugLevelFileText=CONFIG",
			"-KonfigurationsBereichsPid=kb.daUfdTest" };

	/**
	 * Verbindung zum Datenverteiler
	 */
	protected static ClientDavInterface VERBINDUNG = null;

	/**
	 * Randomizer
	 */
	public static Random R = new Random(System.currentTimeMillis());

	
	/**
	 * Erfragt bzw. initialisiert eine
	 * Datenverteiler-Verbindung
	 * 
	 * @return die Datenverteiler-Verbindung
	 * @throws Exception falls die Verbindung nicht
	 * hergestellt werden konnte
	 */
	public static final ClientDavInterface getDav()
	throws Exception {
		
		if(VERBINDUNG == null) {
			StandardApplicationRunner.run(new StandardApplication() {
	
				public void initialize(ClientDavInterface connection)
						throws Exception {
					DAVTest.VERBINDUNG = connection;
				}
	
				public void parseArguments(ArgumentList argumentList)
						throws Exception {
					//
				}
	
			}, CON_DATA);
		}
		
		return VERBINDUNG;
	}
	
	
	/**
	 * Wartet bis zu dem übergebenen Zeitpunkt
	 * 
	 * @param zeitStempel ein Zeitstempel in ms
	 */
	public static final void warteBis(final long zeitStempel){
		while(System.currentTimeMillis() <= zeitStempel){
			Pause.warte(5L);
		}
	}


	/**
	 * Erfragt einen Array mit zufälligen Zahlen von
	 * 0 bis <code>anzahl</code>. Jede Zahl darf nur 
	 * einmal im Array vorkommen.
	 * 
	 * @param anzahl die Obergrenze
	 * @return Array mit zufälligen Zahlen von
	 * 0 bis <code>anzahl</code>
	 */
	public static final int[] getZufaelligeZahlen(int anzahl){
		int belegt = 0;
		int[] zahlen = new int[anzahl];
		for(int i = 0; i<anzahl; i++)zahlen[i] = -1;
		
		while(belegt < anzahl){
			int index = R.nextInt(anzahl);
			if(zahlen[index] == -1)zahlen[index] = belegt++;
		}
		
		return zahlen;
	}

}
