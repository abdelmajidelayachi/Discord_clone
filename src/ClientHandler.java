import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private Socket socket;

    private String clientUserName ;

    public void broadcastMessage(String message) throws IOException {
        for (ClientHandler client: clients) {
            try{
                if(!client.clientUserName.equals(clientUserName)){
                        File sourceFile = new File(message);
                    if(sourceFile.isFile()){
                        String nameFile = client.clientUserName + sourceFile.getName();
//                            System.out.println(nameFile);
                        File destinationPath = new File("C:\\Users\\YC\\OneDrive\\Documents\\java\\course\\socket\\Document\\"+nameFile);
                        try
                        {
                            Files.move(sourceFile.toPath(),destinationPath.toPath(), REPLACE_EXISTING);
//                            Files.copy(sourceFile.toPath(),destinationPath.toPath(), REPLACE_EXISTING);
                        }catch (IOException ioe){
                            ioe.printStackTrace();
                        }
                            client.bufferedWriter.write(clientUserName+" : file -> "+ destinationPath.toURI());
                    }else{
                        client.bufferedWriter.write(message);
                    }
                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();
                }
            }catch (IOException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }

    }
    public void clientLeft() throws IOException {
        clients.remove(this);
        broadcastMessage("Server: " + clientUserName + " has left the chat!!");
    }
    public void closeEverything(Socket socketPar,BufferedReader bufferedReaderPar,BufferedWriter bufferedWriterPar) throws IOException {
        clientLeft();
        try{
            if (bufferedReaderPar != null) {
                bufferedReaderPar.close();
            }
            if (bufferedWriterPar != null) {
                bufferedWriterPar.close();
            }
            if (socketPar != null) {
                socketPar.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public ClientHandler(Socket socket) throws IOException {
        try{
            this.socket = socket;
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.clientUserName = bufferedReader.readLine();
            clients.add(this);
            broadcastMessage("Server :" + clientUserName + " has join the chat!!");
        }catch (IOException e)
        {
            closeEverything(socket,bufferedReader,bufferedWriter);
        }
    }

    @Override
    public void run() {
        String messageClient ;
        while (socket.isConnected()){
           try{
               messageClient = bufferedReader.readLine();
               broadcastMessage(messageClient);
           }catch(IOException e){
               try {
                   closeEverything(socket, bufferedReader,bufferedWriter);
               } catch (IOException ex) {
                   throw new RuntimeException(ex);
               }
               break;
           }
        }
    }
}

