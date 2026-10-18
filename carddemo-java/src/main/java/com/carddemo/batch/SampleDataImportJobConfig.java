package com.carddemo.batch;

import com.carddemo.batch.CopybookLayouts.Layout;
import com.carddemo.batch.FixedLengthRecordReader.Encoding;
import com.carddemo.entity.ExportRecord;
import com.carddemo.entity.Transaction;
import com.carddemo.entity.User;
import jakarta.persistence.EntityManagerFactory;
import java.util.Locale;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * {@code sampleDataImportJob}: loads the VSAM unloads in {@code app/data} into the Phase 1 schema.
 * Steps run in foreign-key order. Writers merge, so the job can be re-run over the same data.
 *
 * <p>Replaces, for the dev/test database, the IDCAMS REPRO steps of ACCTFILE, CARDFILE, CUSTFILE,
 * XREFFILE, TCATBALF, DISCGRP, TRANTYPE, TRANCATG and DUSRSECJ, plus the TRANSACT part of CBIMPORT.
 */
@Configuration
public class SampleDataImportJobConfig {

    public static final String JOB_NAME = "sampleDataImportJob";
    private static final int CHUNK = 100;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager txManager;
    private final EntityManagerFactory emf;
    private final SampleDataFiles files;

    public SampleDataImportJobConfig(JobRepository jobRepository, PlatformTransactionManager txManager,
                                     EntityManagerFactory emf, SampleDataFiles files) {
        this.jobRepository = jobRepository;
        this.txManager = txManager;
        this.emf = emf;
        this.files = files;
    }

    @Bean
    public Job sampleDataImportJob(PasswordEncoder passwordEncoder) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(fileStep(SampleDataset.TRANTYPE, CopybookLayouts.TRANSACTION_TYPE))
                .next(fileStep(SampleDataset.TRANCATG, CopybookLayouts.TRANSACTION_CATEGORY))
                .next(fileStep(SampleDataset.DISCGRP, CopybookLayouts.DISCLOSURE_GROUP))
                .next(fileStep(SampleDataset.CUSTDATA, CopybookLayouts.CUSTOMER))
                .next(fileStep(SampleDataset.ACCTDATA, CopybookLayouts.ACCOUNT))
                .next(fileStep(SampleDataset.CARDDATA, CopybookLayouts.CARD))
                .next(fileStep(SampleDataset.CARDXREF, CopybookLayouts.CARD_XREF))
                .next(fileStep(SampleDataset.TCATBAL, CopybookLayouts.TRAN_CAT_BALANCE))
                .next(fileStep(SampleDataset.DALYTRAN, CopybookLayouts.DAILY_TRANSACTION))
                .next(usersStep(passwordEncoder))
                .next(exportHeaderStep())
                .next(exportTransactionStep())
                .build();
    }

    private <T> Step fileStep(SampleDataset dataset, Layout<T> layout) {
        return new StepBuilder("import-" + dataset.name().toLowerCase(Locale.ROOT), jobRepository)
                .<FixedRecord, T>chunk(CHUNK, txManager)
                .reader(files.reader(dataset, layout.recordLength()))
                .processor(layout::map)
                .writer(this.<T>writer())
                .build();
    }

    /**
     * COSGN00C upper-cases the typed password before comparing it to SEC-USR-PWD, so the
     * {@link com.carddemo.config.SecurityConfig} password encoder upper-cases before hashing.
     */
    private Step usersStep(PasswordEncoder passwordEncoder) {
        Layout<User> layout = CopybookLayouts.USER;
        ItemProcessor<FixedRecord, User> processor = r -> {
            User u = layout.map(r);
            u.setUsrId(u.getUsrId().toUpperCase(Locale.ROOT));
            u.setPwd(passwordEncoder.encode(u.getPwd() == null ? "" : u.getPwd()));
            return u;
        };
        return new StepBuilder("import-usrsec", jobRepository)
                .<FixedRecord, User>chunk(CHUNK, txManager)
                .reader(files.reader(SampleDataset.USRSEC, layout.recordLength()))
                .processor(processor)
                .writer(this.<User>writer())
                .build();
    }

    private Step exportHeaderStep() {
        return new StepBuilder("import-export-headers", jobRepository)
                .<FixedRecord, ExportRecord>chunk(CHUNK, txManager)
                .reader(files.reader(SampleDataset.EXPORT, ExportLayout.RECORD_LENGTH, Encoding.EBCDIC))
                .processor(r -> ExportLayout.decode(r).header())
                .writer(this.<ExportRecord>writer())
                .build();
    }

    /** CBIMPORT writes the 'T' records to TRANSACT; the other types are only reconciled, not loaded. */
    private Step exportTransactionStep() {
        return new StepBuilder("import-export-transactions", jobRepository)
                .<FixedRecord, Transaction>chunk(CHUNK, txManager)
                .reader(files.reader(SampleDataset.EXPORT, ExportLayout.RECORD_LENGTH, Encoding.EBCDIC))
                .processor(r -> ExportLayout.decode(r).payload() instanceof Transaction t ? t : null)
                .writer(this.<Transaction>writer())
                .build();
    }

    private <T> JpaItemWriter<T> writer() {
        return new JpaItemWriterBuilder<T>().entityManagerFactory(emf).build();
    }
}
