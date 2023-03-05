package multiplayer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import game.*;
import gui.InterfaceGraphique;

public class Serveur {
	private ArrayList<Socket> clients;
	private ServerSocket socketServer;
	// le socket du client en cours de traitement
	private Socket socketClient;
	private ObjectOutputStream out;
	public ObjectInputStream in;
	public Game game;
	private InterfaceGraphique ui;
	private boolean waitClient;
	public Serveur(Game g) {
		final int port = 2302;
		clients = new ArrayList<>();
		try {
			socketServer = new ServerSocket(port);
			//System.out.println(InetAddress.getLocalHost().getHostAddress());
			InetAddress[] allAdr = InetAddress.getAllByName(InetAddress.getLocalHost().getHostName());
			for (InetAddress a : allAdr) {
				//System.out.println(a);
				String ipLocal = a.toString().split("/")[1];
				if (ipLocal.substring(0, 7).equals("192.168") || ipLocal.substring(0, 4).equals("172.")) System.out.println("Good Address : " + ipLocal);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		game = g;
	}
	public void connexion() {
		final int port = 2302;
		waitClient = false;
		String msg;
		try {
			//socketServer = new ServerSocket(port);
			//while(true) {
				System.out.println("attente...");
				socketClient = socketServer.accept();
				System.out.println("re�u...");
				out = new ObjectOutputStream(socketClient.getOutputStream());
				in = new ObjectInputStream(socketClient.getInputStream());
				// 2 cas alors possibles : 1) le client n'est pas d�j� connect� et on ajoute son joueur ou  bien
				// 2) le client est d�j� en jeu et il faut alors prendre en compte ses actions jusqu'� �puisement de ses points d'action
				InetAddress adr = socketClient.getInetAddress();
				boolean inPlay = false;
				for (Socket sock : clients) {
					if (adr.toString().equals(sock.getInetAddress().toString())) {
						inPlay = true;
						System.out.println("client " + adr + " d�j� en jeu");
					}
				}
				if (inPlay) {
					// traitement ordre
				} else {
					System.out.println("ajout� " + adr + " en jeu");
					clients.add(socketClient);
					String nom = (String)in.readObject();
					String classe = (String)in.readObject();
					Integer idx = game.applyAddPlayer(new String[] {"addPlayer", nom, classe, "+"});
					out.writeObject(idx);
					//System.out.println("perso ajout� : \n" + game.joueurs.get(idx));
					// envoi de la map
					//out.writeObject("map");
					out.writeObject(new InfoGame(game, game.joueurs.get(idx)));
					
				}
				// � la fin on ferme la connexion ??????????????????????????????????????
				//socketClient.close();
			//}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Enregistre le Game dans un fichier donn� en param�tre (sans extension, ajout�e automatiquement en .sav)
	 * @param fileName
	 */
	public void saveGame(String fileName) {
		try {
			FileOutputStream fileOut = new FileOutputStream("./save/" + fileName + ".sav");
			ObjectOutputStream outFile = new ObjectOutputStream(fileOut);
			outFile.writeObject(game);
			outFile.close();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	/**
	 * Charge la sauvegarde nomm�e
	 * @param fileName
	 */
	public void loadGame(String fileName) {
		try {
			String filePath = "./save/" + fileName;
			File f = new File(filePath);
			// si ce fichier n'existe pas...
			if (!f.exists()) {
				System.out.println("pas de tel fichier");
				return;
			}
			FileInputStream fileIn = new FileInputStream(filePath);
			ObjectInputStream inFile = new ObjectInputStream(fileIn);
			game = (Game)inFile.readObject();
			inFile.close();
			if (game.joueurs.size()>1) {
				InfoGame info = new InfoGame(game, game.joueurs.get(1));
				System.out.println(Game.mapToString(info.map));
			}
			
		} catch(IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// fonction recherche de liste de sauvegardes pr�alables
	/**
	 * Renvoie les listes des noms de fichiers sauvegard�s
	 * @return
	 */
	public String[] getSaves() {
		try {
			// envoi de la liste des noms de sauvegardes
			// lecture des fichiers dans le dossier "save"
			// lecture du dossier
			String[] nomSaves;
			// This filter will only include files ending with .sav
			FilenameFilter filter = new FilenameFilter() {
				@Override
				public boolean accept(File f, String name) {
					return name.endsWith(".sav");
				}
			};
			File f = new File("./save");
			nomSaves = f.list(filter);
			return nomSaves;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Envoie la mise � jour du jeu au client
	 * @param idx indice du joueur
	 */
	public void sendMap(int idx) {
		// mise � jour de l'affichage :
		try {
			out.writeObject("map");
			InfoGame info = new InfoGame(game, game.joueurs.get(idx));
			//System.out.println(Game.mapToString(info.map));
			out.writeObject(info);
		} catch (Exception e) {
			System.out.println("envoi map multiplayer impossible");
			//e.printStackTrace();
		}
	}

	/**
	 * Envoie la mise � jour du jeu au client assortie d'un message
	 * @param idx indice du joueur
	 * @param msg message � envoyer
	 */
	public void sendMap(int idx, String msg) {
		// mise � jour de l'affichage :
		try {
			out.writeObject("map");
			InfoGame info = new InfoGame(game, game.joueurs.get(idx));
			//System.out.println(Game.mapToString(info.map));
			//System.out.println(Game.isSeenPerso(game.joueurs.get(idx)));
			info.msg = msg;
			out.writeObject(info);
		} catch (Exception e) {
			System.out.println("envoi map multiplayer impossible");
			//e.printStackTrace();
		}
	}
	public void sendReady() {
		String msg = "ready";
		try {
			out.writeObject(msg);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Initialisation du jeu
	 */
	public void initGame() {
		game = new Game();
	}
	// applique l'ordre ou essaie de l'appliquer
	/**
	 * Applique l'ordre donn� au jeu de la part du joueur
	 * @param p joueur
	 * @param order ordre � appliquer
	 * @return
	 */
	public void tryOrder(Personnage p, String order) {
		// est-ce le tour de ce personnage ? et a-t-il encore le droit d'agir ?
		if (!game.playerTurn(p)) return;
		// il agit
		String msg;
		if (order.equals("invite")) connexion();
		else if (order.startsWith("save ")) {
			// sauvegarde du fichier de nom "nom"
			String fileName = order.substring(5);
			if (fileName.length()<2) fileName = "encours";
			saveGame(fileName);
		} else if (order.startsWith("load ")) {
			// sauvegarde du fichier de nom "nom"
			String fileName = order.substring(5);
			if (fileName.length()<2) fileName = "encours";
			loadGame(fileName);
			sendMap(1);
		// ordres de debug
		} else if (order.startsWith("showgame")) {
			System.out.println(game);
		} else {
			msg = game.applyOrder(order);
			InfoGame ig = new InfoGame(game, game.joueurs.get(1));
			ig.setMsg(msg);
			System.out.println(msg);
			try {
				out.writeObject(ig);
			} catch (Exception e) {
				System.out.println("envoi multiplayer impossible");
				//e.printStackTrace();
			}
		}
		if (!game.heroTurn()) System.out.println(game.applySwitch());
		//ui.refresh(new InfoGame(game, game.joueurs.get(0)));
	}
	// en r�ception, re�oit l'ordre envoy� par un client
	public String getOrder() {
		waitClient = true;
		// commencer par d�sactiver l'interface ?
		String msg, order;
		System.out.println("Que fait-il ?");
		try {
			msg = "ready";
			out.writeObject(msg);
			order = (String)in.readObject();
			System.out.println("ordre distant = " + order);
			return order;
		} catch (Exception e) {
			e.printStackTrace();
			order = "probl�me ?";
			System.exit(0);
			return order;
		}
	}
	// se met juste en r�ception
	public String getMsg() {
		try {
			return (String)in.readObject();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		return "ouille";
	}
	
	public void sendOrder(String order) {
		try {
			out.writeObject(order);
		} catch (Exception e) {
			System.out.println("envoi multiplayer impossible");
			//e.printStackTrace();
			//System.exit(0);
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Game g = new Game();
		String order;
		String msg;
		Map m = Utils.viewMap(g.map);
		System.out.println(g.showAround(0, 0));
		Serveur serv = new Serveur(g);
		Scanner sc = new Scanner(System.in);
		String yesno = "o";
		while (yesno.equals("o")) {
			System.out.println("Invite (o/n) : ");
			yesno = sc.nextLine();
			if (yesno.equals("o")) {
				serv.connexion();
			}
		}
		// tour de jeu
		m.setPlayer(g.heros);
		while(true) {
			// tour du h�ros (3 points d'action � la base)
			while (g.heroTurn()) {
				System.out.println("Que faites-vous ?");
				order = sc.nextLine();
				if (order.equals("invite")) serv.connexion();
				else if (order.startsWith("save ")) {
					// sauvegarde du fichier de nom "nom"
					String fileName = order.substring(5);
					if (fileName.length()<2) fileName = "encours";
					serv.saveGame(fileName);
				} else if (order.startsWith("load ")) {
					// sauvegarde du fichier de nom "nom"
					String fileName = order.substring(5);
					if (fileName.length()<2) fileName = "encours";
					serv.loadGame(fileName);
					g = serv.game;
					serv.sendMap(1);
				// ordres de debug
				} else if (order.startsWith("showgame")) {
					System.out.println(g);
				} else {
					msg = g.applyOrder(order);
					System.out.println(msg);
					try {
						serv.out.writeObject(msg);
					} catch (Exception e) {
						System.out.println("envoi multiplayer impossible");
						//e.printStackTrace();
					}
					
				}
				//m.setPlayer(g.heros);
				m.refresh(new InfoGame(g, g.joueurs.get(0)));
				//m.refresh(g.heros.getxPos(), g.heros.getyPos(), g.distVue);
			}
			// tour de l'autre joueur
			System.out.println(g.applySwitch());
			//m.setPlayer(g.heros);
			while (g.heroTurn()) {
				System.out.println("Que fait-il ?");
				try {
					msg = "ready";
					serv.out.writeObject(msg);
					order = (String)serv.in.readObject();
				} catch (Exception e) {
					e.printStackTrace();
					order = "probl�me ?";
				}
				System.out.println("ordre distant = " + order);
				msg = g.applyOrder(order);
				System.out.println(msg);
				
				// mise � jour de l'affichage :
				try {
					//serv.out.writeObject(msg);
					serv.sendMap(1,msg);
					//System.out.println(Game.mapToString(info.map));
				} catch (Exception e) {
					e.printStackTrace();
				}
				//m.setPlayer(g.heros);
				m.refresh(new InfoGame(g, g.joueurs.get(0)));
				//m.refresh(g.joueurs.get(0).getxPos(), g.joueurs.get(0).getyPos(), g.distVue);
			}
			
			
			// revient au joueur n�0
			System.out.println(g.applySwitch());
			m.setPlayer(g.heros);
			// tour des pnj
			System.out.println("Les monstres se d�placent !");
			//g.monstersPlay();
			g.monstersChase();
			//m.refresh(g.heros.getxPos(), g.heros.getyPos(), g.distVue);
			m.refresh(new InfoGame(g, g.joueurs.get(0)));
			serv.sendMap(1);
			
			System.out.println(g.showAround(g.heros.getxPos(), g.heros.getyPos()));
			
		}
	}
}
