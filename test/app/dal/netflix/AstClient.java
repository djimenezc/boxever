package app.dal.netflix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.netflix.astyanax.AstyanaxContext;
import com.netflix.astyanax.Keyspace;
import com.netflix.astyanax.MutationBatch;
import com.netflix.astyanax.connectionpool.OperationResult;
import com.netflix.astyanax.connectionpool.exceptions.ConnectionException;
import com.netflix.astyanax.model.Column;
import com.netflix.astyanax.model.ColumnFamily;
import com.netflix.astyanax.model.ColumnList;
import com.netflix.astyanax.serializers.IntegerSerializer;
import com.netflix.astyanax.serializers.StringSerializer;

import dal.CassandraAstyanaxConnection;
import dal.ConnectKeyspaceConfig;

/**
 * Example code for demonstrating how to access Cassandra using Astyanax.
 * 
 * @author elandau
 * @author Marko Asplund
 */
public class AstClient {
	private static final Logger logger = LoggerFactory.getLogger(AstClient.class);

	private AstyanaxContext<Keyspace> context;
	private Keyspace keyspace;
	private ColumnFamily<Integer, String> EMP_CF;
	private static final String EMP_CF_NAME = "employees2";

	public static void main(final String[] args) {
		final AstClient c = new AstClient();
		c.init();
		c.insert(222, 333, "Eric", "Cartman");
		c.read(222);
	}

	public void createCF() {
	}

	public void init() {
		logger.debug("init()");

		final ConnectKeyspaceConfig parameterObject = new ConnectKeyspaceConfig();
		final String keyspaceName = "currencies";
		parameterObject.setKeyspace(keyspaceName);
		context = CassandraAstyanaxConnection.connectClusterContext(parameterObject);

		context.start();
		keyspace = context.getEntity();

		EMP_CF = ColumnFamily.newColumnFamily(EMP_CF_NAME, IntegerSerializer.get(), StringSerializer.get());
	}

	public void insert(final int empId, final int deptId, final String firstName, final String lastName) {
		final MutationBatch m = keyspace.prepareMutationBatch();

		m.withRow(EMP_CF, empId).putColumn(ModelConstantsTest.COL_NAME_EMPID, empId, null)
				.putColumn(ModelConstantsTest.COL_NAME_DEPTID, deptId, null)
				.putColumn(ModelConstantsTest.COL_NAME_FIRST_NAME, firstName, null)
				.putColumn(ModelConstantsTest.COL_NAME_LAST_NAME, lastName, null);

		try {
			@SuppressWarnings("unused")
			final OperationResult<Void> result = m.execute();
		} catch (final ConnectionException e) {
			logger.error("failed to write data to C*", e);
			throw new RuntimeException("failed to write data to C*", e);
		}
		logger.debug("insert ok");
	}

	public void read(final int empId) {
		OperationResult<ColumnList<String>> result;
		try {
			result = keyspace.prepareQuery(EMP_CF).getKey(empId).execute();

			final ColumnList<String> cols = result.getResult();
			logger.debug("read: isEmpty: " + cols.isEmpty());

			// process data

			// a) iterate over columsn
			logger.debug("emp");
			for (final Column<String> c : cols) {
				Object v = null;
				if (c.getName().endsWith("id")) {
					v = c.getIntegerValue();
				}
				else {
					v = c.getStringValue();
				}
				logger.debug("- col: '" + c.getName() + "': " + v);
			}

			// b) get columns by name
			logger.debug("emp");
			logger.debug("- emp id: " + cols.getIntegerValue(ModelConstantsTest.COL_NAME_EMPID, null));
			logger.debug("- dept: " + cols.getIntegerValue(ModelConstantsTest.COL_NAME_DEPTID, null));
			logger.debug("- firstName: " + cols.getStringValue(ModelConstantsTest.COL_NAME_FIRST_NAME, null));
			logger.debug("- lastName: " + cols.getStringValue(ModelConstantsTest.COL_NAME_LAST_NAME, null));

		} catch (final ConnectionException e) {
			logger.error("failed to read from C*", e);
			throw new RuntimeException("failed to read from C*", e);
		}
	}

}
