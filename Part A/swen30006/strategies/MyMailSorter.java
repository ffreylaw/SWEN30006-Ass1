package strategies;

import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;

import automail.Building;
import automail.Clock;
import automail.IMailSorter;
import automail.MailItem;
import automail.StorageTube;
import exceptions.TubeFullException;


public class MyMailSorter implements IMailSorter {

	private MyMailPool myMailPool;

	/** 
	 * This delivery array contains mail items 
	 * that are going to be in the storage tube of the robot
	 */
	private LinkedList<MailItem> deliveryMailItems;

	public MyMailSorter(MyMailPool myMailPool) {
		this.myMailPool = myMailPool;
		deliveryMailItems = new LinkedList<MailItem>();
	}

	@Override
	public boolean fillStorageTube(StorageTube tube) {
		try {
			/** A new delivery attempt */
			if (!myMailPool.isEmptyPool()) {
				/** Sort the mail pool then generate the delivery array */
				readyForDeliver(tube);
				/**  Add each item in the delivery array to the tube */
				for (MailItem item: deliveryMailItems) {
					tube.addItem(item);
					myMailPool.remove(item);
				}
				return true;
			}
		}
		/** Here will never catch any exceptions since the delivery array 
		 *  has already taken the capacity of the tube in consideration,
		 *  which the delivery array is perfectly fit the tube
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

	/** 
	 * Generate the delivery array that ready for 
	 * adding delivery mail items to the tube
	 * @param tube
	 */
	public void readyForDeliver(StorageTube tube) {
		/** Get all mail items from the mail pool 
		 */
		LinkedList<MailItem> allMailItems = myMailPool.getAllMailItems();

		/** Clear delivery array due to new delivery attempt 
		 */
		deliveryMailItems.clear();

		/** Sort all mail items by priority level: HIGH -> MEDIUM -> LOW
		 */
		Comparator<MailItem> sortByPriority = new MailItemComparator();
		allMailItems.sort(sortByPriority);

		/** Create a temporary array to store the mail items that currently
		 *  are the highest and the same priority in the allMailItems array
		 */
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

		/** Sort the temporary array by distance 
		 *  (distance from the mail room floor to destination floor) 
		 */
		Comparator<MailItem> sortByDistance = (MailItem o1, MailItem o2) -> Math.abs(o1.getDestFloor() - Building.MAILROOM_LOCATION) - Math.abs(o2.getDestFloor() - Building.MAILROOM_LOCATION);
		tempMailItems.sort(sortByDistance);

		/** Detect whether the lowest distance mail item in the array is short distance,
		 *  which within the range of the longest distance from mail room to destination floor divided by 2.
		 */
		int shortDistance = Math.abs(Building.FLOORS - Building.MAILROOM_LOCATION) > Math.abs(Building.LOWEST_FLOOR - Building.MAILROOM_LOCATION) ?
					Math.abs(Building.FLOORS - Building.MAILROOM_LOCATION)/2 : Math.abs(Building.LOWEST_FLOOR - Building.MAILROOM_LOCATION)/2;
		boolean isShortDistance = Math.abs(tempMailItems.getFirst().getDestFloor() - Building.MAILROOM_LOCATION) < shortDistance;

		/** Selecting the most suitable item to be deliver at current state
		 */
		if (!isShortDistance) {
			/** If the mail item is short distance, then sort the temporary array by size
			 *  from small to large, then get the item with the earliest arrival time
			 *  in the array, and then add that item to the delivery array
			 */
			Comparator<MailItem> sortBySize = (MailItem o1, MailItem o2) -> o1.getSize() - o2.getSize();
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
			/** Otherwise straight add the first item of temporary array to the delivery array
			 */
			deliveryMailItems.add(tempMailItems.getFirst());
		}

		/** Set the flag whether the item in the delivery array
		 *  that its destination floor is in the lower floor 
		 */
		boolean isLowerDestFloor = deliveryMailItems.getFirst().getDestFloor() <= Building.MAILROOM_LOCATION;

		/** Initialize size of the delivery array
		 *  Note: The maximum capacity of the delivery array is the same as the tube 
		 */
		int size = deliveryMailItems.getFirst().getSize();

		/** Iterate through all mail items and finding the suitable items that 
		 *  respect to the given item already in the delivery array
		 */
		for (MailItem item: allMailItems) {
			if (item != deliveryMailItems.getFirst()) {
				if (size + item.getSize() <= tube.MAXIMUM_CAPACITY) {
					if (item.getDestFloor() <= Building.MAILROOM_LOCATION && isLowerDestFloor) {
						deliveryMailItems.add(item);
						size += item.getSize();
					}
					if (item.getDestFloor() > Building.MAILROOM_LOCATION && !isLowerDestFloor) {
						if (Math.abs(item.getDestFloor() - Building.MAILROOM_LOCATION) < Building.MAILROOM_LOCATION && isShortDistance) {
							deliveryMailItems.add(item);
							size += item.getSize();
						}
						if (Math.abs(item.getDestFloor() - Building.MAILROOM_LOCATION) >= Building.MAILROOM_LOCATION && !isShortDistance) {
							deliveryMailItems.add(item);
							size += item.getSize();
						}
					}
				}
			}
		}

		/** Finalize the delivery array;
		 *  To sort the array by destination floor and the order will be from high to low
		 *  destination floor when pushing into the tube (stack)
		 */
		Comparator<MailItem> sortByDestFloor = (MailItem o1, MailItem o2) -> o2.getDestFloor() - o1.getDestFloor();
		deliveryMailItems.sort(sortByDestFloor);
	}

	/** Comparator class to compare priority level of mail items 
	 */
	private class MailItemComparator implements Comparator<MailItem> {

		private HashMap<String, Double> priority_level_hashmap;

		public MailItemComparator() {
			priority_level_hashmap = new HashMap<String, Double>();
			priority_level_hashmap.put("LOW", 1.0);
			priority_level_hashmap.put("MEDIUM", 1.5);
			priority_level_hashmap.put("HIGH", 2.0);
		}

		@Override
		public int compare(MailItem o1, MailItem o2) {
			double o1_priority = priority_level_hashmap.get(o1.getPriorityLevel());
			double o2_priority = priority_level_hashmap.get(o2.getPriorityLevel());

			if (o1_priority > o2_priority) {
				return -1;
			} else if (o1_priority < o2_priority) {
				return 1;
			}
			return 0;
		}
	}

}
