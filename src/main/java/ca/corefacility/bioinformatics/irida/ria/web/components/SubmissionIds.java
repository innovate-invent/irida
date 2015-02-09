package ca.corefacility.bioinformatics.irida.ria.web.components;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used to store workflow submission ids in the session.  This should be temporary until
 * they are stored in the database.  Storing a list in a class seems to be handles better by Spring.
 *
 * @author Josh Adam<josh.adam@phac-aspc.gc.ca>
 */
public class SubmissionIds {
	List<Long> ids;

	public SubmissionIds() {
		ids = new ArrayList<>();
	}

	public void addId(Long id) {
		ids.add(id);
	}
}