package com.example.dp4coruna.mapmanagement.indoorRouting.DataStructures;

public class Neighbor {
    public int fnum;
    public Neighbor next;
    public Neighbor(int fnum, Neighbor next) {
        this.fnum = fnum;
        this.next = next;
    }
}
