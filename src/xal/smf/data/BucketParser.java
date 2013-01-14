/*
 * BucketParser.java
 *
 * Created on February 26, 2002, 2:10 PM
 */

package xal.smf.data;

import java.util.*;
import xal.smf.attr.*;
import xal.tools.data.*;

/**
 * BucketParser is a wrapper for the collection of attribute buckets
 * associated with an AcceleratorNode.  Since each attribute bucket has 
 * a type and that type is used as the bucket's data node name within the
 * "attributes" data node hierarchy, a wrapper is needed to hold all such
 * buckets.  The BucketParser is that wrapper.
 *
 * @author  tap
 */
public class BucketParser implements DataListener {
    private Map<String,AttributeBucket> bucketTable;

    /** Creates new BucketParser */
    public BucketParser() {
        bucketTable = new HashMap<String,AttributeBucket>();
    }
    
    
    /** creates a new BucketParser from a list of buckets */
    public BucketParser( final Collection<AttributeBucket> bucketList ) {
        this();

		for ( final AttributeBucket bucket : bucketList ) {
            add(bucket);
        }
    }

    
    /** add the bucket to the table of buckets and key it by type */
    private void add(AttributeBucket bucket) {
        String bucketType = bucket.getType();
        bucketTable.put(bucketType, bucket);
    }
    
    
    // DataListener interface ---------------
    
    /** DataListener interface support */
    public String dataLabel() { return "attributes"; }
        
    
    /** DataListener interface support */
    public void update(DataAdaptor adaptor) throws NumberFormatException {
        final List<DataAdaptor> bucketAdaptors = adaptor.childAdaptors();
        for ( final DataAdaptor bucketAdaptor : bucketAdaptors ) {
			String name = bucketAdaptor.name();
			AttributeBucket bucket = getBucket(name);
			if ( bucket == null ) {
				try {
					bucket = AttributeBucketFactory.create(name);
				}
				catch(ClassNotFoundException exception) {
					throw new RuntimeException("Failed to create attribute bucket for: " + name, exception);
				}
				add(bucket);
			}
			bucket.update(bucketAdaptor);
        }        
    }
    
    
    /** DataListener interface support */
    public void write(DataAdaptor adaptor) {
        adaptor.writeNodes( bucketTable.values() );
    }
    
    // end DataListener interface ----------------
    
    
    /** get a collection of all the buckets */
    public Collection<AttributeBucket> getBuckets() {
        return bucketTable.values();
    }
    
    
    /** get a specific bucket keyed by the bucket type */
    public AttributeBucket getBucket(String bucketType) {
        return bucketTable.get(bucketType);
    }
}
