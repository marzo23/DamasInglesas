package com.example.cmariscal.damasinglesas;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class ArtificialInteligence{
    public Casilla[][] casillas;
    public int teamNumber;
    public int team1ChipsNum;
    public int team2ChipsNum;

    public int depth;
    public List<CasillaEvaluation> evaluationsList;

    public List<CasillaEvaluation> previousMovements;

    public ArtificialInteligence(Casilla[][] casillas, int depth){
        teamNumber = 1;
        team1ChipsNum = 12;
        team2ChipsNum = 12;
        this.casillas = casillas;
        this.depth = depth;
        evaluationsList = new ArrayList<CasillaEvaluation>();
        previousMovements = new ArrayList<CasillaEvaluation>();
        fillPlantilla();
    }

    public int[][] plantilla;

    public void fillPlantilla(){
        plantilla = new int[casillas.length][casillas.length];
        int aux = plantilla.length/2;
        int k = 0;
        for(int i = aux; i>0; i--){
            for(int j = aux-i; j<plantilla.length-k; j++){
                plantilla[k][j] = i;
                plantilla[plantilla.length-k-1][j] = i;
                plantilla[j][k] = i;
                plantilla[j][plantilla.length-k-1] = i;
            }
            k++;
        }
    }

    public boolean move(){
        Casilla[][] tabTmp = cloneTablero(casillas);
        evaluate(tabTmp, 0, 0, 0, null);
        Log.d("EvaluationListIA", "Size: "+evaluationsList.size());
        if(evaluationsList.size()>0) {
            evaluationsList = orderByHeuristicValue(evaluationsList);
            CasillaEvaluation aux = selectMovement();
            String process = "Heuristic: "+aux.heuristicValue+" Equipo"+aux.from.teamNumber+": "+aux.from.x+", "+aux.from.y+" -> "+aux.to.x+", "+aux.to.y;
            //process+= "; Equipo"+aux.from.teamNumber+": "+aux.from.x+", "+aux.from.y+" -> "+aux.to.x+", "+aux.to.y;
            Log.d("moveIA", process);
            casillas[aux.from.y][aux.from.x].setMovements(true);
            evaluationsList = new ArrayList<CasillaEvaluation>();
            previousMovements.add(new CasillaEvaluation(casillas[aux.from.y][aux.from.x], casillas[aux.to.y][aux.to.x], 0, null));
            if(previousMovements.size()>5)
                previousMovements.remove(0);
            return casillas[aux.from.y][aux.from.x].move(casillas[aux.to.y][aux.to.x]);
        }
        evaluationsList = new ArrayList<CasillaEvaluation>();
        return false;
    }

    public CasillaEvaluation selectMovement(){
        CasillaEvaluation maxPosition = null;
        int k = 0;

        while (maxPosition==null && k<evaluationsList.size()){
            boolean flag = true;
            for (int i = 0; i<previousMovements.size(); i++){
                if ((previousMovements.get(i).from == evaluationsList.get(k).from && previousMovements.get(i).to == evaluationsList.get(k).to)
                   ||(previousMovements.get(i).to == evaluationsList.get(k).from && previousMovements.get(i).from == evaluationsList.get(k).to))
                    flag = false;
            }
            if(flag)
                maxPosition = evaluationsList.get(k);
            k++;
        }
        for (int i = 0; i<evaluationsList.size(); i++){
            if(evaluationsList.get(i).heuristicValue>=maxPosition.heuristicValue) {
                if (plantilla[maxPosition.to.x][maxPosition.to.y] <= plantilla[evaluationsList.get(i).to.x][evaluationsList.get(i).to.y]) {
                    boolean flag = true;
                    for (int j = 0; j<previousMovements.size(); j++) {
                        if ((previousMovements.get(j).from == evaluationsList.get(i).from && previousMovements.get(j).to == evaluationsList.get(i).to)
                           || (previousMovements.get(j).to == evaluationsList.get(i).from && previousMovements.get(j).from == evaluationsList.get(i).to))
                            flag = false;
                    }
                    if(flag)
                        maxPosition = evaluationsList.get(i);
                }
            }
        }
        CasillaEvaluation aux = null;

        if(maxPosition.previous!=null){
            aux = maxPosition.previous;
            while (aux.previous!=null) {
                aux = aux.previous;
            }
        }else {
            aux = maxPosition;
        }
        return aux;
    }

    public void evaluate(Casilla[][] casillas, int step, int fichasPerdidas, int fichasGanadas, CasillaEvaluation previous){
        if(step<depth)// && (fichasGanadas-fichasPerdidas)>=0)
        {
            List<CasillaEvaluation> evaluations = getMovementsByTeamNumber(step%2==0?teamNumber:(teamNumber==1?2:1), casillas, previous);
            if(evaluations!=null?evaluations.size()>0:false){
                for (int i = 0; i<evaluations.size(); i++){
                    evaluations.get(i).heuristicValue = fichasGanadas+(step%2==0?evaluations.get(i).jumps:0) - fichasPerdidas+(step%2==0?0:evaluations.get(i).jumps);
                }
                if(step==depth-1 && evaluations!=null){
                    if(evaluations.size()>0){
                        evaluations = orderByHeuristicValue(evaluations);
                        evaluationsList.addAll(evaluations);
                        //evaluationsList.add(evaluations.get(0));
                    }
                }else {
                    evaluations = orderByJumps(evaluations, 1);
                    int maxGanadas=step % 2 == 0 ? evaluations.get(0).jumps : 0;
                    CasillaEvaluation maxPosition = evaluations.get(0);
                    for (int i = 1; i < evaluations.size(); i++) {
                        int ganadas = step % 2 == 0 ? evaluations.get(i).jumps : 0;
                        if(ganadas>=maxGanadas) {
                            if (plantilla[maxPosition.to.x][maxPosition.to.y] <= plantilla[evaluations.get(i).to.x][evaluations.get(i).to.y]) {
                                maxPosition = evaluations.get(i);
                            }
                        }else
                            evaluations.remove(i);
                    }

                    for (int i = 0; i < evaluations.size(); i++) {
                        int perdidas = step % 2 == 0 ? 0 : maxPosition.jumps;
                        int ganadas = step % 2 == 0 ? maxPosition.jumps : 0;
                        if (plantilla[maxPosition.to.x][maxPosition.to.y] <= plantilla[evaluations.get(i).to.x][evaluations.get(i).to.y]) {
                            Casilla[][] tabTmp = cloneTablero(casillas);
                            tabTmp[evaluations.get(i).from.y][evaluations.get(i).from.x].setMovements(false);
                            if (tabTmp[evaluations.get(i).from.y][evaluations.get(i).from.x].moveNoUI(tabTmp[evaluations.get(i).to.y][evaluations.get(i).to.x])) {
                                resetStates(tabTmp, false);
                                evaluate(tabTmp, step + 1, fichasPerdidas + perdidas, fichasGanadas + ganadas, evaluations.get(i));
                            }
                        }
                    }
                }
            }
        }
    }

    public void resetStates(Casilla[][] casillas, boolean UI){
        for(int i = 0; i<casillas.length; i++) {
            for (int j = 0; j < casillas.length; j++) {
                casillas[i][j].unselect(UI);
            }
        }
    }


    public List<CasillaEvaluation> getMovementsByTeamNumber(int team, Casilla[][] casillas, CasillaEvaluation previous){
        List<CasillaEvaluation> movementsList = new ArrayList<CasillaEvaluation>();
        for(int i = 0; i<casillas.length; i++) {
            for (int j = 0; j < casillas.length; j++) {
                if(casillas[i][j].teamNumber==team){
                    casillas[i][j].setMovements(false);
                    for(int k = 0; k<casillas[i][j].allowedMovements.size(); k++){
                        Casilla aux = casillas[i][j].allowedMovements.get(k).getPreviousJump();
                        int jumpCount = 1;
                        while (!aux.equals(casillas[i][j]) && aux!=null && !aux.equals(aux.getPreviousJump()) && jumpCount<12){
                            jumpCount++;
                            aux = aux.getPreviousJump();
                        }
                        if(aux.equals(casillas[i][j]))
                            movementsList.add(new CasillaEvaluation(casillas[i][j], casillas[i][j].allowedMovements.get(k), jumpCount/2, previous));
                    }
                }
            }
        }

        List<CasillaEvaluation> movementsListAux = new ArrayList<CasillaEvaluation>();
        for (int i = 0; i<movementsList.size(); i++){
            CasillaEvaluation aux = null;
            for (int j = 0; j < movementsListAux.size(); j++) {
                if(movementsList.get(i).from.equals(movementsListAux.get(j).from)) {
                    aux = movementsListAux.get(j);
                    break;
                }
            }
            if(aux!=null){
                if(aux.jumps<movementsList.get(i).jumps){
                    movementsListAux.remove(aux);
                    movementsListAux.add(movementsList.get(i));
                }
            }else
                movementsListAux.add(movementsList.get(i));
        }
        //MainActivity.resetStates(casillas);
        return movementsListAux;
    }

    public Casilla[][] cloneTablero(Casilla[][] tablero){
        final Casilla[][] casillas = new Casilla[tablero.length][tablero.length];
        for(int i = 0; i<tablero.length; i++){
            for(int j = 0; j<tablero.length; j++){
                boolean isBlack = true;
                if((i+j)%2==0)
                    isBlack = false;

                casillas[i][j] = tablero[i][j].clone();
            }
        }

        for(int i = 0; i<casillas.length; i++) {
            for (int j = 0; j < casillas.length; j++) {

                if(i+1<casillas.length && j+1<casillas.length)
                    casillas[i][j].casillaList.add(casillas[i+1][j+1]);

                if(i-1>=0 && j+1<casillas.length)
                    casillas[i][j].casillaList.add(casillas[i-1][j+1]);

                if(i+1<casillas.length && j-1>=0)
                    casillas[i][j].casillaList.add(casillas[i+1][j-1]);

                if(i-1>=0 && j-1>=0)
                    casillas[i][j].casillaList.add(casillas[i-1][j-1]);
            }
        }
        return casillas;
    }

    public List<CasillaEvaluation> orderByHeuristicValue(List<CasillaEvaluation> movementsList){
        if(movementsList.size()>1) {
            int k = movementsList.size() - 1, L = 1, R = movementsList.size() - 1;
            do {
                for (int j = R; j >= L; j--)
                    if (movementsList.get(j - 1).heuristicValue < movementsList.get(j).heuristicValue) {
                        CasillaEvaluation x = movementsList.get(j - 1);
                        movementsList.remove(j - 1);
                        movementsList.add(j, x);
                        k = j;
                    }
                L = k + 1;
                for (int j = L; j <= R; j++)
                    if (movementsList.get(j - 1).heuristicValue < movementsList.get(j).heuristicValue) {
                        CasillaEvaluation x = movementsList.get(j - 1);
                        movementsList.remove(j - 1);
                        movementsList.add(j, x);
                        k = j;
                    }
                R = k - 1;
            } while (L <= R);

        }
        return movementsList;
    }

    public List<CasillaEvaluation> orderByJumps(List<CasillaEvaluation> movementsList, int order){
        int k = movementsList.size()-1, L = 1, R = movementsList.size()-1;
        do{
            for(int j = R; j >= L; j--)
                if (movementsList.get(j-1).jumps * order < movementsList.get(j).jumps* order){
                    CasillaEvaluation x = movementsList.get(j-1);
                    movementsList.remove(j-1);
                    movementsList.add(j, x);
                    k = j;
                }
            L = k+1;
            for(int j = L; j<=R;j++)
                if (movementsList.get(j-1).jumps* order < movementsList.get(j).jumps* order){
                    CasillaEvaluation x = movementsList.get(j-1);
                    movementsList.remove(j-1);
                    movementsList.add(j, x);
                    k = j;
                }
            R = k-1;
        }while(L <= R);
        return movementsList;
    }

}