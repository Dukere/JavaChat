package chatproject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Scanner;

public class MultiClient {
	
	public static void main(String[] args) {
		
		Connection con = null;
		Statement stmt = null;
		ResultSet rs;
		
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
		
		

		String s_name="";
		String name="";
		String num = "";
		String db_name = "";
		try {
		while(s_name.isEmpty()) {
		System.out.print("이름을 입력하세요:");
		Scanner sc = new Scanner(System.in);
		name = sc.nextLine();

		String sql = "select name from chatting_member where name like " + name;
		stmt = con.createStatement();
		rs = stmt.executeQuery(sql);
		while (rs.next()) {
			db_name = rs.getString("name");
		}		
		if(name.equals(db_name)){
			System.out.println("이름이 중복입니다. 다시 입력하세요.");
			continue;
		}
		s_name = name;
		String query ="insert into chatting_member values (member_seq.nextval," + s_name + ",default)";
		stmt.execute(query);
		}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		try {
			String ServerIP="localhost";
			if(args.length>0) {
				ServerIP=args[0];
			}
			Socket socket = new Socket(ServerIP,9999);
			System.out.println("서버와 연결되었습니다...");
			
			Thread receiver = new Receiver(socket);
			receiver.start();
			Thread sender = new Sender(socket,s_name);
			sender.start();
			
		}
		catch(Exception e) {
			System.out.println("예외발생[MultiClient]" + e);
		}
	}

}
