package cybersoft.java18.repository;

import cybersoft.java18.model.JobModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

class JobRepositoryTest {
    JobRepository jobRepository = new JobRepository();
    @Test
    void findAllJobTest() {
        Assertions.assertNotNull(jobRepository.findAllJobs());
    }
    @Test
    void addJobAndDeleteTest() {
        //before add jobs
        List<JobModel> jobList = jobRepository.findAllJobs();
        Assertions.assertEquals(3, jobList.size());
        //add job
        JobModel jobModel1 = new JobModel().id(4).name("Dự án D").startDate("2020-01-01").endDate("2020-02-02");
        jobRepository.addJob(jobModel1);
        //after add jobs
        jobList = jobRepository.findAllJobs();
        Assertions.assertEquals(4, jobList.size());
        Assertions.assertEquals(jobModel1, jobList.get(3));
        //find job with id = 2 before deleting it
        JobModel jobModel = jobList.stream().filter(e -> e.getId() == 2).findFirst().orElse(null);
        Assertions.assertNotNull(jobModel); //not null cz it exists
        //delete job with id = 2
        jobRepository.deleteJobById(2);
        //find job with id = 2 after deletion
        jobList = jobRepository.findAllJobs();
        jobModel = jobList.stream().filter(e -> e.getId() == 2).findFirst().orElse(null);
        Assertions.assertNull(jobModel);
    }
    @Test
    void updateJobTest() {
        JobModel jobModel = jobRepository.findAllJobs().get(0).name("Updated Job").startDate("1999-09-09").endDate("2000-10-10"); //job with id = 1
        jobRepository.updateJob(jobModel);
        Assertions.assertEquals(jobModel, jobRepository.findAllJobs().get(0));
    }
    @Test
    void findStatisticTest() { //we have 3 status: "Chưa bắt đầu", "Đang thực hiện", "Đã hoàn thành"
        Map<String, Integer> map = new HashMap<>();
        //with id = 1, it should be "Chưa bắt đầu" = 3;
        map.put("Chưa bắt đầu", 3);
        Assertions.assertEquals(map, jobRepository.findStatisticById(1));
        //with id = 2, it should be "Chưa bắt đầu" = 2 & "Đang thực hiện" = 2
        map.clear();
        map.put("Chưa bắt đầu", 2);
        map.put("Đang thực hiện", 2);
        Assertions.assertEquals(map, jobRepository.findStatisticById(2));
        //with id = 3, it should be "Đang thực hiện" = 3 & "Đã hoàn thành" = 2
        map.clear();
        map.put("Đang thực hiện", 3);
        map.put("Đã hoàn thành", 2);
        Assertions.assertEquals(map, jobRepository.findStatisticById(3));
    }
}
