import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;


public class ClientHandler implements Runnable {
    public static ArrayList<ClientHandler> clients = new ArrayList<>();
    private BufferedWriter bufferedWriter;
    private BufferedReader bufferedReader;
    private static Socket socket;

    private String clientUserName ;

    public void broadcastMessage(String message) throws IOException {
        for (ClientHandler client: clients) {
            try{
                if(!client.clientUserName.equals(clientUserName)){
                    String[] messageContent = message.split(": ");
                    if(messageContent[0].equals("file")){
//                        File destinationPath = new File("Document/"+System.currentTimeMillis()+messageContent[2]);
//                        System.out.println("hello" + destinationPath.getPath());
                        String  sender = messageContent[1];
                        String dataFile = messageContent[2];
                        String fileName = messageContent[3];

                         receiveFile(sender,dataFile,fileName,client);

                    }else if (messageContent[0].equals("text")){
                        client.bufferedWriter.write(messageContent[1] + " : " + messageContent[2]);
                    }

                    client.bufferedWriter.newLine();
                    client.bufferedWriter.flush();

                }
            }catch (IOException e){
                closeEverything(socket,bufferedReader,bufferedWriter);
            }
        }

    }

    public static void receiveFile(String sender,String dataFile, String fileName,ClientHandler client) throws IOException {
        String downloadPath = System.getProperty("user.dir") + File.separator + "download" + File.separator + fileName;
        File downloadFolder = new File(System.getProperty("user.dir") + File.separator + "download");
        if(!downloadFolder.exists()) downloadFolder.mkdir();
        File file  = new File(downloadPath);
        client.bufferedWriter.write("File is Downloading...");
        try{
            file.createNewFile();
        }catch (IOException ioException)
        {
            ioException.printStackTrace();
        }
        byte[] fileArrayBytes = convertStringDataToBytes(dataFile);
        try{
            FileOutputStream fileOutputStream1 =  new FileOutputStream(file);
            fileOutputStream1.write(fileArrayBytes);
            fileOutputStream1.close();
            client.bufferedWriter.write(">> "+sender+" : "+downloadPath);
        }catch (IOException ioException){
            ioException.printStackTrace();
        }

    }
    public static byte[] convertStringDataToBytes(String dataString){
        String[] arrayString = dataString.substring(1,dataString.length()-1).split(", ");
        byte[] bytes = new byte[arrayString.length];
        for (int i = 0; i < arrayString.length; i++) {
            bytes[i] = Byte.parseByte(arrayString[i]);
        }
        return bytes;
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
            ClientHandler.socket = socket;
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

