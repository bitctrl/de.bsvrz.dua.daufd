package de.bsvrz.dua.daufd;

public class MesswertBearbeitungAllgemein {
	
	/**
	 * Generiert ein Array von Messwerten fuer Testfaelle
	 * 
	 * @param min min Messwert
	 * @param max max Messwert
	 * @return Messwerte
	 */
	public static double [] generiereMesswerte(double min, double max) {
		
		final int N = 31;
		final int M = 32;
		final int K = 7;
		final int L = 30;
		final int ANZAHL = N+M+K+L;
		final double intervall = max - min;
		double schritt;
	
		double [] Messwert = new double[ANZAHL];
	
		// Zuerst steigende und dann sinkende Werte
		schritt = intervall / N;
		for(int i=0; i<N; i++) {
			Messwert[i] = max - Math.abs(2*i*schritt - intervall);
		}
		
		// Divergiert vo der Mitte des INtervalles 
		schritt = intervall / M;
		for(int i=N; i<N+M; i++) {
			int j = i - N;
			Messwert[i] = intervall/2 - Math.pow(-1.0, j)*j*schritt/2;  
		}
		
		// Zuerst  sinkende und dann steigende Werte, mit groesserem Gradient
		schritt = intervall / K;
		for(int i=N+M; i<N+M+K; i++) {
			int j = i - N-M;
			Messwert[i] = min + Math.abs(intervall-j*schritt*2);
		}
		
		// Zufaellige Werte
		for(int i=N+M+K; i<N+M+K+L; i++) {
			Messwert[i] = min + Math.random() * intervall;
		}
		
		return Messwert;
	}
	
	/**
	 * Generiert gerausch und setzt zufaellige Werte als  0 
	 * @param Messwert Original Array
	 * @param intervall Intervall des Gerausches
	 * @param anzahlNullWerte Anzahl der Messwerte, die als 0 gesetzt werden
	 */
	public static void gerauescheMesswerte(double [] Messwert, double intervall, int anzahlNullWerte ) {
		for(int i =0; i<Messwert.length; i++) {
			Messwert[i] += Math.random()*intervall-intervall/2;
		}
		for(int i=0; i<anzahlNullWerte; i++) {
			int j = (int)(Math.random()*Messwert.length);
			Messwert[j] = 0;
		}
	}
	
	/**
	 * Glaettet die Messwerte
	 * 
	 * @param Messwert Messwerte
	 * @param b Koefizient b
	 * @param MesswertGlatt Ausgabe geglaettete Messwerte
	 * @param f Koefizient f
	 * @param b0 Koefizient b[0]
	 */
	public static void geglaetteMesswerte(double [] Messwert, double [] b, double [] MesswertGlatt, double f, double b0) {
		
		b[0] = b0;
		MesswertGlatt[0] = Messwert[0];
		
		for(int i=1; i<Messwert.length; i++) {
			if(Messwert[i] == 0) {
				MesswertGlatt[i] = 0;
				b[i] = b[0];
			}
			else {
				b[i] = b[0] + (1.0 - f* MesswertGlatt[i-1]/Messwert[i]);
				if(b[i] < b[0] || b[i] > 1.0) b[i] = b[0];
				MesswertGlatt[i] = b[i]*Messwert[i] + (1.0 - b[i])*MesswertGlatt[i-1];
			}
		}
	}
}
