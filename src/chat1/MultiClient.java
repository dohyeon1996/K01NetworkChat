package chat1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
   
 
public class MultiClient {

	public static void main(String[] args) {
		
		
		System.out.println("이름을 입력하세요:");
		Scanner scanner = new Scanner(System.in);
		String s_name = scanner.nextLine();
		PrintWriter out = null;
		BufferedReader in  = null;
		
		try {
			String ServerIP = "localhost";
			if(args.length>0) {
				ServerIP = args[0];
				
			}
			Socket socket = new Socket(ServerIP, 9999);
			System.out.println("서버와 연결되었습니다....");
			
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			out.println(s_name);
			System.out.println("Receive:"+in.readLine());
			
			in.close();
			out.close();
			socket.close();
			
		}
		catch(Exception e) {
			System.out.println("예외발생[MultiClient]"+ e);
		}
	}

}
