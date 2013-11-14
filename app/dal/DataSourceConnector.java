/**
 * 
 */
package dal;

import com.netflix.astyanax.Keyspace;

/**
 * @author david
 * 
 */
public interface DataSourceConnector {

	/**
	 * @param parameterObject
	 * @return
	 */
	Keyspace connectKeyspace(ConnectKeyspaceConfig parameterObject);

}
