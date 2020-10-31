package be4rjp.sclat.server;

import be4rjp.sclat.Main;
import be4rjp.sclat.Sclat;
import be4rjp.sclat.data.DataMgr;
import be4rjp.sclat.manager.PlayerStatusMgr;
import be4rjp.sclat.manager.SettingMgr;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import static be4rjp.sclat.Main.conf;

public class EquipmentServer extends Thread {
    
    private ServerSocket sSocket = null;
    
    //private List<String> commands = new ArrayList<>();
    
    private final int port;
    
    public EquipmentServer(int port){
        this.port = port;
    }
    
    public void run(){
        try{
            //ソケットを作成
            sSocket = new ServerSocket(port);
            System.out.println("Waiting for status client...");
            
            //クライアントからの要求待ち
            while (true) {
                Socket socket = sSocket.accept();
                new EquipEchoThread(socket).start();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                if (sSocket!=null)
                    sSocket.close();
                System.out.println("Equipment server is stopped!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}


//非同期スレッド
class EquipEchoThread extends Thread {
    
    private Socket socket;
    
    public EquipEchoThread(Socket socket) {
        this.socket = socket;
        System.out.println("Connected " + socket.getRemoteSocketAddress());
    }
    
    public void run() {
        try {
            System.out.println("Waiting for commands...");
            //クライアントからの受取用
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
            //サーバーからクライアントへの送信用
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            
            String cmd = null;
            //命令受け取り用ループ
            while (true) {
                if((cmd = reader.readLine()) != null) {
                    
                    if(cmd.equals("stop")){
                        socket.close();
                        System.out.println("Socket closed.");
                        return;
                    }
                    
                    System.out.println(cmd);
                    
                    EquipmentServerManager.addEquipmentCommand(cmd);
    
                    String args[] = cmd.split(" ");
                    if(args[0].equals("setting")){ //setting [settingData] [uuid]
                        if(args.length == 3){
                            if(args[1].length() == 9 && args[2].length() == 36){
                                conf.getPlayerSettings().set("Settings." + args[2], args[1]);
                            }
                        }
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {}
            System.out.println("Disconnected " + socket.getRemoteSocketAddress());
        }
    }
    
}
