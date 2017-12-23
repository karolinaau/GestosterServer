package pl.edu.uj.gestosterserver;

import java.awt.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Properties;
import java.util.Scanner;

import pl.edu.uj.gestosterserver.interpreters.OrientationInterpreter;
import pl.edu.uj.gestosterserver.interpreters.SensorsInterpreter;

/**
 * Klasa służąca do komunikacji ze światem zewnętrznym
 * - tworzy instancję serwera odbierającego dane z telefonu
 */

public class Server {

	public static void main(String[] args) throws InterruptedException, FileNotFoundException, AWTException {
		if (args.length < 1) {
			System.out.println("Usage: java Server gesturescontrol.properties");
			System.exit(0);


		}

		Server server = new Server(args[0]);

		try (
				ServerSocket serverSocket = new ServerSocket(8189)
				) {
			// oczekuje na połączenie z klientem
			while(true) {
				try (
						Socket incoming = serverSocket.accept()
						) {
					server.parseClientsData(incoming);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.out.println("Error in opening Socket");
		}
	}

	// referencja do konfiguracji programu
	private Properties config;

	/**
	 * Konstruktor inicjujący konfigurację programu
	 * @param configFile - plik z konfiguracją
	 */
	private Server(String configFile) {
		config = new ConfigProperties(configFile);
	}

	/**
	 * Funkcja pobiera dane od klienta i przeprowadza ich interpretację
	 *
	 * @param incoming - gniazdo klienckie
	 * @throws AWTException - wyjątek rzucany przy braku mośliwości uruchomienia interpreterów
	 */
	private void parseClientsData(Socket incoming) throws AWTException {
		OrientationInterpreter orientationInterpreter = new OrientationInterpreter();
		SensorsInterpreter sensorsInterpreter = new SensorsInterpreter(config);

		//DEBUG - START
		File file = new File("acce1.txt");
		//DEBUG - STOP

		try (
				InputStream inStream = incoming.getInputStream();
				OutputStream outStream = incoming.getOutputStream();
				Scanner in = new Scanner(inStream);
				PrintWriter out = new PrintWriter(outStream, true);

				//DEBUG - START
				PrintWriter outF = new PrintWriter(file)
						//DEBUG - STOP
				) {
			out.println("Hello!");
			System.out.println("hello!");

			while (in.hasNextLine()) {

				String line = in.nextLine();

				if (line.trim().equals("BYE")) {
					System.out.println("BYE");
					out.println("BYE");
					return;
				} else {
					double[] data = new double[7];

					data[0] = Double.parseDouble(line.split(",")[0]); // licznik
					data[1] = Double.parseDouble(line.split(",")[1]); // przysp x
					data[2] = Double.parseDouble(line.split(",")[2]); // przysp y
					data[3] = Double.parseDouble(line.split(",")[3]); // dt
					data[4] = Double.parseDouble(line.split(",")[4]); // yaw
					data[5] = Double.parseDouble(line.split(",")[5]); // pitch
					data[6] = Double.parseDouble(line.split(",")[6]); // roll6

					//DEBUG - START
					outF.println(String.valueOf(data[0]) + " " + String.valueOf(data[1]) + " "
							+ String.valueOf(data[2]) + " " + String.valueOf(data[3]) + " "
							+ String.valueOf(data[4]) + " " + String.valueOf(data[5]) + " "
							+ String.valueOf(data[6]));
					//DEBUG - STOP

					if (data[0] == 1 && Math.abs(data[5]) < 40) {

						System.out.println(data[4]);
						orientationInterpreter.angleChange(data[4], data[5], data[6]);

					} else { 

						if (Math.abs(data[5]) > 50) {

							// System.out.println("acce "+ list.get(list.size()-1)[0] +" "+ list.get(list.size()-1)[1]);
							sensorsInterpreter.interpreteSensorsData(data);
						}

					}
				}
			}

		} catch (IOException e) {
			System.out.println("Read failed");
		}
	} // parseClientsData

}
