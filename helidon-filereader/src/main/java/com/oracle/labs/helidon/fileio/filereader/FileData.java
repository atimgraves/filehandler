package com.oracle.labs.helidon.fileio.filereader;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileData {
	private String fileContents;
	private FileInfo fileInfo;
}
