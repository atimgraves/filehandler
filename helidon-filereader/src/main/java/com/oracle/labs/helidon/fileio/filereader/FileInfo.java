package com.oracle.labs.helidon.fileio.filereader;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileInfo {
	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private final static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
	private String name;
	private Long size;
	private String createdTs;

	public FileInfo createdTS(Date ts) {
		this.createdTs = formatter.format(ts);
		return this;
	}

	public FileInfo createdTS(Long ts) {
		return createdTS(new Date(ts));
	}

}
