package com.example.cmariscal.damasinglesas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.Shape;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int ancho = size.x;
        int alto = size.y;

        int numCasillas = 8;
        int depth = 3;


        TableLayout tabla = new TableLayout(this);
        tabla.setLayoutParams(new TableLayout.LayoutParams(
                TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.FILL_PARENT));
        tabla.setColumnStretchable(1, true);

        final Casilla[][] casillas = new Casilla[numCasillas][numCasillas];
        TableRow[] rows = new TableRow[numCasillas];

        for(int i = 0; i<numCasillas; i++){
            rows[i] = new TableRow(this);
            rows[i].setLayoutParams(new TableLayout.LayoutParams(
                    TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            tabla.addView(rows[i]);
            for(int j = 0; j<numCasillas; j++){
                boolean isBlack = true;
                if((i+j)%2==0)
                    isBlack = false;

                casillas[i][j] = new Casilla(new TextView(this), true, isBlack, j,i, ancho/numCasillas, numCasillas);
                rows[i].addView(casillas[i][j].view);
            }
        }

        setCasillaRelations(casillas);
        ia = new ArtificialInteligence(casillas, depth);
        //Casillas =casillas;
        TextView playButton = new TextView(this);
        playButton.setWidth(ancho/numCasillas);
        playButton.setHeight(ancho/numCasillas);
        playButton.setBackgroundResource(R.drawable.retry);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNewGame(casillas);
            }
        });

        TextView passButton = new TextView(this);
        passButton.setWidth(ancho/numCasillas);
        passButton.setHeight(ancho/numCasillas);
        passButton.setBackgroundResource(R.drawable.arrow);
        passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStates(casillas);
                turnoMsg();
            }
        });

        TableRow buttonRow = new TableRow(this);
        buttonRow.addView(playButton);
        buttonRow.addView(passButton);
        tabla.addView(buttonRow);



        setContentView(tabla);
    }
    ArtificialInteligence ia;
    int I = -1;
    int J = -1;
    int turno = 0;


    public void setCasillaRelations(final Casilla[][] casillas){
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

                casillas[i][j].view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        boolean aux = false;
                        if(validateIsOnlySelected(casillas, view) && (I<0 || J<0)){

                            resetStates(casillas);
                            //turnoMsg();
                            aux = true;
                        }
                        else{
                            selectCasilla(casillas[I][J]);
                            //resetStates(casillas);
                        }
                        if(aux){
                            ia.move();
                            resetStates(casillas);
                            //turnoMsg();

                        }
                    }
                });
            }
        }
    }

    public static void resetStates(Casilla[][] casillas){
        int team1Count = 0;
        int team2Count = 0;
        for(int i = 0; i<casillas.length; i++) {
            for (int j = 0; j < casillas.length; j++) {
                casillas[i][j].unselect();
                if(!casillas[i][j].getIsEmpty()){
                    if(casillas[i][j].teamNumber==1)
                        team1Count++;
                    else if(casillas[i][j].teamNumber==2)
                        team2Count++;
                }
            }
        }
        if(team1Count==0 || team2Count==0)
            Toast.makeText(casillas[0][0].view.getContext(), "GANÓ EQUIPO: "+(team1Count==0?"Blanco":"Rojo"), Toast.LENGTH_SHORT).show();
            //win(team1Count==0?1:2);

    }

    public void turnoMsg(){
        if(turno == 1) {
            turno = 2;
            //ia.move();
        }
        else if(turno == 2)
            turno = 1;
        Toast.makeText(this, "TURNO EQUIPO: "+(turno==1?"Blanco":"Rojo"), Toast.LENGTH_SHORT).show();
    }

    /*public static void win(int team){
        Toast.makeText(this, "GANÓ EQUIPO: "+(team==1?"Blanco":"Rojo"), Toast.LENGTH_SHORT).show();
    }*/

    public void selectCasilla(Casilla casilla){
        if(turno==casilla.teamNumber)
            casilla.select();
        else{
            Toast.makeText(this, "Movimiento erroneo, turno equipo: "+(turno==1?"Blanco":"Rojo"), Toast.LENGTH_SHORT).show();
        }
        //casilla.select();
    }

    public boolean validateIsOnlySelected(Casilla[][] casillas, View view){
        boolean flag = false;
        Casilla currentSelected = null;
        Casilla newSelected = null;
        if(I>-1 && J>-1)
            currentSelected = casillas[I][J];

        for(int i = 0; i<casillas.length; i++) {
            for (int j = 0; j < casillas.length; j++) {
                if(casillas[i][j].view.equals(view)){
                    if(currentSelected!=null && casillas[i][j].getIsValidMovement() && !casillas[i][j].getIsSelected()){
                        I = -1;
                        J = -1;
                        flag = currentSelected.move(casillas[i][j]);
                    }
                    else {
                        I = i;
                        J = j;
                        newSelected = casillas[i][j];
                    }

                }else {
                    if(casillas[i][j].getIsValidMovement()){
                        if(newSelected!=null? !newSelected.allowedMovements.contains(casillas[i][j]):true)
                            casillas[i][j].unselect();
                    }
                    if(casillas[i][j].getIsSelected()) {
                        casillas[i][j].unselect();
                    }
                }
            }
        }
        return flag;
    }

    public  void setNewGame(Casilla[][] casillas){
        turno = 2;
        for(int i = 0; i<casillas.length-3; i++){
            for(int j = 0; j<casillas.length; j++){
                if(casillas[i][j].isBlack && i<3){
                    casillas[i][j].teamNumber = 1;
                    casillas[i][j].setIsEmplty(false);

                    casillas[casillas.length-1-i][casillas.length-1-j].teamNumber = 2;
                    casillas[casillas.length-1-i][casillas.length-1-j].setIsEmplty(false);
                }else {
                    casillas[i][j].teamNumber = 0;
                    casillas[i][j].setIsEmplty(true);
                }
            }
        }
    }


}

class ArtificialInteligence{
    public Casilla[][] casillas;
    public int teamNumber;
    public int team1ChipsNum;
    public int team2ChipsNum;

    public int depth;
    public List<CasillaEvaluation> evaluationsList;

    public ArtificialInteligence(Casilla[][] casillas, int depth){
        teamNumber = 1;
        team1ChipsNum = 12;
        team2ChipsNum = 12;
        this.casillas = casillas;
        this.depth = depth;
        evaluationsList = new ArrayList<CasillaEvaluation>();
    }

    public boolean move(){
        Casilla[][] tabTmp = cloneTablero(casillas);
        evaluate(tabTmp, 0, 0, 0, null);
        if(evaluationsList.size()>0) {
            evaluationsList = orderByHeuristicValue(evaluationsList);
            CasillaEvaluation aux = null;
            if(evaluationsList.get(0).previous!=null){
                aux = evaluationsList.get(0).previous;
                while (aux.previous!=null)
                    aux = aux.previous;
                casillas[aux.from.y][aux.from.x].setMovements(true);
                return casillas[aux.from.y][aux.from.x].move(casillas[aux.to.y][aux.to.x]);
            }else {
                aux = evaluationsList.get(0);
                casillas[aux.from.y][aux.from.x].setMovements(true);
                return casillas[aux.from.y][aux.from.x].move(casillas[aux.to.y][aux.to.x]);
            }
        }
        return false;
    }

    public void evaluate(Casilla[][] casillas, int step, int fichasPerdidas, int fichasGanadas, CasillaEvaluation previous){
        if(step<depth && (fichasGanadas-fichasPerdidas)>=0)
        {
            List<CasillaEvaluation> evaluations = getMovementsByTeamNumber(step%2==0?teamNumber:(teamNumber==1?2:1), casillas, previous);

            for (int i = 0; i<evaluations.size(); i++){
                evaluations.get(i).heuristicValue = fichasGanadas+(step%2==0?evaluations.get(i).jumps:0) - fichasPerdidas+(step%2==0?0:evaluations.get(i).jumps);
            }
            if(step==depth-1){
                evaluationsList.addAll(evaluations);
            }else
            for (int i = 0; i<evaluations.size(); i++){
                Casilla[][] tabTmp = cloneTablero(casillas);
                tabTmp[evaluations.get(i).from.y][evaluations.get(i).from.x].setMovements(false);
                if(tabTmp[evaluations.get(i).from.y][evaluations.get(i).from.x].moveNoUI(tabTmp[evaluations.get(i).to.y][evaluations.get(i).to.x]))
                    evaluate(tabTmp, step+1, fichasPerdidas+(step%2==0?0:evaluations.get(i).jumps), fichasGanadas+(step%2==0?evaluations.get(i).jumps:0), evaluations.get(i));
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
                        int jumpCount = 0;
                        while (!aux.equals(casillas[i][j])){
                            jumpCount++;
                            aux = aux.getPreviousJump();
                        }
                        movementsList.add(new CasillaEvaluation(casillas[i][j], casillas[i][j].allowedMovements.get(k), jumpCount, previous));
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
        int k = movementsList.size()-1, L = 1, R = movementsList.size()-1;
        do{
            for(int j = R; j >= L; j--)
                if (movementsList.get(j-1).heuristicValue < movementsList.get(j).heuristicValue){
                    CasillaEvaluation x = movementsList.get(j-1);
                    movementsList.remove(j-1);
                    movementsList.add(j, x);
                    k = j;
                }
            L = k+1;
            for(int j = L; j<=R;j++)
                if (movementsList.get(j-1).heuristicValue < movementsList.get(j).heuristicValue){
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

class Casilla{
    public TextView view;
    private boolean isEmplty;
    public boolean isBlack;
    public boolean isDama;
    public List<Casilla> allowedMovements;
    private boolean isValidMovement;
    private boolean isSelected;
    private Casilla previousJump;
    private int numCasillas;
    public int teamNumber;
    public int x;
    public int y;
    public int size;

    public List<Casilla> casillaList;

    public Casilla(TextView view, final boolean isEmplty, boolean isBlack, int x, int y, int size, int numCasillas){
        casillaList = new ArrayList<Casilla>();
        allowedMovements = new ArrayList<Casilla>();
        this.view = view;
        this.isBlack = isBlack;
        this.isEmplty = isEmplty;
        this.isValidMovement = false;
        this.isSelected = false;
        this.isDama = false;
        this.previousJump = null;
        this.numCasillas = numCasillas;
        this.x = x;
        this.y = y;
        this.size = size;
        teamNumber = 0;

        if(isBlack)
            view.setBackgroundResource(R.drawable.black_empty);
        else
            view.setBackgroundResource(R.drawable.white_empty);
        view.setText("("+x+","+y+")");
        view.setTextColor(Color.GREEN);
        view.setHeight(size);
        view.setWidth(size);

    }

    private Casilla(Casilla origin){
        this.casillaList = new ArrayList<Casilla>();
        this.allowedMovements = new ArrayList<Casilla>();

        this.view = null;
        this.isBlack = origin.isBlack;
        this.isEmplty = origin.isEmplty;
        this.isValidMovement = origin.isValidMovement;
        this.isSelected = origin.isSelected;
        this.isDama = origin.isDama;
        this.previousJump = null;
        this.numCasillas = origin.numCasillas;
        this.x = origin.x;
        this.y = origin.y;
        this.size = origin.size;
        this.teamNumber = origin.teamNumber;
    }
    //*
    public Casilla clone(){
        return new Casilla(this);
    }
    //*/

    public boolean getIsSelected(){
        return isSelected;
    }

    public Casilla getPreviousJump(){
        return previousJump;
    }

    public boolean getIsValidMovement(){
        return isValidMovement;
    }

    public void unselect(){
        isSelected = false;
        previousJump = null;
        this.isValidMovement = false;
        if(isEmplty){
            if(isBlack)
                view.setBackgroundResource(R.drawable.black_empty);
            else
                view.setBackgroundResource(R.drawable.white_empty);
        }else {
            if(teamNumber==1)
                view.setBackgroundResource(R.drawable.black_full);
            else if(teamNumber==2)
                view.setBackgroundResource(R.drawable.black_full2);
        }
    }

    public void select(){
        if(!isEmplty){
            isSelected = true;
            if(teamNumber==1)
                view.setBackgroundResource(R.drawable.black_selected);
            else if(teamNumber==2)
                view.setBackgroundResource(R.drawable.black_selected2);
            setMovements(true);

        }
    }

    public boolean move(Casilla destiny){
        if(destiny.isEmplty && this.allowedMovements.contains(destiny) && destiny.isValidMovement){
            if((this.teamNumber==2 && destiny.y == 0) || (this.teamNumber==1 && destiny.y == numCasillas-1))
                destiny.isDama = true;
            if(destiny.previousJump!=null)
                if(destiny.previousJump.equals(this)){
                    if(teamNumber==1)
                        destiny.view.setBackgroundResource(R.drawable.black_full);
                    else if(teamNumber==2)
                        destiny.view.setBackgroundResource(R.drawable.black_full2);
                    destiny.isEmplty = false;
                    destiny.teamNumber = teamNumber;
                    this.remove();
                    return true;
                }else {
                    Casilla aux = destiny.previousJump;
                    while(aux.previousJump!=null){
                        if(aux.teamNumber!=this.teamNumber)// && aux.teamNumber!=0)
                            aux.remove();
                        else{
                            Toast.makeText(view.getContext(), "Error, brincaste una ficha de tu color.", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        aux = aux.previousJump;
                    }
                    if(aux.equals(this)){
                        if(teamNumber==1)
                            destiny.view.setBackgroundResource(R.drawable.black_full);
                        else if(teamNumber==2)
                            destiny.view.setBackgroundResource(R.drawable.black_full2);
                        destiny.isEmplty = false;
                        destiny.teamNumber = teamNumber;
                        this.remove();
                        for (int j = 0; j<allowedMovements.size(); j++)
                            allowedMovements.get(j).unselect();
                        allowedMovements = new ArrayList<Casilla>();
                        return true;
                    }
                }
        }else
            Toast.makeText(this.view.getContext(), "Movimiento Erroneo.", Toast.LENGTH_SHORT).show();
        return false;
    }

    public boolean moveNoUI(Casilla destiny){
        if(destiny.isEmplty && destiny.isValidMovement){
            if((this.teamNumber==2 && destiny.y == 0) || (this.teamNumber==1 && destiny.y == numCasillas-1))
                destiny.isDama = true;
            if(destiny.previousJump!=null) {
                if (destiny.previousJump.equals(this)) {
                    destiny.isEmplty = false;
                    destiny.teamNumber = teamNumber;
                    if (!this.isEmplty) {
                        this.isEmplty = true;
                        this.isValidMovement = false;
                        this.isSelected = false;
                        this.isDama = false;
                        this.teamNumber = 0;
                    }
                    return true;
                } else {
                    Casilla aux = destiny.previousJump;
                    while (aux.previousJump != null) {
                        if (aux.teamNumber != this.teamNumber) {
                            if (!aux.isEmplty) {
                                aux.isEmplty = true;
                                aux.isValidMovement = false;
                                aux.isSelected = false;
                                aux.isDama = false;
                                aux.teamNumber = 0;
                            }
                        } else {
                            break;
                        }
                        aux = aux.previousJump;
                    }
                    if (aux.equals(this)) {

                        destiny.isEmplty = false;
                        destiny.teamNumber = teamNumber;

                        for (int j = 0; j < allowedMovements.size(); j++) {
                            allowedMovements.get(j).isSelected = false;
                            allowedMovements.get(j).previousJump = null;
                            allowedMovements.get(j).isValidMovement = false;
                        }
                        allowedMovements = new ArrayList<Casilla>();
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void remove(){
        if(!this.isEmplty){
            this.isEmplty = true;
            this.isValidMovement = false;
            this.isSelected = false;
            this.isDama = false;
            this.teamNumber = 0;
            if(isBlack)
                view.setBackgroundResource(R.drawable.black_empty);
            else
                view.setBackgroundResource(R.drawable.white_empty);
        }
    }

    public void setMovements(boolean UI){

        if(this.isDama)
            damaMovements(this, UI);
        else
            normalMovements(this, UI);
    }

    private void damaMovements(Casilla casilla, boolean UI){
        for (int i = 0; i<casilla.casillaList.size(); i++){
            casilla.casillaList.get(i).previousJump = casilla;
            if(casilla.casillaList.get(i).isEmplty)
                simpleJump(casilla.casillaList.get(i), UI);
            else
                recursiveMovement(casilla.casillaList.get(i), UI);
        }
    }

    private void simpleJump(Casilla destiny, boolean UI){
        destiny.isValidMovement = true;
        destiny.previousJump = this;
        this.allowedMovements.add(destiny);
        if(this.isDama)
            destiny.isDama = true;
        if(UI)
            destiny.view.setBackgroundResource(R.drawable.allowed);
    }

    private void normalMovements(Casilla casilla, boolean UI){
        for (int i = 0; i<casilla.casillaList.size(); i++){
            boolean isAdvancing =  (casilla.casillaList.get(i).y>casilla.y && this.teamNumber ==1)
                                || (casilla.casillaList.get(i).y<casilla.y && this.teamNumber ==2);
            if(!isAdvancing)
                continue;
            casilla.casillaList.get(i).previousJump = casilla;
            if(casilla.casillaList.get(i).isEmplty)
                simpleJump(casilla.casillaList.get(i), UI);
            else
                recursiveMovement(casilla.casillaList.get(i), UI);
        }
    }

    private void recursiveJump(Casilla from, Casilla destiny, boolean UI){
        if(destiny.x!= from.previousJump.x && destiny.y!= from.previousJump.y){
            destiny.isValidMovement = true;
            destiny.previousJump = from;
            this.allowedMovements.add(destiny);
            if(UI)
                destiny.view.setBackgroundResource(R.drawable.allowed);
            if(this.isDama)
                destiny.isDama = true;
            for (int i = 0; i<destiny.casillaList.size(); i++){
                boolean isAdvancing =  (destiny.casillaList.get(i).y>destiny.y && this.teamNumber ==1)
                                    || (destiny.casillaList.get(i).y<destiny.y && this.teamNumber ==2)
                                    || this.isDama;
                if(!destiny.casillaList.get(i).isEmplty && isAdvancing && !destiny.casillaList.get(i).equals(from)) {
                    destiny.casillaList.get(i).previousJump = destiny;
                    recursiveMovement(destiny.casillaList.get(i), UI);
                }
            }
        }
    }

    private void recursiveMovement(Casilla casilla, boolean UI){
        if(!casilla.isEmplty && casilla.teamNumber!=this.teamNumber){
            for (int i = 0; i<casilla.casillaList.size(); i++){
                if(casilla.casillaList.get(i).isEmplty)
                    recursiveJump(casilla, casilla.casillaList.get(i), UI);
            }
        }
    }

    private void removeChip(Casilla casilla){
        if(!casilla.isEmplty){
            casilla.isEmplty = true;
            if(isBlack)
                view.setBackgroundResource(R.drawable.black_empty);
            else
                view.setBackgroundResource(R.drawable.white_empty);
        }
    }

    public void setIsEmplty(boolean isEmplty){
        if(isEmplty){
            if(isBlack)
                view.setBackgroundResource(R.drawable.black_empty);
            else
                view.setBackgroundResource(R.drawable.white_empty);
        }else {
            if(teamNumber==1)
                view.setBackgroundResource(R.drawable.black_full);
            else if(teamNumber==2)
                view.setBackgroundResource(R.drawable.black_full2);
        }
        this.isEmplty = isEmplty;
    }

    public  boolean getIsEmpty (){
        return isEmplty;
    }
}