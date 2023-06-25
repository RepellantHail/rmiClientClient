package com.example.rmiclientclient;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

public class ChatServerImpl extends UnicastRemoteObject implements ChatServer {
    private static final int RMI_PORT = 1099;
    private List<ChatClient> clients;

    public ChatServerImpl() throws RemoteException {
        clients = new ArrayList<>();
    }

    public static void main(String[] args) {
        try {
            // Crear instancia del servidor
            ChatServerImpl server = new ChatServerImpl();

            // Registrar el servidor en el registro RMI
            Registry registry = LocateRegistry.createRegistry(RMI_PORT);
            registry.rebind("ChatServer", server);

            System.out.println("Servidor RMI en ejecución...");
        } catch (RemoteException e) {
            System.err.println("Error al iniciar el servidor RMI: " + e.getMessage());
        }
    }

    @Override
    public void register(String name, ChatClient client) throws RemoteException {
        clients.add(client);
        client.notifyUserJoined(name);
        notifyClients(client);
    }

    private void notifyClients(ChatClient newClient) {
        try {
            List<String> userList = getUserList();
            newClient.updateUserList(userList);
            broadcastUserList(userList);
        } catch (RemoteException e) {
            // Manejar la excepción apropiadamente
        }
    }

    @Override
    public List<String> getUserList() throws RemoteException {
        List<String> userList = new ArrayList<>();
        for (ChatClient client : clients) {
            userList.add(client.getName());
        }
        return userList;
    }
    @Override
    public void unregister(ChatClient client) throws RemoteException {
        clients.remove(client);
        System.out.println("Cliente eliminado: " + client.getName());
    }

    @Override
    public void broadcastMessage(String sender, String message) throws RemoteException {
        String formattedMessage = sender + ": " + message;
        for (ChatClient client : clients) {
            client.receiveMessage(formattedMessage);
        }
    }

    private void broadcastUserList(List<String> userList) {
        for (ChatClient client : clients) {
            try {
                client.updateUserList(userList);
            } catch (RemoteException e) {
                // Manejar la excepción apropiadamente
            }
        }
    }
    @Override
    public synchronized void sendMessage(String sender, String message) throws RemoteException {
        String formattedMessage = sender + ": " + message;
        System.out.println(formattedMessage);
        broadcastMessage(sender,formattedMessage);
    }

    @Override
    public void sendMessageToClient(String recipient, String message) throws RemoteException {
        for (ChatClient client : clients) {
            if (client.getName().equals(recipient)) {
                String formattedMessage = "[Private] " + client.getName() + ": " + message;
                client.receiveMessage(formattedMessage);
                break;
            }
        }
    }
}