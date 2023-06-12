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
        // System.out.println(this);
        tempo();
        while (!this.isInterrupted()) {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (!this.haveToRunProcess)
                continue;
            if (!this.isWaiting()) {
                if (this.messages.size() > 0) {
                    handleMessages();
                    continue;
                }

                if (this.isFinished())
                    continue;
                // pathToTarget = dijkstra(grid[posY][posX]);
                pathToTarget = euclidianPath(grid[posY][posX]);
                // String path = "";
                // for (Case c : pathToTarget) {
                // path += c + " -> ";
                // }
                // System.out.println("Path of " + getId() + ": " + path);
                if (pathToTarget.isEmpty())
                    continue;

                this.waiting = !this.moveTo(pathToTarget.get(0));
                // System.out.println(this);

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
                sendMessage(target.getAgent(), MessageType.MOVE);
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

    public ArrayList<Case> euclidianPath(Case start) {
        // Euclidian algorithm to find the shortest path from the current position to
        // the target
        Random random = new Random();

        Case nextPos = grid[getPosY()][getPosX()];

        ArrayList<Case> path = new ArrayList<>();
        int minDist = Integer.MAX_VALUE;

        while (!nextPos.equals(grid[this.targetCase.getY()][this.targetCase.getX()])) {
            List<Case> neighbors = nextPos.getNeighbours();

            // To allow the agent to get out of blocking situation we add a little bit of
            // randomness
            // regarding the choice of the next case
            int isRandom = random.nextInt(0, 20);
            if (isRandom == 0) {
                int randomIndex;
                do {
                    randomIndex = random.nextInt(0, neighbors.size());
                } while (neighbors.get(randomIndex).equals(grid[getPosY()][getPosX()]));
                nextPos = neighbors.get(randomIndex);
            } else {
                for (Case neighbor : neighbors) {
                    int newDist = calculateDistance(neighbor, grid[this.targetCase.getY()][this.targetCase.getX()]);

                    if (newDist < minDist || (newDist == minDist && Math.random() > 0.5)) {
                        minDist = newDist;
                        nextPos = neighbor;
                    }
                }
            }
            path.add(nextPos);
        }

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

    @Override
    public String toString() {
        return "#" + getId() + " (" + posX + ", " + posY + ")";
    }

    public void addMessage(Message message) {
        this.messages.add(message);
    }

    public void sendMessage(Agent _receiver, MessageType _type) {
        Message message = new Message(this, _receiver, _type);
        // System.out.println(this + " send message to " + _receiver);
        message.send();
    }

    public void handleMessages() {
        Message message = this.messages.poll();
        // System.out.println(this + " received message from " + message.getSender());

        if (message.getType() == MessageType.FINISH) {
            this.waiting = false;
            return;
        }

        // If message type is Move, agent is asking to move to a neighbour case
        List<Case> neighboursCases = grid[this.posY][this.posX].getNeighbours();
        List<Agent> occupiedNeighbours = new ArrayList<>(
                neighboursCases.stream().filter(Case::isOccupied).map(aCase -> aCase.getAgent()).toList());
        List<Case> freeNeighboursCases = new ArrayList<>(
                neighboursCases.stream().filter(neighbour -> !neighbour.isOccupied()).toList());
        freeNeighboursCases.remove(message.getSender().targetCase);
        Random random = new Random();
        if (freeNeighboursCases.size() >= 1) {
            // If a neighbour case is available agent is moving to it
            Case target = freeNeighboursCases.stream()
                    .min((c1, c2) -> Integer.compare(calculateDistance(c1, targetCase),
                            calculateDistance(c2, targetCase)))
                    .orElseGet(() -> freeNeighboursCases.get(random.nextInt(freeNeighboursCases.size())));
            this.moveTo(target);
            this.sendMessage(message.getSender(), MessageType.FINISH);
            this.waiting = true;
            return;
        }
        // If no free neighbour case available agent is asking random neighbour agent to
        // move
        occupiedNeighbours.remove(message.getSender());
        if (occupiedNeighbours.size() == 0) {
            this.sendMessage(message.getSender(), MessageType.FINISH);
            this.waiting = true;
            return;
        }
        Agent target = occupiedNeighbours.get(random.nextInt(occupiedNeighbours.size()));
        this.sendMessage(target, MessageType.MOVE);
        this.messages.clear();
    }

    public int calculateDistance(Case a, Case b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }
}
