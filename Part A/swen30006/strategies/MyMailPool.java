package strategies;

import java.util.LinkedList;

import automail.IMailPool;
import automail.MailItem;

public class MyMailPool implements IMailPool {

	private LinkedList<MailItem> allMailItems;

	public MyMailPool() {
		allMailItems = new LinkedList<MailItem>();
	}

	@Override
	public void addToPool(MailItem mailItem) {
		allMailItems.add(mailItem);
	}

	public boolean isEmptyPool(){
		return allMailItems.isEmpty();
	}

	public void remove(MailItem item) {
		allMailItems.remove(item);
	}

	public LinkedList<MailItem> getAllMailItems() {
		return allMailItems;
	}

}

