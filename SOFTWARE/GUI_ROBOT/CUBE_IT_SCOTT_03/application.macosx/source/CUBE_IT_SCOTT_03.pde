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
 Open Source Scott développé par l'association
 Amiénoise La Machinerie ) en utilisant des 
 information TUIO. Ces informations sont 
 envoyées par une table sous reactivision.
 ----------------------------------------------
 */

/*------IMPORT DES LIBRAIRIES DU PROJET------*/
 
import controlP5.*;                  // Inporte la librairie CP5 pour la gestion du GUI
import java.util.*;
import javax.swing.JOptionPane;      // Importe la librairie pour générer une Pop-Up
import TUIO.*;                       // Importe la librairie TUIO
import processing.serial.*;          // Importe la librairie pour utiliser le port Série
 
/*------DECLARATION DES OBJETS DU SKETCH------*/

TuioProcessing tuioClient;           // Déclare un client TuioProcessing
Serial myPort;                       // Déclare le port série
ControlP5 cp5;                       // Déclare un objet CP5 pour le GUI


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
boolean scott = false ;                 // Flag de disponibilité de Scott
boolean lancementProgramme = false;     // 
int lf = 10;                            // ASCII linefeed 

/*------DECLARATION DES VARAIBLES D'ORDRE------*/

// Constantes d'état d'ordre
final byte nonTransmis = 0 ;            // L'ordre n'est pas encore transmis
final byte transmis = 1 ;               // L'ordre est transmis, en attente d'exécution par le robot
final byte effectue = 2 ;               // L'ordre a été éffectué par le robot

byte etatScott = 0;                     // Stockage de l'état de l'ordre pour Scott ( nonTransmis, transmis, effectue )
boolean saut = false;                   // Demande un saut vers la prochaine fin de test conditionnel
boolean retour = false;                 // Demande un retour vers le precedent "TANT QUE" si vrai

int[][] ETAPE ;                         // Tableau de stockage des étapes de programmation pour le robot
int nbEtape = 0;                        // Nombre d'étape détéctées 
int etapeActuelle = 0 ;                 // Pointeur du tableau d'étape

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
 Fonction exécutée une fois au début du programme. Elle permet d'initialiser ce 
 dernier en mettant en place le cadre général.
 ------------------------------------------------------------------------------------*/
void setup()
{
  smooth(4);                                 // Lisse les lignes 
  fill(255);                            
  size(1250, 640);    // Taille de la fenêtre
  //fullScreen();
  noStroke();                                // Pas de bords
  CP5_GUI_CREATE();                          // Création de l'interface 
  tuioClient  = new TuioProcessing(this);    // Create the TUIO Client
  
  img = loadImage("CUBE_NOIR_BLANC_LEGER.png");

  /*----------GESTION DU CALLBACK------------*/
  
  if (!callback) {
    frameRate(5);
    loop();
  } else noLoop(); // or callback updates 

  /*----------LISTE DES PORTS SERIE------------*/
  println("Bienvenue sur l'interface de gestion du CUBE");
  println("Les ports série disponibles sont les suivants :");
  printArray(Serial.list());
}

/*-----------------------------------------------------------------------------------
                                FONCTION : draw()
 ------------------------------------------------------------------------------------
 DESCRIPTION : 
 C'est le coeur du programme. La fonction est appellée régulierement et met à jour 
 l'affichage et les elements graphique de la fenêtre en plus des divers communication
 avec le robot.
 ------------------------------------------------------------------------------------*/
void draw()
{
  GUI_BUTTON();                        // Gestion de l'état des différents bouttons 
  GUI_BACK();                          // Gestion du fond graphique de l'application
  SELECT_ORDRE();                      // Envoi des ordres en fonction de l'état du robot
  AFFICHAGES_TAGS();                   // Gestion de l'affichage des tags sur la fenêtre
  DEMARRAGE_SCOTT();                   // Test de Scott si celui-ci n'est pas encore démarré
}

/*-----------------------------------------------------------------------------------
                                FONCTION : serialEvent()
 ------------------------------------------------------------------------------------
 DESCRIPTION : 
 Cette fonction est appelée à chaque fois que le sketch processing recoit un element
 sur le port série. il stock ensuite cet élément dans la variable "inString".
 ------------------------------------------------------------------------------------*/
void serialEvent(Serial p) 
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