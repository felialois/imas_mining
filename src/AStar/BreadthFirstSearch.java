/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package AStar;

import cat.urv.imas.map.Cell;
import cat.urv.imas.map.PathCell;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

/**
 *
 * @author santi
 */
public class BreadthFirstSearch {
    
    static class Node{
        public Cell cell;
        public Node parent;
        
        public Node(Cell cell, Node parent) {
            this.cell = cell;
            this.parent = parent;
        }
        
        public boolean equals(Node node) {
            return node.cell.equals(this.cell);
        }
    }
    
    public static Cell shortestPath(Cell startingPoint, Cell endingPoint, Cell[][] map) {
        
        Queue<Node> q = new LinkedList<Node>();
        ArrayList<Node> visited = new ArrayList<Node>();
        q.add(new Node(startingPoint, null));
        Node actNode = null;
        while(!q.isEmpty()) {
            actNode = q.poll();
            if(actNode.cell.equals(endingPoint)){
                break;
            }
            visited.add(actNode);
            // Up
            if(actNode.cell.getRow() > 0) {
                Cell c = map[actNode.cell.getRow()-1][actNode.cell.getCol()];
                Node newNode = new Node(c, actNode);
                if(!visited.contains(newNode) && c instanceof PathCell) {
                    q.add(new Node(c, actNode));
                }
            }
            //Down
            if(actNode.cell.getRow() < map.length) {
                Cell c = map[actNode.cell.getRow()+1][actNode.cell.getCol()];
                Node newNode = new Node(c, actNode);
                if(!visited.contains(newNode) && c instanceof PathCell) {
                    q.add(new Node(c, actNode));
                }
            }
            // Left
            if(actNode.cell.getCol() > 0) {
                Cell c = map[actNode.cell.getRow()][actNode.cell.getCol()-1];
                Node newNode = new Node(c, actNode);
                if(!visited.contains(newNode) && c instanceof PathCell) {
                    q.add(new Node(c, actNode));
                }
            }
            // Right
            if(actNode.cell.getCol() < map[0].length) {
                Cell c = map[actNode.cell.getRow()][actNode.cell.getCol()+1];
                Node newNode = new Node(c, actNode);
                if(!visited.contains(newNode) && c instanceof PathCell) {
                    q.add(new Node(c, actNode));
                }
            }
        }
        if(actNode.cell.equals(endingPoint)) {
            Node n = actNode;
            while(!n.parent.cell.equals(startingPoint)) {
                n = n.parent;
            }
            return n.cell;
        }
        return null;
    }
}
