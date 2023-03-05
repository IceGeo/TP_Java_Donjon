package gui;

import game.Game;
import game.InfoGame;
import multiplayer.Client;
import multiplayer.Serveur;
import gui.MapPanel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class InterfaceGraphique extends JFrame implements KeyListener, MouseListener {
	private MapPanel mapPane;
	private Game game;
	private Client client;
	private Serveur serveur;
	private InfoGame infoGame;
	public Thread receiveOrder;
	private boolean isServeur;
	private JPanel generalPane, rightPane, leftPane, titlePane, statsPane, invPane, usePane, consPane;
	private JLabel hpLabel, nameLabel, mvtLabel, turnLabel, attLabel, defLabel;
	private JComboBox<String> itemCombo;
	private JComboBox<String> fileCombo;
	private JTextArea invInfo, consInfo;
	private String ordreDistant, actualTurn;
	private String[] lst_obj = {"-------"};
	private JMenuBar menuBar;
	private JMenu mnuFile;
	private JMenuItem mnuSave, mnuLoad, mnuExit;


	public InterfaceGraphique(InfoGame ig, boolean isServeur) {
		// initialisation de la fen�tre
		super("Donjon");
		this.setLocationRelativeTo(null);
		this.infoGame = ig; // re�oit de quoi construire la map et les infos du joueur
		mapPane = new MapPanel(ig.map); // construit la map
		this.isServeur = isServeur; // true si serveur, false si client

		// *****************  Validation de fermeture de fen�tre  *****************
		// � coder : cf document de TP
		// ************************************************************************

		/*
		 * Verifie si les dossiers/fichiers de sauvegarde existe
		 */
		File f; 
		f = new File("./save");
		if(!f.exists()) { // Verifie l'existance des dossiers, si non, le cr�e.
			f.mkdir();
		}
		addKeyListener(this);  // sert � r�cup�rer les �v�nements clavier
		setFocusable(true);  // sert � pouvoir cliquer sur la fen�tre
		setFocusTraversalKeysEnabled(false); // sert � interdire les touches de d�placement d'un composant � l'autre
		// reprend le focus quand fen�tre reprend le focus
		addWindowFocusListener(new WindowAdapter() {
			public void windowGainedFocus(WindowEvent e) {
				requestFocusInWindow();
			}
		});
		addMouseListener(this);
		mapPane.addMouseListener(this);
		// *****************  Cr�ation de la fen�tre  *****************
		// les trois JPanel de l'appli : general (contient les deux autres), rightPane pour map, et leftPane pour infos
		generalPane = (JPanel) this.getContentPane();
		this.setJMenuBar( this.createMenuBar() );
		leftPane = new JPanel();
		leftPane.setLayout(new BoxLayout(leftPane, BoxLayout.Y_AXIS));
		rightPane = new JPanel();
		rightPane.add(mapPane);
		// un split entre les deux parties
		JSplitPane horizSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPane, rightPane);
		horizSplit.setResizeWeight(0.50);
		generalPane.add(horizSplit);
		// *****************  Ajout des �l�ments graphiques  *****************
		// A vous de jouer !
		// *********************  panel titre Nom + hp  **************************
		GridBagLayout lay = new GridBagLayout();
		GridBagConstraints gc = new GridBagConstraints(); 
		//gc.insets = new Insets(1, 1, 1, 1);
		titlePane = new JPanel();
		titlePane.setLayout(lay);
		leftPane.add(titlePane);
		nameLabel = new JLabel(infoGame.persoName);
		if(infoGame.idxPlayerTurn==0) {
			actualTurn="Your turn";
		}else {
			actualTurn="Not your turn";
		}
		turnLabel = new JLabel(actualTurn);
		mvtLabel = new JLabel("MVT : "+Integer.toString(infoGame.mvt)+" / "+Integer.toString(infoGame.mvtMax));
		hpLabel = new JLabel(Integer.toString(infoGame.hp)+" / "+Integer.toString(infoGame.hpMax)+" hp");
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.anchor = GridBagConstraints.FIRST_LINE_START;
		gc.gridx = 0; 
		gc.gridy = 0; 
		titlePane.add(nameLabel, gc);
		gc.gridx = 1; 
		gc.gridy = 0; 
		titlePane.add(hpLabel, gc);
		gc.gridx = 0; 
		gc.gridy = 1; 
		titlePane.add(mvtLabel, gc);
		gc.gridx = 1; 
		gc.gridy = 1; 
		titlePane.add(turnLabel, gc);

		// **********************  panel att / def  *************************
		statsPane = new JPanel();
		statsPane.setLayout(lay);
		leftPane.add(statsPane);
		attLabel = new JLabel("Att : "+Integer.toString(infoGame.att));
		defLabel = new JLabel("Def : "+Integer.toString(infoGame.def));

		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.anchor = GridBagConstraints.FIRST_LINE_START;

		gc.gridx = 0; 
		gc.gridy = 0;
		statsPane.add(attLabel, gc);
		gc.gridx = 0; 
		gc.gridy = 1;
		statsPane.add(defLabel, gc);
		// **********************  panel inventaire  *************************
		invPane = new JPanel();
		invPane.setLayout(lay);
		leftPane.add(invPane);

		gc.fill = GridBagConstraints.HORIZONTAL;

		gc.gridx = 0; 
		gc.gridy = 0;
		invInfo = new JTextArea();
		invInfo.setText(infoGame.inventaire);
		invInfo.setEditable(false);
		invPane.add(invInfo,gc);
		// **********************  panel USE  *************************
		usePane = new JPanel();
		usePane.setLayout(lay);
		leftPane.add(usePane);

		gc.fill = GridBagConstraints.HORIZONTAL;

		gc.gridx = 0; 
		gc.gridy = 0;
		itemCombo = new JComboBox<String>(lst_obj);
		itemCombo.addActionListener(this::useObj);
		usePane.add(itemCombo, gc);
		// ********************* panel console ************************
		consPane = new JPanel();
		consPane.setLayout(lay);
		leftPane.add(consPane);
		gc.fill = GridBagConstraints.HORIZONTAL;

		gc.gridx = 0; 
		gc.gridy = 0;
		consInfo = new JTextArea();
		consInfo.setText("The quick brown fox jumps over the lazy dog.");
		consInfo.setEditable(false);
		consPane.add(consInfo,gc);
		// *****************  Si c'est le serveur, ajout d'un menu  *****************
		// A vous de jouer  : faites puis appelez la fonction qui ajoute le menu

		// *****************  Affichage de la fen�tre  *****************
		this.pack();
		this.setVisible(true);

	}
	private JMenuBar createMenuBar() {

		// La barre de menu � proprement parler
		menuBar = new JMenuBar();

		// D�finition du menu d�roulant "File" et de son contenu
		mnuFile = new JMenu( "Fichier" );
		mnuFile.setMnemonic( 'F' );

		mnuSave = new JMenuItem( "Save to file" );
		mnuSave.setMnemonic( 'N' );
		mnuSave.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_DOWN_MASK) );
		mnuSave.addActionListener( this::mnuSaveListener );
		mnuFile.add(mnuSave);

		mnuFile.addSeparator();

		mnuLoad = new JMenuItem( "Load from file ..." );
		mnuLoad.setMnemonic( 'O' );
		mnuLoad.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK) );
		mnuLoad.addActionListener( this::mnuLoadListener );
		mnuFile.add(mnuLoad);

		mnuFile.addSeparator();

		mnuExit = new JMenuItem( "Quitter" );
		mnuExit.setMnemonic( 'x' );
		mnuExit.setAccelerator( KeyStroke.getKeyStroke(KeyEvent.VK_F4, KeyEvent.ALT_DOWN_MASK) );
		mnuExit.addActionListener( this::mnuExitListener );
		mnuFile.add(mnuExit);

		menuBar.add(mnuFile);

		return menuBar;
	}

	public void mnuSaveListener( ActionEvent event ) {
		if(event.getSource()==mnuSave) {
			String filename = JOptionPane.showInputDialog(mnuSave,
					"Nom de la sauvegarde ?", null);
			if(filename!=null&&!filename.equals(null)) {
				//if (isServeur) serverApply("save "+filename); else client.sendOrder("save "+filename);
				serveur.saveGame(filename);
			}
		}
	}
	public void mnuLoadListener( ActionEvent event ) {
		if(event.getSource()==mnuLoad) {
			fileCombo = new JComboBox<String>();
			File[] lst_file_item;
			lst_file_item = new File("./save/").listFiles();
			for (File file : lst_file_item) {
				if (file.isFile()) {
					fileCombo.addItem(file.getName());
				} else if (file.isDirectory()) {
					// handle directory
				}
			}
			fileCombo.addActionListener(new ActionListener () 
			{
				public void actionPerformed(ActionEvent e) {
					if(e.getSource()==fileCombo) {
						serveur.loadGame((String) fileCombo.getSelectedItem());
						game = serveur.game;
						JOptionPane.showMessageDialog(mnuLoad, new String("Chargement effectuer avec succ�s"), "Information", getDefaultCloseOperation());
					}
				}
			});
			JOptionPane.showConfirmDialog(mnuLoad, fileCombo, "Charger votre partie",
					JOptionPane.OK_CANCEL_OPTION);
			
			//if (isServeur) serverApply("save "+filename); else client.sendOrder("save "+filename);
		}
	}
	public void mnuExitListener( ActionEvent event ) {
		if(event.getSource()==mnuExit) {
			System.exit(1);
		}
	}
	// ******************************** M�thode � impl�menter **********************************************
	public void showStats() {
		// met � jour les stats visibles sur la fen�tre graphique
		// TODO
		hpLabel.setText(Integer.toString(infoGame.hp)+" / "+Integer.toString(infoGame.hpMax)+" hp");
		mvtLabel.setText("MVT : "+Integer.toString(infoGame.mvt)+" / "+Integer.toString(infoGame.mvtMax));
		attLabel.setText("Att : "+Integer.toString(infoGame.att));
		defLabel.setText("Def : "+Integer.toString(infoGame.def));
		invInfo.setText(infoGame.inventaire);
		String[] lst_obj = infoGame.inventaire.split("\n");
		lst_obj[0]="-------";
		itemCombo.removeAllItems();
		for(int i = 0; i<lst_obj.length;i++) {
			itemCombo.addItem(lst_obj[i]);
		}

	}
	// *****************  Gestion des �v�nements clavier  *****************
	// rappel : les m�thodes suivantes sont appel�es automatiquement par le syst�me quand on impl�mente KeyListener
	// c'est tout le principe des Interfaces en Java
	public void keyTyped(KeyEvent e) {
		// rien � faire ici
	}
	public void keyPressed(KeyEvent e) {
		// rien de sp�cial sauf si vous le souhaitez
	}
	public void keyReleased(KeyEvent e) {

		// ici le code � ex�cuter quand une touche est rel�ch�e
		// les 4 touches fl�ch�es sont reconnues
		// par exemple if (e.getKeyCode() == KeyEvent.VK_RIGHT) { ... } pour la touche fl�ch�e droite
		// et pour les autres touches, KeyEvent.VK_... pour le nom de la touche (LEFT, UP, DOWN, RIGHT, ENTER, SPACE, ...)
		// TODO
		if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
			if (isServeur) serverApply("e"); else client.sendOrder("e");
		}else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
			if (isServeur) serverApply("o"); else client.sendOrder("o");
		}else if (e.getKeyCode() == KeyEvent.VK_UP) {
			if (isServeur) serverApply("n"); else client.sendOrder("n");
		}else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
			if (isServeur) serverApply("s"); else client.sendOrder("s");
		}
	}

	public void useObj(ActionEvent e) {
		// si l'�tat du combobox est modifi�e 
		if(e.getSource()==itemCombo && itemCombo.hasFocus()) {
			String order = (String) itemCombo.getSelectedItem();
			if(!order.equals("-------")){
				if (isServeur) serverApply("use "+order); else client.sendOrder("use "+order);
			}else {
				System.out.println("Selectionne un item valide");
			}
		}
	}




	// *****************  Gestion des �v�nements souris  *****************
	// rappel : les m�thodes suivantes sont appel�es automatiquement par le syst�me quand on impl�mente MouseListener
	// c'est tout le principe des Interfaces en Java
	public void mousePressed(MouseEvent e) {
		// System.out.println("Mouse pressed; # of clicks: " + e.getClickCount(), e);
	}

	public void mouseReleased(MouseEvent e) {
		//saySomething("Mouse released; # of clicks: " + e.getClickCount(), e);
		System.out.println("reprise de focus !");
		this.requestFocusInWindow();
	}

	public void mouseEntered(MouseEvent e) {
		// rien � faire ici
	}

	public void mouseExited(MouseEvent e) {
		// rien � faire ici
	}

	public void mouseClicked(MouseEvent e) {
		// rien � faire ici
	}

	// ******************* THREADS ************************
	// avec des threads de RECEPTION uniquement pour ne pas bloquer l'UI
	/**
	 * D�claration du Thread d'�coute soit c�t� Client, soit c�t� Serveur
	 */
	public void declareThread() {
		if (isServeur) {
			receiveOrder = new Thread(new Runnable() {

				@Override
				public void run() {
					// met le serveur en attente
					while (true) {
						ordreDistant = serveur.getMsg();
						if (infoGame.idxPlayerTurn == 1) serverApply(ordreDistant);
						else System.out.println("not player 1 turn !");
					}
				}
			});
		} else {
			receiveOrder = new Thread(new Runnable() {
				@Override
				public void run() {
					// met le client en attente
					InfoGame ig;
					while(true) {
						ig = client.waitServer();
						if (ig != null) refresh(ig);
						else System.out.println("info re�ue == null");
					}
				}
			});
		}
	}
	/**
	 * Si l'UI est c�t� Serveur, cette m�thode est appel�e pour effectuer un ordre re�u du Client
	 * @param order
	 */
	public void serverApply(String order) {
		InfoGame ig;
		String msg;
		game.setActivePlayer();
		System.out.println("ordre : " + order);
		msg = game.applyOrder(order);
		ig = new InfoGame(game, game.joueurs.get(0));
		ig.msg = msg;
		System.out.println(ig.msg);
		refresh(ig);
		// envoi du r�sultat de l'action au client
		serveur.sendMap(1, msg);
		// les monstres jouent si n�cessaire !
		if (game.whoseTurn() == -1) monsterPlay();
	}
	/**
	 * Si l'UI est c�t� Client, m�thode appel�e � chaque r�ception d'une info // obsol�te, non utilis�e
	 * @param ig
	 */
	public void clientApply(InfoGame ig) {
		if (ig != null) refresh(ig);
		else {
			System.out.println("info re�ue == null");
		}
	}
	/**
	 * M�thode appel�e - c�t� serveur uniquement - pour effectuer le tour des monstres
	 */
	public void monsterPlay() {
		String msg;
		InfoGame ig;
		msg = game.monstersChase();    //game.monstersPlay();
		ig = new InfoGame(game, game.joueurs.get(0));
		ig.msg = msg;
		refresh(ig);
		serveur.sendMap(1, msg);
	}
	// ******************** FIN THREADS *************************

	/**
	 * Affiche et met � jour les stats en fonction de l'infogame envoy�e
	 * @param ig
	 */
	public void refresh(InfoGame ig) {
		infoGame = ig;
		mapPane.refresh(infoGame);
		showStats();
	}

	public static void main(String[] args ) {
		Scanner sc = new Scanner(System.in);
		// d�termination si nouveau serveur ou nouveau client
		String isServ = "";
		while (!isServ.equals("o") && !isServ.equals("n")) {
			System.out.print("Serveur (o) ou Client (n) ? ");
			isServ = sc.nextLine();
			if (isServ.equals("o")) {
				System.out.println("*************** SERVEUR JEU ****************** ");

				Game g = new Game();
				Serveur serv = new Serveur(g);
				// System.out.println(Game.pathToString(g.pathFinder(3, 3, true))); // test du pathfinder
				// invitation
				System.out.println("Multi (o/n) : ");
				System.out.println("(Si oui, le serveur se met en attente de la connexion client)");
				String yesno = sc.nextLine();
				if (yesno.equals("o")) {
					serv.connexion();
				}
				// cr�ation de l'UI
				gui.InterfaceGraphique ui = new gui.InterfaceGraphique(new InfoGame(g, g.heros), true);
				ui.game = g;
				ui.serveur = serv;
				//ui.isServeur = true;
				// d�finition du Thread ************* THREADS **********************
				if (serv.in == null) return;
				ui.declareThread();
				ui.receiveOrder.start();
			} else if (isServ.equals("n")) {
				System.out.println("*************** CLIENT JEU ****************** ");
				System.out.println("Adresse serveur (tapez ENTREE si localhost) : ");
				String adr = sc.nextLine();
				System.out.println("Nom du personnage : ");
				String nom = sc.nextLine();
				System.out.println("Classe : ");
				String classe = sc.nextLine();
				//System.out.println("Symbol : ");
				Client cl = new Client(nom, classe, adr);
				gui.InterfaceGraphique ui = new gui.InterfaceGraphique(cl.info, false);
				ui.client = cl;
				ui.client.mapPane = ui.mapPane;
				ui.mapPane.refresh(cl.info);
				// d�finition du Thread ************* THREADS **********************
				ui.declareThread();
				ui.receiveOrder.start();
			}
		}
	}
}
