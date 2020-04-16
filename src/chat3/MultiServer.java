package chat3;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {
	
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	static PrintWriter out =null;
	static BufferedReader in =null;
	static String s = "";//클라이언트의 메세지 저장
	
	public MultiServer() {
		
	}
	
	//서버의 초기화를 담당할 메소드
	public static void init() {
		
		//클라이언트로부터 전송받은 이름을 저장
		String name = "";
		
		try {
			//9999번으로 포트번호를 설정하여 
			//서버를 생성하고 클라이언트의 접속을 기다린다.
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			// .... 접속대기증 ....
			
			//클라이언트가 접속요청을 하면 accept()메소드를 통해 받아들인다.
			socket = serverSocket.accept();
			
			System.out.println(socket.getInetAddress() + ":" + socket.getPort());
			//서버 -> 클라이언트 측으로 메세지를 전송(출력)하기 위한 스트림을 생성
			out = new PrintWriter(socket.getOutputStream(), true);
			//클라이언트로부터 메세지를 받기 위한 스트림을 생성
			in = new BufferedReader(new
					InputStreamReader(socket.getInputStream()));
							
			/*
			 클라이언트가 서버로 전송하는 최초의 메세지는 "대화명"이므로
			 메세지를 읽은 후 변수에 저장하고 클라이언트쪽으로 Echo해준다.
			 */
			if(in != null) {
				name=in.readLine();
				System.out.println(name+" 접속");
				out.println(">"+name+"님이 접속했습니다.");
			}
			
			/*
			 두 번째 메세지부터는 실제 대화내용이므로 읽어와서 로그로 출력하고
			 동시에 클라이언트로 Echo한다.
			 */
			while(in != null) {
				s=in.readLine();
				if(s==null) {
					break;
				}
				System.out.println(name+ " ==> "  + s);
				sendAllMsg(name,s);
			}
			
			System.out.println("Bye ... !!!");
		}
		catch ( Exception e) {
			System.out.println("예외1 : " + e);
		}
		
		finally {
			try {
				//입출력스트림 종료
				in.close();
				out.close();
				//소켓종료                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          
				socket.close();
				serverSocket.close();
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
		
	}
	
	//서버가 클라이언트에게 메세지를 Echo해주는 메소드
	public static void sendAllMsg(String name, String msg) {
		try {
			out.println("> " + name + " ==> " + msg);
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {

		init();
		
		
		
	}

}
