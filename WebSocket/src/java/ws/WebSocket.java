/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ws;

import static com.sun.management.HotSpotDiagnosticMXBean.ThreadDumpFormat.JSON;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.json.Json;
import javax.json.JsonObject;
import java.util.Map;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonBuilderFactory;
import javax.json.JsonObject;
import javax.json.JsonReader;
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

    private static Set<Session> clients = Collections.synchronizedSet(new HashSet<Session>());
    private static Map<String, Session> users = Collections.synchronizedMap(new HashMap<String, Session>());

    @OnOpen
    public void onOpen(Session sesion) {
        if (clients.isEmpty()) {
            clients.add(sesion);
            users.put("servidor", sesion);
        } else {
            String queryString = sesion.getQueryString();
            String nombre = queryString.substring(queryString.indexOf("=") + 1);
            System.out.println("Open Connection ..." + sesion.toString());
            System.out.println("Usuario conectado: " + nombre);

            if (users.containsKey(nombre)) {
                System.out.println("Nombre de usuario ya existe. Cerrando conexión.");
                try {
                    sesion.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return;
            }

            // al conectarse un cliente se abre el websocket y se guarda su sesión.
            clients.add(sesion);
            users.put(nombre, sesion);

            // enviar la lista de clientes conectados al cliente que se acaba de conectar, excepto al cliente 0 y al cliente recién conectado
            List<String> connectedUsers = new ArrayList<String>(users.keySet());
            connectedUsers.remove(0); // remover el primer elemento ("servidor")
          
            // Crear un array JSON
            JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();

// Recorrer la lista de usuarios conectados
            for (String user : connectedUsers) {
                // Agregar cada usuario al array
                jsonArrayBuilder.add(user);
            }

// Crear un objeto JSON con el array de usuarios
            JsonObject json = Json.createObjectBuilder()
                    .add("connected_users", jsonArrayBuilder)
                    .build();
            synchronized (clients) {
                    for (Session client : clients) {

                            try {
                                client.getBasicRemote().sendText(json.toString());
                            } catch (IOException ex) {
                                System.out.println(ex);
                            }
                        
                    }
                }
        }
    }

    @OnClose
    public void onClose(Session sesion) {
        System.out.println("Close Connection ...");
        //al cerrarse la conexión por parte del cliente se elimina su sesión en el servidor
        clients.remove(sesion);
        for (Map.Entry<String, Session> entry : users.entrySet()) {
            if (entry.getValue().equals(sesion)) {
                users.remove(entry.getKey());
                System.out.println("Usuario desconectado: " + entry.getKey());
                break;
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        if (!clients.isEmpty() && clients.size() > 1) {

            System.out.println("Message from the client: " + message);
// Crear un JsonReader a partir de la cadena de texto
            JsonReader jsonReader = Json.createReader(new StringReader(message));

// Construir el objeto Mensaje con los valores obtenidos
            JsonObject jsonObject = jsonReader.readObject();
            Mensaje mensajeObj = new Mensaje(jsonObject.getString("nombre"),
                    jsonObject.getString("destinatario"),
                    jsonObject.getString("asunto"),
                    jsonObject.getString("remitente"),
                    jsonObject.getString("mensaje"));
// Obtener el destinatario del mensaje
            String destinatario = mensajeObj.getDestinatario();

            // Verificar si el destinatario es Todo el Publico
            if (destinatario.equals("Todo el Publico")) {
                String echoMsg = message;
                // Transmitir el mensaje a todos los clientes
                synchronized (clients) {
                    for (Session client : clients) {
                        if (!client.equals(session)) {
                            try {
                                client.getBasicRemote().sendText(echoMsg);
                            } catch (IOException ex) {
                                System.out.println(ex);
                            }
                        }
                    }
                }
            } else {
                Session destinatarioSession = users.get(destinatario);
                if (destinatarioSession != null) {
                    try {
                        destinatarioSession.getBasicRemote().sendText(message);
                    } catch (IOException ex) {
                        System.out.println(ex);
                    }
                }
            }
        }
    }

    @OnError
    public void onError(Throwable e) {
        e.printStackTrace();
    }

}
