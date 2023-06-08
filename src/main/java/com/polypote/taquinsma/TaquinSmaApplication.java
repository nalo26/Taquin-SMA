package com.polypote.taquinsma;

import com.polypote.taquinsma.game.Agent;
import com.polypote.taquinsma.game.Case;
import processing.core.PApplet;

// import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TaquinSmaApplication extends PApplet {

    public static final int FACTOR = 100;
    private static Case[][] grid;
    private List<Agent> agentList;

    public TaquinSmaApplication(int dimX, int dimY, int nbAgent) {
        initializeGrid(dimX, dimY);
        initializeAgent(nbAgent);
        randomPlacement(dimX, dimY);
        setNeighbours(dimX, dimY);
        startAgents();
    }

    private static void initializeGrid(int dimX, int dimY) {
        grid = new Case[dimY][dimX];
        for (int row = 0; row < dimY; row++) {
            for (int col = 0; col < dimY; col++) {
                grid[row][col] = new Case(col, row);
            }
        }
    }

    public static void main(String[] args) {
        TaquinSmaApplication application = new TaquinSmaApplication(5, 5, 10);
        PApplet.runSketch(new String[] { "TaquinSmaApplication" }, application);
    }

    @Override
    public void settings() {
        size(grid[0].length * FACTOR, grid.length * FACTOR);
    }

    @Override
    public void setup() {
        frameRate(60);
        rectMode(CORNER);
        ellipseMode(CORNER);
        loop();
    }

    @Override
    public void draw() {
        displayBackground();

        for (Agent a : agentList) {
            displayAgent(a);
        }
    }

    private void displayBackground() {
        fill(255);
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                rect(col * FACTOR, row * FACTOR, FACTOR, FACTOR);
            }
        }
        // We display the agents' target
        for (Agent a : agentList) {
            displayTarget(a);
        }
    }

    public void displayTarget(Agent a) {
        fill(a.getAgentColor().getRGB());
        rect(a.getTargetCase().getX() * FACTOR, a.getTargetCase().getY() * FACTOR, FACTOR, FACTOR);
    }

    /**
     * On dessine un agent
     * We draw an agent
     *
     * @param a Agent à dessiner
     */
    public void displayAgent(Agent a) {
        fill(a.getAgentColor().getRGB());
        ellipse(a.getPosX() * FACTOR, a.getPosY() * FACTOR, FACTOR, FACTOR);
        // fill(Color.BLACK.getRGB());
        // text((int) a.getId(), a.getPosX() * FACTOR + 40, a.getPosY() * FACTOR + 50);
    }

    private void startAgents() {
        for (Agent a : agentList) {
            new Thread(a).start();
        }
    }

    private void setNeighbours(int dimX, int dimY) {
        int[][] cardinals = { { -1, 0 }, { 1, 0 }, { 0, -1 }, { 0, 1 } };
        for (int row = 0; row < dimY; row++) {
            for (int col = 0; col < dimX; col++) {
                for (int[] card : cardinals) {
                    int newX = col + card[0];
                    int newY = row + card[1];
                    if (newX >= 0 && newX < dimX && newY >= 0 && newY < dimY) {
                        getCaseAt(col, row).addNeighbour(getCaseAt(newX, newY));
                    }
                }
            }
        }
    }

    private void initializeAgent(int nbAgent) {
        agentList = new ArrayList<Agent>();
        for (int agentIndex = 0; agentIndex < nbAgent; agentIndex++) {
            agentList.add(new Agent(grid));
        }
    }

    private void randomPlacement(int dimX, int dimY) {
        Random random = new Random();
        for (Agent a : agentList) {
            do {
                a.setPosX(random.nextInt(dimX));
                a.setPosY(random.nextInt(dimY));
            } while (isAgentOverlapping(a));
            grid[a.getPosY()][a.getPosX()].setOccupied(a);

            do {
                a.setTargetCase(grid[random.nextInt(dimY)][random.nextInt(dimX)]);
            } while (isTargetAgentOverlapping(a));
        }
    }

    private boolean isTargetAgentOverlapping(Agent a) {
        Case targetCase = a.getTargetCase();
        int currentAgentX = targetCase.getX();
        int currentAgentY = targetCase.getY();
        for (Agent b : agentList) {
            Case currentCase = b.getTargetCase();
            if (currentCase == null)
                continue;
            if (a != b && currentAgentX == currentCase.getX() && currentAgentY == currentCase.getY())
                return true;
        }
        return false;
    }

    private boolean isAgentOverlapping(Agent a) {
        for (Agent b : agentList) {
            if (a != b && a.getPosX() == b.getPosX() && a.getPosY() == b.getPosY())
                return true;
        }
        return false;
    }

    public Case getCaseAt(int x, int y) {
        return grid[y][x];
    }
}
