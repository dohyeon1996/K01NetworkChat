package chat7;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;



public class MultiServer {
	//ㅋㅋㅋ
	//멤버변수
	static ServerSocket serverSocket = null;
	static Socket socket = null;
	//클라이언트 정보저장을 위한 Map컬렉션 생성
	Map<String, PrintWriter> clientMap;
	//생성자
	public MultiServer() {
		//클라이언트의 이름과 출력스트림을 저장할 HaspMap컬렉션생성
		clientMap=new HashMap<String, PrintWriter>();
		//HashMap 동기화 설정 쓰레드가 사용자정보에 동시에 접근하는것을차단함
		Collections.synchronizedMap(clientMap);
		
		//실행부없음
	}

	//채팅 서버 초기화
	public void init() {

		try {
			serverSocket = new ServerSocket(9999);
			System.out.println("서버가 시작되었습니다.");
			
			/*
			1명의 클라이언트가 접속할때마다 접속을 허용(accept())해주고
			동시에 MultiServerT 쓰레드를 생성한다. 
			해당 쓰레드는 1명의 클라이언트가 전송하는 메세지를 읽어서 Echo
			해주는 역할을 담당한다.  
			 */
			while(true) {
				socket = serverSocket.accept();
				System.out.println(socket.getInetAddress()+"(클라이언트)의"+
						socket.getPort()+ "포트를 통해 "+
						socket.getLocalAddress()+"(서버)의"+
						socket.getLocalPort()+"포트로 연결되었습니다.");
				System.out.println(socket.getInetAddress()+":"+socket.getPort());
				
				//내부클래스의 객체 생성 및 쓰레드시작
				//클라이언트 한명당 하나씩의 쓰레드가 생성된다.
				Thread mst = new MultiServerT(socket);
				
				mst.start();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			try {
				serverSocket.close();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	chat4까지는 init()이 static이었으나, chat5부터는 일반적인
	메소드로 변경되었다. 따라서 객체를 생성후 호출하는 방식으로 변경된다.
	 */
	public static void main(String[] args) {
		MultiServer ms = new MultiServer();
		ms.init();
	}
	//내부클래스
	/*
	init()에 기술되었던 스트림을 생성후 메세지를 읽기/쓰기 하던 부분이
	해당 내부클래스로 이동되었다. 
	 */
	class MultiServerT extends Thread {

		Socket socket;
		PrintWriter out = null;
		BufferedReader in = null;

		/*
		생성자 : 1명의 클라이언트가 접속할때 생성했던 Socket객체를
			매개변수로 받아 이를 기반으로 입출력 스트림을 생성하고있다.
		 */
		public MultiServerT(Socket socket) {
			this.socket = socket;
			try {
				out = new PrintWriter(this.socket.getOutputStream(), true);
				in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
			}
			catch (Exception e) {
				System.out.println("예외:"+ e);
			}
		}

		/*
		쓰레드로 동작할 run()에서는 클라이언트의 
		접속자명과 메세지를 지속적으로 읽어 Echo 해주는 역할을
		하고있다.
		 */
		@Override
		public void run() {

			String name = "";
			String s = "";

			try {
				//클라이언트의 이름을 불러온다. 
				name=in.readLine();
				/*
				방금 접속한 클라이언트를 제외한 나머지에게 입장을 알린다. 
				 */
				sendAllMsg(" ", name+"님이입장하셨습니다.","All");
				//현재 접속한 클라이언트르 HashMap에 저장한다.
				clientMap.put(name, out);
				//접속자의 이름을 서버에 콘솔에 띄워주고
				System.out.println(name+"접속");
				//HAshMap에 저장된 객체의 수로 현재 접속자를 파악할수있다. 
				System.out.println("현재접속자수는 "+clientMap.size()+"명입니다");
				
				//입력한 메세지는 모든 클라이언트에게 Echo된다. 
				while(in!=null) {
					s=in.readLine();
					if(s==null)
						break;
					System.out.println(name+">>"+s);
					//클라이언트 측으로 전송한다. 
					if(s.charAt(0)=='/') {
						String[] strArr=s.split(" ");
						String msgContent="";
						for(int i=2;i<strArr.length;i++) {
							msgContent +=strArr[i]+" ";
						}
						if(strArr[0].equals("/to")) {
							sendAllMsg(strArr[1], msgContent,"One");
						}
					}
					else {
						sendAllMsg(name, s,"All");
					}
				}
			} 
			catch (Exception e) {
				System.out.println("예외:"+ e);
			}
			finally {
				/*
				클라이언트가 접속을 종료하면 Socket예외가 발생하게되어
				finally절로 진입하게된다. 이때 "대화명"을 통해 정보를 삭제한다. 
				 */
				clientMap.remove(name);
				sendAllMsg("", name+"님이퇴장하셨습니다.","All");
				System.out.println(name+" ["+Thread.currentThread().getName()+"]퇴장");
				System.out.println("현재 접속자수는"+clientMap.size()+"명입니다");

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
		//접속된 모든 클라이언트 측으로 서버의 메세지를 Echo해주는 역활담당
		public void sendAllMsg(String name, String msg, String flag)
		{
			//Map에 저장된 객체의 키값(대화명)을 먼저얻어온다. 
			Iterator<String> it=clientMap.keySet().iterator();
			//저장된 객체(클라이언트)의 갯수만큼 반복한다. 
			while(it.hasNext()) {
				try {
					//컬렉션의 Key는 클라이언트대화명이다. 
					String clientName=it.next();
					//각 클라이언트의 PrintWriter  객체를 얻어온다. 
					PrintWriter it_out=(PrintWriter)
							clientMap.get(clientName);
					if(flag.equals("One")) {
						//flag가 One이면 해당클라이언트 한명에게만전송한다.(귓속말)
						if(name.equals(clientName)) {
							//컬렉션에 저장된 접속자명과 일치하는 경우에만 메세지를전송한다.
							it_out.println("[귓속말]"+msg);
						}
						
					}
					else {
						//그외에는 모든클라이언트에게 전송한다. 
						if(name.equals("")) {
							//입장,퇴장에서 사용되는 부분
							it_out.println(msg);
						}
						else {
							//메세지를 보낼때사용되는 부분
							it_out.println("["+name+"]"+msg);
						}
					}
					/*
					클라이언트에게 메세지를 전달할때 매개변수로 name이
					있는경우와 없는경우를 구분해서 전달하게 된다. 
					 */
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			
		
		}
	}
}
