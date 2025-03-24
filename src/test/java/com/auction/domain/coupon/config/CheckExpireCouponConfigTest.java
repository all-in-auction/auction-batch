package com.auction.domain.coupon.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.auction.common.constants.BatchConst.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles(value = "test")
class CheckExpireCouponConfigTest {
    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    @Qualifier(CHECK_EXPIRE_COUPON_PREFIX + JOB_PREFIX)
    Job couponJob;

    @Autowired
    private JobLauncher jobLauncher;

    private JdbcTemplate masterJdbcTemplate;
    private JdbcTemplate slaveJdbcTemplate;

    private final LocalDate nowDate = LocalDate.now();
    private final LocalDate targetDate = nowDate.minusDays(1);

    @Autowired
    public void setMasterDataSource(@Qualifier(MASTER_DATASOURCE) DataSource dataSource) {
        masterJdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Autowired
    public void setSlaveDataSource(@Qualifier(SLAVE_DATASOURCE) DataSource dataSource) {
        slaveJdbcTemplate = new JdbcTemplate(dataSource);
    }

    @BeforeEach
    void beforeEach() {
        jobLauncherTestUtils.setJob(couponJob);
        jobLauncherTestUtils.setJobLauncher(jobLauncher);

        // 만약 schema.sql 이 자동 실행되지 않을 경우 아래 코드 주석 해제
//        String sql = new String(Files.readAllBytes(Paths.get("src/test/resources/schema.sql")));
//        masterJdbcTemplate.execute(sql);
    }

    @Test
    @DisplayName("쿠폰 만료 배치 성공 테스트")
    public void checkExpireCouponJob_success() throws Exception {
        // given
        int couponUserNumber = 10000;
        int userNumber = couponUserNumber * 2;

        // 배치 테스트 데이터 insert
        setData(userNumber, couponUserNumber);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("datetime", LocalDateTime.now())
                .addLocalDate("expireAt", targetDate)
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);
        for (StepExecution stepExecution : jobExecution.getStepExecutions()) {
            System.out.println("Step Name: " + stepExecution.getStepName());
            System.out.println("Read Count: " + stepExecution.getReadCount());
            System.out.println("Write Count: " + stepExecution.getWriteCount());
            System.out.println("Commit Count: " + stepExecution.getCommitCount());
            System.out.println("Step Status: " + stepExecution.getStatus());
        }

        String resultSql = "select count(cu.id) from coupon_user cu join coupon c on cu.coupon_id = c.id" +
                " where c.expire_at = ? and is_available = true";
        Integer coupon1Result = slaveJdbcTemplate.queryForObject(resultSql, Integer.class, targetDate);
        Integer coupon2Result = slaveJdbcTemplate.queryForObject(resultSql, Integer.class, nowDate);

        // then
        assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
        assertEquals(ExitStatus.COMPLETED, jobExecution.getExitStatus());

        // 어제가 만료일인 데이터는 0 이어야 함
        assertEquals(0, coupon1Result == null ? -1 : coupon1Result);
        // 오늘이 만료일인 데이터는 발급된 쿠폰 개수만큼 있어야 함
        assertEquals(couponUserNumber, coupon2Result == null ? -1 : coupon2Result);
    }

    private void setData(int userNumber, int couponUserNumber) {
        for (int i = 1; i <= userNumber; i++) {
            // 사용자 생성
            this.masterJdbcTemplate.update(
                    "insert into `user`(id, email, password, name, authority, activate, zip_code)" +
                            " values (" + i + ", 'email" + i + "@email.com', 'password', 'name', 'USER', true, 12345)");
        }

        // 어제가 만료일인 1번 쿠폰 생성
        this.masterJdbcTemplate.update(
                "insert into coupon(id, name, expire_at, discount_rate, amount)" +
                        " values (" + 1 + ", 'test_coupon', '" + targetDate + "', 10, 1000)"
        );

        // 오늘이 만료일인 2번 쿠폰 생성
        this.masterJdbcTemplate.update(
                "insert into coupon(id, name, expire_at, discount_rate, amount)" +
                        " values (" + 2 + ", 'test_coupon', '" + nowDate + "', 10, 1000)"
        );

        for (int i = 1; i <= couponUserNumber * 2; i++) {
            if (i <= couponUserNumber) {
                // 어제가 만료일인 사용자의 쿠폰 생성
                this.masterJdbcTemplate.update("insert into coupon_user(id, coupon_id, user_id, is_available)" +
                        " values (" + i + ", 1, " + i + ", true)");
            } else {
                // 오늘이 만료일인 사용자의 쿠폰 생성
                this.masterJdbcTemplate.update("insert into coupon_user(id, coupon_id, user_id, is_available)" +
                        " values (" + i + ", 2, " + i + ", true)");
            }
        }
    }

}