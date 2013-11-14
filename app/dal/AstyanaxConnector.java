package dal;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.connectionpool.NodeDiscoveryType;
import com.netflix.astyanax.connectionpool.impl.ConnectionPoolConfigurationImpl;
import com.netflix.astyanax.connectionpool.impl.CountingConnectionPoolMonitor;
import com.netflix.astyanax.impl.AstyanaxConfigurationImpl;
import com.netflix.astyanax.thrift.ThriftFamilyFactory;

public class AstyanaxConnector implements DataSourceConnector {

	public AstyanaxConnector() {
	}

	@Override
	public Keyspace connectKeyspace(final ConnectKeyspaceConfig parameterObject) {

		final AstyanaxContext<Keyspace> context = new AstyanaxContext.Builder()
				// FIXME .forCluster("ClusterName")
				.forKeyspace(parameterObject.getKeyspace())
				.withAstyanaxConfiguration(
						new AstyanaxConfigurationImpl().setDiscoveryType(NodeDiscoveryType.RING_DESCRIBE))
				.withConnectionPoolConfiguration(
						new ConnectionPoolConfigurationImpl(parameterObject.getConnectionPoolName())
								.setPort(parameterObject.getPort())
								.setMaxConnsPerHost(parameterObject.getMaxConnectionPerHost())
								.setSeeds(parameterObject.getSeed()))
				.withConnectionPoolMonitor(new CountingConnectionPoolMonitor())
				.buildKeyspace(ThriftFamilyFactory.getInstance());

		context.start();
		final Keyspace keyspace = context.getEntity();

		return keyspace;
	}

	public void read() {
		// TODO Auto-generated method stub

	}
}
