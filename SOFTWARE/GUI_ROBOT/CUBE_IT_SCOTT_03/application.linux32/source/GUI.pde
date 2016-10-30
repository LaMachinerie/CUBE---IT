void GUI_BACK()
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
  rect(102, 23, 596, 594, 7); // Carré de table blanc
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

void GUI_BUTTON()
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
   JOptionPane.showMessageDialog(frame, "Code créé par Adrien B. pour La Machinerie Amiens - Avril 2016 - Plus d'infos sur lamachinerie.org","Crédits",JOptionPane.PLAIN_MESSAGE);
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


void CP5_GUI_CREATE()
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
   println("" + int(serialList.getValue()));
   serialBegin.setCaptionLabel("re-initialiser la connexion");
   myPort = new Serial(this, Serial.list()[int(serialList.getValue())], 9600); 
   myPort.bufferUntil('\n');
 }
 else
 {
   clicSerialBegin = false ;
 }
}