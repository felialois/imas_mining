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
    
    public Cell shortestPath(Node startingPoint,Node endingPoint,Cell[][] map)
    {
        ArrayList<Node> open = new ArrayList<Node>();
        ArrayList<Node> closed = new ArrayList<Node>();
        ArrayList<Node> succesors = new ArrayList<Node>();
        Cell startingCell = map[startingPoint.getX()][startingPoint.getY()];
        
        startingPoint.setF((float) 0);
        startingPoint.setG((float) 0);
        startingPoint.setH((float) 0);

        
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
                succesors.add((Node)map[startingCell.getRow()+1][startingCell.getCol()]);
            }
            
            if(map[startingCell.getRow()-1][startingCell.getCol()].getCellType().equals(CellType.PATH))
            {
                succesors.add((Node)map[startingCell.getRow()-1][startingCell.getCol()]);
            }
            
            if(map[startingCell.getRow()][startingCell.getCol()+1].getCellType().equals(CellType.PATH))
            {
                succesors.add((Node)map[startingCell.getRow()][startingCell.getCol()+1]);
            }
            
            if(map[startingCell.getRow()][startingCell.getCol()-1].getCellType().equals(CellType.PATH))
            {
                succesors.add((Node)map[startingCell.getRow()][startingCell.getCol()-1]);
            }
            
            //Set the Node Parent as the Parent of the succesors
            for(int i=0; i<succesors.size(); i++)
            {
                succesors.get(i).setParent(q);
            }
            
            for(int j=0; j<succesors.size();j++)
            {
                //If the succesor is the goal stop the search
                if(succesors.get(j).equals(endingPoint))
                {
                    //For us is only interesting the row and column of the first movement
                    //Return the first visited cell after the initial one
                    return open.get(1);
                }
                
                //g+distance between succesor and q, which is always 1
                succesors.get(j).setG(succesors.get(j).getG()+1);
                
                //distance from goal to succesor
                succesors.get(j).setH(Math.abs(endingPoint.getRow()-succesors.get(j).getRow())+
                        Math.abs(endingPoint.getCol()-succesors.get(j).getCol()));
                
                //g+h
                succesors.get(j).setF(succesors.get(j).getG()+succesors.get(j).getH());
            
                    
                for(int i=0; i<open.size();i++)
                {
                    //if a node with the same position as successor is in the OPEN list \
                    //which has a lower f than successor, skip this successor do nothing

                    if((succesors.get(j).getRow() == open.get(i).getRow() 
                            && succesors.get(j).getCol() == open.get(i).getCol()
                            && succesors.get(j).getF() > open.get(i).getF()))
                    {

                    }else{
                        //otherwise, add the node to the open list
                        open.add(succesors.get(j));
                    }
                }

                for(int i=0; i<closed.size();i++)
                {
                    //if a node with the same position as successor is in the OPEN list \
                    //which has a lower f than successor, skip this successor do nothing

                    if((succesors.get(j).getRow() == closed.get(i).getRow() 
                            && succesors.get(j).getCol() == closed.get(i).getCol()
                            && succesors.get(j).getF() > closed.get(i).getF()))
                    {

                    }else{
                        //otherwise, add the node to the open list
                        open.add(succesors.get(j));
                    }
                }
                
                closed.add(q);
        }
                
    }
    
    //No path between the initial cell and the final cell was found
    return null;

    }
}
