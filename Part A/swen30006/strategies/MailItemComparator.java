package strategies;

import java.util.Comparator;
import java.util.HashMap;

import automail.Clock;
import automail.MailItem;

public class MailItemComparator implements Comparator<MailItem> {
	
	private HashMap<String, Double> priority_level_hashmap;
	
	public MailItemComparator() {
		priority_level_hashmap = new HashMap<String, Double>();
		priority_level_hashmap.put("LOW", 1.0);
		priority_level_hashmap.put("MEDIUM", 1.5);
		priority_level_hashmap.put("HIGH", 2.0);
	}

	@Override
	public int compare(MailItem o1, MailItem o2) {
		double o1_score = priority_level_hashmap.get(o1.getPriorityLevel());
		double o2_score = priority_level_hashmap.get(o2.getPriorityLevel());
		
		if (o1_score > o2_score) {
			return -1;
		} else if (o1_score < o2_score) {
			return 1;
		}
		return 0;
	}

}
