package no.odit.gatevas;

import no.odit.gatevas.dao.CourseTypeRepo;
import no.odit.gatevas.model.CourseType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;

import javax.annotation.PostConstruct;

@SpringBootApplication
@Configurable
@EnableAsync
@EnableJpaRepositories
public class GatevasApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(GatevasApplication.class, args);
    }

    @Autowired
    private CourseTypeRepo courseTypeRepo;

    @PostConstruct
    private void loadDefaults() {

        {
            CourseType entryOne = new CourseType();
            entryOne.setShortName("IOT-BP1");
            entryOne.setLongName("Internet of Things");
            if (courseTypeRepo.findByShortName(entryOne.getShortName()).isEmpty())
                courseTypeRepo.save(entryOne);
        }
        {
            CourseType entryTwo = new CourseType();
            entryTwo.setShortName("Lading-BP2");
            entryTwo.setLongName("Lading av bil og båt");
            if (courseTypeRepo.findByShortName(entryTwo.getShortName()).isEmpty())
                courseTypeRepo.save(entryTwo);
        }
        {
            CourseType entryThree = new CourseType();
            entryThree.setShortName("KNX-BP3");
            entryThree.setLongName("KNX");
            if (courseTypeRepo.findByShortName(entryThree.getShortName()).isEmpty())
                courseTypeRepo.save(entryThree);
        }
        {
            CourseType entryFour = new CourseType();
            entryFour.setShortName("Ledelse-BP4");
            entryFour.setLongName("Kvalitetsledelse");
            if (courseTypeRepo.findByShortName(entryFour.getShortName()).isEmpty())
                courseTypeRepo.save(entryFour);
        }
        {
            CourseType entryFive = new CourseType();
            entryFive.setShortName("Vedlikehold-BP5");
            entryFive.setLongName("Vedlikehold og feilsøking");
            if (courseTypeRepo.findByShortName(entryFive.getShortName()).isEmpty())
                courseTypeRepo.save(entryFive);
        }
        {
            CourseType entrySix = new CourseType();
            entrySix.setShortName("EKOM-BP6");
            entrySix.setLongName("EKOM – sertifisering");
            if (courseTypeRepo.findByShortName(entrySix.getShortName()).isEmpty())
                courseTypeRepo.save(entrySix);
        }
        {
            CourseType entrySeven = new CourseType();
            entrySeven.setShortName("Energidesign-BP7");
            entrySeven.setLongName("Energidesign og bygningsfysikk");
            if (courseTypeRepo.findByShortName(entrySeven.getShortName()).isEmpty())
                courseTypeRepo.save(entrySeven);
        }
        {
            CourseType entryEight = new CourseType();
            entryEight.setShortName("EKOM");
            entryEight.setLongName("EKOM");
            if (courseTypeRepo.findByShortName(entryEight.getShortName()).isEmpty())
                courseTypeRepo.save(entryEight);
        }
    }
}