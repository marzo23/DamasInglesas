package com.example.cmariscal.damasinglesas;

import android.graphics.Color;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class Casilla{
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

    public void unselect(boolean UI){
        isSelected = false;
        previousJump = null;
        isValidMovement = false;
        if(isEmplty)
            isDama = false;
        if(UI)
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
        System.gc();
        Log.d("moveUI Team"+this.teamNumber, "from: "+this.x+", "+this.y+"  To: "+destiny.x+", "+destiny.y);
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
                    while(aux.previousJump!=null && !aux.equals(aux.previousJump)){
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
                            allowedMovements.get(j).unselect(true);
                        allowedMovements = new ArrayList<Casilla>();
                        return true;
                    }
                }
        }else
            Toast.makeText(this.view.getContext(), "Movimiento Erroneo.", Toast.LENGTH_SHORT).show();
        return false;
    }

    public boolean moveNoUI(Casilla destiny){
        //Log.d("moveNoUI", "from: "+this.x+", "+this.y+"  To: "+destiny.x+", "+destiny.y);
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
                    while (aux.previousJump != null && !aux.previousJump.equals(aux)) {
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
            //casilla.casillaList.get(i).previousJump = casilla;
            if(casilla.casillaList.get(i).isEmplty)
                casilla.simpleJump(casilla.casillaList.get(i), UI);
            else {
                List<Casilla> aux = new ArrayList<Casilla>();
                aux.add(this);
                casilla.recursiveMovement(casilla.casillaList.get(i), UI, aux);
            }
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
            //casilla.casillaList.get(i).previousJump = casilla;
            if(casilla.casillaList.get(i).isEmplty)
                casilla.simpleJump(casilla.casillaList.get(i), UI);
            else{
                List<Casilla> aux = new ArrayList<Casilla>();
                aux.add(this);
                casilla.recursiveMovement(casilla.casillaList.get(i), UI, aux);
            }

        }
    }

    private void recursiveJump(Casilla from, Casilla destiny, boolean UI, List<Casilla> procesed){
        //if(destiny.x!= from.previousJump.x && destiny.y!= from.previousJump.y){
            destiny.isValidMovement = true;
            destiny.previousJump = from;
            this.allowedMovements.add(destiny);
            if(UI)
                destiny.view.setBackgroundResource(R.drawable.allowed);
            //if(this.isDama)
            //    destiny.isDama = true;
            for (int i = 0; i<destiny.casillaList.size(); i++){
                boolean isAdvancing =  (destiny.casillaList.get(i).y>destiny.y && this.teamNumber ==1)
                        || (destiny.casillaList.get(i).y<destiny.y && this.teamNumber ==2)
                        || this.isDama;
                if(!destiny.casillaList.get(i).isEmplty && isAdvancing && !this.equals(destiny.casillaList.get(i))) {
                    destiny.casillaList.get(i).previousJump = destiny;
                    recursiveMovement(destiny.casillaList.get(i), UI, procesed);
                }
            }
        //}
    }

    private void recursiveMovement(Casilla casilla, boolean UI, List<Casilla> procesed){
        if(!casilla.isEmplty && casilla.teamNumber!=this.teamNumber && !procesed.contains(casilla)){
            procesed.add(casilla);
            boolean flag = false;
            for (int i = 0; i<casilla.casillaList.size(); i++){
                if(casilla.casillaList.get(i).isEmplty && !procesed.contains(casilla.casillaList.get(i))) {
                    procesed.add(casilla.casillaList.get(i));
                    if(casilla.casillaList.get(i).x!= (casilla.previousJump!=null?casilla.previousJump.x:this.x) && casilla.casillaList.get(i).y!= (casilla.previousJump!=null?casilla.previousJump.y:this.y)) {
                        flag = true;
                        recursiveJump(casilla, casilla.casillaList.get(i), UI, procesed);
                    }
                }
            }
            if(flag)
                casilla.previousJump = this;
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