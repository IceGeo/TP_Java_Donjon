package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;

import javax.swing.JPanel;

import game.InfoGame;
import game.Map;

public class MapPanel extends JPanel {
    public ArrayList<int[]> cases;
    public char[][] allCases;
    // définition de la position centrale à partir de laquelle afficher à distVue
    public int[] center = new int[] {0, 0};
    public int distVue = 3;
    // tableau de booléens : la case a-t-elle déjà été vue ?
    public boolean[][] isSeen;
    private int w,h;
    private final int squareSize = 20;
    private Graphics2D mapZone;
    private int deltaH;
    private int deltaW;
    public MapPanel(char[][] listeCases) {
        cases = new ArrayList<>();
        allCases = listeCases;
        w = listeCases.length;
        h = listeCases[0].length;
        isSeen = new boolean[w][h];
        for (int i = 0; i<w; i++) for (int j=0; j<h; j++) isSeen[i][j] = false;

    }
    public void refresh(InfoGame info) {
        allCases = info.map;
        center = new int[] {info.x, info.y};
        distVue = info.distVue;
        isSeen = info.isSeen;
        repaint();
    }
    @Override
    public Dimension getPreferredSize() {
        return new Dimension(squareSize*w, squareSize*h);
    }
    @Override
    public void paint(Graphics g) {
        //super.paint(g);
        int x,y;
        g.setColor(Color.BLACK);
        Graphics2D g2d = (Graphics2D) g;
        mapZone = g2d;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		/*
		for(int[] pt : cases) {
			x = pt[0];
			y = pt[1];
			g2d.fillRect(x * squareSize, y * squareSize, squareSize, squareSize);
		}
		 */
        g2d.setFont(new Font("Cambria",Font.BOLD,squareSize));

        for (int i = 0; i<w; i++) {
            for (int j=0; j<h; j++) {
                // si la case est à plus de distVue :
                if (Math.abs(i - center[0]) > distVue || Math.abs(j - center[1]) > distVue) {
                    // fut-elle déjà visitée ?
                    if (isSeen[i][j]) {

                        if (allCases[i][j] == 'X') {
                            g.setColor(Color.DARK_GRAY);
                            g2d.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                            g.setColor(Color.BLACK);
                        } else if (allCases[i][j] == 'T') {
                            drawCharAt(allCases[i][j], i, j);
                        }
                        else {
                            g.setColor(Color.LIGHT_GRAY);
                            g2d.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                            g.setColor(Color.BLACK);
                        }
                    } else {
                        g.setColor(Color.BLACK);
                        drawCharAt('?', i, j);
                    }
                } else {
                    // la case n'est pas loin, donc elle est vue
                    //isSeen[i][j] = true;
                    if (allCases[i][j] == 'X') {
                        g2d.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                    } else if (allCases[i][j] == ' ') {
                        g.setColor(Color.WHITE);
                        g2d.fillRect(i * squareSize, j * squareSize, squareSize, squareSize);
                        g.setColor(Color.BLACK);
                    }
                    else {
                        //g2d.drawString(String.valueOf(allCases[i][j]), i*squareSize + deltaW, (j+1)*squareSize - deltaH);
                        drawCharAt(allCases[i][j], i, j);
                    }
                }
            }
        }
    }

    private void drawCharAt(char c, int x, int y) {
        if (Math.abs(x - center[0]) > distVue || Math.abs(y - center[1]) > distVue) mapZone.setColor(Color.LIGHT_GRAY); else mapZone.setColor(Color.WHITE);
        mapZone.fillRect(x * squareSize, y * squareSize, squareSize, squareSize);
        mapZone.setColor(Color.BLACK);
        mapZone.drawString(String.valueOf(c), x*squareSize + deltaW, (y+1)*squareSize - deltaH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        //super.paintComponent(g);
		/*
        g.setColor(Color.GREEN);
        g.fillRect(0, 0, 50, 50);
        g.setColor(Color.BLACK);
        g.fillOval(100, 100, 30, 30);
		 */
    }
    public void drawCase(int x, int y) {
        int[] pt = {x, y};
        this.cases.add(pt);
        this.repaint();
    }
}

