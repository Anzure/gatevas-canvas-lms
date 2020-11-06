package no.odit.gatevas.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import no.odit.gatevas.model.Classroom;

@Service
public class LegacyService {

	private static final Logger log = LoggerFactory.getLogger(LegacyService.class);

	@Autowired
	private CourseService courseService;

	@Deprecated
	public Optional<Classroom> importLegacyFile(String path) {

		try (Reader fileReader = new FileReader(path)){
			JsonObject json = JsonParser.parseReader(fileReader).getAsJsonObject();

			if (!json.has("id")) {
				return Optional.empty();
			}

			Classroom course = new Classroom();
//			course.setShortName(json.get("id").getAsString());
//			course.setLongName(json.get("name").getAsString());
			course.setCommunicationLink(json.get("roomLink").getAsString());
			course.setGoogleSheetId(json.get("googleSheetId").getAsString());
			course = courseService.addCourse(course);
			log.debug("CREATE COURSE -> " + course.toString());
			return Optional.of(course);

		} catch (IOException ex) {
			log.error("Failed to import legacy file.", ex);
			return Optional.empty();
		}
	}
}