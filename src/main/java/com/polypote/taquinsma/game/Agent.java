package com.polypote.taquinsma.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.util.List;
import java.util.Queue;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@EqualsAndHashCode(callSuper = true)
@Data
public class Agent extends Thread {
    private static Case[][] grid;
    private final Queue<Message> messages = new LinkedList<>();
    private int posX;
    private int posY;
    private Case targetCase;
    private Color agentColor;
    private ArrayList<Case> pathToTarget;
    private boolean haveToRunProcess = true;
    private boolean waiting = false;
    private int countWaiting = 0;
    private ReentrantLock lock = new ReentrantLock();

    public Agent(Case[][] grid) {
        Agent.grid = grid;
        Random random = new Random();
        agentColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!this.haveToRunProcess || this.isFinished())
                continue;
            if (!this.isWaiting()) {
                // System.out.println(this.getId() + ": (" + posX + "," + posY + ")");
                handleMessages();
                pathToTarget = dijkstra(grid[posY][posX]);
                if (pathToTarget.isEmpty())
                    continue;

                this.waiting = !this.moveTo(pathToTarget.get(0));

            } else if (this.countWaiting < 30)
                this.countWaiting++;
            else {
                this.waiting = false;
                this.countWaiting = 0;
            }
            this.haveToRunProcess = false;
        }
    }

    public boolean moveTo(Case target) {
        lock.lock();
        try {
            if (target.isOccupied()) {
                sendMessage(target.getAgent());
                return false;
            }
            grid[posY][posX].setOccupied(null);
            target.setOccupied(this);

            this.setPosX(target.getX());
            this.setPosY(target.getY());
            tempo();
            return true;
        } finally {
            lock.unlock();
        }
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
            Case current = unvisited.stream().min(Comparator.comparingInt(distance::get)).get();
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

    public boolean isFinished() {
        return this.posX == targetCase.getX() && this.posY == targetCase.getY();
    }

    private void tempo() {
        long tps = 500; // + (long) (Math.random() * 800);
        try {
            sleep(tps);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void sendMessage(Agent _receiver) {
        Message message = new Message(this, _receiver);
        message.send();
    }

    public void handleMessages() {
        Message message = this.messages.poll();
        if (message == null) return;
        List<Case> neighboursCases = grid[this.posY][this.posX].getNeighbours();
        List<Agent> occupiedNeighbours = new ArrayList<>(neighboursCases.stream().filter(Case::isOccupied).map(aCase -> aCase.getAgent()).toList());
        List<Case> freeNeighboursCases = neighboursCases.stream().filter(neighbour -> !neighbour.isOccupied()).toList();
        Random random = new Random();
        if (freeNeighboursCases.size() >= 1) {
            // If a neighbour case is available agent is moving to it
            Case target = freeNeighboursCases.get(random.nextInt(freeNeighboursCases.size()));
            this.moveTo(target);
            return;
        }
        // If no free neighbour case available agent is asking random neighbour agent to move
        occupiedNeighbours.remove(message.getSender());
        Agent target = occupiedNeighbours.get(random.nextInt(occupiedNeighbours.size()));
        this.sendMessage(target);

    }
}
