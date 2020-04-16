package chat5;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiServer {

	static ServerSocket serverSocket = null;
	static Socket socket = null;

	public MultiServer() {

	}

	// 서버의 초기화를 담당할 메소드
	public void init() {

		try {
			// 9999번으로 포트번호를 설정하여
			// 서버를 생성하고 클라이언트의 접속을 기다린다.
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");

			/*
			 1명의 클라이언트가 접속할 때마다 접속을 허용(accept())해주고
			 동시에 MultiServerT 쓰레드를 생성
			 1명의 클라가 전송하는 메세지를 읽어 Echo해주는 역할
			 */
			while (true) {
				socket = serverSocket.accept();

				Thread mst = new MultiServerT(socket);
				mst.start();
			}
		} catch (Exception e) {
			System.out.println(e);
		}

		finally {
			try {

				serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	
	//Server 객체 생성 후 초기화.
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();

	}

	class MultiServerT extends Thread {

		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;

		public MultiServerT(Socket socket) {
			this.socket = socket;

			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				// 클라이언트로부터 메세지를 받기 위한 스트림을 생성
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));

			} catch (Exception e) {
				System.out.println("예외 :" + e);
			}
		}

		@Override
		public void run() {
			String name = "";
			String s = "";

			try {
				if (in != null) {
					name = in.readLine();
					System.out.println(name + " 접속");
					out.println(">" + name + "님이 접속했습니다.");
				}
				while (in != null) {
					s = in.readLine();
					if (s == null) {
						break;
					}
					System.out.println(name + " ==> " + s);
					sendAllMsg(name, s);
				}

			} catch (Exception e) {
				System.out.println("예외 : " + e);
			}
			finally {
				System.out.println(Thread.currentThread().getName()+" 종료");
				
				try {
					in.close();
					out.close();
					socket.close();
				}
				catch(Exception e) {
					e.printStackTrace();
				}
			}

		}
		
		//클라이언트에게 서버의 메세지를 Echo해줌
		public void sendAllMsg(String name, String msg) {
			try {
				out.println("> " + name + " ==> " + msg);
			}
			catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

}
