package com.polypote.taquinsma.game;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.awt.*;
import java.util.Random;

@EqualsAndHashCode(callSuper = true)
@Data
public class Agent extends Thread {
    private static Case[][] grid;
    private int posX;
    private int posY;
    private Case targetCase;
    private Color agentColor;

    public Agent(Case[][] grid) {
        Agent.grid = grid;
        Random random = new Random();
        agentColor = new Color(random.nextInt(256), random.nextInt(256), random.nextInt(256));
    }

    @Override
    public void run() {
        super.run();
    }
}
