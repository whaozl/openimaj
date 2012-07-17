package org.openimaj.experiment.dataset.crossvalidation;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openimaj.experiment.dataset.GroupedDataset;
import org.openimaj.experiment.dataset.Identifiable;
import org.openimaj.experiment.dataset.ListBackedDataset;
import org.openimaj.experiment.dataset.ListDataset;
import org.openimaj.experiment.dataset.MapBackedDataset;
import org.openimaj.experiment.dataset.util.DatasetAdaptors;
import org.openimaj.util.list.AcceptingListView;
import org.openimaj.util.list.SkippingListView;

/**
 * An {@link Iterable} that produces an {@link Iterator} for 
 * Leave-One-Out Cross Validation (LOOCV) with a {@link GroupedDataset}.
 * The number of iterations performed by the iterator is equal
 * to the number of data items.
 * <p>
 * Upon each iteration, the dataset is split into training
 * and validation sets. The validation set will have exactly one
 * instance. All remaining instances are placed in the training
 * set. As the iterator progresses, every instance will be included
 * in the validation set one time. The iterator maintains the respective
 * groups of the training and validation items.
 * 
 * @author Jonathon Hare (jsh2@ecs.soton.ac.uk)
 *
 * @param <KEY> Type of groups
 * @param <INSTANCE> Type of instances 
 *
 */
public class GroupedLeaveOneOutIterable<KEY, INSTANCE extends Identifiable> implements Iterable<CrossValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>> {
	private GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset;
	
	/**
	 * Construct the {@link GroupedLeaveOneOutIterable} with the
	 * given dataset.
	 * @param dataset the dataset.
	 */
	public GroupedLeaveOneOutIterable(GroupedDataset<KEY, ? extends ListDataset<INSTANCE>, INSTANCE> dataset) {
		this.dataset = dataset;
	}
	
	/**
	 * Get the number of iterations that the {@link Iterator}
	 * returned by {@link #iterator()} will perform.
	 * 
	 * @return the number of iterations that will be performed
	 */
	public int numberIterations() {
		return dataset.size();
	}

	@Override
	public Iterator<CrossValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>> iterator() {
		return new Iterator<CrossValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>>() {
			int validationIndex = 0;
			int validationGroupIndex = 0;
			Iterator<KEY> groupIterator = dataset.getGroups().iterator();
			KEY currentGroup = groupIterator.hasNext() ? groupIterator.next() : null;
			List<INSTANCE> currentValues = currentGroup == null ? null : DatasetAdaptors.asList(dataset.getInstances(currentGroup));
			
			@Override
			public boolean hasNext() {
				return validationIndex < dataset.size();
			}

			@Override
			public CrossValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>> next() {
				int selectedIndex;
				
				if (currentValues != null && validationGroupIndex < currentValues.size()) {
					selectedIndex = validationGroupIndex;
					validationGroupIndex++;
				} else {
					validationGroupIndex = 0;
					currentGroup = groupIterator.next();
					currentValues = currentGroup == null ? null : DatasetAdaptors.asList(dataset.getInstances(currentGroup));
					
					return next();
				}
				
				Map<KEY, ListDataset<INSTANCE>> train = new HashMap<KEY, ListDataset<INSTANCE>>();
				for (KEY group : dataset.getGroups()) {
					if (group != currentGroup) 
						train.put(group, dataset.getInstances(group));
				}
				train.put(currentGroup, new ListBackedDataset<INSTANCE>(new SkippingListView<INSTANCE>(currentValues, selectedIndex)));
				
				Map<KEY, ListDataset<INSTANCE>> valid = new HashMap<KEY, ListDataset<INSTANCE>>();
				valid.put(currentGroup, new ListBackedDataset<INSTANCE>(new AcceptingListView<INSTANCE>(currentValues, selectedIndex)));
				
				GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> cvTrain = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>(train);
				GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE> cvValid = new MapBackedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>(valid);

				validationIndex++;
				
				return new CrossValidationData<GroupedDataset<KEY, ListDataset<INSTANCE>, INSTANCE>>(cvTrain, cvValid);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
}
