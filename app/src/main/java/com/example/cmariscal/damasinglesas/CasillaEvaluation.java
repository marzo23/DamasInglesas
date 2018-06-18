package com.example.cmariscal.damasinglesas;


class CasillaEvaluation{
    public CasillaEvaluation previous;
    public Casilla from;
    public Casilla to;
    public int jumps;
    public int heuristicValue;
    public CasillaEvaluation(Casilla from, Casilla to, int jumps, CasillaEvaluation previous){
        this.from = from;
        this.to = to;
        this.jumps = jumps;
        this.previous = previous;
        heuristicValue = 0;
    }
}
