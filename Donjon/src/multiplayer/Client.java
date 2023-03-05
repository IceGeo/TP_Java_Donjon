package multiplayer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import game.*;
import gui.MapPanel;


public class Client {
	private InetAddress servAdr;
	private boolean isConnected, waitServer;
	private final int port = 2302;
	private Socket socket;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	public int idxPlayer;
	private Scanner sc;
	private char symbol;
	public Map map;
	public MapPanel mapPane;
	public InfoGame info;
	public Client(String nom, String classe, String adr) {
		isConnected = connexion(adr);
		waitServer = true;
		try {
			out.writeObject(nom);
			out.writeObject(classe);
			symbol = '$';
			idxPlayer = (Integer)in.readObject();
			System.out.println("Index Player = " + idxPlayer);
			info = (InfoGame)in.readObject();
			//char[][] charmap = (char[][])in.readObject();
			//map = Utils.viewMap(info.map);
			//map.refresh(info);
		} catch (Exception e) {
			isConnected = false;
			e.printStackTrace();
		}
		
	}
	public boolean connexion(String adr) {
		isConnected = true;
		try {
			if (adr.equals(""))	servAdr = InetAddress.getByName("localhost");
			else servAdr = InetAddress.getByName(adr);
			socket = new Socket(servAdr, port);
			in = new ObjectInputStream(socket.getInputStream());
			out = new ObjectOutputStream(socket.getOutputStream());
		} catch (Exception e) {
			isConnected = false;
			e.printStackTrace();
		}
		return isConnected;
	}
	
	/**
	 * Le client attend l'infogame du serveur, si le serveur envoie ready, il renvoie null
	 * @return
	 */
	public InfoGame waitServer() {
		String msg = "";
		String order;
		try {
			System.out.println("attend retour serveur");
			msg = (String)in.readObject();
			System.out.println("serveur a envoy� " + msg);
			if (msg.equals("ready")) {
				System.out.println("Que faites-vous ?");
				waitServer = false;
				return null;
				//order = sc.nextLine();
				//sendOrder(order);
				// c'est au tour de ce joueur...
			} else if (msg.equals("map")) {
				//System.out.println("map received");
				info = (InfoGame)in.readObject();
				System.out.println("Map re�ue du perso : " + info.persoName);
				//mapPane.refresh(info);
				return info;
			} else {
				System.out.println(msg);
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
		return null;
	}
	public void sendOrder(String order) {
		try {
			System.out.println("Ordre : <" + order + "> d�tect� !");
			out.writeObject(order);
			
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
	
	public static void main(String[] args) throws Exception {
		// tests
		String msg;
		String order;
		Scanner sc = new Scanner(System.in);
		System.out.println("Adresse serveur : ");
		String adr = sc.nextLine();
		System.out.println("Nom du personnage : ");
		String nom = sc.nextLine();
		System.out.println("Classe : ");
		String classe = sc.nextLine();
		//System.out.println("Symbol : ");
		Client cl = new Client(nom, classe, adr);
		cl.map = Utils.viewMap(cl.info.map);
		cl.map.refresh(cl.info);
		while (true) {
			// attend les retours du serveur
			try {
				msg = (String)cl.in.readObject();
				if (msg.equals("ready")) {
					System.out.println("Que faites-vous ?");
					order = sc.nextLine();
					cl.out.writeObject(order);
				} else if (msg.equals("map")) {
					//System.out.println("map received");
					InfoGame info = (InfoGame)cl.in.readObject();
					//System.out.println(Game.mapToString(info.map));
					cl.map.refresh(info);
				} else {
					System.out.println(msg);
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(0);
			}
		}
	}
}
