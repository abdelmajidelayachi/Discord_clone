import java.io.*;
import java.net.Socket;
import java.net.URI;
import java.nio.file.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.nio.file.StandardCopyOption.*;

public class Client {
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
                        System.out.println("Pls enter the correct type 1 or 2. "+e.getMessage());
                    }
                }

                if(typeInt == 1){
                    String getMessageToSend = scanner.nextLine();
                    bufferedWriter.write(clientUserName + ": "+ getMessageToSend );
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                } else {
                    String getMessageToSend = scanner.nextLine();
                    Path path = Path.of(getMessageToSend);
                    bufferedWriter.write(path.toFile().getPath());
                    bufferedWriter.newLine();
                    bufferedWriter.flush();
                }
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

