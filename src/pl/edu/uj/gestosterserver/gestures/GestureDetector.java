package pl.edu.uj.gestosterserver.gestures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import net.sf.javaml.distance.fastdtw.dtw.DTW;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeries;
import net.sf.javaml.distance.fastdtw.timeseries.TimeSeriesPoint;

/**
 * Klasa do przetwarzania i wykrywania gestów
 */
public class GestureDetector {

	private int sampleNumber = 0;
	private double aXkal;
	private double aYkal;

	// zestawy wzorcowych próbek
	private Map<String, TimeSeries> gesturesM = new HashMap<>();
	private Map<String, TimeSeries> gesturesS = new HashMap<>();
	//    private Map<String, TimeSeries> gesturesW = new HashMap<>();
	private Map<String, TimeSeries> gesturesD = new HashMap<>();

	// Grupy szeregów czasowych dla próbek
	private TimeSeries tt1;
	//   private TimeSeries wt1;
	private TimeSeries st1;
	private TimeSeries dt1;

	// flagi
	private boolean calibrationFlag = true;
	private boolean calibrationFlag2 = true;

	Properties config;

	public GestureDetector(Properties config) {
		String gesturesDirM = config.getProperty("gesture.signal+deriviate");
		String gesturesDirS = config.getProperty("gesture.signal");
		//       String gesturesDirW = config.getProperty("gesture.coordinates");
		String gesturesDirD = config.getProperty("gesture.deriviate");

		loadFilesFromDir(gesturesDirM, gesturesM);
		//       loadFilesFromDir(gesturesDirW, gesturesW);
		loadFilesFromDir(gesturesDirS, gesturesS);
		loadFilesFromDir(gesturesDirD, gesturesD);

		this.config = config;
	}

	/**
	 * Wykonuje kalibrację próbki danych z sensorów
	 * @param list - listę odczytów z sensorów
	 * @return - indeks wyrazu z listy od którego zaczyna się ruch, lub <code>-1</code> jeżeli kalibracja nie powiodła się
	 */
	private int calibration(List<double[]> list) {
		double ax, ay;
		int i = 0;
		double sumaX = 0;
		double sumaY = 0;

		while (i < list.size() && Math.abs(list.get(i)[1]) < 1.5 && (Math.abs((list.get(i)[2])) < 1.5)) {

			if (i == 0) {
				i++;
			} else {

				ax = list.get(i - 1)[1];
				ay = list.get(i - 1)[2];

				sumaX = sumaX + ax;
				sumaY = sumaY + ay;
				i++;

				if (calibrationFlag2 && i > 10) {
					//         System.out.println("OK ");
					calibrationFlag2 = false;
				}
			}
		}

		if (i > 10 && i < 60) {
			aXkal = sumaX / (i - 1);
			aYkal = sumaY / (i - 1);

			//			 System.out.println("kalibracja " + aXkal + " " + aYkal);
			return (i - 1);
		} else {
			return -1;
		}
	}

	/**
	 * Pobiera wszystkie pliki z katalogu i zamienia je na obiekty TimeSeries
	 * @param path - ścieżka do katalogu
	 * @param gestures - mapa do której należy dodać zaczytane obiekty TimeSeries
	 */
	private void loadFilesFromDir(String path, Map<String, TimeSeries> gestures) {
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();

		for (File file: listOfFiles) {
			if (file.isFile()) {
				String fileName = file.getName();
				//               System.out.println("Processing file: " + fileName);
				gestures.put(fileName, new TimeSeries(path + "\\" + fileName, false));
			}
		}
		System.out.println("Wzorce załadowane");
	}

	/**
	 * Znajduje maksymalną amplitudę w liście
	 * @param instance - tablica na której liczona jest amplituda
	 * @return - zwraca amplitudę
	 */
	private double findMaxAmp(double[] instance) {

		double aMin = Double.MAX_VALUE;
		double aMax = Double.MIN_VALUE;

		for (double b: instance) {
			if (aMin > b) {
				aMin = b;
			} else if (aMax < b) {
				aMax = b;
			}
		}

		return aMax - aMin;
	}

	/**
	 * Zapisuje reprezentację obiektu TimeSeries (gestu) do pliku
	 * @param t - gest
	 * @param fileName - nazwa pliku do zapisu
	 */
	private void write(TimeSeries t, String fileName) {
		File file = new File(fileName);
		try (
				PrintWriter outF = new PrintWriter(file)
				) {
			if ((t.getMeasurementVector(1)).length < 3) {
				for (int i = 0; i < t.size(); i++) {
					outF.println(t.getMeasurement(i, 0) + "," + t.getMeasurement(i, 1));
				}
			} else {
				for (int i = 0; i < t.size(); i++) {
					outF.println(t.getMeasurement(i, 0) + "," + t.getMeasurement(i, 1) + "," +
							t.getMeasurement(i, 2) + "," + t.getMeasurement(i, 3));
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Normalizuje dane dzieląc wszystkie wyrazy z tabeli przez amplitudę
	 * @param instance - lista zawierająca ciąg czasowy dla wykrytego ruchu
	 */
	private void normalizeS(double[] instance) {
		double amp = findMaxAmp(instance);

		for (int i = 0; i < instance.length; i++) {
			if (amp < 3) {
				instance[i] = 0.0;
			} else {
				double b = instance[i] / Math.abs(amp);
				instance[i] = b;
			}
		}
	}

	/**
	 * Normalizuje dane dzieląc wszystkie wyrazy z tabeli przez amplitudę
	 * @param instance - lista zawierająca ciąg czasowy dla wykrytego ruchu
	 */
	private void normalizeP(double[] instance) {
		double amp = findMaxAmp(instance);

		for (int i = 0; i < instance.length; i++) {
			if (amp < 2.0f) {
				instance[i] = 0.0;
			} else {
				double b = instance[i] / Math.abs(amp);
				instance[i] = b;
			}
		}
	}

	/**
	 * Wygładzanie ai = [a(i-1)+ a(i)+a(i+1)]/3
	 * @param instance - lista zawierająca ciąg czasowy dla wykrytego ruchu
	 */
	private void smooth(double[] test) {
		for (int i = 1; i < (test.length-1); i++) {
			test[i] = (test[i-1]+test[i]+test[i+1])/3;
		}
	}
	/**
	 * Obliczanie pochodnej
	 * @param test - lista zawierająca ciąg czasowy dla wykrytego ruchu
	 * @return szereg czasowy -pochodna
	 */
	private double[]  derivative(double[] test) {
		double[] tab = new double[test.length];

		tab[0] = 0;

		for (int i = 1; i < test.length-1; i++) {	
			tab[i] = (-test[i-1]+test[i+1])/2;
		}
		
		tab[test.length-1] = 0;
		
		return tab;
	}

	/**
	 *Uśrednianie 
	 * @param test - lista zawierająca ciąg czasowy dla wykrytego ruchu
	 * @return uśredniony ciąg czasowy
	 */
	private double[]  average(double[] test) {      
		double average;
		List<Double> avArray = new ArrayList<Double>();
		avArray.add(test[0]);

		for (int i = 2; i < test.length; i++) {	
			if (i%2==1) {
				if (!(i+1 <test.length)) {
					avArray.add(test[i]);
				}              
			} else {           	
				average = (test[i] + test[i-1])/2;       	
				avArray.add(average);           	
			}
		}

		double [] tab = new double[avArray.size()];     

		for(int i=0; i<avArray.size(); i++) {        	
			tab[i] = avArray.get(i);        	
		}
		
		return tab;
	}


	/**
	 * Wykrywa gest na podstawie atrybutów obiektu
	 * @param list -
	 * @return nazwę gestu lub <code>null</code> w przypadku braku wykrycia gestu
	 */
	public String getGestureName(List<double[]> list) {
		int j = 0;

		tt1 = new TimeSeries(4);
		//        wt1 = new TimeSeries(2);
		st1 = new TimeSeries(2);
		dt1 = new TimeSeries(2);

		// wykonujemy kalibrację i pobieramy współczynnik kalibracji
		int calibrationDiff = calibration(list);

		// Weryfikujemy, czy kalibracja się powiodła, jeżeli nie, to przerywamy prace
		if (calibrationDiff > 0) {


			// Zliczamy wyrazy gestu, których przyspieszenie jest większe od 1
			for (int i = calibrationDiff; i < list.size(); i++) {
				// delta między wyrazami i, a i-2
				double dax = (list.get(i)[1]) - (list.get(i - 2)[1]);
				double day = (list.get(i)[2]) - (list.get(i - 2)[2]);
				double dax2 = 0, day2 = 0;

				// delta wartości (między i-1, a i+2)
				if (list.size() > i + 2) {
					dax2 = (list.get(i - 1)[1]) - (list.get(i + 2)[1]);
					day2 = (list.get(i - 1)[2]) - (list.get(i + 2)[2]);
				}

				if (Math.abs(list.get(i)[1] - aXkal) > 1 || Math.abs(list.get(i)[2] - aYkal) > 1
						|| Math.abs(dax) > 0.5 || Math.abs(day) > 0.5
						|| Math.abs(dax2) > 0.5 || Math.abs(day2) > 0.5) {

					j++;
				}
			}

			// wejście gdy ciąg czasowy ma conajmniej 30 odczytów
			// kolejne kilka odczytów jest poniżej 1 (bez ruchu)
			if (calibrationDiff + j + 4 < list.size() && j > 30
					&& Math.abs(list.get(calibrationDiff + j + 1)[1] - aXkal) < 1
					&& Math.abs(list.get(calibrationDiff + j + 1)[2] - aYkal) < 1
					&& Math.abs(list.get(calibrationDiff + j + 2)[1] - aXkal) < 1
					&& Math.abs(list.get(calibrationDiff + j + 2)[2] - aYkal) < 1
					&& Math.abs(list.get(calibrationDiff + j + 3)[1] - aXkal) < 1
					&& Math.abs(list.get(calibrationDiff + j + 3)[2] - aYkal) < 1
					&& Math.abs(list.get(calibrationDiff + j + 4)[1] - aXkal) < 1
					&& Math.abs(list.get(calibrationDiff + j + 4)[2] - aYkal) < 1) {

				System.out.println("liczba wyrazow ciagu wynosi:" + (j));

				// Tworzenie ciągów czasowych dla sygnału, pochodnej, współrzędnych i sygnału+pochodnej
				createTimeSeriesFromSamples(j, calibrationDiff, list);

				// DEBUG
				Map<String, Double> gestureTests = new HashMap<>();
				//                Map<String, Double> gestureTestsW = new HashMap<>();
				Map<String, Double> gestureTestsS = new HashMap<>();
				Map<String, Double> gestureTestsD = new HashMap<>();

				// Multi
				String multi = getMinimalDistance(gesturesM, tt1, gestureTests);

				// WSP
				//                String wsp = getMinimalDistance(gesturesW, wt1, gestureTestsW);

				// POCHODNA
				String pocho = getMinimalDistance(gesturesD, dt1, gestureTestsD);

				// SYGNAL
				String syg = getMinimalDistance(gesturesS, st1, gestureTestsS);

				write(st1, ("S" + sampleNumber + syg));
				write(dt1, ("P" + sampleNumber + syg));
				//                write(wt1, ("Wsp" + sampleNumber + syg));
				write(tt1, ("Multi" + sampleNumber + syg));

				if (!syg.equals("Szero.txt")) {
					//                String sygDirection = getGestDirection(pocho);
					//                if (sygDirection != null && sygDirection.equals(getGestDirection(pocho))) {

					sampleNumber++;
					System.out.println(sampleNumber + " " + syg + "    " + pocho + "    "  + multi);

					System.out.println(gestureTestsS.get(syg) + " " + gestureTestsD.get(pocho)
					+ " " + gestureTests.get(multi));

					return verifyGestureLimits(multi, gestureTests);

					//                   }
				}
			} else {
				return null;
			}

		} else {
			if (calibrationFlag) {
				System.out.println("Kalibracja nie powiodła się");
				calibrationFlag = false;
			}

			calibrationFlag2 = true;
		} // ENDIF: calibrationDiff > 0

		return null;
	}

	/**
	 * Funkcja porównuje limity z pliku konfiguracyjnego
	 * @param multi
	 * @param gestureTests
	 * @return
	 */
	private String verifyGestureLimits(String multi, Map<String,Double> gestureTests) {
		// weryfikacja limitów dla gestów
		String gestDirection = getGestDirection(multi);
		String gestDirectionMaxLimitCfg = config.getProperty("gesture." + gestDirection + ".maxlimit");

		if (gestDirectionMaxLimitCfg == null) {
			return null;
		}

		Integer gestDirectionMaxLimit = Integer.parseInt(gestDirectionMaxLimitCfg);

		if (gestureTests.get(multi) < gestDirectionMaxLimit) {
			return gestDirection;
		} else {
			System.out.println("Przekroczono limit graniczny dla gestu: "
					+ gestureTests.get(multi) + " > " + gestDirectionMaxLimit);
			return null;
		}
	}

	/**
	 * Tworzenie ciągów czasowych dla sygnału, pochodnej, współrzędnych i sygnału+pochodnej z dostarczonych próbek
	 * @param j - numer porządkowy odczytu w próbce
	 * @param calibrationDiff - współczynnik kalibracji
	 */
	private void createTimeSeriesFromSamples(int j, int calibrationDiff, List<double[]> list) {
		double[] test3 = new double[j];
		double[] test4 = new double[j];
		double[] test5 = new double[j];
		double[] test6 = new double[j];

		//double[] licznik = new double[j];
		double vx = 0;
		double vy = 0;
		double wspx = 0;
		double wspy = 0;
		double ax, ay;
		double[] tab1 = new double[2];
		double[] tab2 = new double[4];
		double[] tab3 = new double[2];
		double[] tab4 = new double[2];

		for (int i = 0; (i < j) && (i+1 < list.size()); i++) {

			if (i == 0){
				test5[i] = 0;
				test6[i] = 0;    	
			} else {            	
				//licznik[i] = list.get(calibrationDiff + i)[0];
				test3[i] = list.get(calibrationDiff + i)[1];
				test4[i] = list.get(calibrationDiff + i)[2];              
				//               test5[i] = (list.get(calibrationDiff + i+1)[1] -  list.get(calibrationDiff + i-1)[1])/2;
				//               test6[i] = (list.get(calibrationDiff + i+1)[2] -  list.get(calibrationDiff + i-1)[2])/2;

			}
		}

		// uśrednianie
		test3 = average(test3); 
		test4 = average(test4);

		// wygładzanie
		smooth(test3); 
		smooth(test4); 

		//pochodna
		test5 = derivative(test3);
		test6 = derivative(test4);

		//normalizacja     
		normalizeS(test3);
		normalizeS(test4);


		normalizeP(test5);
		normalizeP(test6);

		for (int i = 0; i < test3.length; i++) {
			ax = test3[i];
			ay = test4[i];

			double t = 0.2;
			wspx = wspx + (vx * t + ax * t * t / 2) * 100;
			wspy = wspy + (vy * t + ay * t * t / 2) * 100;

			vx = vx + ax * t;
			vy = vy + ay * t;


			tab1[0] = test5[i];
			tab1[1] = test6[i];


			//pochodna
			TimeSeriesPoint d = new TimeSeriesPoint(tab1);
			dt1.addLast(i, d);

			//multi
			tab2[0] = test3[i];
			tab2[1] = test4[i];
			tab2[2] = test5[i];
			tab2[3] = test6[i];

			TimeSeriesPoint p = new TimeSeriesPoint(tab2);
			tt1.addLast(i, p);

			//współrzędne
			tab3[0] = wspx;
			tab3[1] = wspy;

			//           TimeSeriesPoint w = new TimeSeriesPoint(tab3);
			//           wt1.addLast(i, w);

			//sygnal
			tab4[0] = test3[i];
			tab4[1] = test4[i];

			TimeSeriesPoint s = new TimeSeriesPoint(tab4);
			st1.addLast(i, s);
		}
	}

	/**
	 * Funkcja weryfikuje, który gest z zestawu jest najbliżej aktualnie wykonanego
	 * @param gestureList -
	 * @param ts -
	 * @param gestureTests -
	 * @return nazwę zakwalifikowanego gestu
	 */
	private String getMinimalDistance(Map<String, TimeSeries> gestureList, TimeSeries ts, Map<String, Double> gestureTests) {
		double dystMin = Double.MAX_VALUE;
		String gestureName = null;

		for (String gestureCurr : gestureList.keySet()) {
			double dystCurr = 
					DTW.getWarpDistBetween(gestureList.get(gestureCurr), ts);

			// DEBUG
			gestureTests.put(gestureCurr, dystCurr);

			if (dystMin > dystCurr) {
				dystMin = dystCurr;
				gestureName = gestureCurr;
			}
		}

		return gestureName;
	}

	/**
	 * Funkcja zwraca typ wykrytego gestu
	 * @param gestName - nazwa wykrytego gestu
	 * @return - typ wykrytego gestu lub <code>null</code> w razie braku wykrycia
	 */

	private String getGestDirection(String gestName) {
		if (gestName.contains("prawo")) {
			return "prawo";
		}
		if (gestName.contains("lewo")) {
			return "lewo";
		}
		if (gestName.contains("gora")) {
			return "gora";
		}
		if (gestName.contains("dol")) {
			return "dol";
		}
		if (gestName.contains("kolo")) {
			return "kolo";
		}
		if (gestName.contains("tak")) {
			return "tak";
		}
		if (gestName.contains("nie")) {
			return "nie";
		}
		if (gestName.contains("zero")) {
			return "nie";
		}

		return null;
	}
}
