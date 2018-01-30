/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AStar;
import cat.urv.imas.map.Cell;
import java.util.ArrayList;
import AStar.Node;
import cat.urv.imas.map.CellType;

/**
 *
 * @author Antonio
 */
public class AStar {
    private Node startingPoint;
    private Node endingPoint;
    
    public AStar(Node startingPoint,Node endingPoint,Cell[][] map)
    {
        this.startingPoint=startingPoint;
        this.endingPoint=endingPoint;
    }
    
    public ArrayList<Node> shortestPath(Node startingPoint,Node endingPoint,Cell[][] map)
    {
        ArrayList<Node> open = new ArrayList<Node>();
        ArrayList<Node> closed = new ArrayList<Node>();
        ArrayList<Cell> succesors = new ArrayList<>();
        Cell startingCell = map[startingPoint.getX()][startingPoint.getY()];
        
        startingPoint.setF((float) 0);
        
        //Add starting Node to the open List
        open.add(startingPoint);
        
        while(!open.isEmpty())
        {
            //Find the node with the least f in the open list
            Node q = open.get(0);
            
            for(int i=0;i<open.size();i++)
            {
                if (q.getF() > open.get(i).getF())
                {
                    q = open.get(i);
                }
            }
            
            open.remove(q);
            
            //Succesors
            if(map[startingCell.getRow()+1][startingCell.getCol()].getCellType().equals(CellType.PATH))
            {
                succesors.add(map[startingCell.getRow()+1][startingCell.getCol()]);
            }
        }
        
        
        
        return open;
    }
}
