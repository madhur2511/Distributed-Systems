import java.io.*;
import java.net.*;

public class CatServer{
	public static void main(String [] args){
		ServerSocket receiverSocket = null;
		Socket ss = null;

		try{
			BufferedReader br = null;

			// TODO: Handle args error separately

			String filename = args[0];
			int port = Integer.parseInt(args[1]);
			receiverSocket = new ServerSocket(port);
			ss = receiverSocket.accept();
			DataInputStream in = new DataInputStream(ss.getInputStream());
   	    	DataOutputStream out = new DataOutputStream(ss.getOutputStream());

			System.out.println("Just connected to " + ss.getRemoteSocketAddress());
			while(true){
				String req = in.readUTF();
				String line = null;
				if(req.equals("LINE\n")){
					if (br == null)
						br = new BufferedReader(new FileReader(filename));

					line = br.readLine();

					if(line == null){
						br.close();
						br = new BufferedReader(new FileReader(filename));
						line = br.readLine();
					}
					line = line.toUpperCase();
   		     		out.writeUTF(line + "\n");
				}
				else{
					System.out.println("Received: " + in.readUTF());
				}

        	}
		}catch(Exception e){
			try{
				if(ss != null)	ss.close();
				if(receiverSocket != null) receiverSocket.close();
			}catch(IOException f){
			}
	    }
	}
}
