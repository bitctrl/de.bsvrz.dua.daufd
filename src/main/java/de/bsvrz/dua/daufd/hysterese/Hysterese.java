/*
 * Segment Datenübernahme und Aufbereitung (DUA), SWE Datenaufbereitung UFD
 * Copyright (C) 2007 BitCtrl Systems GmbH 
 * Copyright 2015 by Kappich Systemberatung Aachen
 * Copyright 2016 by Kappich Systemberatung Aachen
 * 
 * This file is part of de.bsvrz.dua.daufd.
 * 
 * de.bsvrz.dua.daufd is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * de.bsvrz.dua.daufd is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with de.bsvrz.dua.daufd.  If not, see <http://www.gnu.org/licenses/>.

 * Contact Information:
 * Kappich Systemberatung
 * Martin-Luther-Straße 14
 * 52062 Aachen, Germany
 * phone: +49 241 4090 436 
 * mail: <info@kappich.de>
 */

package de.bsvrz.dua.daufd.hysterese;

import java.util.ArrayList;
import java.util.List;


/**
 * Das Modul Hysterese stellt eine Berechnung eines diskreten
 * Wertes (Zustandswert) auf Basis von quasi-kontinuierlichen
 * Eingangswerten über eine Hystereseabbildung zur Verfügung.
 * 
 * @author BitCtrl Systems GmbH, Thierfelder
 *
 */
public class Hysterese {

	/**
	 * Menge der Intervall-Anfänge 
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
	 * @param vonMenge1 Menge der Intervall-Anfänge
	 * @param bisMenge1 Menge der Intervall-Enden
	 * @throws HystereseException wenn die beiden übergebenen Mengen leer
	 * sind, oder nicht die gleiche Anzahl an Elementen enthalten, oder zwischen
	 * den Intervallen Lücken existieren, etc.. 
	 */
	public final void initialisiere(final double[] vonMenge1,
								    final double[] bisMenge1)
	throws HystereseException{
		if(vonMenge1 == null || bisMenge1 == null){
			throw new NullPointerException("Eine Wertemenge ist <<null>>"); //$NON-NLS-1$
		}
		if(bisMenge1.length != bisMenge1.length){
			throw new HystereseException("Menge der VON-Begrenzer und BIS-Begrenzer" + //$NON-NLS-1$
					" muss gleich gross sein."); //$NON-NLS-1$
		}
	
		this.vonMenge = new double[vonMenge1.length];
		this.bisMenge = new double[bisMenge1.length];
		for(int i = 0; i < vonMenge1.length; i++){
			this.vonMenge[i] = vonMenge1[i];
			this.bisMenge[i] = bisMenge1[i];		
		}
	}
	
	public boolean isEmpty(){
		return vonMenge == null || vonMenge.length == 0;
	}

	/**
	 * Initialisiert dieses Hysterese mit <code>long</code>-Intervallen
	 * 
	 * @param vonMenge1 Menge der Intervall-Anfänge
	 * @param bisMenge1 Menge der Intervall-Enden
	 * @throws HystereseException wenn die beiden übergebenen Mengen leer
	 * sind, oder nicht die gleiche Anzahl an Elementen enthalten, oder zwischen
	 * den Intervallen Lücken existieren, etc.. 
	 */
	public final void initialisiere(final long[] vonMenge1,
								    final long[] bisMenge1)
	throws HystereseException{		
		if(vonMenge1 == null || bisMenge1 == null){
			throw new NullPointerException("Eine Wertemenge ist <<null>>"); //$NON-NLS-1$
		}
		if(bisMenge1.length != bisMenge1.length){
			throw new HystereseException("Menge der VON-Begrenzer und BIS-Begrenzer" + //$NON-NLS-1$
					" muss gleich gross sein."); //$NON-NLS-1$
		}
		double[] vonMengeDouble = new double[vonMenge1.length];
		double[] bisMengeDouble = new double[bisMenge1.length];
		for(int i=0; i < vonMenge1.length; i++){
			vonMengeDouble[i] = vonMenge1[i];
			bisMengeDouble[i] = bisMenge1[i];
		}
		this.initialisiere(vonMengeDouble, bisMengeDouble);
	}

	
	/**
	 * Errechnet die Hysterese-Stufe eines bestimmten <code>double</code>-Wertes.
	 * 
	 * @param wert ein <code>double</code>-Wert
	 * @return die Hysterese-Stufe des übergebenen <code>double</code>-Wertes oder <code>-1</code>, wenn
	 * keine Hysterese-Stufe errechnet werden konnte
	 */
	public final int getStufe(final double wert){
		int stufe = -1;
		
		List<Integer> intervalle = this.getIntervalle(wert);
		if(this.stufeAlt == -1){
			/**
			 * Die momentane Hysteresestufe ist nicht bekannt oder 
			 * es wurde noch nie eine Stufe ausgerechnet 
			 */
			if(intervalle.size() == 1){
				stufe = intervalle.get(0);
			}else
			if(intervalle.size() >= 2){
				/**
				 * Wähle das Intervall, bei dem der Abstand des übergebenen Wertes
				 * zum Mittelpunkt (des Intervalls) am kleinsten ist
				 */
				stufe = this.getBestesIntervall(intervalle, wert);
			}else{
				stufe = -1;
			}			
		}else{
			/**
			 * Die momentane Hysteresestufe ist bekannt 
			 */
			if(intervalle.size() == 1){
				stufe = intervalle.get(0);
			}else
			if(intervalle.size() >= 2){
				if(intervalle.contains(new Integer(this.stufeAlt))){
					/**
					 * Wenn eine, der errechneten Hysterese-Stufen schon im letzten
					 * Schritt anlag, dann gebe einfach die letzte Stufe zurück
					 */
					stufe = this.stufeAlt;
				}else{
					/**
					 * Wähle das Intervall, bei dem der Abstand des übergebenen Wertes
					 * zum Mittelpunkt (des Intervalls) am kleinsten ist
					 */
					stufe = this.getBestesIntervall(intervalle, wert);
				}
			}else{
				stufe = -1;
			}			
		}
		
		this.stufeAlt = stufe;
		
		return stufe;
	}

	/**
	 * Erfragt das Intervall (der übergebenen Intervalle), bei dem der Abstand des
	 * übergebenen Wertes zum Mittelpunkt (des Intervalls) am kleinsten ist
	 * 
	 * @param intervalle eine Liste mit Intervall-Indizes dieser Hysterese. Diese
	 * Liste darf nicht leer sein! 
	 * @param wert ein Wert
	 * @return das Intervall, bei dem der Abstand des übergebenen Wertes
	 * zum Mittelpunkt (des Intervalls) am kleinsten ist
	 */
	private final int getBestesIntervall(final List<Integer> intervalle, final double wert){
		double aktuellerAbstandZumZentrumDesIntervalls = 1;
		int bestesIntervall = -1;

		for(Integer intervall:intervalle){
			double abstandZumZentrumDummy = Math.abs(( (wert - vonMenge[intervall]) / (bisMenge[intervall] - vonMenge[intervall])) - 0.5);
			if(abstandZumZentrumDummy <= aktuellerAbstandZumZentrumDesIntervalls){
				bestesIntervall = intervall;
				aktuellerAbstandZumZentrumDesIntervalls = abstandZumZentrumDummy; 
			}
			if(aktuellerAbstandZumZentrumDesIntervalls == 0.0){
				break;
			}
		}
		
		return bestesIntervall;
	}
	
	
	/**
	 * Ermittelt die Indizes der Intervalle, in denen der übergebene Wert
	 * liegt.<br>
	 * <b>Achtung:</b> Ein Wert <code>wert</code> liegt innerhalb des Intervalls
	 * <code>[a, b[</code>, wenn gilt: <code>a <= wert < b</code>
	 * 
	 * @param wert ein Wert
	 * @return eine ggf. leere Menge von Intervall-Indizes
	 */
	private final List<Integer> getIntervalle(final double wert){
		List<Integer> intervalle = new ArrayList<Integer>();

		for(int i=0; i<this.vonMenge.length; i++){
			if(this.vonMenge[i] <= wert && wert < this.bisMenge[i]){
				intervalle.add(i);
			}
		}
		
		return intervalle; 
	}
		
}
