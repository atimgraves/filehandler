
package com.oracle.labs.helidon.fileio.filereader;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
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
public class FileReaderResource {

	private String dataDirectory;

	@Inject
	public FileReaderResource(
			@ConfigProperty(name = "app.dataDirectory", defaultValue = "dataDirectory") String dataDirectory) {
		log.info("Using directory " + dataDirectory);
		this.dataDirectory = dataDirectory;
	}

	/**
	 * Return a worldly greeting message.
	 *
	 * @return {@link Message}
	 */
	@GET
	@Path("/file/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public FileData getFile(@PathParam("name") String name) throws IOException, NoSuchFileException {
		log.info("Getting file data from " + name + " from directory " + dataDirectory);
		java.nio.file.Path path = getFilePathFromName(name);
		FileInfo fileInfo = getFileInfo(path);
		FileData fileData = FileData.builder().fileInfo(fileInfo).fileContents(getFileContents(path)).build();
		log.info("File data is " + fileData);
		return fileData;

	}

	/**
	 * Return a worldly greeting message.
	 *
	 * @return {@link Message}
	 */
	@GET
	@Path("/info/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public FileInfo getFileInfo(@PathParam("name") String name) throws IOException, NoSuchFileException {
		log.info("Getting file data from " + name + " from directory " + dataDirectory);
		FileInfo fileInfo = getFileInfo(getFilePathFromName(name));
		log.info("File info is " + fileInfo);
		return fileInfo;
	}

	/**
	 * Return a worldly greeting message.
	 *
	 * @return {@link Message}
	 * @throws NoSuchFileException
	 */
	@GET
	@Path("/info")
	@Produces(MediaType.APPLICATION_JSON)
	public List<FileInfo> getAllInfo() throws IOException, NoSuchFileException {
		log.info("Getting all file info from directory " + dataDirectory);
		java.nio.file.Path directoryPath = getDirectoryPathFromName(dataDirectory);
		List<FileInfo> fileInfos = Files.list(directoryPath).map(filePath -> {
			try {
				return getFileInfo(filePath);
			} catch (IOException e) {
				log.info("IOException getting fileInfo for " + filePath + " details are " + e.getLocalizedMessage());
				return null;
			}
		}).filter(fileInfo -> fileInfo != null).collect(Collectors.toList());
		log.info("Returned listing of " + dataDirectory + " is " + fileInfos);
		return fileInfos;
	}

	private String getFileContents(java.nio.file.Path filePath) throws IOException {
		return Files.lines(filePath).collect(Collectors.joining("\n"));
	}

	private java.nio.file.Path getFilePathFromName(String name) throws NoSuchFileException, IOException {
		java.nio.file.Path filePath = java.nio.file.Path.of(dataDirectory + File.separator + name);

		if (!Files.exists(filePath)) {
			throw new NoSuchFileException("File " + filePath.getFileName() + " does not exist");
		}
		if (!Files.isRegularFile(filePath)) {
			throw new NoSuchFileException("File " + filePath.getFileName() + " is not a regular file");
		}
		if (!Files.isReadable(filePath)) {
			throw new NoSuchFileException("File " + filePath.getFileName() + " cannot be read");
		}
		return filePath;
	}

	private java.nio.file.Path getDirectoryPathFromName(String name) throws NoSuchFileException, IOException {
		java.nio.file.Path directoryPath = java.nio.file.Path.of(dataDirectory);

		if (!Files.exists(directoryPath)) {
			throw new NoSuchFileException("Directory " + directoryPath + " does not exist");
		}
		if (!Files.isDirectory(directoryPath)) {
			throw new NoSuchFileException("Directory " + directoryPath + " is not a directory");
		}
		if (!Files.isReadable(directoryPath)) {
			throw new NoSuchFileException("Directory " + directoryPath + " cannot be read");
		}
		return directoryPath;
	}

	private FileInfo getFileInfo(java.nio.file.Path filePath) throws IOException {
		return FileInfo.builder().name(filePath.getFileName().toString()).size(Files.size(filePath)).build()
				.createdTS(filePath.toFile().lastModified());
	}
}
