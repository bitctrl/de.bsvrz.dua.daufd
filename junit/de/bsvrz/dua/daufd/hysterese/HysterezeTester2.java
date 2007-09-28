package de.bsvrz.dua.daufd.hysterese;

import junit.framework.Assert;

import org.junit.Test;

public class HysterezeTester2 {
	/**
	 * Stufe untere Grenzwerte
	 */
	private  double stufeVon[] = null;
	/**
	 * Stufe obere Grenzwerte 
	 */
	private  double stufeBis[] = null;
	
	private long anzahlTest = 0;

	public void init(double von[], double bis[]) {
		stufeVon = new double[von.length];
		stufeBis = new double[bis.length];
		for(int i=0; i<von.length-1; i++) {
			Assert.assertTrue("Naechste Stufe beginnt nach luecke: [" + von[i] + "," + bis[i] + "] naechste von: " + von[i+1], 
					von[i+1] <= bis[i]);
			Assert.assertTrue("Naechste Stufe ueberdeckt die Vorherige: [" + von[i] + "," + bis[i] + "] naechste von: " + von[i+1],
					von[i+1] > von[i]);
		}
		for(int i=0; i<von.length; i++) {
			stufeVon[i] = von[i];
			stufeBis[i] = bis[i];
		}
	}
	
	/**
	 * Berechnet den HysterezeWert
	 * @param wert Messwert
	 * @param stufeAlt vorherige Stufe
	 * @return neue Stufe
	 */
	public int hystereze(double wert, int stufeAlt) {
		
		double dist = 1.0, dist_neu;
		int j = -1;
		
		for(int i=0; i<this.stufeVon.length; i++) {
			if(stufeVon[i] <= wert && wert < stufeBis[i]) {
				if(stufeAlt == i) return stufeAlt;
				else  {
					dist_neu = Math.abs((wert - stufeVon[i])/(stufeBis[i] - stufeVon[i]) - 0.5);
					if(dist_neu < dist) {
						dist = dist_neu;
						j = i;
					}
				}
			}
		}		
		return j;
	}
	
	/**
	 * Testet die Hystereze Klasse
	 */
	@Test
	public void Test() {
		
		for(int i =0; i<100; i++) {
			// Zufaelliger Anzahl der Intervalle, 2 - 20;
			int intervalle  = 2 +  (int)(Math.random() * 18);  
		
			double [] vonMenge  = new double[intervalle];
			double [] bisMenge  = new double[intervalle];
			
			double minWert = 0; //Math.random() * 100 - 50;
			double maxWert = Math.random() * 100;
			final int ANZAHL_TESTS = 1000;
			
			vonMenge[0] = minWert;
			bisMenge[intervalle-1] = maxWert;
			
			// Zufaellige Intervallgrenzen
			for(int j=0; j<intervalle-1; j++) {
				bisMenge[j] = vonMenge[j] + Math.random() * (maxWert - vonMenge[j]);
				vonMenge[j+1] = vonMenge[j] + Math.random() * (bisMenge[j] - vonMenge[j]);
			}
			
			Hysterese hyst = new Hysterese();
			try {
				hyst.initialisiere(vonMenge, bisMenge);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
			}
			this.init(vonMenge, bisMenge);
			
			// Vergleicht 1000 zufaellige Nummer
			int alt = -1;
			for(int k=0; k<ANZAHL_TESTS; k++) {
				double rand = 1.1 * Math.random() * (maxWert  - minWert) - 0.05 * (maxWert - minWert);
				int r1 = hyst.getStufe(rand);
				int r2 = this.hystereze(rand, alt);
				
				Assert.assertTrue(r1 == r2);
				
				System.out.println(String.format("Test Nr.  %6d  >  %2d == %2d  BESTANDEN ", anzahlTest++, r1, r2));
				alt = r2;
			}
		}
	}
}