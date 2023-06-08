package com.polypote.taquinsma.game;

import lombok.Data;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Data
public class Case {
    private int x;
    private int y;
    private boolean isOccupied;
    private Agent agent;
    private List<Case> neighbours;
    private Color color;

    public Case(int xdim, int ydim) {
        this.x = xdim;
        this.y = ydim;
        this.neighbours = new ArrayList<Case>();
    }

    public void setOccupied(Agent agent) {
        this.isOccupied = true;
        this.agent = agent;
    }

    public void addNeighbour(Case caseAt) {
        neighbours.add(caseAt);
    }
}
