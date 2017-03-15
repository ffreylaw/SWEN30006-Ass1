package strategies;

import automail.Clock;
import automail.IMailSorter;
import automail.MailItem;
import automail.StorageTube;
import exceptions.TubeFullException;

public class MyMailSorter implements IMailSorter {
	
	private MyMailPool myMailPool;
	
	public MyMailSorter(MyMailPool myMailPool) {
		this.myMailPool = myMailPool;
	}

	@Override
	public boolean fillStorageTube(StorageTube tube) {
        try {
            if (!myMailPool.isEmptyPool()) {
            	if (tube.isEmpty()) {
            		myMailPool.readyForDeliver();
            	}
            	if (!myMailPool.isEmptyList()) {
	            	/** Gets the first item from the ArrayList */
		            MailItem mailItem = myMailPool.get();
		            /** Add the item to the tube */
		            tube.addItem(mailItem);
		            /** Remove the item from the ArrayList */
		            myMailPool.remove(mailItem);
            	} else {
            		return true;
            	}
            }
        }
        /** Refer to TubeFullException.java --
         *  Usage below illustrates need to handle this exception. However you should
         *  structure your code to avoid the need to catch this exception for normal operation
         */
        catch (TubeFullException e) {
        	return true;
        }      
        /** 
         * Handles the case where the last delivery time has elapsed and there are no more
         * items to deliver.
         */
        if (Clock.Time() > Clock.LAST_DELIVERY_TIME && myMailPool.isEmptyPool() && !tube.isEmpty()) {
            return true;
        }
        
		return false;
	}

}
