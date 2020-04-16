package chat6;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MultiClient {

	public static void main(String[] args) {

		System.out.print("이름을 입력하세요:");
		Scanner sc = new Scanner(System.in);
		String s_name = sc.nextLine();
		//Sender가 기능을 가져감
		//PrintWriter out = null;
		
		//서버의 메세지를 읽어오는 기능이 Receiver로 옮겨짐. 
		//BufferedReader in = null;
		
		try {
			//별도의 매개변수가 없으면 접속IP는 localhost로 고정됨
			/*
			 C:\> java 패키지명.MultiClient 접속할IP주소
			 	=> 위와 같이 하면 해당 IP주소로 접속할 수 있다.
			 */
			String ServerIP="localhost";
			//클라이언트 실행 시 매개 별수가 있는 경우 아이피로 설정함.
			if(args.length>0) {
				ServerIP=args[0];
			}
			//IP주소와 포트를 기반으로 소켓객체를 생성하여 서버에 접속함
			Socket socket = new Socket(ServerIP,9999);
			//서버와 연결되면 메세지 출력
			System.out.println("서버와 연결되었습니다...");
			
			//서버에서 보내는 메세지를 읽어올 Receiver쓰레드 시작
			Thread receiver = new Receiver(socket);
			//setDaemen(true)가 없으므로 독립쓰레드로 생성됨.
			receiver.start();
			// 클라이언트 메세지를 서버로 전송해주는 쓰레드 생성
			Thread sender = new Sender(socket,s_name);
			sender.start();
			
		}
		catch(Exception e) {
			System.out.println("예외발생[MultiClient]" + e);
		}
	}

}
