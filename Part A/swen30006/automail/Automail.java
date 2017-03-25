/* SWEN30006 Software Modeling and Design
 * Assignment 1 Part A
 * Author: Geoffrey Law <glaw> (759218)
 */
package automail;

import strategies.*;

public class Automail {
	      
    public Robot robot;
    public IMailPool mailPool;
    
    Automail(IMailDelivery delivery) {
    	
    /** CHANGE NOTHING ABOVE HERE */
    	
//    	/** Initialize the MailPool */
//    	SimpleMailPool simpleMailPool = new SimpleMailPool();
//    	mailPool = simpleMailPool;
//    	
//        /** Initialize the MailSorter */
//    	IMailSorter sorter = new SimpleMailSorter(simpleMailPool);
    	
    	/** Initialize the MailPool */
    	MyMailPool myMailPool = new MyMailPool();
    	mailPool = myMailPool;
    	
        /** Initialize the MailSorter */
    	IMailSorter sorter = new MyMailSorter(myMailPool);
    	
    /** CHANGE NOTHING BELOW HERE */
    	
    	/** Initialize robot */
    	robot = new Robot(sorter, delivery);
    	
    }
    
}
