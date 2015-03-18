/*
 * Segment 4 Daten�bernahme und Aufbereitung (DUA), SWE 4.8 Datenaufbereitung UFD
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
 * Wei�enfelser Stra�e 67<br>
 * 04229 Leipzig<br>
 * Phone: +49 341-490670<br>
 * mailto: info@bitctrl.de
 */

package de.bsvrz.dua.daufd.hysterese;

import java.util.ArrayList;
import java.util.List;

/**
 * Das Modul Hysterese stellt eine Berechnung eines diskreten Wertes
 * (Zustandswert) auf Basis von quasi-kontinuierlichen Eingangswerten �ber eine
 * Hystereseabbildung zur Verf�gung.
 *
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class Hysterese {

	/**
	 * Menge der Intervall-Anf�nge
	 */
	private double[] vonMenge = null;

	/**
	 * Menge der Intervall-Enden
	 */
	private double[] bisMenge = null;

	/**
	 * die Hysterese-Stufe, die zuletzt errechnet wurde
	 */
	private int stufeAlt = -1;

	/**
	 * Initialisiert dieses Hysterese mit <code>double</code>-Intervallen
	 *
	 * @param vonMenge1
	 *            Menge der Intervall-Anf�nge
	 * @param bisMenge1
	 *            Menge der Intervall-Enden
	 * @throws HystereseException
	 *             wenn die beiden �bergebenen Mengen leer sind, oder nicht die
	 *             gleiche Anzahl an Elementen enthalten, oder zwischen den
	 *             Intervallen L�cken existieren, etc..
	 */
	public final void initialisiere(final double[] vonMenge1,
			final double[] bisMenge1) throws HystereseException {
		if ((vonMenge1 == null) || (bisMenge1 == null)) {
			throw new NullPointerException("Eine Wertemenge ist <<null>>"); //$NON-NLS-1$
		}
		if (bisMenge1.length != bisMenge1.length) {
			throw new HystereseException(
					"Menge der VON-Begrenzer und BIS-Begrenzer" + //$NON-NLS-1$
							" muss gleich gross sein."); //$NON-NLS-1$
		}

		this.vonMenge = new double[vonMenge1.length];
		this.bisMenge = new double[bisMenge1.length];
		for (int i = 0; i < vonMenge1.length; i++) {
			// if(vonMenge1[i] >= bisMenge1[i]){
			//				throw new HystereseException("Intervall Nr." + i + " hat negative Gr��e: [" //$NON-NLS-1$ //$NON-NLS-2$
			//						+ vonMenge1[i] + ", " + bisMenge1[i] + "["); //$NON-NLS-1$ //$NON-NLS-2$
			// }
			// if(i < vonMenge1.length - 1){
			// if(vonMenge1[i + 1] <= vonMenge1[i]){
			//					throw new HystereseException("Zwischen zwei aufeinander folgenden" + //$NON-NLS-1$
			//							" Intervall-Anf�ngen muss ein positiver Abstand liegen:\n" + //$NON-NLS-1$
			//							"Intervall Nr." + i + ": [" + + vonMenge1[i] + ", " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//							+ bisMenge1[i] + "[\n" +  //$NON-NLS-1$
			//							"Intervall Nr." + (i + 1) + ": [" + + vonMenge1[i + 1] +//$NON-NLS-1$ //$NON-NLS-2$
			//							", " + bisMenge1[i + 1] + "[\n");//$NON-NLS-1$ //$NON-NLS-2$
			// }
			// if(vonMenge1[i + 1] > bisMenge1[i]){
			//					throw new HystereseException("Zwischen zwei aufeinander folgenden" + //$NON-NLS-1$
			//							" Intervallen darf kein Abstand liegen:\n" + //$NON-NLS-1$
			//							"Intervall Nr." + i + ": [" + + vonMenge1[i] + ", " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			//							+ bisMenge1[i] + "[\n" +  //$NON-NLS-1$
			//							"Intervall Nr." + (i + 1) + ": [" + + vonMenge1[i + 1] +//$NON-NLS-1$ //$NON-NLS-2$
			//							", " + bisMenge1[i + 1] + "[\n");//$NON-NLS-1$ //$NON-NLS-2$
			// }
			// }
			this.vonMenge[i] = vonMenge1[i];
			this.bisMenge[i] = bisMenge1[i];
		}
	}

	/**
	 * Initialisiert dieses Hysterese mit <code>long</code>-Intervallen
	 *
	 * @param vonMenge1
	 *            Menge der Intervall-Anf�nge
	 * @param bisMenge1
	 *            Menge der Intervall-Enden
	 * @throws HystereseException
	 *             wenn die beiden �bergebenen Mengen leer sind, oder nicht die
	 *             gleiche Anzahl an Elementen enthalten, oder zwischen den
	 *             Intervallen L�cken existieren, etc..
	 */
	public final void initialisiere(final long[] vonMenge1,
			final long[] bisMenge1) throws HystereseException {
		if ((vonMenge1 == null) || (bisMenge1 == null)) {
			throw new NullPointerException("Eine Wertemenge ist <<null>>"); //$NON-NLS-1$
		}
		if (bisMenge1.length != bisMenge1.length) {
			throw new HystereseException(
					"Menge der VON-Begrenzer und BIS-Begrenzer" + //$NON-NLS-1$
							" muss gleich gross sein."); //$NON-NLS-1$
		}
		final double[] vonMengeDouble = new double[vonMenge1.length];
		final double[] bisMengeDouble = new double[bisMenge1.length];
		for (int i = 0; i < vonMenge1.length; i++) {
			vonMengeDouble[i] = vonMenge1[i];
			bisMengeDouble[i] = bisMenge1[i];
		}
		this.initialisiere(vonMengeDouble, bisMengeDouble);
	}

	/**
	 * Errechnet die Hysterese-Stufe eines bestimmten <code>double</code>
	 * -Wertes.
	 *
	 * @param wert
	 *            ein <code>double</code>-Wert
	 * @return die Hysterese-Stufe des �bergebenen <code>double</code>-Wertes
	 *         oder <code>-1</code>, wenn keine Hysterese-Stufe errechnet werden
	 *         konnte
	 */
	public final int getStufe(final double wert) {
		int stufe = -1;

		final List<Integer> intervalle = this.getIntervalle(wert);
		if (this.stufeAlt == -1) {
			/**
			 * Die momentane Hysteresestufe ist nicht bekannt oder es wurde noch
			 * nie eine Stufe ausgerechnet
			 */
			if (intervalle.size() == 1) {
				stufe = intervalle.get(0);
			} else if (intervalle.size() >= 2) {
					/**
					 * W�hle das Intervall, bei dem der Abstand des �bergebenen
				 * Wertes zum Mittelpunkt (des Intervalls) am kleinsten ist
					 */
					stufe = this.getBestesIntervall(intervalle, wert);
				} else {
					stufe = -1;
				}
		} else {
			/**
			 * Die momentane Hysteresestufe ist bekannt
			 */
			if (intervalle.size() == 1) {
				stufe = intervalle.get(0);
			} else if (intervalle.size() >= 2) {
					if (intervalle.contains(new Integer(this.stufeAlt))) {
						/**
						 * Wenn eine, der errechneten Hysterese-Stufen schon im
					 * letzten Schritt anlag, dann gebe einfach die letzte Stufe
					 * zur�ck
						 */
						stufe = this.stufeAlt;
					} else {
						/**
						 * W�hle das Intervall, bei dem der Abstand des �bergebenen
					 * Wertes zum Mittelpunkt (des Intervalls) am kleinsten ist
						 */
						stufe = this.getBestesIntervall(intervalle, wert);
					}
				} else {
					stufe = -1;
				}
		}

		this.stufeAlt = stufe;

		return stufe;
	}

	/**
	 * Errechnet die Hysterese-Stufe eines bestimmten <code>long</code>-Wertes.
	 *
	 * @param wert
	 *            ein <code>long</code>-Wert
	 * @return die Hysterese-Stufe des �bergebenen <code>long</code>-Wertes oder
	 *         <code>-1</code>, wenn keine Hysterese-Stufe errechnet werden
	 *         konnte
	 */
	public final int getStufe(final long wert) {
		return this.getStufe((double) wert);
	}

	/**
	 * Erfragt das Intervall (der �bergebenen Intervalle), bei dem der Abstand
	 * des �bergebenen Wertes zum Mittelpunkt (des Intervalls) am kleinsten ist
	 *
	 * @param intervalle
	 *            eine Liste mit Intervall-Indizes dieser Hysterese. Diese Liste
	 *            darf nicht leer sein!
	 * @param wert
	 *            ein Wert
	 * @return das Intervall, bei dem der Abstand des �bergebenen Wertes zum
	 *         Mittelpunkt (des Intervalls) am kleinsten ist
	 */
	private int getBestesIntervall(final List<Integer> intervalle,
			final double wert) {
		double aktuellerAbstandZumZentrumDesIntervalls = 1;
		int bestesIntervall = -1;

		for (final Integer intervall : intervalle) {
			final double abstandZumZentrumDummy = Math
					.abs(((wert - vonMenge[intervall]) / (bisMenge[intervall] - vonMenge[intervall])) - 0.5);
			if (abstandZumZentrumDummy <= aktuellerAbstandZumZentrumDesIntervalls) {
				bestesIntervall = intervall;
				aktuellerAbstandZumZentrumDesIntervalls = abstandZumZentrumDummy;
			}
			if (aktuellerAbstandZumZentrumDesIntervalls == 0.0) {
				break;
			}
		}

		return bestesIntervall;
	}

	/**
	 * Ermittelt die Indizes der Intervalle, in denen der �bergebene Wert liegt.<br>
	 * <b>Achtung:</b> Ein Wert <code>wert</code> liegt innerhalb des Intervalls
	 * <code>[a, b[</code>, wenn gilt: <code>a <= wert < b</code>
	 *
	 * @param wert
	 *            ein Wert
	 * @return eine ggf. leere Menge von Intervall-Indizes
	 */
	private List<Integer> getIntervalle(final double wert) {
		final List<Integer> intervalle = new ArrayList<Integer>();

		for (int i = 0; i < this.vonMenge.length; i++) {
			if ((this.vonMenge[i] <= wert) && (wert < this.bisMenge[i])) {
				intervalle.add(i);
			}
		}

		return intervalle;
	}

}
