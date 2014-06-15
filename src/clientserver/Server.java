/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package clientserver;

import java.util.Random;

/**
 *
 * @author Jan
 */
public class Server extends abiturklassen.netzklassen.Server
{
    int players = 0;
    BackgroundWorker worker = null;
    String cards[] = {"000",
                      "001",
                      "002",
                      "003",
                      "004",
                      "005",
                      "006",
                      "007",
                      "008",
                      "009",
                      "010",
                      "011",
                      "012",
                      "013",
                      "014",
                      "015",
                      "016",
                      "017",
                      "018",
                      "019",
                      "020",
                      "021",
                      "022",
                      "023",
                      "024",
                      "025",
                      "026",
                      "027",
                      "028",
                      "029",
                      "030",
                      "031"};
    
    public Server(int port)
    {
        super(port);
        shuffleArray(cards);
        worker = new BackgroundWorker();
        worker.start();
    }
    
    @Override
    public void processNewConnection(String pClientIP, int pClientPort)
    {
        System.out.println("New Connection: " + pClientIP + ":" + pClientPort);
        players++;
        
        if(players == 4)
        {
            worker.setPhase("start");
            System.out.println("Starting...");
            send(pClientIP, pClientPort, "X;Connected");
        }
        if(players > 4)
        {
            closeConnection(pClientIP, pClientPort);
            players--;
        }
    }
    
    @Override
    public void processMessage(String pClientIP, int pClientPort, String pMessage)
    {
        
    }
    
    class BackgroundWorker extends Thread
    {
        String phase = "wait";
        String wonIP = "";
        int wonPort = 0;
        
        public BackgroundWorker()
        {
            
        }
        
        public void run()
        {
            while(true)
            {
                if("wait".equals(phase))
                {
                    verbindungen.toFirst();
                    ServerConnection l;
                    
                    while (verbindungen.hasAccess())
                    {
                        l = (Server.ServerConnection) verbindungen.getObject();
                        send(l.getRemoteIP(), l.getRemotePort(), "S;Waiting");
                        verbindungen.next();
                    }
                }
                if("start".equals(phase))
                {
                    verbindungen.toFirst();
                    ServerConnection l;
                    int i = 0;
        
                    while (verbindungen.hasAccess())
                    {
                        l = (Server.ServerConnection) verbindungen.getObject();
                        send(l.getRemoteIP(), l.getRemotePort(), "S;" + cards[i] + ";" + cards[i+1] + ";" + cards[i+2] + ";" + cards[i+3] + ";" + cards[i+4] + ";" + cards[i+5] + ";" + cards[i+6] + ";" + cards[i+7]);
                        send(l.getRemoteIP(), l.getRemotePort(), "X;Got cards");
                        System.out.println("S;" + cards[i] + ";" + cards[i+1] + ";" + cards[i+2] + ";" + cards[i+3] + ";" + cards[i+4] + ";" + cards[i+5] + ";" + cards[i+6] + ";" + cards[i+7]);
                        i += 8;
                        verbindungen.next();
                    }
                    
                    phase = "play";
                }
                if("play".equals(phase))
                {
                    verbindungen.toFirst();
                    ServerConnection l;
                    String lastCards = "999;999;999;999";
                    
                    while (true)
                    {
                        if(verbindungen.hasAccess())
                        {
                            l = (Server.ServerConnection) verbindungen.getObject();
                            send(l.getRemoteIP(), l.getRemotePort(), "P;" + lastCards);
                            send(l.getRemoteIP(), l.getRemotePort(), "X;Got top of card stack");
                            System.out.println("P;" + lastCards);
                        
                            String c = l.receive();
                            while(c == null)
                                c = l.receive();
                        
                            if(c.equals("w"))
                            {
                                wonIP = l.getRemoteIP();
                                wonPort = l.getRemotePort();
                                send(l.getRemoteIP(), l.getRemotePort(), "X;Won");
                                phase = "won";
                                break;
                            }
                            
                            lastCards = c;
                            System.out.println(c);
                        
                            verbindungen.next();
                        }
                        else
                            verbindungen.toFirst();
                    }
                }
                if("won".equals(phase))
                {
                    verbindungen.toFirst();
                    ServerConnection l;
                    
                    while (verbindungen.hasAccess())
                    {
                        l = (Server.ServerConnection) verbindungen.getObject();
                        
                        if(!l.getRemoteIP().equals(wonIP) && l.getRemotePort() != wonPort)
                        {
                            send(l.getRemoteIP(), l.getRemotePort(), "L");
                            send(l.getRemoteIP(), l.getRemotePort(), "X;Lost");
                        }
                        
                        closeConnection(l.getRemoteIP(), l.getRemotePort());
                        verbindungen.next();
                    }
                    
                    players = 0;
                    phase = "wait";
                }
            }
        }
        
        public void setPhase(String pPhase)
        {
            phase = pPhase;
        }
    }
    
    //Fisher-Yates-Shuffle
    static void shuffleArray(String[] ar)
    {
        Random rnd = new Random();
        for (int i = ar.length - 1; i > 0; i--)
        {
            int index = rnd.nextInt(i + 1);
            String a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }
    }

}
