import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import controlP5.*; 
import java.util.*; 
import javax.swing.JOptionPane; 
import TUIO.*; 
import processing.serial.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class CUBE_IT_SCOTT_03 extends PApplet {

/*
 ----------------------------------------------
              CUBE_IT for SCOTT
 ----------------------------------------------
 Auteur : Adrien B.
 Date : Avril 2016
 Licence : CC-BY-NC
 ----------------------------------------------
 Description : ce sketch permet de piloter un
 robot sous arduino ( en l'occurence le robot 
 Open Source Scott d\u00e9velopp\u00e9 par l'association
 Ami\u00e9noise La Machinerie ) en utilisant des 
 information TUIO. Ces informations sont 
 envoy\u00e9es par une table sous reactivision.
 ----------------------------------------------
 */

/*------IMPORT DES LIBRAIRIES DU PROJET------*/
 
                  // Inporte la librairie CP5 pour la gestion du GUI

      // Importe la librairie pour g\u00e9n\u00e9rer une Pop-Up
                       // Importe la librairie TUIO
          // Importe la librairie pour utiliser le port S\u00e9rie
 
/*------DECLARATION DES OBJETS DU SKETCH------*/

TuioProcessing tuioClient;           // D\u00e9clare un client TuioProcessing
Serial myPort;                       // D\u00e9clare le port s\u00e9rie
ControlP5 cp5;                       // D\u00e9clare un objet CP5 pour le GUI


/*------DECLARATION DES VARIABLES pour la GUI CP5------*/

// Gestion de la console
Textarea myTextarea;
Println console;

// Gestion des zones de texte
Textfield TFinString;
Textfield TFetatString;
Textfield TFetatProgramme;

// Gestion des listes
ScrollableList serialList;

// Gestion des bouttons
Button serialBegin;
Button Simulator;
Button Reactivision;
Button Credits;

// Etat des bouttons
boolean clicSerialBegin = false ;
boolean clicSimulator = false ;
boolean clicReactivision = false ;
boolean clicCredits = false ;

/*------DECLARATION DES VARAIBLES DE COMMUNICATION------*/

String inString = " ";                  // Chaine de reception brute
String etatString;                      // 
String etatProgramme;                   // 
boolean scott = false ;                 // Flag de disponibilit\u00e9 de Scott
boolean lancementProgramme = false;     // 
int lf = 10;                            // ASCII linefeed 

/*------DECLARATION DES VARAIBLES D'ORDRE------*/

// Constantes d'\u00e9tat d'ordre
final byte nonTransmis = 0 ;            // L'ordre n'est pas encore transmis
final byte transmis = 1 ;               // L'ordre est transmis, en attente d'ex\u00e9cution par le robot
final byte effectue = 2 ;               // L'ordre a \u00e9t\u00e9 \u00e9ffectu\u00e9 par le robot

byte etatScott = 0;                     // Stockage de l'\u00e9tat de l'ordre pour Scott ( nonTransmis, transmis, effectue )
boolean saut = false;                   // Demande un saut vers la prochaine fin de test conditionnel
boolean retour = false;                 // Demande un retour vers le precedent "TANT QUE" si vrai

int[][] ETAPE ;                         // Tableau de stockage des \u00e9tapes de programmation pour le robot
int nbEtape = 0;                        // Nombre d'\u00e9tape d\u00e9t\u00e9ct\u00e9es 
int etapeActuelle = 0 ;                 // Pointeur du tableau d'\u00e9tape

/*-------------AUTRES VARIABLES----------------*/

float cursor_size = 15;
float object_size = 50;
float table_size = 500;
float scale_factor = 1;
PFont font;
int top = 60;
PImage img;

boolean verbose = false;                // print console debug messages
boolean callback = false;               // updates only after callbacks


/*-----------------------------------------------------------------------------------
                                FONCTION : setup()
 ------------------------------------------------------------------------------------
 DESCRIPTION : 
 Fonction ex\u00e9cut\u00e9e une fois au d\u00e9but du programme. Elle permet d'initialiser ce 
 dernier en mettant en place le cadre g\u00e9n\u00e9ral.
 ------------------------------------------------------------------------------------*/
public void setup()
{
                                   // Lisse les lignes 
  fill(255);                            
      // Taille de la fen\u00eatre
  //fullScreen();
  noStroke();                                // Pas de bords
  CP5_GUI_CREATE();                          // Cr\u00e9ation de l'interface 
  tuioClient  = new TuioProcessing(this);    // Create the TUIO Client
  
  img = loadImage("CUBE_NOIR_BLANC_LEGER.png");

  /*----------GESTION DU CALLBACK------------*/
  
  if (!callback) {
    frameRate(5);
    loop();
  } else noLoop(); // or callback updates 

  /*----------LISTE DES PORTS SERIE------------*/
  println("Bienvenue sur l'interface de gestion du CUBE");
  println("Les ports s\u00e9rie disponibles sont les suivants :");
  printArray(Serial.list());
}

/*-----------------------------------------------------------------------------------
                                FONCTION : draw()
 ------------------------------------------------------------------------------------
 DESCRIPTION : 
 C'est le coeur du programme. La fonction est appell\u00e9e r\u00e9gulierement et met \u00e0 jour 
 l'affichage et les elements graphique de la fen\u00eatre en plus des divers communication
 avec le robot.
 ------------------------------------------------------------------------------------*/
public void draw()
{
  GUI_BUTTON();                        // Gestion de l'\u00e9tat des diff\u00e9rents bouttons 
  GUI_BACK();                          // Gestion du fond graphique de l'application
  SELECT_ORDRE();                      // Envoi des ordres en fonction de l'\u00e9tat du robot
  AFFICHAGES_TAGS();                   // Gestion de l'affichage des tags sur la fen\u00eatre
  DEMARRAGE_SCOTT();                   // Test de Scott si celui-ci n'est pas encore d\u00e9marr\u00e9
}

/*-----------------------------------------------------------------------------------
                                FONCTION : serialEvent()
 ------------------------------------------------------------------------------------
 DESCRIPTION : 
 Cette fonction est appel\u00e9e \u00e0 chaque fois que le sketch processing recoit un element
 sur le port s\u00e9rie. il stock ensuite cet \u00e9l\u00e9ment dans la variable "inString".
 ------------------------------------------------------------------------------------*/
public void serialEvent(Serial p) 
{ 
  inString = p.readStringUntil('\n');
  if (etatScott == transmis && inString.indexOf("OK") > -1)
  {
    println("Etape ok");
    etatScott = effectue;
  }
  else if (etatScott == transmis && inString.indexOf("FIN_SI") > -1)
  {
    println("Fin de Si");
    etatScott = effectue;
    println ("Etape suivante");
    saut = true ;
  }
  else if (etatScott == transmis && inString.indexOf("FIN_TANTQUE") > -1)
  {
    println("Fin de tant que");
    etatScott = effectue;
    println ("Etape suivante");
    saut = true ;
  }
  if (inString.indexOf("Coucou") > -1)
  {
    println(inString);
    scott = true ;
  }
} 
public void DEMARRAGE_SCOTT()
{
  //--------------------------------------
  // Test de demarrage de scott
  //--------------------------------------
  if (scott == false)
  {
    if (inString.indexOf(" ") > -1 && clicSerialBegin == true)
    {
      etatString = " ";
      scott = true ;
    }
    else if (inString.indexOf(" ") > -1 && clicSerialBegin == false)
    {
      etatString = "Tentative de connection";
      scott = true ;
    }
    else if ( inString.indexOf("Coucou") > -1)
    {
      etatString = "En attente d'ordre";
      scott = true ;
    } 
  }
  
}

public void SELECT_ORDRE()
{
  //--------------------------------------
  // Gestion de ordres 
  //--------------------------------------
  int changeParametre ;
  if (scott == true)
  {
    if ( lancementProgramme == true)
    {
      if (etatScott == nonTransmis)
      {
        switch (ETAPE[0][etapeActuelle])
        {
          case 8: 
            changeParametre = ETAPE[1][etapeActuelle] ;
          break;
          case 4: 
            changeParametre = 120 ;
          break;
          case 3: 
            changeParametre = 120 ;
          break;
          case 10: 
            changeParametre = 150 ;
          break;
          default:
            changeParametre = 250;
          break;
        }
        SEND_ORDRE(ETAPE[0][etapeActuelle] , changeParametre);
        etatScott = transmis ;
        println ("etape transmise");
      }
      else if (etatScott == effectue)
      {
        if (saut == true)
        {
          boolean detectionFin = false ;
          while (detectionFin == false)
          {
            if (ETAPE[0][etapeActuelle] == 6 || ETAPE[0][etapeActuelle] == 7 )
            {
              detectionFin = true;
            }
            else
            {
              etapeActuelle ++;
            }
          }
          saut = false ;
        }
        etapeActuelle ++;
        // Si on sort du tableau, on revient au d\u00e9but
        if (etapeActuelle >= nbEtape)
        {
          etapeActuelle = 0;
        }
        // Si on est \u00e9gal \u00e0 0, on ignore et on passe \u00e0 la suite
        if (ETAPE[0][etapeActuelle]==0)
        {
          etapeActuelle ++;
        }
        // Si on est \u00e9gale \u00e0 fin de Tant que, on revient au pr\u00e9c\u00e9dent tant que
        if (ETAPE[0][etapeActuelle]==7)
        {
          while (ETAPE[0][etapeActuelle] != 11)
          {
            etapeActuelle --;
          }
        }
        if (etapeActuelle >= nbEtape)
        {
          etapeActuelle = 0;
        }
        etatScott = nonTransmis;
        println ("etape suivante");
      }
    } 
    else if ( lancementProgramme == false)
    {
      etapeActuelle = 0;
    }
  }
}


public void SEND_ORDRE(int ordreNumber, int ordreParameter)
{
  String ordreArduino = str(ordreNumber)+";"+ str(ordreParameter)+'\n';
  myPort.write(ordreArduino);
  print("ordre envoy\u00e9 :"+ordreArduino);
}



/*-----------------------------------------------------------------------------------
                                FONCTION : TRI()
 ------------------------------------------------------------------------------------
 DESCRIPTION : 
 Cette fonction r\u00e9cupere tous les elements pr\u00e9sent sur la table est le place dans un
 tableau. Elle tri ensuite ce dernier en fonction de la position des tags : a savoir
 de haut en bas.
 ------------------------------------------------------------------------------------*/
public void TRI()
{
  ArrayList<TuioObject> tuioObjectList = tuioClient.getTuioObjectList();
  //ETAPE = new int[3][tuioObjectList.size()];
  ETAPE = new int[3][30];
  for (int i=0;i<30;i++) 
  {
    ETAPE [0][i] = 0;
    ETAPE [1][i] = 0;
    ETAPE [2][i] = 0;
  }
    
  nbEtape = tuioObjectList.size();
  println("Nouveau Tableau");
  for (int i=0;i<nbEtape;i++) 
  {
    TuioObject tobj = tuioObjectList.get(i);
    ETAPE [0][i] = tobj.getSymbolID();
    ETAPE [1][i] = PApplet.parseInt(degrees(tobj.getAngle()+0.3f));
    ETAPE [2][i] = tobj.getScreenY(590);
    print(ETAPE[0][i]);
    print(";");
    print(ETAPE[1][i]);
    print(";");
    println(ETAPE[2][i]);
  }
  
  // Tri \u00e0 bulle 
  for (int i=1;i<nbEtape;i++)
  {
    for (int k=0;k<(nbEtape-i);k++)
    {
      if (ETAPE[2][k]>ETAPE[2][k+1])
      {
        int temp = ETAPE[2][k+1] ;
        ETAPE[2][k+1] = ETAPE[2][k] ;
        ETAPE[2][k] = temp ;
        temp = ETAPE[1][k+1] ;
        ETAPE[1][k+1] = ETAPE[1][k] ;
        ETAPE[1][k] = temp ;
        temp = ETAPE[0][k+1] ;
        ETAPE[0][k+1] = ETAPE[0][k] ;
        ETAPE[0][k] = temp ;
      }
    }
  }
  
  println("Tableau tri\u00e9");
  for (int i=0;i<nbEtape;i++) 
  {
    print(ETAPE[0][i]);
    print(";");
    print(ETAPE[1][i]);
    print(";");
    println(ETAPE[2][i]);
  }
  etatScott = nonTransmis;
  
}
//--------------------------------------
//      Affichage des tags
//-------------------------------------- 

public void AFFICHAGES_TAGS ()
{

  float obj_size = object_size*scale_factor; 
  
  ArrayList<TuioObject> tuioObjectList = tuioClient.getTuioObjectList();
  for (int i=0;i<tuioObjectList.size();i++) 
  {
    TuioObject tobj = tuioObjectList.get(i);
    stroke(0);
    pushMatrix();
    if ( tobj.getSymbolID() == 8)
     {  
       fill(0xffFFE203);
       translate(tobj.getScreenX(790),tobj.getScreenY(590)+23);
       rotate(tobj.getAngle());
       ellipse(0,0,obj_size,obj_size);
     }
     else
     { 
       fill(0xff24B952);
       translate(tobj.getScreenX(790),tobj.getScreenY(590)+23);
       rotate(tobj.getAngle());
       rect(-obj_size/2,-obj_size/2,obj_size,obj_size,10);
     }
     popMatrix();
     fill(0);
     if ( tobj.getSymbolID() == 8)
     {
       text(""+(PApplet.parseInt(degrees(tobj.getAngle()+0.3f)))*10, tobj.getScreenX(790)-5, tobj.getScreenY(590)+23);
     }
     else
     {
       text(""+tobj.getSymbolID(), tobj.getScreenX(790), tobj.getScreenY(590)+23);
     }
   }
}

//--------------------------------------
//    Fonctions CALLBACKS pour TUIO
//-------------------------------------- 

// --------------------------------------------------------------
// these callback methods are called whenever a TUIO event occurs
// there are three callbacks for add/set/del events for each object/cursor/blob type
// the final refresh callback marks the end of each TUIO frame

// called when an object is added to the scene

public void addTuioObject(TuioObject tobj) {
  if (verbose) println("add obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+") "+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle());
  if (tobj.getSymbolID() == 0) 
  {
    etatProgramme = "Programme lance";
    lancementProgramme = true;
    TRI();
  }
}

// called when an object is moved
public void updateTuioObject (TuioObject tobj) {
  if (verbose) println("set obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+") "+tobj.getX()+" "+tobj.getY()+" "+tobj.getAngle()
          +" "+tobj.getMotionSpeed()+" "+tobj.getRotationSpeed()+" "+tobj.getMotionAccel()+" "+tobj.getRotationAccel());
}

// called when an object is removed from the scene
public void removeTuioObject(TuioObject tobj) {
  if (verbose) println("del obj "+tobj.getSymbolID()+" ("+tobj.getSessionID()+")");
  if (tobj.getSymbolID() == 0)
  {
    etatProgramme = "Programme stop";
    lancementProgramme = false ;
    SEND_ORDRE(32 , 250);
  }
}


// --------------------------------------------------------------
// called when a cursor is added to the scene
public void addTuioCursor(TuioCursor tcur) {
  if (verbose) println("add cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+ ") " +tcur.getX()+" "+tcur.getY());
  //redraw();
}

// called when a cursor is moved
public void updateTuioCursor (TuioCursor tcur) {
  if (verbose) println("set cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+ ") " +tcur.getX()+" "+tcur.getY()
          +" "+tcur.getMotionSpeed()+" "+tcur.getMotionAccel());
  //redraw();
}

// called when a cursor is removed from the scene
public void removeTuioCursor(TuioCursor tcur) {
  if (verbose) println("del cur "+tcur.getCursorID()+" ("+tcur.getSessionID()+")");
  //redraw()
}

// --------------------------------------------------------------
// called when a blob is added to the scene
public void addTuioBlob(TuioBlob tblb) {
  if (verbose) println("add blb "+tblb.getBlobID()+" ("+tblb.getSessionID()+") "+tblb.getX()+" "+tblb.getY()+" "+tblb.getAngle()+" "+tblb.getWidth()+" "+tblb.getHeight()+" "+tblb.getArea());
  //redraw();
}

// called when a blob is moved
public void updateTuioBlob (TuioBlob tblb) {
  if (verbose) println("set blb "+tblb.getBlobID()+" ("+tblb.getSessionID()+") "+tblb.getX()+" "+tblb.getY()+" "+tblb.getAngle()+" "+tblb.getWidth()+" "+tblb.getHeight()+" "+tblb.getArea()
          +" "+tblb.getMotionSpeed()+" "+tblb.getRotationSpeed()+" "+tblb.getMotionAccel()+" "+tblb.getRotationAccel());
  //redraw()
}

// called when a blob is removed from the scene
public void removeTuioBlob(TuioBlob tblb) {
  if (verbose) println("del blb "+tblb.getBlobID()+" ("+tblb.getSessionID()+")");
  //redraw()
}



// --------------------------------------------------------------
// called at the end of each TUIO frame
public void refresh(TuioTime frameTime) {
  if (verbose) println("frame #"+frameTime.getFrameID()+" ("+frameTime.getTotalMilliseconds()+")");
  if (callback) redraw();
}
public void GUI_BACK()
{
  //--------------------
  // Fond de la fenetre
  //--------------------
  background(255);
  
  //--------------------
  // Fond de la table
  //--------------------
  fill(0);
  noStroke();
  rect(0, 0, 1300, 800); // Fond
  noFill();
  stroke(200);
  strokeWeight(3);
  rect(102, 23, 596, 594, 7); // Carr\u00e9 de table blanc
  ellipse(400, 320, 596, 594); // Cercle de table blanc
  
  //--------------------
  // Logo de CUBE-IT
  //--------------------
  image(img, 940, 400 + top ,120, 130);
  
  //--------------------
  // Zone de texte
  //--------------------
  
  TFetatString.setText("  " + etatString);
  TFinString.setText("  " + inString);
  TFetatProgramme.setText("  " + etatProgramme);
  
}

public void GUI_BUTTON()
{
  if (clicSimulator == true)
  {
   println("Lancement du simulateur TUIO");
   launch(dataPath("TUIO_Simulator/TuioSimulator.jar"));
   clicSimulator = false;
  }
  
  if (clicReactivision == true)
  {
   println("Lancement de REACTIVISION");
   launch(dataPath("reacTIVision-1.5-32bit-mac/reacTIVision.app"));
   clicReactivision = false;
  }
  
  if (clicCredits == true)
  {
   //Lancer une pop-up
   JOptionPane.showMessageDialog(frame, "Code cr\u00e9\u00e9 par Adrien B. pour La Machinerie Amiens - Avril 2016 - Plus d'infos sur lamachinerie.org","Cr\u00e9dits",JOptionPane.PLAIN_MESSAGE);
   clicCredits = false;
  }
  
  if (clicSerialBegin == false)
  {
     serialBegin.setCaptionLabel("Se connecter a Scott");
  }
  else
  {
   }
}


public void CP5_GUI_CREATE()
{
  //----------------------------------------------------
  // Create the GUI ControlP5 instance
  cp5 = new ControlP5(this);
  // Instance permettant d'afficher la console
  myTextarea = cp5.addTextarea("txt")
                  .setPosition(810, 235 + top)
                  .setSize(395, 150)
                  .setFont(createFont("", 10))
                  .setLineHeight(14)
                  .setColor(color(255))
                  .setColorBackground(color(20,104,129))
                  .setColorForeground(color(255, 100));
  ;
  console = cp5.addConsole(myTextarea);
  

  TFetatString = cp5.addTextfield("Donnees recus")
                  .setPosition(810 + 210, 75 + top)
                  .setSize(180, 20)
                  ;
                  
  TFinString = cp5.addTextfield("Etat Scott")
                  .setPosition(810 + 210, 115 + top)
                  .setSize(180, 20)
                  ;
                 
  TFetatProgramme = cp5.addTextfield("Etat du programme")
                  .setPosition(810 + 210, 155 + top)
                  .setSize(180, 20)
                  ;
                
  serialList = cp5.addScrollableList("dropdown")
     .setCaptionLabel("Liste des liaisons serie")
     .setPosition(810, 75 + top)
     .setSize(180, 160)
     .setBarHeight(20)
     .setItemHeight(20)
     .addItems(Serial.list());
     ;
                  
  // create a new button with name 'buttonA'
  serialBegin = cp5.addButton("serialBegin")
     .setCaptionLabel("Se connecter a scott")
     .setPosition(810,195+top)
     .setSize(390,20)
     ;
     
  // create a new button with name 'buttonA'
  Simulator = cp5.addButton("Simulator")
     .setCaptionLabel("Simulateur TUIO")
     .setPosition(810+210,60)
     .setSize(180,20)
     ;
     
  // create a new button with name 'buttonA'
  Reactivision = cp5.addButton("Reactivision")
     .setCaptionLabel("Reactivision")
     .setPosition(810+210,95)
     .setSize(180,20)
     ;
     
  // create a new button with name 'buttonA'
  Credits = cp5.addButton("Credits")
     .setCaptionLabel("Credits")
     .setPosition(1100,580)
     .setSize(100,20)
     ;
                  
  //----------------------------------------------------
  
  
}

public void Credits() 
{
 if (clicCredits == false)
 {
   clicCredits = true ;
 }
}

public void Simulator() 
{
 if (clicSimulator == false)
 {
   clicSimulator = true ;
 }
}

public void Reactivision() 
{
 if (clicReactivision == false)
 {
   clicReactivision = true ;
 }
}

public void serialBegin() 
{
 if (clicSerialBegin == false)
 {
   clicSerialBegin = true ;
   println("" + PApplet.parseInt(serialList.getValue()));
   serialBegin.setCaptionLabel("re-initialiser la connexion");
   myPort = new Serial(this, Serial.list()[PApplet.parseInt(serialList.getValue())], 9600); 
   myPort.bufferUntil('\n');
 }
 else
 {
   clicSerialBegin = false ;
 }
}
  public void settings() {  size(1250, 640);  smooth(4); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "CUBE_IT_SCOTT_03" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
