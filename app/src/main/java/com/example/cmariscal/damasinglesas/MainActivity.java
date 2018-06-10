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
        playButton.setBackgroundResource(R.drawable.retry);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setNewGame(casillas);
            }
        });

        TableRow buttonRow = new TableRow(this);
        buttonRow.addView(playButton);
        tabla.addView(buttonRow);

        setContentView(tabla);
    }
    int I = -1;
    int J = -1;
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
                        if(validateIsOnlySelected(casillas, view) && (I<0 || J<0))
                            resetStates(casillas);
                        else
                            selectCasilla(casillas[I][J]);
                    }
                });
            }
        }
    }

    public void resetStates(Casilla[][] casillas){

        boolean flag = false;
        for(int i = 0; i<casillas.length; i++) {
            for (int j = 0; j < casillas.length; j++) {
                casillas[i][j].unselect();
                if(!casillas[i][j].getIsEmpty()){
                    if(I>-1 && J>-1){
                        if(casillas[I][J].teamNumber==casillas[i][j].teamNumber){
                            flag = true;
                            I = i;
                            J = j;
                        }else
                            flag = false;
                    }
                }
            }
        }
        if(!flag){
            I = -1;
            J = -1;
        }else
            win(casillas[I][J]);

    }

    public void win(Casilla casilla){
        Toast.makeText(casilla.view.getContext(), "GANÓ EQUIPO: "+casilla.teamNumber, Toast.LENGTH_SHORT).show();
    }

    public void selectCasilla(Casilla casilla){
        casilla.select();
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
                        currentSelected.move(casillas[i][j]);
                        flag = true;
                    }
                    //else if(!casillas[i][j].getIsValidMovement())
                        //casillas[i][j].unselect();
                    else {
                        I = i;
                        J = j;
                    }

                }else {
                    if(casillas[i][j].getIsSelected()) {
                        if(!casillas[i][j].getIsValidMovement())
                            casillas[i][j].unselect();
                    }
                }
            }
        }
        return flag;
    }

    public  void setNewGame(Casilla[][] casillas){
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
                /*else {
                    casillas[casillas.length-1-i][casillas.length-1-j].setIsEmplty(false);
                }*/
            }
        }
    }


}



class Casilla{
    public TextView view;
    private boolean isEmplty;
    public boolean isBlack;
    public boolean isDama;
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
            setMovements(this);

        }
    }

    public void move(Casilla destiny){
        if(destiny.isEmplty){
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
            }else {
                Casilla aux = destiny.previousJump;
                while(aux.previousJump!=null){
                    if(aux.teamNumber!=this.teamNumber && aux.teamNumber!=0)
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
                }
            }
        }

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

    private boolean setMovements(Casilla casilla){
        boolean flag = false;
        for (int i = 0; i<casilla.casillaList.size(); i++){
            boolean isAdvancing = false;
            Casilla aux = null;
            if(casilla.previousJump==null){
                isAdvancing = (casilla.casillaList.get(i).y>casilla.y && casilla.teamNumber ==1)
                        || (casilla.casillaList.get(i).y<casilla.y && casilla.teamNumber ==2);
            }else {
                aux = casilla.previousJump;
                while (aux.previousJump!=null){
                    aux = aux.previousJump;
                }
                isAdvancing = (casilla.casillaList.get(i).y>casilla.y && aux.teamNumber ==1)
                        || (casilla.casillaList.get(i).y<casilla.y && aux.teamNumber ==2);
            }

            if(casilla.casillaList.get(i).isEmplty && (casilla.casillaList.get(i).isDama || isAdvancing)) {
                if(casilla.previousJump==null){
                    casilla.casillaList.get(i).isValidMovement = true;
                    casilla.casillaList.get(i).previousJump = casilla;
                    casilla.casillaList.get(i).view.setBackgroundResource(R.drawable.allowed);
                    flag = true;
                }else {
                    if(!casilla.isEmplty && casilla.casillaList.get(i).x!= casilla.previousJump.x){
                        casilla.casillaList.get(i).isValidMovement = true;
                        casilla.casillaList.get(i).view.setBackgroundResource(R.drawable.allowed);
                        flag = true;
                        casilla.casillaList.get(i).previousJump = casilla;
                        if (flag)
                            setMovements(casilla.casillaList.get(i));
                        else
                            flag = setMovements(casilla.casillaList.get(i));
                    }
                }
            }else if(!casilla.casillaList.get(i).isEmplty && ( casilla.casillaList.get(i).isDama || isAdvancing)){
                int tn = -1;
                if(casilla.isEmplty && casilla.previousJump!=null && aux!=null){
                    tn = aux.teamNumber;
                }else if(!casilla.isEmplty && casilla.previousJump==null)
                    tn = casilla.teamNumber;
                if(tn > 0 && casilla.casillaList.get(i).teamNumber != tn){
                    casilla.casillaList.get(i).previousJump = casilla;
                    if (flag)
                        setMovements(casilla.casillaList.get(i));
                    else
                        flag = setMovements(casilla.casillaList.get(i));
                }

            }
        }
        return flag;
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