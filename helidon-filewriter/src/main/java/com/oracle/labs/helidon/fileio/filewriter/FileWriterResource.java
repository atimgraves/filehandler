
package com.oracle.labs.helidon.fileio.filewriter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import lombok.extern.java.Log;

/**
 * A simple JAX-RS resource to greet you. Examples:
 *
 * Get default greeting message: curl -X GET http://localhost:8080/greet
 *
 * Get greeting message for Joe: curl -X GET http://localhost:8080/greet/Joe
 *
 * Change greeting curl -X PUT -H "Content-Type: application/json" -d
 * '{"greeting" : "Howdy"}' http://localhost:8080/greet/greeting
 *
 * The message is returned as a JSON object.
 */
@Path("/")
@ApplicationScoped
@Log
public class FileWriterResource {

	private String dataDirectory;

	@Inject
	public FileWriterResource(
			@ConfigProperty(name = "app.dataDirectory", defaultValue = "dataDirectory") String dataDirectory) {
		log.info("Using directory " + dataDirectory);
		this.dataDirectory = dataDirectory;
	}

	/**
	 * Return a worldly greeting message.
	 *
	 * @return {@link Message}
	 */
	@POST
	@Path("/file/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.TEXT_PLAIN)
	public FileInfo postFile(@PathParam("name") String name, String contents)
			throws IOException, DuplicateFileException {
		log.info("Saving file data of " + contents + " to " + name + " from directory " + dataDirectory);
		java.nio.file.Path filePath = getFilePathFromName(name);
		if (Files.exists(filePath)) {
			throw new DuplicateFileException("File " + filePath.getFileName() + " already exists");
		}
		try (BufferedWriter bw = new BufferedWriter(new FileWriter(filePath.toFile()))) {
			bw.write(contents);
			bw.flush();
			bw.close();
		}
		FileInfo fileInfo = getFileInfo(filePath);
		log.info("Saved " + name + " to " + filePath + " Resulting file info is " + fileInfo);
		return fileInfo;

	}

	private java.nio.file.Path getFilePathFromName(String name) {
		java.nio.file.Path filePath = java.nio.file.Path.of(dataDirectory + File.separator + name);
		return filePath;
	}

	private FileInfo getFileInfo(java.nio.file.Path filePath) throws IOException {
		return FileInfo.builder().name(filePath.getFileName().toString()).size(Files.size(filePath)).build()
				.createdTS(filePath.toFile().lastModified());
	}
}
