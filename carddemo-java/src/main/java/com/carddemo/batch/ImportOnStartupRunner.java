package com.carddemo.batch;

import com.carddemo.config.ImportProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** Runs {@code sampleDataImportJob} at startup when {@code carddemo.import.on-startup=true}. */
@Component
@ConditionalOnProperty(prefix = "carddemo.import", name = "on-startup", havingValue = "true")
public class ImportOnStartupRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ImportOnStartupRunner.class);

    private final JobLauncher jobLauncher;
    private final Job sampleDataImportJob;
    private final ImportProperties props;

    public ImportOnStartupRunner(JobLauncher jobLauncher, Job sampleDataImportJob, ImportProperties props) {
        this.jobLauncher = jobLauncher;
        this.sampleDataImportJob = sampleDataImportJob;
        this.props = props;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        JobExecution execution = jobLauncher.run(sampleDataImportJob, new JobParametersBuilder()
                .addString("source", props.source().name())
                .addLong("startedAt", System.currentTimeMillis())
                .toJobParameters());
        for (StepExecution step : execution.getStepExecutions()) {
            log.info("{}: read={} written={} filtered={}", step.getStepName(),
                    step.getReadCount(), step.getWriteCount(), step.getFilterCount());
        }
        if (execution.getStatus() != BatchStatus.COMPLETED) {
            throw new IllegalStateException(SampleDataImportJobConfig.JOB_NAME + " ended with " + execution.getStatus(),
                    execution.getAllFailureExceptions().stream().findFirst().orElse(null));
        }
    }
}
