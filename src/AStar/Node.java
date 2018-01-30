/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AStar;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.CellType;

/**
 *
 * @author Antonio
 */
public abstract class Node extends Cell{
    
    private int x,y;
    private float f,g,h;
    private Node parent;
    
    public Node(int row, int col,int x,int y, float f, float g, float h, Node parent)
    {
        super(CellType.PATH,row,col);
        this.x=x;
        this.y=y;
        this.f=f;
        this.g=g;
        this.h=h;
        this.parent=parent;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public float getF()
    {
        return f;
    }
    
    public float getG()
    {
        return g;
    }
    
    public float getH()
    {
        return h;
    }
    
    public Node getParent()
    {
        return parent;
    }
    
    public void setX(int x)
    {
        this.x=x;
    }
    
    public void setY(int y)
    {
        this.y=y;
    }
    
    public void setF(float f)
    {
        this.f=f;
    }
    
    public void setG(float g)
    {
        this.g=g;
    }
    
    public void setH(float h)
    {
        this.h=h;
    }
    
    public void setParent(Node parent)
    {
        this.parent=parent;
    }
}
