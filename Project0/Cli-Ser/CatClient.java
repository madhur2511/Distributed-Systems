import java.io.*;
import java.net.*;

class CatClient{
	public static void main(String[] args){
		Socket clientSocket = null;
		try{
			String filename = args[0];
			int port = Integer.parseInt(args[1]);
			BufferedReader br = null;
			clientSocket = new Socket("CatServer", port);

			DataOutputStream out = new DataOutputStream(clientSocket.getOutputStream());
       	  	DataInputStream in = new DataInputStream(clientSocket.getInputStream());
			String line = null;
			String response = null;
			br = new BufferedReader(new FileReader(filename));

			for(int i = 0; i < 10; ++i){
       		 	out.writeUTF("LINE\n");
        		response = (in.readUTF()).trim();
				line = br.readLine();
				if (line == null){
					br = new BufferedReader(new FileReader(filename));
					line = br.readLine();
				}
				if (response.equals(line.toUpperCase()))
					System.out.print("OK\n");
				else
					System.out.print("MISSING\n");
				try{
	   				Thread.sleep(3000);
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
