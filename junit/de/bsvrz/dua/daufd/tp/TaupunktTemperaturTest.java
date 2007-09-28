package de.bsvrz.dua.daufd.tp;

import org.junit.Test;

public class TaupunktTemperaturTest {

	public double taupunktTemperaturLuft(double relativeLuftFeuchtigkeit, double luftTemperatur) {
		return taupunktTemperatur(relativeLuftFeuchtigkeit, luftTemperatur);
	}
	
	public double taupunktTemperaturFahrbahn(double relativeLuftFeuchtigkeit, double fahrBahnTemperatur) {
		return taupunktTemperatur(relativeLuftFeuchtigkeit, fahrBahnTemperatur);
	}
	
	public double taupunktTemperatur(double feuchtigkeit, double temperatur) {
		double RF = feuchtigkeit;
		double T = temperatur;
		double TPT;
		
		TPT = (241.2 * Math.log(RF/100.0) + (4222.03716*T)/(241.2 + T))/
				(17.5043 - Math.log(RF/100.0) - (17.5043*T)/(241.2 + T));
		
		return TPT;
	}
	
	public double mittelWert(double [] werte) {
		double sum = 0.0;
		for(int i = 0; i< werte.length; i++)
			sum += werte[i];
		
		return sum/werte.length;
	}
	public double trendExtrapolationKorrekt(double [] werte, double [] zeitPunkte, double t) {
		
		double wertMittel;
		double zeitMittel;
		
		wertMittel = mittelWert(werte);
		zeitMittel = mittelWert(zeitPunkte);
		
		double summe = 0.0;
		double summeKvadrat = 0.0;
		double n = werte.length;
		
		for(int i=0; i<werte.length; i++) 
			summe += werte[i] * zeitPunkte[i];
		
		summe -= n * wertMittel * zeitMittel;
		
		for(int i=0; i<zeitPunkte.length; i++) 
			summeKvadrat += zeitPunkte[i]*zeitPunkte[i];
		
		summeKvadrat -= n * zeitMittel * zeitMittel;
		
		double  a = summe / summeKvadrat;
		double b = wertMittel - a * zeitMittel;
		
		return a * t + b;
	}

	public double trendExtrapolation(double [] werte, double [] zeitPunkte, double t) {
		
		double wertMittel;
		double zeitMittel;
		
		wertMittel = mittelWert(werte);
		zeitMittel = mittelWert(zeitPunkte);
		
		double summe = 0.0;
		double summeKvadrat = 0.0;
		double n = werte.length;
		
		for(int i=0; i<werte.length; i++) 
			summe += werte[i] * zeitPunkte[i] - n * wertMittel * zeitMittel;
		
		for(int i=0; i<zeitPunkte.length; i++) 
			summeKvadrat += zeitPunkte[i]*zeitPunkte[i] - n * zeitMittel * zeitMittel;
		
		double  a = summe / summeKvadrat;
		double b = wertMittel - a * zeitMittel;
		
		return a * t + b;
	}
	
	@Test
	public void Test() {
		
		double [] x = new double [] { 1, 2, 3, 4, 5 };
		double [] y = new double [] { 1, 1.5, 2, 2.5, 3 };
		
		double a [] = new double [8];
		double b [] = new double [8];
		
		for(int i = 0; i<8; i++) {
			a[i] =  trendExtrapolation(y, x, i);
			b[i] =  trendExtrapolationKorrekt(y, x, i);
		}
		
		double T [] = new double [] { 0.1, -0.2, 0.0, 1.1 -1.0};
		double feuchte [] = new double [] { 83, 99.9, 100.0, 70.1, 6.2 };
		
		for(int i = 0; i< T.length; i++ )
			for(int j = 0; j< feuchte.length; j++)
			{
				double taupunkt = taupunktTemperatur(feuchte[j], T[i]); 
			}
	}
}
