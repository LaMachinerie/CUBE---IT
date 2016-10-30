void DEMARRAGE_SCOTT()
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

void SELECT_ORDRE()
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
        // Si on sort du tableau, on revient au début
        if (etapeActuelle >= nbEtape)
        {
          etapeActuelle = 0;
        }
        // Si on est égal à 0, on ignore et on passe à la suite
        if (ETAPE[0][etapeActuelle]==0)
        {
          etapeActuelle ++;
        }
        // Si on est égale à fin de Tant que, on revient au précédent tant que
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


void SEND_ORDRE(int ordreNumber, int ordreParameter)
{
  String ordreArduino = str(ordreNumber)+";"+ str(ordreParameter)+'\n';
  myPort.write(ordreArduino);
  print("ordre envoyé :"+ordreArduino);
}



/*-----------------------------------------------------------------------------------
                                FONCTION : TRI()
 ------------------------------------------------------------------------------------
 DESCRIPTION : 
 Cette fonction récupere tous les elements présent sur la table est le place dans un
 tableau. Elle tri ensuite ce dernier en fonction de la position des tags : a savoir
 de haut en bas.
 ------------------------------------------------------------------------------------*/
void TRI()
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
    ETAPE [1][i] = int(degrees(tobj.getAngle()+0.3));
    ETAPE [2][i] = tobj.getScreenY(590);
    print(ETAPE[0][i]);
    print(";");
    print(ETAPE[1][i]);
    print(";");
    println(ETAPE[2][i]);
  }
  
  // Tri à bulle 
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
  
  println("Tableau trié");
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