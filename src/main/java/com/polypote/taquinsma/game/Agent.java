package com.polypote.taquinsma.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

@EqualsAndHashCode(callSuper = true)
@Data
public class Agent extends Thread {
    private static Case[][] grid;
    private int posX;
    private int posY;
    private Case targetCase;
    private Color agentColor;
    private ArrayList<Case> pathToTarget;
    private boolean haveToRunProcess = true;
    private boolean waiting = false;

    public Agent(Case[][] grid) {
        Agent.grid = grid;
        Random random = new Random();
        agentColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!haveToRunProcess)
                continue;
            // System.out.println(this.getId() + ": (" + posX + "," + posY + ")");

            pathToTarget = dijkstra(grid[posY][posX]);
            if (pathToTarget.isEmpty())
                continue;

            this.waiting = !this.moveTo(pathToTarget.get(0));
        }
    }

    public boolean moveTo(Case target) {
        // TODO: concurential access, leads to teleportation
        if (target.isOccupied())
            return false;

        grid[posY][posX].setOccupied(null);
        target.setOccupied(this);

        this.setPosX(target.getX());
        this.setPosY(target.getY());

        return true;
    }

    public ArrayList<Case> dijkstra(Case start) {
        // Dijkstra algorithm to find the shortest path from the current position to the
        // target, and avoiding other agents
        ArrayList<Case> unvisited = new ArrayList<Case>();
        HashMap<Case, Integer> distance = new HashMap<Case, Integer>();
        HashMap<Case, Case> previous = new HashMap<Case, Case>();

        for (Case[] row : grid) {
            for (Case c : row) {
                distance.put(c, Integer.MAX_VALUE);
                previous.put(c, null);
                unvisited.add(c);
            }
        }

        distance.put(start, 0);

        while (!unvisited.isEmpty()) {
            Case current = unvisited.stream().min((a, b) -> distance.get(a) - distance.get(b)).get();
            unvisited.remove(current);

            if (current == targetCase)
                break;

            for (Case neighbour : current.getNeighbours()) {
                if (neighbour.isOccupied() || !unvisited.contains(neighbour))
                    continue;

                int alt = distance.get(current) + 1;
                if (alt < distance.get(neighbour)) {
                    distance.put(neighbour, alt);
                    previous.put(neighbour, current);
                }
            }
        }

        ArrayList<Case> path = new ArrayList<Case>();
        Case current = targetCase;
        while (previous.get(current) != null) {
            path.add(current);
            current = previous.get(current);
        }
        Collections.reverse(path);
        return path;
    }
}
