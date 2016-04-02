import java.io.*;
import java.net.*;


class CatClient{
	public static void main(String[] args){
		Socket clientSocket = null;
		try{
			String filename = args[0];
			int port = Integer.parseInt(args[1]);
			BufferedReader br = null;
			clientSocket = new Socket(InetAddress.getByName("127.0.0.1"), port);
			System.out.println("Just connected to " + clientSocket.getRemoteSocketAddress());

			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
       	  	DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			String line = null;
			String response = null;
			br = new BufferedReader(new FileReader(filename));

			for(int i = 0; i < 10; ++i){
       		 	out.writeUTF("LINE\n");
        		response = (in.readUTF()).trim();
				System.out.println(response);		
				line = br.readLine();
				if (line == null){
					br = new BufferedReader(new FileReader(filename));
					line = br.readLine();
				}
				

				if (response.equals(line.toUpperCase()))
					System.out.println("OK\n");		
				else
					System.out.println("MISSING\n");
				
				try{
	   			 Thread.sleep(1000);
				}catch(InterruptedException e){
   				 System.out.println("got interrupted!");
				}	
			}
			clientSocket.close();
			
		}catch(Exception e){
			e.printStackTrace();
		}	
	}
}
