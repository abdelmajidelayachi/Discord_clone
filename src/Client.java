import java.io.*;
import java.net.Socket;
import java.nio.file.*;
import java.util.Scanner;

public class Client {

    private static DataOutputStream dataOutputStream = null;
    private static DataInputStream dataInputStream = null;
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private Socket socket;
    private String clientUserName ;

    public Client(Socket socket, String username){
        try{
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.clientUserName = username;
        }catch (IOException e){
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    public void sendFile(String path) throws IOException {
        int bytes = 0;
        // Open the File where he located in your pc

        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
        File file = new File(path);
        FileInputStream fileInputStream
                = new FileInputStream(file);

        // Here we send the File to Server
        dataOutputStream.writeLong(file.length());
        // Here we  break file into chunks
        byte[] buffer = new byte[4 * 1024];
        while ((bytes = fileInputStream.read(buffer))
                != -1) {
            // Send the file to Server Socket
            dataOutputStream.write(buffer, 0, bytes);
            dataOutputStream.flush();
        }
        // close the file here
        fileInputStream.close();


    }
    public  void sendMessage(){
        try{
            bufferedWriter.write(clientUserName);
            bufferedWriter.newLine();
            bufferedWriter.flush();

            Scanner scanner = new Scanner(System.in);
            while (socket.isConnected()){
                    int typeInt = 0;
                while (true) {
                    System.out.println("Select the type of message");
                    System.out.println("1 : Text");
                    System.out.println("2 : file");
                    String messageType = scanner.nextLine();
                    try {
                        typeInt =Integer.parseInt(messageType);
                        if (typeInt == 1 || typeInt == 2) {
                            break;
                        }
                    } catch (Exception e) {
                        System.out.println("Pls enter the correct type 1 or 2. " + e.getMessage());
                    }
                }

                if(typeInt == 1){
                    System.out.print("Enter the your message : ");
                    String getMessageToSend = scanner.nextLine();
                    bufferedWriter.write("text:"+clientUserName + ":"+ getMessageToSend );
                } else {
                    System.out.print("Enter file path : ");
                    String getMessageToSend = scanner.nextLine();
                    Path path = Path.of(getMessageToSend);
                    bufferedWriter.write("file:"+clientUserName + ":"+ path.getFileName());
//                    System.out.println(path);
                    sendFile(path.toString());
                }
                bufferedWriter.newLine();
                bufferedWriter.flush();
            }
        }catch (IOException e){
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    public void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter){
        try{
            if (socket != null) {
                socket.close();
            }
            if (bufferedReader != null){
                bufferedReader.close();
            }
            if (bufferedWriter != null) {
                bufferedWriter.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void listenToNewMessage(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String messageFromGroup ;
                while (socket.isConnected()){
                    try{
                        messageFromGroup = bufferedReader.readLine();
                        System.out.println(messageFromGroup);
                    }catch (IOException e){
                        closeEverything(socket,bufferedReader,bufferedWriter);
                    }
                }
            }
        }).start();
    }

    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter Your Username : ");
        String username = scanner.nextLine();
        Socket socket1 = new Socket("127.0.0.1", 5000);
        Client client = new Client(socket1,username);
        client.listenToNewMessage();
        client.sendMessage();
    }
}

