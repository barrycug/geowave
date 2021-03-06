package mil.nga.giat.geowave.cli.geoserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import mil.nga.giat.geowave.core.cli.annotations.GeowaveOperation;
import mil.nga.giat.geowave.core.cli.api.Command;
import mil.nga.giat.geowave.core.cli.api.OperationParams;
import mil.nga.giat.geowave.core.cli.operations.config.options.ConfigOptions;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.beust.jcommander.Parameters;

@GeowaveOperation(name = "addstyle", parentOperation = GeoServerSection.class)
@Parameters(commandDescription = "Add a GeoServer style")
public class GeoServerAddStyleCommand implements
		Command
{
	private GeoServerRestClient geoserverClient = null;

	@Parameter(names = {
		"-sld",
		"--stylesld"
	}, required = true, description = "<style sld file>")
	private String stylesld = null;

	@Parameter(description = "<GeoWave style name>")
	private List<String> parameters = new ArrayList<String>();
	private String gwStyle = null;

	@Override
	public boolean prepare(
			OperationParams params ) {
		if (geoserverClient == null) {
			// Get the local config for GeoServer
			File propFile = (File) params.getContext().get(
					ConfigOptions.PROPERTIES_FILE_CONTEXT);

			GeoServerConfig config = new GeoServerConfig(
					propFile);

			// Create the rest client
			geoserverClient = new GeoServerRestClient(
					config);
		}

		// Successfully prepared
		return true;
	}

	@Override
	public void execute(
			OperationParams params )
			throws Exception {
		if (parameters.size() != 1) {
			throw new ParameterException(
					"Requires argument: <style name>");
		}

		gwStyle = parameters.get(0);

		if (gwStyle == null) {
			throw new ParameterException(
					"Requires argument: <style xml file>");
		}

		File styleXmlFile = new File(
				stylesld);
		try (final FileInputStream inStream = new FileInputStream(
				styleXmlFile)) {
			Response addStyleResponse = geoserverClient.addStyle(
					gwStyle,
					inStream);

			if (addStyleResponse.getStatus() == Status.OK.getStatusCode()
					|| addStyleResponse.getStatus() == Status.CREATED.getStatusCode()) {
				System.out.println("Add style for '" + gwStyle + "' on GeoServer: OK");
			}
			else {
				System.err.println("Error adding style for '" + gwStyle + "' on GeoServer; code = "
						+ addStyleResponse.getStatus());
			}
		}
	}
}
