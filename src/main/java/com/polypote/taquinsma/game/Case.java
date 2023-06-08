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
        this.isOccupied = agent != null;
        this.agent = agent;
    }

    public void addNeighbour(Case caseAt) {
        neighbours.add(caseAt);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Case other = (Case) obj;
        if (x != other.x)
            return false;
        if (y != other.y)
            return false;
        if (color == null) {
            if (other.color != null)
                return false;
        } else if (!color.equals(other.color))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + x;
        result = prime * result + y;
        result = prime * result + ((color == null) ? 0 : color.hashCode());
        return result;
    }

}
