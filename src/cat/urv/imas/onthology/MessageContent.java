/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.onthology;

/**
 * Content messages for inter-agent communication.
 */
public class MessageContent {
    
    /**
     * Message sent from Coordinator agent to System agent to get the whole
     * city information.
     */
    public static final String GET_MAP = "Get map";
    
    public static final String METAL = "Metal";
    
    public static final String READY = "Ready";
    
    public static final String GET_AREA = "Get area";
    
    public static final String AREAS = "Areas";
    
    public static final String GET_MOVEMENT = "Get movement";
    
    public static final String RANDOM = "Random";
    
    public static final String MOVE_TO = "Move to";
    
    public static final String GET_PROS = "Get prospector";
    
    public static final String GET_PROS_BY_AREA = "Get prospector by area";
    
    public static final String PROS_ASSIGNED = "Prospectors assigned";
    
    public static final String EXTRACT_METAL = "Extract metal";
            
    public static final String METAL_TO_MC = "Metal given to manufacturing center";
    
    public static final String CONTRACT_PROPOSE = "Contract Propose";
    
    public static final String CONTRACT_ACCEPT = "Contract Accept";
    
    public static final String CONTRACT_REJECT = "Contract Reject";
    
    public static final String CONTRACT_ASIGN = "Contract Assign";
    
    public static final String CONTRACT_BID = "Contract Bid";
}
