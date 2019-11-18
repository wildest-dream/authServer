package authServer;
import java.net.*;
import java.sql.*;
import java.util.Random;
import java.io.*;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
public class authServer {
	private ServerSocket server;
	static private String voice;//temporally share the same voice between password and voice authentication ClientHandler
	                     //in real cases, voice variable should be transported between different ClientHandler(Thread) and be owned by each thread
	public void start(int port) {
		try {
			server=new ServerSocket(port);
			while(true) {
				new clientHandler(server.accept()).start();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void stop() {
		try {
			server.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static class clientHandler extends Thread{
		private Socket clientSocket;
		private PrintWriter writer;
		private BufferedReader reader;
		private ResultSet res;
		private String inputLine,id,pwd,query;
		private PreparedStatement stmt;
		public static final String ACCOUNT_SID =
	            "AC0bd8a502cc087419203ccd742052a3b0";
	    public static final String AUTH_TOKEN =
	            "37b1e60ac11c2c21afd4be4245516fb2";
		public clientHandler(Socket socket) {
			// TODO Auto-generated constructor stub
			this.clientSocket=socket;
			this.res=null;
			this.inputLine=this.id=this.pwd=this.query=null;
			this.stmt=null;
		}
		public void run() {
			try {
				reader=new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
				writer=new PrintWriter(clientSocket.getOutputStream(),true);
				inputLine=reader.readLine();
				System.out.println("Client connected\n");
				Connection connection=DriverManager.getConnection("jdbc:mysql://localhost:3306/authDB?serverTimezone=EST5EDT&allowPublicKeyRetrieval=true&useSSL=false", "root", "Dqy.960330ll");
				                                                 //connect to database
				System.out.println("database connected\nClient content:\n"+inputLine+"\n");
				inputLine=reader.readLine();
				System.out.println(inputLine+"\n");
				if(inputLine.equals("Password Authentication")) {
					while((inputLine=reader.readLine())!=null) {
						System.out.println(inputLine+"\n");
						if(inputLine.startsWith("id:")) {
							id=inputLine.split(":")[1];
							query="select pwd,tel from password where user_id=?";
							stmt=connection.prepareStatement(query);//search password and telephone number
							stmt.setString(1, id);
							res=stmt.executeQuery();
							if(!res.next()) {//test if there is no row in the result, if not, move the cursor to the first
								writer.println("No such id! Session terminated.\n");
								stmt.close();
								break;
							}
						}else if(inputLine.startsWith("pwd:")) {
							pwd=inputLine.split(":")[1];
							if(res==null) {
								writer.println("Content error. Session terminated.\n");//no id, only pwd
							}else {
								if(res.getString(1).equals(pwd)) {//test if the password is correct
									writer.println("Password correct! A short meesage has been sent to your phone!\n");
									Random rand=new Random();//generate random message
									int msg=rand.nextInt(4);
									switch(msg) {
										case 0:   
											voice="Zoos are filled with small and large animals";
											break;
										case 1:
											voice="Remember to wash your hands before eating";
											break;
										case 2:
											voice="I am very hungry after working all day";
											break;
										case 3:
											voice="Bit coin is the payment method of the future";
											break;
										default:
											voice="Zoos are filled with small and large animals";
											break;
									}
									Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
							        Message message = Message
							                .creator(new PhoneNumber(res.getString(2)), // to
							                        new PhoneNumber("+12018904689"), // from
							                        "\nPlease record the message below using your voice:\n\n"
							                        +voice)
							                .create();//send message to target phone
							        System.out.println("Valid user. Message sent. "+message.getDateCreated().toString()+"\n");
								}else {
									writer.println("Password wrong! Session terminated.\n");
								}
								stmt.close();
							}
							break; 
						}else {
							writer.println("Content Error. Session terminated.\n");//no id
						}
					}
				}else if(inputLine.equals("Voice Authentication")) {
					inputLine=reader.readLine();
					System.out.println(inputLine);
					if(!inputLine.startsWith("id:")) {
						writer.println("Content Error. Session Terminated.\n");
					}else {
						id=inputLine.split(":")[1];
					    InputStream is = clientSocket.getInputStream();//receive audio file from client
					    FileOutputStream fos = new FileOutputStream("template.3gp");
					    BufferedOutputStream bos = new BufferedOutputStream(fos);
					    byte[] byteArray=is.readAllBytes();
					    bos.write(byteArray, 0, byteArray.length);
					    bos.close();
					    fos.close();
					    //is.close();
					    clientSocket.shutdownInput();//don't use is.close(),it will close the clientSocket
					    File file=new File("template.3gp");
					    VoiceIt2 myVoiceIt=new VoiceIt2("key_56892fd254944f6798ae7b4f9f255ed9","tok_619bb122dcb54b50baa32c8ceae34896");
					    System.out.println(myVoiceIt.getAllVoiceEnrollments("usr_3350eb95bb694ac28b6555a345a324c2"));//temporally assume this is user id, but it should set up a table to store the matching relationship between the user ids in my server and the VoiceIt server 
					    String verify=myVoiceIt.voiceVerification("usr_3350eb95bb694ac28b6555a345a324c2", "en-US", voice, file);//voice verification
					    //System.out.println(myVoiceIt.deleteVoiceEnrollment("usr_3350eb95bb694ac28b6555a345a324c2", 63882));
					    //System.out.println(myVoiceIt.createVoiceEnrollment("usr_3350eb95bb694ac28b6555a345a324c2", "en-US", "", file));
					    //System.out.println(myVoiceIt.voiceVerification("usr_3350eb95bb694ac28b6555a345a324c2", "en-US", "never forget tomorrow is a new day", file));
					    System.out.println(verify);
					    if(verify.endsWith("\"SUCC\",\"status\":200}")) {//check the authentication result according to the JSON message it returns
					    	writer.println("Congratulations! Voice Authentication Successful!\n");
					    }else {
					    	writer.println("Voice Authentication Failed! Session Terminated!\n");
					    }
					}
				}else {
					writer.println("Content Error. Session Terminated.\n");//no Password Authentication
				}
				writer.close();
				reader.close();
				clientSocket.close();
				connection.close();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		new authServer().start(8998);
	}

}
