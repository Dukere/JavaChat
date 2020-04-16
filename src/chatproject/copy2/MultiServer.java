package chatproject.copy2;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public class MultiServer {

	static ServerSocket serverSocket = null;
	static Socket socket = null;
	Connection con;
	Statement stmt;
	PreparedStatement psmt;
	ResultSet rs;
	boolean hold = false;
	String holdName = "";
	ArrayList<String[]> rooms = new ArrayList<String[]>();

	Map<String, PrintWriter> clientMap;

	public MultiServer() {
		clientMap = new HashMap<String, PrintWriter>();
		Collections.synchronizedMap(clientMap);

		try {
			Class.forName("oracle.jdbc.OracleDriver");
			String url = "jdbc:oracle:thin://@localhost:1521:orcl";
			String userid = "KOSMO";
			String userpw = "1234";
			con = DriverManager.getConnection(url, userid, userpw);

			if (con != null) {
				System.out.println("Oracle DB 연결성공");
			} else {
				System.out.println("연결실패ㅠㅠ");
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 서버의 초기화를 담당할 메소드
	public void init() {

		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");

			while (true) {
				socket = serverSocket.accept();
				Thread mst = new MultiServerT(socket, "waiting");
				mst.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				serverSocket.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();

	}

	public void sendAllMsg(String name, String msg) {
		Iterator<String> it = clientMap.keySet().iterator();
		while (it.hasNext()) {
			try {
				PrintWriter it_out = (PrintWriter) clientMap.get(it.next());
				if (name.equals("")) {
					it_out.println(URLEncoder.encode(msg, "UTF-8"));
				} else {
					it_out.println("[" + URLEncoder.encode(name, "UTF-8") + "]:" + URLEncoder.encode(msg, "UTF-8"));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String checkingRoom(String name) {
		int check = 0;
		Iterator<String[]> it = rooms.iterator();
		while (it.hasNext()) {
			String[] arr = it.next();
			for (int i = 1; i < arr.length; i++) {
				if (name.equals(arr[i])) {
					return arr[0];
				}
			}
		}
		return null;
	}

	public void sendroomMsg(String name, String msg) {
		Iterator<String> it = clientMap.keySet().iterator();
		while (it.hasNext()) {
			try {
				String checkName = it.next();
				String roomName = checkingRoom(name);
				String[] checkRoom = null;
				Iterator<String[]> it_room = rooms.iterator();
				while (it_room.hasNext()) {
					checkRoom = it_room.next();
					if (checkRoom[0].equals(roomName)) {
						for (int i = 1; i < checkRoom.length; i++) {
							PrintWriter it_out = (PrintWriter) clientMap.get(checkRoom[i]);
							if (checkName.equals(checkRoom[i])) {
								it_out.println("[" + URLEncoder.encode(name, "UTF-8") + "]:"
										+ URLEncoder.encode(msg, "UTF-8"));
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	class MultiServerT extends Thread {

		String name;
		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;

		public MultiServerT(Socket socket, String room) {
			this.name = room;
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream(), "UTF-8"));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			String name = "";
			String s = "";
			String sql = "insert into chatting_tb values(chat_sq.nextval,?,?,default)";

			try {
				name = in.readLine();
				name = URLDecoder.decode(name, "UTF-8");

				sendroomMsg("", name + "님이 입장하셨습니다.");

				clientMap.put(name, out);

				System.out.println(name + " 접속");
				System.out.println("현재 접속자 수는 " + clientMap.size() + "명 입니다.");

				while (in != null) {
					s = in.readLine();
					s = URLDecoder.decode(s, "UTF-8");
					if (s == null) {
						break;
					}
					if (hold) {
						command(s, name);
						continue;
					} else if (s.charAt(0) == '/') {
						command(s, name);
						continue;
					}
					System.out.println(name + " >> " + s);
					sendroomMsg(name, s);
					psmt = con.prepareStatement(sql);
					psmt.setString(1, name);
					psmt.setString(2, s);
					psmt.executeUpdate();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				clientMap.remove(name);
				sendroomMsg("", name + "님이 퇴장하셨습니다.");
				System.out.println(name + " [" + Thread.currentThread().getName() + "] 퇴장");
				System.out.println("현재 접속자 수는 " + clientMap.size() + "명 입니다.");
				try {
					in.close();
					out.close();
					socket.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}

		public void showlist() {
			Iterator<String> mapIter = clientMap.keySet().iterator();
			int i = 1;
			out.println("===접속자 명단===");
			while (mapIter.hasNext()) {

				String name = mapIter.next();
				try {
					out.println(URLEncoder.encode(i + "." + name, "UTF-8"));
					out.println("================");
				} catch (Exception e) {
					e.printStackTrace();
				}
				i++;
			}
		}

		public void command(String s, String myName) {

			StringTokenizer st = new StringTokenizer(s);
			String sql = "insert into chatting_tb values(chat_sq.nextval,?,?,default)";

			String com = st.nextToken(" ");
			String name = "";
			String chat = "";
			int i = 0;

			if (s.equals("/list")) {
				showlist();
			} else if (com.equals("/to")) {
				if (hold) {
					out.println("귓속말 해제");
					hold = false;
					return;
				} else if (st.hasMoreTokens()) {
					name = st.nextToken(" ");
				} else {
					out.println("귓속말 상대를 입력하세요.");
					return;
				}
				while (st.hasMoreTokens()) {
					chat = chat + st.nextToken() + " ";
					i++;
				}
				try {
					if (i == 0) {
						if (!hold) {
							out.println("귓속말 고정 (/to 입력 시 해제)");
							hold = true;
							holdName = name;
							return;
						}

					} else {
						out.println("귓속말 to " + name + " : " + chat);

						Iterator<String> it = clientMap.keySet().iterator();
						while (it.hasNext()) {
							String a = it.next();
							PrintWriter it_out = (PrintWriter) clientMap.get(a);
							if (a.equals(name)) {
								it_out.println(URLEncoder.encode(myName + "님의 귓속말 : " + chat, "UTF-8"));
							}
						}
						psmt = con.prepareStatement(sql);
						psmt.setString(1, myName);
						psmt.setString(2, chat + "(to " + name + ")");
						psmt.executeUpdate();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (hold) {
				out.println("귓속말 to " + holdName + " : " + s);
				try {
					Iterator<String> it = clientMap.keySet().iterator();
					while (it.hasNext()) {
						String a = it.next();
						PrintWriter it_out = (PrintWriter) clientMap.get(a);
						if (a.equals(holdName)) {
							it_out.println(URLEncoder.encode(myName + "님의 귓속말 : " + s, "UTF-8"));
						}
					}
					psmt = con.prepareStatement(sql);
					psmt.setString(1, myName);
					psmt.setString(2, s + "(to " + holdName + ")");
					psmt.executeUpdate();
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else if (com.equals("/make")) {
				if (st.hasMoreTokens()) {
					name = st.nextToken(" ");
					String[] room = new String[100];
					room[0]=name;
					room[1]=myName;
					rooms.add(room);
					out.println("룸생성완료");
					if (st.hasMoreTokens()) {
						out.println("형식이 다릅니다. (/make 방 이름)");
					}

				} else {
					out.println("방 이름을 입력하세요. (/make 방 이름)");
					return;
				}
			}else if(com.equals("/enter")) {
				if (st.hasMoreTokens()) {
					name = st.nextToken(" ");
					Iterator<String[]> it_room = rooms.iterator();
					while (it_room.hasNext()) {
						String[] checkRoom = it_room.next();
						if (checkRoom[0].equals(name)) {
							}
						}
					
					if (st.hasMoreTokens()) {
						out.println("형식이 다릅니다. (/enter 방 이름)");
					}

				} else {
					out.println("방 이름을 입력하세요. (/enter 방 이름)");
					return;
				}
			}else {
				out.println("명령어 목록 : /list, /to, /make");
			}
		}

	}

}
