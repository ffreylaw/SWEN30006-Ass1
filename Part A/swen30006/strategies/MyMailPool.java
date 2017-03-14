package strategies;

import java.util.Comparator;
import java.util.LinkedList;

import automail.IMailPool;
import automail.MailItem;

public class MyMailPool implements IMailPool {
	
	private LinkedList<MailItem> allMailItems;
	private LinkedList<MailItem> deliveryMailItems;
	
	public MyMailPool() {
		allMailItems = new LinkedList<MailItem>();
		deliveryMailItems = new LinkedList<MailItem>();
	}

	@Override
	public void addToPool(MailItem mailItem) {
		// TODO Auto-generated method stub
		allMailItems.add(mailItem);
	}
	
	public boolean isEmptyPool(){
        return allMailItems.isEmpty();
    }
    
    public MailItem get(){
    	return deliveryMailItems.peek();
    }
    
    public void remove(MailItem item) {
    	allMailItems.remove(item);
    	deliveryMailItems.remove(item);
    }
    
    public void readyForDeliver() {
    	deliveryMailItems.clear();
    	
    	Comparator<MailItem> sortByPriority = new MailItemComparator();
    	allMailItems.sort(sortByPriority);
    	
    	LinkedList<MailItem> tempMailItems = new LinkedList<MailItem>();
    	String currentStatePriority = null;
    	for (MailItem item: allMailItems) {
    		if (currentStatePriority != null) {
    			if (currentStatePriority == item.getPriorityLevel()) {
    				tempMailItems.add(item);
    			} else {
    				break;
    			}
    		} else {
    			currentStatePriority = item.getPriorityLevel();
    			tempMailItems.add(item);
    		}
    	}
    	
    	Comparator<MailItem> sortByDistance = (MailItem o1, MailItem o2) -> Math.abs(o1.getDestFloor() - 3) - Math.abs(o2.getDestFloor() - 3);
    	Comparator<MailItem> sortBySize = (MailItem o1, MailItem o2) -> o1.getSize() - o2.getSize();
    	tempMailItems.sort(sortByDistance);
    	boolean isShortDistance = Math.abs(tempMailItems.getFirst().getDestFloor() - 3) < 3;
    	if (!isShortDistance) {
    		tempMailItems.sort(sortBySize);
    		int smallestSize = tempMailItems.getFirst().getSize();
    		int earliestArrivalTime = tempMailItems.getFirst().getArrivalTime();
    		MailItem earliestItem = tempMailItems.getFirst();
    		for (MailItem item: tempMailItems) {
    			if (item.getArrivalTime() < earliestArrivalTime) {
    				if (item.getSize() == smallestSize) {
	    				earliestArrivalTime = item.getArrivalTime();
	    				earliestItem = item;
    				} else {
    					break;
    				}
    			}
    		}
    		deliveryMailItems.add(earliestItem);
    	} else {
    		deliveryMailItems.add(tempMailItems.getFirst());
    	}
    	
    	boolean isLowerDestFloor = deliveryMailItems.getFirst().getDestFloor() <= 3;
    	int size = deliveryMailItems.getFirst().getSize();
    	for (MailItem item: allMailItems) {
    		if (item != deliveryMailItems.getFirst()) {
    			if (size + item.getSize() <= 4) {
	    			if (item.getDestFloor() <= 3 && isLowerDestFloor) {
	    				deliveryMailItems.add(item);
		    			size += item.getSize();
	    			}
	    			if (item.getDestFloor() > 3 && !isLowerDestFloor) {
	    				if (Math.abs(item.getDestFloor() - 3) < 3 && isShortDistance) {
	    					deliveryMailItems.add(item);
		    				size += item.getSize();
	    				}
	    				if (Math.abs(item.getDestFloor() - 3) >= 3 && !isShortDistance) {
	    					deliveryMailItems.add(item);
		    				size += item.getSize();
	    				}
	    			}
    			}
    		}
    	}
    	
    	Comparator<MailItem> sortByDestFloor = (MailItem o1, MailItem o2) -> o2.getDestFloor() - o1.getDestFloor();
    	deliveryMailItems.sort(sortByDestFloor);
    }
    
    public boolean isEmptyStack() {
    	return deliveryMailItems.isEmpty();
    }
 
}

