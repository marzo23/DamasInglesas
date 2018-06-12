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

                        if(validateIsOnlySelected(casillas, view) && (I<0 || J<0)){

                            resetStates(casillas);
                            turnoMsg();
                        }
                        else
                            selectCasilla(casillas[I][J]);
                    }
                });
            }
        }
    }

    public void resetStates(Casilla[][] casillas){
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
            win(team1Count==0?1:2);

    }

    public void turnoMsg(){
        if(turno == 1)
            turno = 2;
        else if(turno == 2)
            turno = 1;
        Toast.makeText(this, "TURNO EQUIPO: "+(turno==1?"Blanco":"Rojo"), Toast.LENGTH_SHORT).show();
    }

    public void win(int team){
        Toast.makeText(this, "GANÓ EQUIPO: "+(team==1?"Blanco":"Rojo"), Toast.LENGTH_SHORT).show();
    }

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
                    }

                }else {
                    if(casillas[i][j].getIsValidMovement() && currentSelected!=null){
                        if(!currentSelected.allowedMovements.contains(casillas[i][j]))
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
        turno = 1;
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
            setMovements();

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

    public void remove(){
        if(!this.isEmplty){
            this.isEmplty = true;
            this.isValidMovement = false;
            this.isSelected = false;
            this.isDama = false;
            teamNumber = 0;
            if(isBlack)
                view.setBackgroundResource(R.drawable.black_empty);
            else
                view.setBackgroundResource(R.drawable.white_empty);
        }
    }

    private void setMovements(){
        /*boolean flag = false;
        for (int i = 0; i<casilla.casillaList.size(); i++){
            boolean isAdvancing = false;
            Casilla aux = null;
            if(casilla.previousJump==null){
                isAdvancing = (casilla.casillaList.get(i).y>casilla.y && casilla.teamNumber ==1)
                        || (casilla.casillaList.get(i).y<casilla.y && casilla.teamNumber ==2)
                        || this.isDama;
            }else {
                aux = this;
                isAdvancing = (casilla.casillaList.get(i).y>casilla.y && aux.teamNumber ==1)
                        || (casilla.casillaList.get(i).y<casilla.y && aux.teamNumber ==2)
                        || this.isDama;
            }

            if(casilla.casillaList.get(i).isEmplty && isAdvancing) {
                if(casilla.previousJump==null){
                    casilla.casillaList.get(i).isValidMovement = true;
                    casilla.casillaList.get(i).previousJump = casilla;
                    this.allowedMovements.add(casilla.casillaList.get(i));
                    casilla.casillaList.get(i).view.setBackgroundResource(R.drawable.allowed);
                    flag = true;
                }else {
                    if(!casilla.isEmplty && casilla.casillaList.get(i).x!= casilla.previousJump.x && casilla.casillaList.get(i).y!= casilla.previousJump.y){
                        casilla.casillaList.get(i).isValidMovement = true;
                        casilla.casillaList.get(i).previousJump = casilla;
                        this.allowedMovements.add(casilla.casillaList.get(i));
                        casilla.casillaList.get(i).view.setBackgroundResource(R.drawable.allowed);
                        flag = true;
                        if (flag)
                            setMovements(casilla.casillaList.get(i));
                        else
                            flag = setMovements(casilla.casillaList.get(i));
                    }
                }
            }else if(!casilla.casillaList.get(i).isEmplty && (casilla.isEmplty || casilla.previousJump==null) && isAdvancing){
                int tn = -1;
                if(casilla.previousJump!=null && aux!=null){ // casilla.isEmplty &&
                    tn = aux.teamNumber;
                }else if(!casilla.isEmplty && casilla.previousJump==null)
                    tn = casilla.teamNumber;
                if(tn > 0 && casilla.casillaList.get(i).teamNumber != tn){
                    casilla.casillaList.get(i).previousJump = casilla;
                    //this.allowedMovements.add(casilla.casillaList.get(i));
                    if (flag)
                        setMovements(casilla.casillaList.get(i));
                    else
                        flag = setMovements(casilla.casillaList.get(i));
                }

            }
        }
        return flag;//*/
        if(this.isDama)
            damaMovements(this);
        else
            normalMovements(this);
    }

    private void damaMovements(Casilla casilla){
        for (int i = 0; i<casilla.casillaList.size(); i++){
            casilla.casillaList.get(i).previousJump = casilla;
            if(casilla.casillaList.get(i).isEmplty)
                simpleJump(casilla.casillaList.get(i));
            else
                recursiveMovement(casilla.casillaList.get(i));
        }
    }

    private void simpleJump(Casilla destiny){
        destiny.isValidMovement = true;
        destiny.previousJump = this;
        this.allowedMovements.add(destiny);
        if(this.isDama)
            destiny.isDama = true;
        destiny.view.setBackgroundResource(R.drawable.allowed);
    }

    private void normalMovements(Casilla casilla){
        for (int i = 0; i<casilla.casillaList.size(); i++){
            boolean isAdvancing =  (casilla.casillaList.get(i).y>casilla.y && this.teamNumber ==1)
                                || (casilla.casillaList.get(i).y<casilla.y && this.teamNumber ==2);
            if(!isAdvancing)
                continue;
            casilla.casillaList.get(i).previousJump = casilla;
            if(casilla.casillaList.get(i).isEmplty)
                simpleJump(casilla.casillaList.get(i));
            else
                recursiveMovement(casilla.casillaList.get(i));
        }
    }

    private void recursiveJump(Casilla from, Casilla destiny){
        if(destiny.x!= from.previousJump.x && destiny.y!= from.previousJump.y){
            destiny.isValidMovement = true;
            destiny.previousJump = from;
            this.allowedMovements.add(destiny);
            destiny.view.setBackgroundResource(R.drawable.allowed);
            if(this.isDama)
                destiny.isDama = true;
            for (int i = 0; i<destiny.casillaList.size(); i++){
                boolean isAdvancing =  (destiny.casillaList.get(i).y>destiny.y && this.teamNumber ==1)
                                    || (destiny.casillaList.get(i).y<destiny.y && this.teamNumber ==2)
                                    || this.isDama;
                if(!destiny.casillaList.get(i).isEmplty && isAdvancing && !destiny.casillaList.get(i).equals(from)) {
                    destiny.casillaList.get(i).previousJump = destiny;
                    recursiveMovement(destiny.casillaList.get(i));
                }
            }
        }
    }

    private void recursiveMovement(Casilla casilla){
        if(!casilla.isEmplty && casilla.teamNumber!=this.teamNumber){
            for (int i = 0; i<casilla.casillaList.size(); i++){
                if(casilla.casillaList.get(i).isEmplty)
                    recursiveJump(casilla, casilla.casillaList.get(i));
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