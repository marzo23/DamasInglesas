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
    int numCasillas = 8;
    int depth = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int ancho = size.x;
        int alto = size.y;

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

        /*TextView passButton = new TextView(this);
        passButton.setWidth(ancho/numCasillas);
        passButton.setHeight(ancho/numCasillas);
        passButton.setBackgroundResource(R.drawable.arrow);
        passButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetStates(casillas);
                turnoMsg();
            }
        });*/

        TableRow buttonRow = new TableRow(this);
        buttonRow.addView(playButton);
        //buttonRow.addView(passButton);
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

    public void resetStates(Casilla[][] casillas){
        int team1Count = 0;
        int team2Count = 0;
        for(int i = 0; i<casillas.length; i++) {
            for (int j = 0; j < casillas.length; j++) {
                casillas[i][j].unselect(true);
                if(!casillas[i][j].getIsEmpty()){
                    if(casillas[i][j].teamNumber==1)
                        team1Count++;
                    else if(casillas[i][j].teamNumber==2)
                        team2Count++;
                }
            }
        }
        if(team1Count==0 || team2Count==0) {
            Toast.makeText(casillas[0][0].view.getContext(), "GANÓ EQUIPO: " + (team1Count == 0 ? "Rojo" : "Blanco"), Toast.LENGTH_SHORT).show();
            setNewGame(casillas);
        }
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
                            casillas[i][j].unselect(true);
                    }
                    if(casillas[i][j].getIsSelected()) {
                        casillas[i][j].unselect(true);
                    }
                }
            }
        }
        return flag;
    }

    public void setNewGame(Casilla[][] casillas){
        ia = new ArtificialInteligence(casillas, depth);
        turno = 2;
        for(int i = 0; i<casillas.length-3; i++){
            for(int j = 0; j<casillas.length; j++){
                casillas[i][j].remove();
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


