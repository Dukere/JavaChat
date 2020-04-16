package chatproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.Scanner;

//클라이언트가 입력한 메세지를 서버로 전송해주는 쓰레드 클래스
public class Sender extends Thread {

	Socket socket;
	PrintWriter out = null;
	String name;

	// Socket 객체를 매개변수로 받는 생성자
	public Sender(Socket socket, String name) {
		this.socket = socket;

		// Socket객체를 기반으로 input스트림을 생성한다.
		// 서버가 보내는 메세지를 읽어오는 역할을 한다.
		try {
			out = new PrintWriter(this.socket.getOutputStream(), true);
			this.name = name;
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/*
	 * Thread에서 main()메소드 역할을 하는 함수로 직접 호출하면 안되고 반드시 start()를 통해 간접호출해야 쓰레드가 생성된다.
	 */
	@Override
	public void run() {

		Scanner s = new Scanner(System.in);

		try {
			// 클라이언트가 입력한 "대화명"을 서버로 전송
			out.println(URLEncoder.encode(name, "UTF-8"));
			// 스트림을 통해 서버가 보낸 내용을 라인단위로 읽어온다.
			while (out != null) {
				try {
					String s2 = "";
					while(s2.isEmpty()) {
					s2 = s.nextLine();
				}
					if (s2.equalsIgnoreCase("Q")) {
						break;
					} else {
						out.println(URLEncoder.encode(s2, "UTF-8"));
					}
				}  catch (Exception e) {
					e.printStackTrace();
				}

			}

			out.close();
			socket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
