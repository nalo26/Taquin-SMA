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

            pathToTarget = dijkstra(grid[posY][posX]);
            if (pathToTarget.isEmpty())
                continue;

            this.waiting = !this.moveTo(pathToTarget.get(0));
        }
    }

    public boolean moveTo(Case target) {
        if (target.isOccupied())
            return false;

        grid[posY][posX].setOccupied(null);
        target.setOccupied(this);

        this.setPosX(target.getX());
        this.setPosY(target.getY());

        return true;
    }

    public ArrayList<Case> dijkstra(Case start) {
        HashMap<Case, Integer> dist = new HashMap<>();
        HashMap<Case, Case> prev = new HashMap<>();
        ArrayList<Case> Q = new ArrayList<Case>();

        for (Case[] row : grid) {
            for (Case c : row) {
                dist.put(c, Integer.MAX_VALUE);
                prev.put(c, null);
                Q.add(c);
            }
        }
        dist.put(start, 0);

        while (!Q.isEmpty()) {
            Case u = Q.stream().min((a, b) -> dist.get(a) - dist.get(b)).get();
            Q.remove(u);

            for (Case v : u.getNeighbours()) {
                int alt = dist.get(u) + 1;
                if (alt < dist.get(v)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                }
            }
        }

        ArrayList<Case> path = new ArrayList<Case>();
        Case u = targetCase;
        while (prev.get(u) != null) {
            path.add(u);
            u = prev.get(u);
        }
        path.add(u);
        Collections.reverse(path);
        path.remove(grid[posY][posX]);
        return path;
    }
}
