/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ws;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 *
 * @author josej
 */
@ServerEndpoint("/websocketendpoint")
public class WebSocket {
	//para guardar la sesión de cada cliente y poder replicar el mensaje a cada uno
	//se hace una colección sincronizada para el manejo de la concurrencia
    private static Set<Session> clients = 
            Collections.synchronizedSet(new HashSet<Session>());    
    
    @OnOpen
    public void onOpen(Session sesion){
        System.out.println("Open Connection ...");
		//al conectarse un cliente se abre el websocket y se guarda su sesión.
        clients.add(sesion);
    }
     
    @OnClose
    public void onClose(Session sesion){
        System.out.println("Close Connection ...");
		//al cerrarse la conexión por parte del cliente se elimina su sesión en el servidor
        clients.remove(sesion);
    }
     
    @OnMessage
    public void onMessage(String message, Session sesion){        
        System.out.println("Message from the client: " + message);
        String echoMsg = "Echo from the client " + sesion.getId() + ": " + message;
		//se hace un bloque sincronizado para manejar la concurrencia, tal como en los sockets e hilos
        synchronized(clients){
          // Se itera sobre la sesiones (clientes) guardados para transmitir el mensaje
          for(Session client : clients){
            if (!client.equals(sesion)){
                try {
                    client.getBasicRemote().sendText(echoMsg);
                } catch (IOException ex) {
                    System.out.println(ex);
                }
            }
          }
        }        
    }
 
    @OnError
    public void onError(Throwable e){
        e.printStackTrace();
    }    
    
}
